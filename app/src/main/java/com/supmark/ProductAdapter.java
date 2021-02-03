package com.supmark;

import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.supmark.model.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.CardViewHolder> {

    private final String listID;
    private ArrayList<Product> products;
    public Product mRecentlyDeletedItem;
    public int mRecentlyDeletedItemPosition;
    private Context context;
    private View view;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ProductAdapter(ArrayList<Product> products, View view, Context context, String currListID) {
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
        TextView productUser;
        TextView productQuantity;
        TextView productNotes;

        product = holder.itemView.findViewById(R.id.product_name);
        productImage = holder.itemView.findViewById(R.id.product_image);
        productUser = holder.itemView.findViewById(R.id.user_added);
        productQuantity = holder.itemView.findViewById(R.id.quantity);
        productNotes = holder.itemView.findViewById(R.id.notes);

        product.setSelected(true);
        productUser.setSelected(true);

        final ProgressBar productImageProgress = holder.itemView.findViewById(R.id.product_image_progressBar);

        Product currItem = products.get(position);

        product.setText(currItem.getProduct());
        String url = currItem.getProductImage();
        productUser.setText(currItem.getUser());
        productQuantity.setText(String.valueOf(currItem.getQuantity()));
        productNotes.setText(currItem.getNotes());

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
        Map<String, Object> toUpdate = new HashMap<>();

        toUpdate.put("products." + products.get(position).getProduct(), FieldValue.delete());

        mRecentlyDeletedItem = products.get(position);
        mRecentlyDeletedItemPosition = position;
        products.remove(position);
        notifyItemRemoved(position);

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

        public CardViewHolder(@NonNull final View itemView) {
            super(itemView);
            final TextView quantityView = itemView.findViewById(R.id.quantity);

            itemView.findViewById(R.id.add_quantity).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int p = getLayoutPosition();
                    int quantity = Integer.parseInt(quantityView.getText().toString());
                    quantity = quantity + 1;
                    products.get(p).setQuantity(quantity);
                    quantityView.setText(String.valueOf(quantity));
                    adjustQuantity(products.get(p));
                }
            });

            itemView.findViewById(R.id.remove_quantity).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int p = getLayoutPosition();
                    int quantity = Integer.parseInt(quantityView.getText().toString());
                    if (quantity - 1 > 0) {
                        quantity = quantity - 1;
                        products.get(p).setQuantity(quantity);
                        quantityView.setText(String.valueOf(quantity));
                        adjustQuantity(products.get(p));
                    }
                }
            });

            itemView.findViewById(R.id.constraint_to_add_notes).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int p = getLayoutPosition();
                    addNotes(products.get(p), p);
                }
            });

        }
    }

    private void adjustQuantity(Product product) {
        Map<String, Object> toUpdate = new HashMap<>();

        toUpdate.put("user", product.getUser());
        toUpdate.put("quantity", String.valueOf(product.getQuantity()));
        toUpdate.put("notes", product.getNotes());

        db.collection("lists")
                .document(listID).update("products." + product.getProduct(), toUpdate);

    }


    private void addNotes(final Product product, final int p) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Add Notes!");
        builder.setMessage(product.getProduct());

        final EditText input = new EditText(view.getContext());

        if (!product.getNotes().equals("")) {
            input.setText(product.getNotes());
        }

        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String m_Text;
                m_Text = input.getText().toString();

                product.setNotes(m_Text);

                Map<String, Object> toUpdate = new HashMap<>();

                toUpdate.put("user", product.getUser());
                toUpdate.put("quantity", String.valueOf(product.getQuantity()));
                toUpdate.put("notes", m_Text);

                db.collection("lists")
                        .document(listID).update("products." + product.getProduct(), toUpdate);

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

}
