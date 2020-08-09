package com.supmark;

import android.content.DialogInterface;
import android.content.Intent;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.CardViewHolder> {

    private static ArrayList<ListItem> supermarkets;

    public class CardViewHolder extends RecyclerView.ViewHolder {

        public TextView listName;
        private ImageButton edit;
        private ImageButton delete;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            listName = itemView.findViewById(R.id.listName);
            edit = itemView.findViewById(R.id.edit_supermarket);
            delete = itemView.findViewById(R.id.delete_supermarket);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    int p = getLayoutPosition();
                    System.out.println("LongClick: " + p);
                    return true;
                }
            });

            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final int p = getLayoutPosition();

                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle("Edit Supermarket");
                    builder.setMessage("Specify the name of the entry.");

                    final EditText input = new EditText(view.getContext());

                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_TEXT);
                    builder.setView(input);

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String m_Text;
                            m_Text = input.getText().toString();
                            if (!m_Text.equals("")) {
                                supermarkets.set(p, new ListItem(m_Text, "2"));
                                notifyItemChanged(p);
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
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final int p = getLayoutPosition();

                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle("Delete Supermarket");
                    builder.setMessage("Are you sure you want to delete this entry?");

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            supermarkets.remove(p);
                            notifyItemChanged(p);
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
            });
        }
    }


    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_layout, parent, false);
        CardViewHolder cvh = new CardViewHolder(v);
        return cvh;
    }

    public ListAdapter(ArrayList<ListItem> supermarketsList) {
        supermarkets = supermarketsList;
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        final ListItem currItem = supermarkets.get(position);

        holder.listName.setText(currItem.getListName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ProductActivity.class);
                intent.putExtra("LIST", currItem.getListID());
                v.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return supermarkets.size();
    }

}
