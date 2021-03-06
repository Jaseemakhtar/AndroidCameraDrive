package com.jsync.infilectcamera.imageGallery.recyclerView;

import java.io.Serializable;

public class ImageGalleryModel implements Serializable {
    private String fileId;
    private String fileName;
    private String filePath;

    public ImageGalleryModel(){

    }

    public ImageGalleryModel(String fileId, String fileName, String filePath) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.filePath = filePath;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
