package com.jsync.infilectcamera;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;


import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DriverServiceHelper {
    private final Executor executor = Executors.newSingleThreadExecutor();
    private Drive drive;

    public DriverServiceHelper(Drive drive){
        this.drive = drive;
    }

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

}
