package com.supmark;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.supmark.model.List;

import java.util.ArrayList;

import static android.content.Context.CLIPBOARD_SERVICE;


public class ShareListAdapter extends RecyclerView.Adapter<ShareListAdapter.ShareListViewHolder> {
    private final ArrayList<List> lists;

    public static class ShareListViewHolder extends RecyclerView.ViewHolder {
        public TextView listName;
        public TextView listID;
        public ImageView listCopy;

        public ShareListViewHolder(@NonNull View itemView) {
            super(itemView);

            listName = itemView.findViewById(R.id.share_list_name);
            listID = itemView.findViewById(R.id.share_list_link);
            listCopy = itemView.findViewById(R.id.share_copy);
        }
    }

    public ShareListAdapter(ArrayList<List> lists) {
        this.lists = lists;
    }

    @Override
    public ShareListAdapter.ShareListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.share_list_item, parent, false);

        ShareListViewHolder vh = new ShareListViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ShareListViewHolder holder, int position) {

        holder.listName.setText(lists.get(position).getListName());
        holder.listID.setText(lists.get(position).getListID());
        holder.listID.setTextIsSelectable(true);

        holder.listCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), holder.listID.getText(), Toast.LENGTH_SHORT).show();
                ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("listID", holder.listID.getText());
                clipboard.setPrimaryClip(clip);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return lists.size();
    }
}
