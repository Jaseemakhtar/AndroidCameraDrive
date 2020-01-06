package com.jsync.infilectcamera.imageGallery;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.jsync.infilectcamera.DriveServiceHelper;
import com.jsync.infilectcamera.R;

import java.util.Collections;

public class GalleryActivity extends AppCompatActivity {
    private final String TAG = "GalleryActivity";
    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    private final int REQUEST_SIGN_IN = 342;
    private GridLayoutManager gridLayoutManager;

    private ImageGalleryAdapter adapter;
    private DriveServiceHelper driveServiceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerView);

        adapter = new ImageGalleryAdapter();

        gridLayoutManager = new GridLayoutManager(GalleryActivity.this, 3);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);


        setTitle("Infilect Gallery");
        loadImage();
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
        ImageLoaderAsyncTask imageLoaderAsyncTask = new ImageLoaderAsyncTask(adapter);
        imageLoaderAsyncTask.execute();
        imageLoaderAsyncTask.setImageLoadListener(new ImageLoaderAsyncTask.ImageLoadListener() {
            @Override
            public void onComplete(String msg) {
                hideLoading();
                Toast.makeText(GalleryActivity.this, msg, Toast.LENGTH_SHORT).show();
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
                                driveServiceHelper = new DriveServiceHelper(getGoogleDriveService(googleSignInAccount));
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "Failed to SignIn");
                            }
                        });
            }else{
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
            checkSignIn();
            return true;
        }

        return false;
    }

    private void checkSignIn(){
        Toast.makeText(GalleryActivity.this, "Syncing...", Toast.LENGTH_SHORT).show();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());

        if (account == null) {
            signInUser();
        } else {
            //email.setText(account.getEmail());
            driveServiceHelper = new DriveServiceHelper(getGoogleDriveService(account));
        }
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
}
