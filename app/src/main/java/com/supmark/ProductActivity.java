package com.supmark;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.Lists;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;


public class ProductActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AutoCompleteTextView searchBox;
    private RecyclerView.Adapter mAdapter;
    AutoCompleteProductSearchAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private final String TAG = "123";
    private List<ProductItem> allProducts;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_layout);

        Intent intent = getIntent();
        String currentList = intent.getStringExtra("LIST");

        recyclerView = findViewById(R.id.recycler_view_product);
        searchBox = findViewById(R.id.search_products);

        getProducts();
        getProductsFromList(currentList);

    }

    public String getId() {
        String id = android.provider.Settings.System.getString(super.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        System.out.println(id + "  ----------------------------------------------------------------");
        return id;
    }

    public void getProducts(){

    }

    public void getProductsFromList(final String currentList) {
        final ArrayList<ProductItem> productsList = new ArrayList<ProductItem>();

        db.collection("lists").document(currentList).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@androidx.annotation.Nullable DocumentSnapshot snapshot, @androidx.annotation.Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    System.err.println("Listen failed: " + e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    final List<String> productsInList = (List<String>) snapshot.get("products");

                    db.collection("products")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        ArrayList<ProductItem> listProducts = new ArrayList<>();
                                        allProducts = new ArrayList<>();
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            allProducts.add(new ProductItem(document.getString("image"), document.getId()));
                                            for (int i = 0; i < productsInList.size(); i++) {
                                                final int productPosition = i;
                                                if (productsInList.get(productPosition).equals(document.getId())) {
                                                    listProducts.add(new ProductItem(document.getString("image"), document.getId()));
                                                }
                                            }
                                        }
                                        setProductsList(listProducts);
                                    } else {
                                        Log.w(TAG, "Error getting products.", task.getException());
                                    }
                                }
                            });

                } else {
                    System.out.print("No such user");
                }
            }

        });

    }

    public void setProductsList(ArrayList<ProductItem> products) {
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
//        recyclerView.setNestedScrollingEnabled(false);
        mAdapter = new ProductAdapter(products, getApplicationContext());
        mAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(mAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback((ProductAdapter) mAdapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        adapter = new AutoCompleteProductSearchAdapter(this, allProducts);
        searchBox.setAdapter(adapter);
    }

    private void fillProductSearchBox() {

    }
}
