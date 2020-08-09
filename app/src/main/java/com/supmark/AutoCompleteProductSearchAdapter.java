package com.supmark;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class AutoCompleteProductSearchAdapter extends ArrayAdapter<ProductItem> {

    private List<ProductItem> productListFull;

    public AutoCompleteProductSearchAdapter(@NonNull Context context, @NonNull List<ProductItem> productList) {
        super(context, 0, productList);

        productListFull = new ArrayList<>(productList);
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

        ProductItem productItem = getItem(position);
        if (productItem != null) {
            textViewName.setText(productItem.getProduct());
            String url = productItem.getProductImage();
            Glide.with(getContext()).load(url).into(imageViewProduct);
        }

        return convertView;
    }

    private Filter productFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<ProductItem> suggestions = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                suggestions.addAll(productListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (ProductItem item : productListFull) {
                    if (item.getProduct().toLowerCase().contains(filterPattern)) {
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
            return ((ProductItem) resultValue).getProduct();
        }
    };
}
