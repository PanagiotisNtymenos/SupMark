package com.supmark;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

public class ListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView recyclerViewShareList;
    private RecyclerView.Adapter shareListAdapter;
    private RecyclerView.LayoutManager layoutManagerShareList;
    private TextView foundLists;
    private ProgressBar loadLists;
    private final String TAG = "123";
    final ArrayList<ListItem> lists = new ArrayList<ListItem>();
    private List<String> userLists = new ArrayList<>();
    public String currentUser;
    private Context currContext;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_layout);
        recyclerView = findViewById(R.id.recycler_view);

        foundLists = findViewById(R.id.load_lists_text);
        loadLists = findViewById(R.id.load_lists_bar);

        final CardView moreOptions = findViewById(R.id.more_options);
        moreOptions.setVisibility(View.INVISIBLE);

        setContext();
        currentUser = getId();
        getLists(currentUser);

        FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.add);
        addButton.setOnClickListener(mAddListener);

        findViewById(R.id.join_invite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (moreOptions.getVisibility() == View.VISIBLE) {
                    moreOptions.setVisibility(View.INVISIBLE);
                } else {
                    moreOptions.setVisibility(View.VISIBLE);
                }
            }
        });

        findViewById(R.id.share_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lists != null) {
                    if (!lists.isEmpty())
                        shareList();
                }
            }
        });

        findViewById(R.id.join_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinList();
            }
        });

    }

    private void shareList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ListActivity.this);
        builder.setTitle("Share your List!");
        builder.setMessage("You need to copy the list's link, and then send it to the user you want to share it with.");

        LayoutInflater factory = LayoutInflater.from(this);
        final View view = factory.inflate(R.layout.share_list_layout, null);
        builder.setCancelable(false);
        builder.setView(view);

        recyclerViewShareList = (RecyclerView) view.findViewById(R.id.share_lists_recycler_view);

        layoutManagerShareList = new LinearLayoutManager(getApplicationContext());
        recyclerViewShareList.setLayoutManager(layoutManagerShareList);

        shareListAdapter = new ShareListAdapter(lists);
        recyclerViewShareList.setAdapter(shareListAdapter);

        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dlg, int sumthin) {
                findViewById(R.id.join_invite).performClick();
            }
        });

        builder.show();
    }

    private void joinList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ListActivity.this);
        builder.setTitle("Join List!");
        builder.setMessage("You need to paste the list's link you got, from the other user.");

        LayoutInflater factory = LayoutInflater.from(this);
        final View view = factory.inflate(R.layout.join_list_layout, null);
        builder.setCancelable(false);
        builder.setView(view);

        final EditText listLink = view.findViewById(R.id.join_list);
        final ImageView paste = view.findViewById(R.id.paste_list);

        paste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                String pasteData = "";

                ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);

                pasteData = item.getText().toString();

                listLink.setText(pasteData);
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String m_Text = listLink.getText().toString();
                if (!m_Text.equals("")) {
                    addListByID(m_Text);
                } else {
                    Toast.makeText(view.getContext(), "No list added!", Toast.LENGTH_SHORT).show();
                }
                findViewById(R.id.join_invite).performClick();

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                findViewById(R.id.join_invite).performClick();
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void addListByID(final String listID) {

        db.collection("lists")
                .document(listID)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable final DocumentSnapshot snapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        if (snapshot != null && snapshot.exists()) {
                            if (!userLists.contains(listID)) {
                                lists.add(new ListItem(snapshot.getString("name"), listID));

                                List<String> listIDs = new ArrayList<>();
                                for (int i = 0; i < lists.size(); i++) {
                                    listIDs.add(lists.get(i).getListID());
                                }

                                db.collection("users")
                                        .document(currentUser)
                                        .update("lists", FieldValue.arrayUnion(listID))
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(getApplicationContext(), snapshot.getString("name") + " list added!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                Toast.makeText(getApplicationContext(), "You're already on this list!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Wrong link!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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
                    TextView username = findViewById(R.id.username);
                    username.setText(snapshot.getString("name"));
                    username.setSelected(true);

                    db.collection("lists")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        userLists = (List<String>) snapshot.get("lists");
                                        if (!lists.isEmpty())
                                            lists.clear();
                                        for (QueryDocumentSnapshot document : task.getResult()) {

                                            if (userLists.contains(document.getId()))
                                                lists.add(new ListItem(document.getString("name"), document.getId()));
                                        }
                                        if (lists.isEmpty()) {
                                            foundLists.setVisibility(View.VISIBLE);
                                            foundLists.setText("You have no lists yet..");
                                        } else {
                                            foundLists.setVisibility(View.INVISIBLE);
                                        }
                                        loadLists.setVisibility(View.INVISIBLE);
                                        setLists(lists);
                                    } else {
                                        Log.w(TAG, "Error getting products.", task.getException());
                                    }
                                }
                            });
                } else {
                    System.out.print("No such user");
                    giveUsername();
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
                        addListToUser(lists, m_Text);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    private void addListToUser(final ArrayList<ListItem> lists, final String listName) {
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
                        Toast.makeText(getApplicationContext(), "'" + listName + "' list added!", Toast.LENGTH_SHORT).show();
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
                                    .update("lists", FieldValue.arrayRemove(list.getListID()))
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

    private void addUser(String username) {
        ArrayList<String> lists = new ArrayList<>();
        final Map<String, Object> user = new HashMap<>();
        user.put("lists", lists);
        if (username.equals("")) {
            user.put("name", "user" + currentUser.charAt(0) + currentUser.charAt(1) + currentUser.charAt(2) + currentUser.charAt(3));
        } else {
            user.put("name", username);
        }

        db.collection("users")
                .document(currentUser)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Object>() {
                    @Override
                    public void onSuccess(Object o) {
                        Toast.makeText(getApplicationContext(), "You're now signed in as '" + user.get("name") + "'", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void giveUsername() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Hi!");
        builder.setMessage("It seems that you are a brand new user! Please, enter a nickname below..");
        builder.setCancelable(false);

        final EditText input = new EditText(this);

        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String m_Text;
                m_Text = input.getText().toString();
                if (!m_Text.equals("")) {
                    addUser(m_Text);
                } else {
                    addUser("");
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addUser("");
                dialog.cancel();
            }
        });
        builder.show();
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


    private void setContext() {
        currContext = getApplicationContext();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSlideLeft(this); //fire the slide left animation
    }

}