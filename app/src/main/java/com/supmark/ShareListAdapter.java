package com.supmark;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.text.BreakIterator;
import java.util.ArrayList;

public class ShareListAdapter extends RecyclerView.Adapter<ShareListAdapter.ShareListViewHolder> {
    private final ArrayList<ListItem> lists;

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

    public ShareListAdapter(ArrayList<ListItem> lists) {
        this.lists = lists;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ShareListAdapter.ShareListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.share_list_item, parent, false);

        ShareListViewHolder vh = new ShareListViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ShareListViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.listName.setText(lists.get(position).getListName());
        holder.listID.setText(lists.get(position).getListID());
        holder.listID.setTextIsSelectable(true);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return lists.size();
    }
}
