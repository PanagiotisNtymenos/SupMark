package com.supmark;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static androidx.core.content.FileProvider.getUriForFile;

public class ListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView recyclerViewShareList;
    private RecyclerView.Adapter shareListAdapter;
    private RecyclerView.LayoutManager layoutManagerShareList;
    private TextView foundLists;
    private ProgressBar loadLists;
    private final String TAG = "ListActivity";
    private final ArrayList<ListItem> lists = new ArrayList<ListItem>();
    private List<String> userLists = new ArrayList<>();
    public String currentUser;
    public String currentUsername;
    private Context currContext;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_layout);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

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

        findViewById(R.id.dummy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (moreOptions.getVisibility() == View.VISIBLE) {
                    moreOptions.setVisibility(View.INVISIBLE);
                }
            }
        });

        findViewById(R.id.scrollView2constraint).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (moreOptions.getVisibility() == View.VISIBLE) {
                    moreOptions.setVisibility(View.INVISIBLE);
                }
            }
        });

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

        findViewById(R.id.update_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkForUpdates();
            }
        });

    }

    private void checkForUpdates() {
        final AlertDialog loader = new AlertDialog.Builder(this).create();

        LayoutInflater factory = LayoutInflater.from(this);
        final View view = factory.inflate(R.layout.loader_layout, null);
        loader.setCancelable(true);
        loader.setView(view);
        loader.show();

        AppUpdater appUpdater = new AppUpdater(this);
        appUpdater.setUpdateFrom(UpdateFrom.JSON)
                .setUpdateJSON("https://github.com/PanagiotisNtymenos/SupMark/raw/master/update-changelog.json")
                .setDisplay(Display.DIALOG)
                .showAppUpdated(true)
                .setTitleOnUpdateAvailable("Update available :)")
                .setContentOnUpdateAvailable("Check out the latest version available of SupMark!")
                .setTitleOnUpdateNotAvailable("Update not available")
                .setContentOnUpdateNotAvailable("No update available. Check for updates again later!")
                .setButtonUpdate("Update")
                .setButtonUpdateClickListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loader.dismiss();
                        findViewById(R.id.join_invite).performClick();
                        Toast.makeText(getApplicationContext(), "Update started!", Toast.LENGTH_SHORT).show();
//                        downloadAPK();
                        RunAPK(getBaseContext());
                    }
                })
                .setButtonDismiss("Maybe later")
                .setButtonDismissClickListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loader.dismiss();
                        findViewById(R.id.join_invite).performClick();
                        Toast.makeText(getApplicationContext(), "Update canceled!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setButtonDoNotShowAgain(null)
                .setCancelable(false)
                .start();

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
                            Toast.makeText(getApplicationContext(), "Wrong link. No such list exists!", Toast.LENGTH_SHORT).show();
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
                    currentUsername = snapshot.getString("name");
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

        mAdapter = new ListAdapter(lists, currentUsername);
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

    private void downloadAPK() {
        String url = "https://github.com/PanagiotisNtymenos/SupMark/raw/master/SupMark-latest.apk";
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription("SupMark is updating...");
        request.setTitle("SupMark");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "SupMark-latest.apk");

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

    private void RunAPK(Context context) {
        requestPermissionsToRead();
    }

    private void requestPermissionsToRead() {
        // ASK RUNTIME PERMISSIONS
        ActivityCompat.requestPermissions(ListActivity.this, new String[]{READ_EXTERNAL_STORAGE}, 111);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0) {
            if (requestCode == 111 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
/*
                File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File file1 = new File(downloads + "//SupMark-latest.apk");//downloads.listFiles()[0];

                Uri contentUri1 = getUriForFile(this, BuildConfig.APPLICATION_ID, file1);

                Intent intent = new Intent(Intent.ACTION_VIEW, contentUri1);
                intent.setDataAndType(contentUri1, "application/vnd.android.package-archive");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
 */
                String destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";
                String fileName = "SupMark-" + BuildConfig.VERSION_NAME + ".apk";
                destination += fileName;
                final Uri uri = Uri.parse("file://" + destination);

                //Delete update file if exists
                File file = new File(destination);
                if (file.exists())
                    //file.delete() - test this, I think sometimes it doesnt work
                    file.delete();

                //get url of app on server
                String url = ListActivity.this.getString(R.string.update_apk_location);

                //set downloadmanager
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setDescription("Downloading updates...");
                request.setTitle("SupMark");

                //set destination
                request.setDestinationUri(uri);

                // get download service and enqueue file
                final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                final long downloadId = manager.enqueue(request);

                //set BroadcastReceiver to install app when .apk is downloaded
                BroadcastReceiver onComplete = new BroadcastReceiver() {
                    public void onReceive(Context ctxt, Intent intent) {
                        Intent install = new Intent(Intent.ACTION_VIEW);
                        install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        install.setDataAndType(uri, manager.getMimeTypeForDownloadedFile(downloadId));
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(install);

                        unregisterReceiver(this);
                        finish();
                    }
                };
                //register receiver for when .apk download is compete
                registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSlideLeft(this); //fire the slide left animation
    }

}