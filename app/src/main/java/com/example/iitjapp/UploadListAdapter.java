package com.example.iitjapp;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UploadListAdapter extends RecyclerView.Adapter<UploadListAdapter.ViewHolder>
{
    public List<String> fileNameList;
    public List<Uri> fileImageList;

    public UploadListAdapter(List<String> fileNameList, List<Uri> fileImageList)
    {
        this.fileNameList = fileNameList;
        this.fileImageList = fileImageList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.upload_design, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        String fileName = fileNameList.get(position);
        holder.fileNameView.setText(fileName);

        Uri fileUri = fileImageList.get(position);
        holder.fileImageView.setImageURI(fileUri);
    }

    @Override
    public int getItemCount() {
        return fileNameList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public TextView fileNameView;
        public ImageView fileImageView;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);

            mView = itemView;

            fileNameView = mView.findViewById(R.id.upload_image_name);
            fileImageView = mView.findViewById(R.id.upload_image);
        }
    }
}
