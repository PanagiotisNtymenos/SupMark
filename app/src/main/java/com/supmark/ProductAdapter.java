package com.supmark;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.CardViewHolder> {

    private final String listID;
    private ArrayList<ProductItem> products;
    public ProductItem mRecentlyDeletedItem;
    public int mRecentlyDeletedItemPosition;
    private Context context;
    private View view;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ProductAdapter(ArrayList<ProductItem> products, View view, Context context, String currListID) {
        this.products = products;
        this.context = context;
        this.view = view;
        this.listID = currListID;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_item_layout, parent, false);
        CardViewHolder cvh = new CardViewHolder(v);
        return cvh;
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {

        TextView product;
        ImageView productImage;

        product = holder.itemView.findViewById(R.id.product_name);
        productImage = holder.itemView.findViewById(R.id.product_image);
        final ProgressBar productImageProgress = holder.itemView.findViewById(R.id.product_image_progressBar);

        ProductItem currItem = products.get(position);

        product.setText(currItem.getProduct());
        String url = currItem.getProductImage();
        Glide.with(getContext()).load(url).listener(new RequestListener() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                productImageProgress.setVisibility(View.INVISIBLE);
                return false;
            }

            @Override
            public boolean onResourceReady(Object resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {
                productImageProgress.setVisibility(View.INVISIBLE);
                return false;
            }
        }).into(productImage);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void deleteItem(int position) {
        mRecentlyDeletedItem = products.get(position);
        mRecentlyDeletedItemPosition = position;
        products.remove(position);
        notifyItemRemoved(position);

        Map<String, Object> toUpdate = new HashMap<String, Object>();
        List<String> productNames = new ArrayList<>();

        for (int i = 0; i < products.size(); i++) {
            productNames.add(products.get(i).getProduct());
        }

        toUpdate.put("products", productNames);

        showUndoSnackbar(toUpdate);
    }

    public Context getContext() {
        return context;
    }

    public View getView() {
        return view;
    }

    private void showUndoSnackbar(final Map<String, Object> toUpdate) {
        Toast.makeText(getContext(), "'" + mRecentlyDeletedItem.getProduct() + "' removed from the list!", Toast.LENGTH_SHORT).show();

        View view = getView();
        Snackbar snackbar = Snackbar.make(view, "Undo Delete",
                Snackbar.LENGTH_SHORT);
        snackbar.setAction("Undo", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProductAdapter.this.undoDelete();
                Toast.makeText(getContext(), "'" + mRecentlyDeletedItem.getProduct() + "' is back into the list!", Toast.LENGTH_SHORT).show();
            }
        });

        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                    db.collection("lists")
                            .document(listID)
                            .update(toUpdate)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                }
                            });
                }
            }
        });

        snackbar.show();
    }

    private void undoDelete() {
        products.add(mRecentlyDeletedItemPosition, mRecentlyDeletedItem);
        notifyItemInserted(mRecentlyDeletedItemPosition);
    }

    public class CardViewHolder extends RecyclerView.ViewHolder {

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);

        }
    }
}
