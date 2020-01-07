package com.jsync.infilectcamera.imageGallery.backgroundTasks;

import android.os.AsyncTask;

import com.jsync.infilectcamera.MainActivity;
import com.jsync.infilectcamera.imageGallery.recyclerView.ImageGalleryModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageLoaderAsyncTask extends AsyncTask<Void, Void, String> {
    private List<ImageGalleryModel> infilectPics;
    private List<ImageGalleryModel> resultList;
    private ImageLoadListener imageLoadListener;

    public ImageLoaderAsyncTask(List<ImageGalleryModel> infilectPics){
        this.infilectPics = infilectPics;
        resultList = new ArrayList<>();
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
                for(ImageGalleryModel driveFile: infilectPics){
                    if(driveFile.getFileName().equals(model.getFileName()))
                        model.setFileId(driveFile.getFileId());
                }

                resultList.add(model);
            }
        }

        return resultList.size() + " images loaded";
    }

    public List<ImageGalleryModel> getResultList(){
        return resultList;
    }

    @Override
    protected void onPostExecute(String msg) {
        super.onPostExecute(msg);
        if(imageLoadListener!=null)
            imageLoadListener.onComplete(msg);
    }

    public interface ImageLoadListener{
         void onComplete(String msg);
    }


}
