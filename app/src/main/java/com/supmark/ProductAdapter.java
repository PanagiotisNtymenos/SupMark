package com.supmark;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.CardViewHolder> {

    private ArrayList<ProductItem> products;
    public ProductItem mRecentlyDeletedItem;
    public int mRecentlyDeletedItemPosition;
    private Context context;

    public ProductAdapter(ArrayList<ProductItem> products, Context context) {
        this.products = products;
        this.context = context;
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
        ProductItem currItem = products.get(position);

        holder.product.setText(currItem.getProduct());
        String url = currItem.getProductImage();
        Glide.with(getContext()).load(url).into(holder.productImage);
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
//        showUndoSnackbar();
    }

    public Context getContext() {
        return context;
    }

//    private void showUndoSnackbar() {
//        View view = findViewById(R.id.coordinator_layout);
//        Snackbar snackbar = Snackbar.make(view, "Undo Done",
//                Snackbar.LENGTH_LONG);
//        snackbar.setAction("Undo", new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ProductAdapter.this.undoDelete();
//            }
//        });
//        snackbar.show();
//    }
//
//    private void undoDelete() {
//        products.add(mRecentlyDeletedItemPosition,
//                mRecentlyDeletedItem);
//        notifyItemInserted(mRecentlyDeletedItemPosition);
//    }

    public class CardViewHolder extends RecyclerView.ViewHolder {

        public TextView product;
        public ImageView productImage;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            product = itemView.findViewById(R.id.product_name);
            productImage = itemView.findViewById(R.id.product_image);
        }
    }
}
