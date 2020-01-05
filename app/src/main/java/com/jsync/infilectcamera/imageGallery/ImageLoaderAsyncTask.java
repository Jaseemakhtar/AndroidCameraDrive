package com.jsync.infilectcamera.imageGallery;

import android.os.AsyncTask;

import com.jsync.infilectcamera.MainActivity;

import java.io.File;

public class ImageLoaderAsyncTask extends AsyncTask<Void, Void, String> {
    private ImageGalleryAdapter adapter;
    private ImageLoadListener imageLoadListener;

    public ImageLoaderAsyncTask(ImageGalleryAdapter adapter){
        this.adapter = adapter;
    }

    public void setImageLoadListener(ImageLoadListener imageLoadListener){
        this.imageLoadListener = imageLoadListener;
    }

    @Override
    protected String doInBackground(Void... voids) {
        File infilectDirectory = MainActivity.infilectDirectory;
        File[] files = infilectDirectory.listFiles();
        if(files.length <= 0)
            return "No Images";

        for(File file: files){
            if(file.getName().contains(".jpg") || file.getName().contains(".jpeg")){
                ImageGalleryModel model = new ImageGalleryModel(null, file.getName(), file.getAbsolutePath());
                adapter.add(model);
            }
        }

        return adapter.getItemCount() + " images loaded";
    }

    @Override
    protected void onPostExecute(String msg) {
        super.onPostExecute(msg);
        if(imageLoadListener!=null)
            imageLoadListener.onComplete(msg);
    }

    public interface ImageLoadListener{
        public void onComplete(String msg);
    }


}
