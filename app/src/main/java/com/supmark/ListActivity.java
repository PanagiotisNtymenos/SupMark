package com.supmark;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
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

public class ListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private final String TAG = "123";
    final ArrayList<ListItem> lists = new ArrayList<ListItem>();
    public String currentUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_layout);
        recyclerView = findViewById(R.id.recycler_view);

        currentUser = getId();
        getLists(currentUser);

        FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.add);
        addButton.setOnClickListener(mAddListener);


    }

    public String getId() {
        String id = android.provider.Settings.System.getString(super.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        return id;
    }

    private void getLists(String currentUser) {

        db.collection("users").document(currentUser).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@androidx.annotation.Nullable final DocumentSnapshot snapshot, @androidx.annotation.Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    System.err.println("Listen failed: " + e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    db.collection("lists")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        List<String> userLists = (List<String>) snapshot.get("lists");
                                        if (!lists.isEmpty())
                                            lists.clear();
                                        for (QueryDocumentSnapshot document : task.getResult()) {

                                            if (userLists.contains(document.getId()))
                                                lists.add(new ListItem(document.getString("name"), document.getId()));
                                        }

                                        setLists(lists);
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

    private void setLists(ArrayList<ListItem> lists) {
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new ListAdapter(lists);
        mAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(mAdapter);
    }

    private void addList(final String m_Text) {
        final Map<String, Object> list = new HashMap<>();
        list.put("name", m_Text);
        list.put("products", new ArrayList<String>());

        db.collection("lists")
                .add(list)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        lists.add(new ListItem(m_Text, documentReference.getId()));
                        addListToUser(lists);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    private void addListToUser(final ArrayList<ListItem> lists) {
        Map<String, Object> toUpdate = new HashMap<String, Object>();
        List<String> listIDs = new ArrayList<>();
        for (int i = 0; i < lists.size(); i++) {
            listIDs.add(lists.get(i).getListID());
        }
        toUpdate.put("lists", listIDs);

        db.collection("users")
                .document(getId())
                .update(toUpdate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                });
    }

    public void deleteListFromUser(final ListItem list, final String user) {

        db.collection("users")
                .document(user)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@androidx.annotation.Nullable final DocumentSnapshot snapshot, @androidx.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            System.err.println("Listen failed: " + e);
                            return;
                        }

                        if (snapshot != null && snapshot.exists()) {
                            List<String> lists = (List<String>) snapshot.get("lists");
                            for (int i = 0; i < lists.size(); i++) {
                                if (lists.get(i).equals(list.getListID())) {
                                    lists.remove(i);
                                    break;
                                }
                            }
                            db.collection("users")
                                    .document(user)
                                    .update("lists", lists)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                        }
                                    });
                        } else {
                            System.out.print("No such user");
                        }
                    }
                });
    }

    private View.OnClickListener mAddListener = new View.OnClickListener() {
        private String m_Text;

        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ListActivity.this);
            builder.setTitle("Add Supermarket");
            builder.setMessage("Specify the name of the entry.");

            final EditText input = new EditText(ListActivity.this);

            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    m_Text = input.getText().toString();
                    if (!m_Text.equals("")) {
                        addList(m_Text);
                    }
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
    };
}