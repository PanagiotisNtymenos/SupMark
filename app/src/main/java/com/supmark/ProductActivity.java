package com.supmark;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ProductActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AutoCompleteTextView searchBox;
    private RecyclerView.Adapter mAdapter;
    AutoCompleteProductSearchAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private TextView loadProductText;
    private ProgressBar loadProductBar;
    private final String TAG = "123";
    private List<ProductItem> allProducts;
    private ArrayList<ProductItem> listProducts = new ArrayList<>();
    private String currListID;
    private String user;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_layout);

        Intent intent = getIntent();
        String currentList = intent.getStringExtra("LIST_ID");
        String currentListName = intent.getStringExtra("LIST_NAME");
        user = intent.getStringExtra("USER");

        TextView listNameDisplay = findViewById(R.id.list_name_display_text);
        listNameDisplay.setText(currentListName);

        recyclerView = findViewById(R.id.recycler_view_product);
        searchBox = findViewById(R.id.search_products);

        loadProductBar = findViewById(R.id.load_products_bar);
        loadProductText = findViewById(R.id.load_products_text);

        getProductsFromList(currentList);

        searchBox.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                searchBox.setText("");
                addProduct(adapter.getItem(position));
            }
        });

        findViewById(R.id.constraintLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ListActivity.class);
                v.getContext().startActivity(intent);
                Animatoo.animateZoom(v.getContext());

            }
        });

        findViewById(R.id.hide_keyboard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBox = findViewById(R.id.search_products);
                if (!searchBox.getText().toString().equals("")) {
                    hideKeyboard(getApplicationContext(), v);
                    searchBox.showDropDown();
                }
            }
        });

    }

    public void getProductsFromList(final String currentList) {

        db.collection("lists").document(currentList).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@androidx.annotation.Nullable DocumentSnapshot snapshot, @androidx.annotation.Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    System.err.println("Listen failed: " + e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    List<String> productsInListDB = (List<String>) snapshot.get("products");
                    currListID = snapshot.getId();
                    final List<String> productsInList = new ArrayList<>();
                    final List<String> userAddedProduct = new ArrayList<>();

                    for (int i = 0; i < productsInListDB.size(); i++) {
                        String string = productsInListDB.get(i);
                        String[] parts = string.split("-|-");
                        String product = parts[0];
                        String user = parts[2];

                        productsInList.add(product);
                        userAddedProduct.add(user);
                    }


                    db.collection("products")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        if (!listProducts.isEmpty())
                                            listProducts.clear();
                                        allProducts = new ArrayList<>();
                                        for (QueryDocumentSnapshot document : task.getResult()) {

                                            allProducts.add(new ProductItem(document.getString("image"), document.getId()));
                                            for (int i = 0; i < productsInList.size(); i++) {
                                                final int productPosition = i;
                                                if (productsInList.get(productPosition).equals(document.getId())) {
                                                    listProducts.add(new ProductItem(document.getString("image"), document.getId(), userAddedProduct.get(productPosition)));
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
        adapter = new AutoCompleteProductSearchAdapter(this, allProducts, products);
        searchBox.setAdapter(adapter);
        if (mAdapter == null) {
            emptyListDisplay();
            layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
            mAdapter = new ProductAdapter(products, getWindow().getDecorView().findViewById(R.id.recycler_view_product), getApplicationContext(), currListID);
            recyclerView.setAdapter(mAdapter);
            recyclerView.setNestedScrollingEnabled(false);

            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback((ProductAdapter) mAdapter));
            itemTouchHelper.attachToRecyclerView(recyclerView);

        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    public void addProduct(final ProductItem product) {
        Map<String, Object> toUpdate = new HashMap<String, Object>();
        List<String> productNames = new ArrayList<>();

        for (int i = 0; i < listProducts.size(); i++) {
            productNames.add(listProducts.get(i).getProduct());
        }

        if (productNames.contains(product.getProduct())) {
            Toast.makeText(getApplicationContext(), "'" + product.getProduct() + "' is already on the list!", Toast.LENGTH_SHORT).show();
        } else {
            productNames.add(product.getProduct());
            List<String> productsWithUsers = new ArrayList<>();
            for (int i = 0; i < productNames.size(); i++) {
                productsWithUsers.add(productNames.get(i) + "-|-" + user);
            }

            toUpdate.put("products", productsWithUsers);

            db.collection("lists")
                    .document(currListID)
                    .update(toUpdate)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            hideKeyboard(getApplicationContext(), searchBox.getRootView());
                            Toast.makeText(getApplicationContext(), "'" + product.getProduct() + "' added to the list!", Toast.LENGTH_SHORT).show();
                            mAdapter.notifyDataSetChanged();
                            //                            setProductsList(listProducts);
                        }
                    });
        }
    }

    public static void hideKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void emptyListDisplay() {
        if (listProducts.isEmpty()) {
            loadProductBar.setVisibility(View.VISIBLE);
            loadProductText.setText("You have no products in the list..");
        } else {
            loadProductText.setVisibility(View.INVISIBLE);
        }
        loadProductBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        startActivity(new Intent(this, ListActivity.class));
//        Animatoo.animateZoom(this);
        Animatoo.animateSlideRight(this); //fire the slide left animation
    }

}
