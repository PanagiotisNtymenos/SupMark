package com.supmark;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.supmark.model.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AutoCompleteProductSearchAdapter extends ArrayAdapter<Product> {

    private List<Product> productListFull;
    private List<Product> productsInList;

    public AutoCompleteProductSearchAdapter(@NonNull Context context, @NonNull List<Product> productList, ArrayList<Product> products) {
        super(context, 0, productList);

        productListFull = new ArrayList<>(productList);
        productsInList = new ArrayList<>(products);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return productFilter;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.search_product_row, parent, false);
        }

        TextView textViewName = convertView.findViewById(R.id.product_name_list);
        ImageView imageViewProduct = convertView.findViewById(R.id.product_image_list);
        ImageView add = convertView.findViewById(R.id.add_product);
        ImageView tick = convertView.findViewById(R.id.tick_product);

        Product productItem = getItem(position);
        if (productItem != null) {
            textViewName.setText(productItem.getProduct());
            String url = productItem.getProductImage();
            final ProgressBar productImageProgress = convertView.findViewById(R.id.list_product_image_progressBar);
            productImageProgress.setVisibility(View.VISIBLE);
            Glide.with(getContext()).load(url).listener(new RequestListener() {
                @Override
                public boolean onLoadFailed(@javax.annotation.Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                    productImageProgress.setVisibility(View.INVISIBLE);
                    return false;
                }

                @Override
                public boolean onResourceReady(Object resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {
                    productImageProgress.setVisibility(View.INVISIBLE);
                    return false;
                }
            }).into(imageViewProduct);

            ArrayList<String> productNamesInList = new ArrayList<>();
            for (Product pi : productsInList) {
                productNamesInList.add(pi.getProduct());
            }

            if (productNamesInList.contains(productItem.getProduct())) {
                add.setVisibility(View.INVISIBLE);
                tick.setVisibility(View.VISIBLE);
            } else {
                add.setVisibility(View.VISIBLE);
                tick.setVisibility(View.INVISIBLE);
            }

        }

        return convertView;
    }

    private Filter productFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<Product> suggestions = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                suggestions.addAll(productListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Product item : productListFull) {

                    String ignoreAccent = removeAccents(item.getProduct().toLowerCase().trim());

                    if (item.getProduct().toLowerCase().contains(filterPattern) || ignoreAccent.contains(filterPattern)) {
                        suggestions.add(item);
                    }
                }
            }
            results.values = suggestions;
            results.count = suggestions.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            addAll((List) results.values);
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((Product) resultValue).getProduct();
        }

        private String removeAccents(String prod) {
            String noAccents = "";
            HashMap<Character, Character> accentsToReplace = new HashMap<>();
            accentsToReplace.put('ά', 'α');
            accentsToReplace.put('έ', 'ε');
            accentsToReplace.put('ό', 'ο');
            accentsToReplace.put('ί', 'ι');
            accentsToReplace.put('ύ', 'υ');
            accentsToReplace.put('ώ', 'ω');
            accentsToReplace.put('ή', 'η');

            char[] searchToChars = new char[prod.length()];

            for (int i = 0; i < prod.length(); i++) {
                searchToChars[i] = prod.charAt(i);
            }

            for (char c : searchToChars) {
                if (accentsToReplace.containsKey(c)) {
                    noAccents = noAccents + accentsToReplace.get(c);
                } else {
                    noAccents = noAccents + c;
                }
            }
            return noAccents;
        }

    };
}
