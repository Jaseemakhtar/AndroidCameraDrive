package com.jsync.infilectcamera.imageGallery;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.jsync.infilectcamera.R;
import com.jsync.infilectcamera.imageGallery.backgroundTasks.FileUploaderService;
import com.jsync.infilectcamera.imageGallery.backgroundTasks.GetFilesAsyncTask;
import com.jsync.infilectcamera.imageGallery.backgroundTasks.ImageLoaderAsyncTask;
import com.jsync.infilectcamera.imageGallery.recyclerView.ImageGalleryAdapter;
import com.jsync.infilectcamera.imageGallery.recyclerView.ImageGalleryModel;
import com.jsync.infilectcamera.imageGallery.utils.DriveServiceHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GalleryActivity extends AppCompatActivity implements FileUploaderService.UploadListener {
    private final String TAG = "GalleryActivity";
    public static final String DRIVE_INFILECT_FOLDER = "InfilectGallery";
    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    private final int REQUEST_SIGN_IN = 342;
    private GridLayoutManager gridLayoutManager;

    private ImageGalleryAdapter adapter;
    private DriveServiceHelper driveServiceHelper;

    private List<ImageGalleryModel> drivePics;
    private List<ImageGalleryModel> localPics;
    private String folderId;

    private FileUploaderService uploader;
    private Intent service;
    private boolean isBound;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            uploader = ((FileUploaderService.LocalBinder)service).getService();
            uploader.setUploadListener(GalleryActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };
    private boolean isReadyToSync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerView);

        localPics = new ArrayList<>();
        adapter = new ImageGalleryAdapter();

        gridLayoutManager = new GridLayoutManager(GalleryActivity.this, 3);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);

        setTitle("Infilect Gallery");
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkSignIn();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(isBound){
            unbindService(connection);
            isBound = false;
        }
    }

    private void showLoading(){
        recyclerView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoading(){
        recyclerView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void signInUser(){
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .build();

        GoogleSignInClient signInClient = GoogleSignIn.getClient(GalleryActivity.this, signInOptions);
        startActivityForResult(signInClient.getSignInIntent(), REQUEST_SIGN_IN);
    }

    private void loadImage(){
        final ImageLoaderAsyncTask imageLoaderAsyncTask = new ImageLoaderAsyncTask(drivePics);
        imageLoaderAsyncTask.execute();
        imageLoaderAsyncTask.setImageLoadListener(new ImageLoaderAsyncTask.ImageLoadListener() {
            @Override
            public void onComplete(String msg) {
                hideLoading();
                Toast.makeText(GalleryActivity.this, msg, Toast.LENGTH_SHORT).show();
                localPics = imageLoaderAsyncTask.getResultList();
                adapter.removeAll();
                adapter.addAll(localPics);
                isReadyToSync = true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_SIGN_IN){
            if(resultCode == RESULT_OK){
                GoogleSignIn.getSignedInAccountFromIntent(data)
                        .addOnSuccessListener(new OnSuccessListener<GoogleSignInAccount>() {
                            @Override
                            public void onSuccess(GoogleSignInAccount googleSignInAccount) {
                                Log.d(TAG, "Signed in as " + googleSignInAccount.getEmail());
                                // Use the authenticated account to sign in to the Drive service.
                                // The DriveServiceHelper encapsulates all REST API and SAF functionality.
                                // Its instantiation is required before handling any onClick actions.
                                driveServiceHelper = DriveServiceHelper.getInstance();
                                driveServiceHelper.init(getGoogleDriveService(googleSignInAccount));
                                getFilesFromDrive();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "Failed to SignIn");
                                hideLoading();
                            }
                        });
            }else{
                hideLoading();
                Toast.makeText(GalleryActivity.this, "You need to give Drive permission in order to upload!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gallery_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.btnSync){
            if(isReadyToSync)
                syncImages();
            else
                Toast.makeText(GalleryActivity.this, "Try again!", Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }

    private void syncImages() {
        service = new Intent(GalleryActivity.this, FileUploaderService.class);
        service.putExtra("files", (Serializable) localPics);
        service.putExtra("folderId", folderId);
        startService(service);
        bindService(service, connection, BIND_AUTO_CREATE);
        isBound = true;
        isReadyToSync = false;
        Toast.makeText(this, "Starting to sync", Toast.LENGTH_SHORT).show();
    }

    private void checkSignIn(){
        showLoading();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());

        if (account == null) {
            signInUser();
        } else {
            driveServiceHelper = DriveServiceHelper.getInstance();
            driveServiceHelper.init(getGoogleDriveService(account));
            Log.d(TAG, "User signed in: " + account.getEmail());
            getFilesFromDrive();
        }
    }

    private void getFilesFromDrive(){
        final GetFilesAsyncTask getFilesAsyncTask = new GetFilesAsyncTask(driveServiceHelper);
        getFilesAsyncTask.setResultListener(new GetFilesAsyncTask.ResultListener() {
            @Override
            public void onSuccess(List<ImageGalleryModel> drivePics) {
                GalleryActivity.this.drivePics = drivePics;
                folderId = getFilesAsyncTask.getFolderId();
                hideLoading();
                loadImage();
                Log.d(TAG, "Success in fetching files: ");
            }

            @Override
            public void onFail(String msg) {
                Toast.makeText(GalleryActivity.this, msg, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Unable to query file from infilect root folder of Google Drive.");
                hideLoading();
            }
        });
        getFilesAsyncTask.execute();
    }

    public Drive getGoogleDriveService(GoogleSignInAccount account) {
        GoogleAccountCredential credential =
                GoogleAccountCredential.usingOAuth2(
                        GalleryActivity.this, Collections.singleton(DriveScopes.DRIVE_FILE));
        credential.setSelectedAccount(account.getAccount());
        return new Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                new GsonFactory(),
                credential)
                .setApplicationName("Infilect Camera")
                .build();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onComplete(String msg) {
        Toast.makeText(GalleryActivity.this, msg, Toast.LENGTH_SHORT).show();
        isReadyToSync = true;
    }

    @Override
    public void onUpdate(ImageGalleryModel resultsModel) {
        Toast.makeText(GalleryActivity.this, resultsModel.getFileName() + " uploaded successfully!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(String error, int errorCode) {
        Toast.makeText(GalleryActivity.this, error, Toast.LENGTH_SHORT).show();
        isReadyToSync = false;
    }
}
