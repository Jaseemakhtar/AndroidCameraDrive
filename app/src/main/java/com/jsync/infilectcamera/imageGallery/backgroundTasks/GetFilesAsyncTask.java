package com.jsync.infilectcamera.imageGallery.backgroundTasks;

import android.os.AsyncTask;

import com.jsync.infilectcamera.imageGallery.GalleryActivity;
import com.jsync.infilectcamera.imageGallery.recyclerView.ImageGalleryModel;
import com.jsync.infilectcamera.imageGallery.utils.DriveServiceHelper;

import java.util.List;

public class GetFilesAsyncTask extends AsyncTask<Void, Void, String> {
    private DriveServiceHelper driveServiceHelper;
    private ResultListener resultListener;
    private List<ImageGalleryModel> imageGalleryModels;
    private String folderId;

    public GetFilesAsyncTask(DriveServiceHelper driveServiceHelper){
        this.driveServiceHelper = driveServiceHelper;
    }

    public void setResultListener(ResultListener resultListener){
        this.resultListener = resultListener;
    }

    public String getFolderId(){
        return folderId;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            ImageGalleryModel folder = driveServiceHelper.createFolderIfNotExist2(GalleryActivity.DRIVE_INFILECT_FOLDER);
            folderId = folder.getFileId();
            imageGalleryModels = driveServiceHelper.queryFiles2(folder.getFileId());
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if(resultListener!=null){
            if(s.equals("success"))
                resultListener.onSuccess(imageGalleryModels);
            else
                resultListener.onFail(s);
        }
    }

    public interface ResultListener{
        void onSuccess(List<ImageGalleryModel> drivePics);
        void onFail(String msg);

    }
}
