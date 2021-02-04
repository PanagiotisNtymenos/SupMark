package com.supmark.activity;

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
import com.supmark.AutoCompleteProductSearchAdapter;
import com.supmark.ProductAdapter;
import com.supmark.R;
import com.supmark.SwipeToDeleteCallback;
import com.supmark.model.Product;
import com.supmark.activity.ListActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.internal.Internal;


public class ProductActivity extends AppCompatActivity {

    private final String imageQueryURL = "https://drive.google.com/uc?export=view&id=";
    private RecyclerView recyclerView;
    private AutoCompleteTextView searchBox;
    private RecyclerView.Adapter mAdapter;
    AutoCompleteProductSearchAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private TextView loadProductText;
    private ProgressBar loadProductBar;
    private final String TAG = "123";
    private List<Product> allProducts;
    private ArrayList<Product> listProducts = new ArrayList<>();
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
                    final HashMap<String, HashMap<String, String>> productsInListDB = (HashMap<String, HashMap<String, String>>) snapshot.get("products");
                    final List<String> productsInList = new ArrayList<>();
                    final List<String> userAddedProduct = new ArrayList<>();
                    final List<Integer> quantityProduct = new ArrayList<>();
                    final List<String> notesProduct = new ArrayList<>();
                    currListID = snapshot.getId();

                    for (Map.Entry<String, HashMap<String, String>> currProduct : productsInListDB.entrySet()) {
                        String product = currProduct.getKey();
                        HashMap<String, String> productDetails = currProduct.getValue();
                        String user = productDetails.get("user");
                        int quantity = Integer.parseInt(productDetails.get("quantity"));
                        String notes = productDetails.get("notes");

                        productsInList.add(product);
                        userAddedProduct.add(user);
                        quantityProduct.add(quantity);
                        notesProduct.add(notes);
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
                                        ArrayList<Integer> knownProducts = new ArrayList<>();
                                        for (QueryDocumentSnapshot document : task.getResult()) {

                                            allProducts.add(new Product(imageQueryURL + document.getString("image"), document.getId()));
                                            for (int i = 0; i < productsInList.size(); i++) {
                                                final int productPosition = i;
                                                if (productsInList.get(productPosition).equals(document.getId())) {
                                                    listProducts.add(new Product(imageQueryURL + document.getString("image"), document.getId(), userAddedProduct.get(productPosition), quantityProduct.get(productPosition), notesProduct.get(productPosition)));
                                                    knownProducts.add(i);
                                                }
                                            }
                                        }

                                        if (listProducts.size() < productsInList.size()) {
                                            for (int i = 0; i < productsInList.size(); i++) {
                                                if (!knownProducts.contains(i)) {
                                                    listProducts.add(new Product("", productsInList.get(i), userAddedProduct.get(i), quantityProduct.get(i), notesProduct.get(i)));
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

    public void setProductsList(ArrayList<Product> products) {
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

    public void addProduct(final Product product) {
        List<String> productNames = new ArrayList<>();

        for (int i = 0; i < listProducts.size(); i++) {
            productNames.add(listProducts.get(i).getProduct());
        }

        if (productNames.contains(product.getProduct())) {
            Toast.makeText(getApplicationContext(), "'" + product.getProduct() + "' is already on the list!", Toast.LENGTH_SHORT).show();
        } else {

            HashMap<String, String> productDetails = new HashMap<>();
            productDetails.put("user", user);
            productDetails.put("quantity", "1");
            productDetails.put("notes", "");

            db.collection("lists")
                    .document(currListID)
                    .update("products." + product.getProduct(), productDetails)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            hideKeyboard(getApplicationContext(), searchBox.getRootView());
                            Toast.makeText(getApplicationContext(), "'" + product.getProduct() + "' added to the list!", Toast.LENGTH_SHORT).show();
                            mAdapter.notifyDataSetChanged();

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
