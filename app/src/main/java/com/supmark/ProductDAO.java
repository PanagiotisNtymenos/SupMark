package com.supmark;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProductDAO {

    private final String TAG = "123";
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void products() {

        final ProductActivity pa = new ProductActivity();
        final ArrayList<ProductItem> productsList = new ArrayList<ProductItem>();

        db.collection("products")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                productsList.add(new ProductItem(document.getString("image"), document.getId()));
                            }
                            pa.setProductsList(productsList);
                        } else {
                            Log.w(TAG, "Error getting products.", task.getException());
                        }
                    }
                });
    }

}
