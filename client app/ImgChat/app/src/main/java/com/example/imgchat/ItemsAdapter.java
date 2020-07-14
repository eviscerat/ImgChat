package com.example.imgchat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class ItemsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder > {

     private ArrayList<ImageObject> ImgObjects;
     private static final int VIEW_TYPE_MESSAGE_SENT = 1;
     private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder

    private class ReceivedHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;
        public TextView mNicknameView;
        public TextView mCoordView;

        ReceivedHolder(View itemView) {
            super(itemView);

            mImageView = itemView.findViewById(R.id.his_image);
            mNicknameView = itemView.findViewById(R.id.his_name);
            mCoordView = itemView.findViewById(R.id.his_coordinates);
        }

        void bind(ImageObject imageObject) {
            mImageView.setImageBitmap(imageObject.getImg());
            mNicknameView.setText(imageObject.getNickname());
            mCoordView.setText(imageObject.getCoordinates());
        }
    }

    private class SentHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;
        public TextView mNicknameView;
        public TextView mCoordView;

        SentHolder(View itemView) {
            super(itemView);

            mImageView = itemView.findViewById(R.id.my_image);
            mNicknameView = itemView.findViewById(R.id.my_name);
            mCoordView = itemView.findViewById(R.id.my_coordinates);
        }

        void bind(ImageObject imageObject) {
            mImageView.setImageBitmap(imageObject.getImg());
            mNicknameView.setText(imageObject.getNickname());
            mCoordView.setText(imageObject.getCoordinates());
        }
    }

    public void updateList(ArrayList<ImageObject> picturesList) {
        this.ImgObjects = picturesList;
        this.notifyDataSetChanged();
    }
    // Provide a suitable constructor that takes a dataset
    public ItemsAdapter(ArrayList<ImageObject> items, ChatActivity chatActivity) {
        this.ImgObjects = items;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View view;
        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.my_message, parent, false);
            return new SentHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.their_message, parent, false);
            return new ReceivedHolder(view);
        }
        return null;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        ImageObject img = ImgObjects.get(position);
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentHolder) holder).bind(img);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedHolder) holder).bind(img);
        }
    }

    @Override
    public int getItemViewType(int position) {
        ImageObject img = ImgObjects.get(position);

        // 0 is the id set for the client
        if (img.getUserId() == 0 ) {
            // If the current user is the sender of the message
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            // If some other user sent the message
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return ImgObjects.size();
    }
}

