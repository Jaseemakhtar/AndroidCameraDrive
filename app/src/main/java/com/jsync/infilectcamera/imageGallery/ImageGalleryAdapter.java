package com.jsync.infilectcamera.imageGallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
        if(file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            ((ImageGalleryViewHolder) holder).imageView.setImageBitmap(bitmap);
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
