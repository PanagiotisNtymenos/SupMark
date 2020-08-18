package com.supmark;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.CardViewHolder> {

    private final String listID;
    private ArrayList<ProductItem> products;
    public ProductItem mRecentlyDeletedItem;
    public int mRecentlyDeletedItemPosition;
    private Context context;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ProductAdapter(ArrayList<ProductItem> products, Context context, String currListID) {
        this.products = products;
        this.context = context;
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

        ProductItem currItem = products.get(position);

        product.setText(currItem.getProduct());
        String url = currItem.getProductImage();
        Glide.with(getContext()).load(url).into(productImage);
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

        db.collection("lists")
                .document(listID)
                .update(toUpdate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "'" + mRecentlyDeletedItem.getProduct() + "' removed from the list!", Toast.LENGTH_SHORT).show();
                    }
                });

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

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);

        }
    }
}
