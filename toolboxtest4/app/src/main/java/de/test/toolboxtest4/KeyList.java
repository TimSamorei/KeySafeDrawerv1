package de.test.toolboxtest4;

import android.support.annotation.DrawableRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class KeyList extends RecyclerView.Adapter<KeyList.ViewHolder> {

    private ArrayList<KeyListItem> keylist;

    public KeyList(ArrayList<KeyListItem> keylist) {
        this.keylist = keylist;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public KeyList.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.keylist_item, null);
        // create ViewHolder
        ViewHolder viewHolder = new ViewHolder(itemLayoutView);
        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        // - get data from your itemsData at this position
        // - replace the contents of the view with that itemsData
        viewHolder.text_keyalias.setText(keylist.get(position).getAlias());
        viewHolder.text_keyalgo.setText(keylist.get(position).getAlgorithm());
        viewHolder.text_keyformat.setText(keylist.get(position).getFormat());
        viewHolder.img_key.setImageResource(R.drawable.ic_key);
    }

    // inner class to hold a reference to each item of RecyclerView
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView text_keyalias;
        public TextView text_keyalgo;
        public TextView text_keyformat;
        public ImageView img_key;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            text_keyalias = (TextView) itemLayoutView.findViewById(R.id.text_keyalias);
            text_keyalgo = (TextView) itemLayoutView.findViewById(R.id.text_keyalgo);
            text_keyformat = (TextView) itemLayoutView.findViewById(R.id.text_keyformat);
            img_key = (ImageView) itemLayoutView.findViewById(R.id.img_keys);
    }
    }

    // Return the size of your itemsData (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return keylist.size();
    }
}
