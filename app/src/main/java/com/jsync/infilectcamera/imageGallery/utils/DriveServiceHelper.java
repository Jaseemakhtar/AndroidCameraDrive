package com.jsync.infilectcamera.imageGallery.utils;

import androidx.annotation.Nullable;

import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.jsync.infilectcamera.imageGallery.recyclerView.ImageGalleryModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DriveServiceHelper {
    public static String TYPE_GOOGLE_DRIVE_FOLDER = DriveFolder.MIME_TYPE;
    public static String TYPE_PHOTO = "application/vnd.google-apps.photo";
    public static String EXPORT_TYPE_JPEG = "application/zip";
    private Drive drive;

    private static DriveServiceHelper driveServiceHelper;

    private DriveServiceHelper(){
    }

    public synchronized static DriveServiceHelper getInstance() {
        if(driveServiceHelper == null)
            driveServiceHelper = new DriveServiceHelper();
        return driveServiceHelper;
    }

    public void init(Drive drive){
        this.drive = drive;
    }

    /*
    public Task<String> createFile(final java.io.File fileToUpload) {
        return Tasks.call(executor, new Callable<String>() {
            @Override
            public String call() throws Exception {

                File fileMetadata = new File();
                fileMetadata.setName(fileToUpload.getName());
                java.io.File filePath = new java.io.File(fileToUpload.getAbsolutePath());
                FileContent mediaContent = new FileContent("image/jpeg", filePath);
                File file = drive.files().create(fileMetadata, mediaContent)
                        .setFields("id")
                        .execute();

                if (file == null) {
                    throw new IOException("Null result when requesting file creation.");
                }

                return file.getId();
            }
        });
    }

    public Task<ImageGalleryModel> createFolderIfNotExist(final String folderName) {
        return Tasks.call(executor, new Callable<ImageGalleryModel>() {
            @Override
            public ImageGalleryModel call() throws Exception {
                ImageGalleryModel imageGalleryModel = new ImageGalleryModel() ;
                FileList result = drive.files().list()
                        .setQ("mimeType = '" + DriveFolder.MIME_TYPE + "' and name = '" + folderName + "' ")
                        .setSpaces("drive")
                        .execute();

                if (result.getFiles().size() > 0) {
                    imageGalleryModel.setFileId(result.getFiles().get(0).getId());
                    imageGalleryModel.setFileName(result.getFiles().get(0).getName());
                    return imageGalleryModel;

                } else {

                    List<String> root = Collections.singletonList("root");

                    File metadata = new File()
                            .setParents(root)
                            .setMimeType(DriveFolder.MIME_TYPE)
                            .setName(folderName);

                    File googleFile = drive.files().create(metadata).execute();
                    if (googleFile == null) {
                        throw new IOException("Null result when requesting file creation.");
                    }
                    imageGalleryModel.setFileId(googleFile.getId());
                    return imageGalleryModel;
                }
            }
        });
    }

    public Task<List<ImageGalleryModel>> queryFiles(@Nullable final String folderId) {
        return Tasks.call(executor, new Callable<List<ImageGalleryModel>>() {
                    @Override
                    public List<ImageGalleryModel> call() throws Exception {
                        List<ImageGalleryModel> googleDriveFileHolderList = new ArrayList<>();
                        String parent = "root";
                        if (folderId != null) {
                            parent = folderId;
                        }

                        FileList result = drive.files().list().setQ("'" + parent + "' in parents").setFields("files(id, name,size,createdTime,modifiedTime,starred,mimeType)").setSpaces("drive").execute();

                        for (int i = 0; i < result.getFiles().size(); i++) {

                            ImageGalleryModel imageGalleryModel = new ImageGalleryModel();
                            imageGalleryModel.setFileId(result.getFiles().get(i).getId());
                            imageGalleryModel.setFileName(result.getFiles().get(i).getName());

                            googleDriveFileHolderList.add(imageGalleryModel);

                        }
                        return googleDriveFileHolderList;
                    }
                }
        );
    }

    */

    public String createFile2(final java.io.File fileToUpload, String folderId) throws Exception{
        File fileMetadata = new File();
        fileMetadata.setName(fileToUpload.getName());
        fileMetadata.setParents(Collections.singletonList(folderId));
        java.io.File filePath = new java.io.File(fileToUpload.getAbsolutePath());
        FileContent mediaContent = new FileContent("image/jpeg", filePath);
        File file = drive.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();

        if (file == null) {
            throw new IOException("Null result when requesting file creation.");
        }

        return file.getId();
    }

    public ImageGalleryModel createFolderIfNotExist2(final String folderName) throws Exception{
        ImageGalleryModel imageGalleryModel = new ImageGalleryModel() ;
        FileList result = drive.files().list()
                .setQ("mimeType = '" + DriveFolder.MIME_TYPE + "' and name = '" + folderName + "' ")
                .setSpaces("drive")
                .execute();

        if (result.getFiles().size() > 0) {
            imageGalleryModel.setFileId(result.getFiles().get(0).getId());
            imageGalleryModel.setFileName(result.getFiles().get(0).getName());
            return imageGalleryModel;

        } else {

            List<String> root = Collections.singletonList("root");

            File metadata = new File()
                    .setParents(root)
                    .setMimeType(DriveFolder.MIME_TYPE)
                    .setName(folderName);

            File googleFile = drive.files().create(metadata).execute();
            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }
            imageGalleryModel.setFileId(googleFile.getId());
            return imageGalleryModel;
        }
    }

    public List<ImageGalleryModel> queryFiles2(@Nullable final String folderId) throws Exception{
        List<ImageGalleryModel> googleDriveFileHolderList = new ArrayList<>();
        String parent = "root";
        if (folderId != null) {
            parent = folderId;
        }

        FileList result = drive.files().list().setQ("'" + parent + "' in parents").setFields("files(id, name,size,createdTime,modifiedTime,starred,mimeType)").setSpaces("drive").execute();

        for (int i = 0; i < result.getFiles().size(); i++) {

            ImageGalleryModel imageGalleryModel = new ImageGalleryModel();
            imageGalleryModel.setFileId(result.getFiles().get(i).getId());
            imageGalleryModel.setFileName(result.getFiles().get(i).getName());

            googleDriveFileHolderList.add(imageGalleryModel);

        }
        return googleDriveFileHolderList;
    }

}
