package com.jsync.infilectcamera.imageGallery.recyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jsync.infilectcamera.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageGalleryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    List<ImageGalleryModel> imageGalleryModels;

    public ImageGalleryAdapter(){
        imageGalleryModels = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_item, parent, false);
        return new ImageGalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ImageGalleryModel model = imageGalleryModels.get(position);

        File file = new File(model.getFilePath());
        Uri imageUri = Uri.fromFile(file);

        if(file.exists()) {
            /*Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            ((ImageGalleryViewHolder) holder).imageView.setImageBitmap(bitmap);*/
            Glide.with(holder.itemView).load(imageUri).into(((ImageGalleryViewHolder) holder).imageView);
            if(model.getFileId() != null)
                ((ImageGalleryViewHolder) holder).imgNotUpload.setVisibility(View.GONE);
            else
                ((ImageGalleryViewHolder) holder).imgUpload.setVisibility(View.GONE);
        }
    }

    public void add(ImageGalleryModel model){
        imageGalleryModels.add(model);
        notifyDataSetChanged();
    }

    public void addAll(List<ImageGalleryModel> imageGalleryModels){
        this.imageGalleryModels = imageGalleryModels;
        notifyDataSetChanged();
    }

    public void removeAll(){
        if(imageGalleryModels.size() > 0){
            imageGalleryModels.clear();
        }
    }

    @Override
    public int getItemCount() {
        return imageGalleryModels.size();
    }

    private class ImageGalleryViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        ImageView imgUpload;
        ImageView imgNotUpload;

        public ImageGalleryViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            imgUpload = itemView.findViewById(R.id.imageUpload);
            imgNotUpload = itemView.findViewById(R.id.imageNotUpload);
        }
    }

    public interface OnClickImage{
        void onClickImage(int pos);
    }
}
