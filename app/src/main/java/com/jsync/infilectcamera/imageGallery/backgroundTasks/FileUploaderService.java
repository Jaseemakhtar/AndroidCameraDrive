package com.jsync.infilectcamera.imageGallery.backgroundTasks;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.Nullable;

import com.jsync.infilectcamera.imageGallery.recyclerView.ImageGalleryModel;
import com.jsync.infilectcamera.imageGallery.utils.DriveServiceHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUploaderService extends Service implements Runnable, Handler.Callback {
    private IBinder binder = new LocalBinder();
    private UploadListener uploadListener;
    private final int ERROR = 1;
    private final int UPDATE = 2;
    private final int COMPLETE = 3;
    private Thread thread;
    private Handler handler;
    private List<ImageGalleryModel> files;
    private String folderId;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        files = (ArrayList<ImageGalleryModel>) intent.getSerializableExtra("files");
        folderId = intent.getStringExtra("folderId");
        thread = new Thread(this);
        thread.start();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }

    public void setUploadListener(UploadListener uploadListener){
        this.uploadListener = uploadListener;
    }

    @Override
    public void run() {
        DriveServiceHelper driveServiceHelper = DriveServiceHelper.getInstance();
        int count = 0;
        try {
            for (int i = 0; i < files.size(); i++) {
                ImageGalleryModel file = files.get(i);
                if(file.getFileId() == null){
                    driveServiceHelper.createFile2(new File(file.getFilePath()), folderId);
                    log(file.getFileName() + " uploaded successfully!");
                    sendMessage(UPDATE, file.getFileName() + " uploaded successfully!", 0, file);
                    count++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(ERROR, e.getMessage(), 0, null);
            log("error: " + e.getMessage());
        }

        sendMessage(COMPLETE, count + " files uploaded!", 0, null);

    }


    private void sendMessage(int code, String msg, int errorCode, ImageGalleryModel model){
        Message message = new Message();
        message.setTarget(handler);
        Bundle bundle = new Bundle();
        message.what = code;

        switch (code){
            case ERROR:
                bundle.putString("message", msg);
                bundle.putInt("errorCode", errorCode);
                break;

            case UPDATE:
                bundle.putSerializable("result", model);
                break;

            case COMPLETE:
                bundle.putString("message", msg);
                break;
        }

        message.setData(bundle);
        handler.sendMessage(message);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what){
            case ERROR:
                uploadListener.onError(msg.getData().getString("message"), msg.getData().getInt("errorCode"));
                return true;

            case UPDATE:
                uploadListener.onUpdate((ImageGalleryModel) msg.getData().getSerializable("result"));
                return true;

            case COMPLETE:
                uploadListener.onComplete(msg.getData().getString("message"));
                return true;
        }
        return false;
    }

    public class LocalBinder extends Binder {
        public FileUploaderService getService(){
            return FileUploaderService.this;
        }
    }

    public interface UploadListener {
        void onComplete(String msg);
        void onUpdate(ImageGalleryModel resultsModel);
        void onError(String error, int errorCode);
    }

    private void log(String msg){
        Log.i("service", msg);
    }
}