package com.example.socialmedianetworkoperator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.FacebookSdk;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PostActivity extends AppCompatActivity  {

    private static final String POST_ACTIV_LOG = "Post Activity" ;
    private static final int IMAGE_CAPTURE_REQUEST = 100 ;
    private static final int IMAGE_PICK_REQUEST = 200;
    private static final int CAMERA_PERM_REQ_CODE = 300;

    private ImageView imageView;
    private String imageViewImage = null;
    EditText statusEditText = null;

    private File imageFile = null;
    private FacebookPage facebookPage;
    private TwitterUser twitterUser;
    private ArrayList<Integer> selectedSocialMedia;



    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        Button cleanBtn = findViewById(R.id.cleanImageViewBtn);
        Button storyBtn = findViewById(R.id.postStoryBtn);
        Button statusBtn = findViewById(R.id.postStatusBtn);
        Button addImageButton = findViewById(R.id.addImageButton);
        imageView = findViewById(R.id.uploadedImage);
        statusEditText = findViewById(R.id.statusText);


        Intent intent = getIntent();
        if(intent!=null){
            twitterUser = (TwitterUser) intent.getSerializableExtra("TwitterUser");
            facebookPage = (FacebookPage) intent.getSerializableExtra("FacebookPage");
            selectedSocialMedia = intent.getIntegerArrayListExtra("SelectedSocialMedia");
        }


        View.OnClickListener addImageListener = v -> addImage();

        View.OnClickListener cleanImageViewListener = v -> {
            if(imageViewImage != null) {
                imageViewImage = null;
                imageView.setImageResource(R.drawable.uploadimage);
            }
        };

        View.OnClickListener makeStatusListener = view -> {

            //Create Post object
            String statusTxt = statusEditText.getText().toString();
            SocialMediaStatus status = new SocialMediaStatus(statusTxt, imageViewImage);

            if (selectedSocialMedia.contains(MainActivity.INSTAGRAM)) {
                if(areAppsInstalled()) {
                    if (imageViewImage != null)
                        shareStatusOnInstagram(status);
                    else
                        Toast.makeText(PostActivity.this, "You must first add an image in order to post status on Instagram!", Toast.LENGTH_SHORT).show();
                }
            }else{
                 if(isUserLoggedIn()) {
                     if (imageViewImage != null)
                         status.setStatusImgRealPath(new File(getRealPathFromURI(status.getStatusImage())));

                     if (status.getStatusImage() != null || !status.getStatusText().equals("")) {
                         //Post On selected By user social media platforms
                         if (selectedSocialMedia.contains(MainActivity.TWITTER))
                             postStatusOnTwitter(status);
                         if (selectedSocialMedia.contains(MainActivity.FACEBOOK))
                             postStatusOnFacebook(status);
                     }
                 }
            }
        };

        View.OnClickListener makeStoryListener = v -> {
            if(areAppsInstalled()) {
                if (imageViewImage != null) {
                    //Create story object
                    Story story = new Story();
                    story.setStoryImage(imageViewImage);

                    //Post On selected By user social media platforms
                    if (selectedSocialMedia.contains(MainActivity.TWITTER))
                        shareFleetOnTwitter(story);
                    if (selectedSocialMedia.contains(MainActivity.FACEBOOK))
                        shareStoryOnFacebook(story);
                    if (selectedSocialMedia.contains(MainActivity.INSTAGRAM))
                        shareStoryOnInstagram(story);
                } else {
                    Toast.makeText(PostActivity.this, "You must first add an image in order to post a story!", Toast.LENGTH_LONG).show();
                }
            }
        };
        addImageButton.setOnClickListener(addImageListener);
        cleanBtn.setOnClickListener(cleanImageViewListener);
        statusBtn.setOnClickListener(makeStatusListener);
        storyBtn.setOnClickListener(makeStoryListener);
    }

    private void addImage(){
        final CharSequence[] items = {"Camera","Gallery","Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(PostActivity.this);
        builder.setTitle("Add Image From:");
        builder.setItems(items, (dialog, i) -> {
            if(items[i].equals("Camera")){
                checkCameraPermission();
                Log.d(POST_ACTIV_LOG,"The user pressed 'From Camera'");
            }else if(items[i].equals("Gallery")){
                pickImageFromGallery();
                Log.d(POST_ACTIV_LOG,"The user pressed 'From Gallery'");
            }else {
                dialog.dismiss();
                Log.d(POST_ACTIV_LOG,"The user pressed 'Cancel'");
            }
        });
        builder.show();

    }

    private void checkCameraPermission(){
        int haveCameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if(haveCameraPermission == PackageManager.PERMISSION_GRANTED){
            Log.d(POST_ACTIV_LOG,"Camera permission is already granted!");
            captureNewPhoto();
        }
        else{
            Log.d(POST_ACTIV_LOG,"Camera permission does not exist.Requesting now...");
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.CAMERA},CAMERA_PERM_REQ_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == CAMERA_PERM_REQ_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d(POST_ACTIV_LOG,"Camera permission is now granted!");
                captureNewPhoto();
            }
            else{
                Log.d(POST_ACTIV_LOG,"Camera permission is denied!");
                Toast.makeText(this,"Camera permission is required for capturing new photo!",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_"+timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName, //prefix
                ".jpg", //suffix
                storageDir    //directory
        );
    }

    private void captureNewPhoto(){
        Intent capturePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //Ensure that there is a camera activity to handle the intent
        if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            try{
                //Create file where the photo should be saved
                imageFile = createImageFile();
                Uri imageUri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", imageFile);
                capturePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(capturePhotoIntent, IMAGE_CAPTURE_REQUEST);
            } catch (IOException ex){
                Log.d(POST_ACTIV_LOG,"Error occured while creating the File where captured imaged should be saved");
            }
        }
        else{
            Toast.makeText(this,"There is not a Camera App in order to take new photo",Toast.LENGTH_LONG).show();
            Log.d(POST_ACTIV_LOG,"There is not camera app installed in this device");
        }

    }

    private void pickImageFromGallery(){
        Intent pickImageIntent = new Intent(Intent.ACTION_PICK);
        if(pickImageIntent.resolveActivity(getPackageManager())!=null){
            pickImageIntent.setType("image/*");
            startActivityForResult(pickImageIntent, IMAGE_PICK_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case IMAGE_CAPTURE_REQUEST:
                    Bitmap imageViewBitmapImage = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                   imageViewImage = imageFile.toString();
                   //Appearing the photo on the imageview
                   imageView.setImageBitmap(imageViewBitmapImage);
                   break;
                case IMAGE_PICK_REQUEST:
                    //Saving Uri of the selected from gallery photo
                    imageViewImage = data.getData().toString();
                    //Appearing the photo on the imageview
                    imageView.setImageURI(Uri.parse(imageViewImage));
            }
        }
    }

    private void shareStoryOnFacebook(Story story){
        // Instantiate implicit intent with ADD_TO_STORY action
        Intent storyIntent = new Intent("com.facebook.stories.ADD_TO_STORY");
        storyIntent.setDataAndType(Uri.parse(story.getStoryImage()), "image/jpeg");
        storyIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        storyIntent.putExtra("com.facebook.platform.extra.APPLICATION_ID", FacebookSdk.getApplicationId());

        if(storyIntent.resolveActivity(getPackageManager())!=null)
            startActivityForResult(storyIntent,0);

    }

    private void shareStoryOnInstagram(Story story){
        ShareToInstagram instagram = new ShareToInstagram();
        Intent storyIntent = instagram.createStoryIntent(story);
        if(storyIntent.resolveActivity(getPackageManager())!=null)
            startActivity(storyIntent);
    }

    //This method publishes user's post on a specific page
    private void postStatusOnFacebook(SocialMediaStatus post){
        FacebookPostStatusRequest facebook = new FacebookPostStatusRequest(facebookPage);
        facebook.execute(post);
    }

    private void shareStatusOnInstagram(SocialMediaStatus status){
        ShareToInstagram instagram = new ShareToInstagram();
        Intent statusIntent = instagram.createPostIntent(status);
        if(statusIntent.resolveActivity(getPackageManager())!=null)
            startActivity(statusIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void postStatusOnTwitter(SocialMediaStatus status){
        TwitterPostStatusRequest twitterStatus= new TwitterPostStatusRequest(twitterUser);
        twitterStatus.execute(status);
    }

    private void shareFleetOnTwitter(Story story) {
        Intent storyIntent = new Intent(Intent.ACTION_SEND);
        storyIntent.setPackage("com.twitter.android");
        storyIntent.setType("image/*");
        storyIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(story.getStoryImage()));
        if(storyIntent.resolveActivity(getPackageManager())!=null)
            startActivity(storyIntent);
    }

    private String getRealPathFromURI(String contentURIString) {
        String result;
        Uri contentURI = Uri.parse(contentURIString);
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    private boolean isUserLoggedIn(){
        if(selectedSocialMedia.contains(MainActivity.FACEBOOK))
            if(facebookPage.getPageId().equals("")){
                Toast.makeText(getApplicationContext(), "First, login to Facebook.", Toast.LENGTH_SHORT).show();
                return false;
            }
        if(selectedSocialMedia.contains(MainActivity.TWITTER))
            if(twitterUser.getAccessToken().equals("")){
                Toast.makeText(getApplicationContext(), "First, login to Twitter.", Toast.LENGTH_SHORT).show();
                return false;
            }
        return true;
    }

    private boolean isPackageInstalled(Context c, String targetPackage) {
        PackageManager pm = c.getPackageManager();
        try {
            pm.getPackageInfo(targetPackage, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return true;
        }
        return false;
    }

    private Boolean areAppsInstalled(){
        if (selectedSocialMedia.contains(MainActivity.FACEBOOK))
            if(isPackageInstalled(getApplicationContext(), "com.facebook.katana")) {
                Toast.makeText(getApplicationContext(), "First, install Facebook App on your device.", Toast.LENGTH_SHORT).show();
                return false;
            }
        if (selectedSocialMedia.contains(MainActivity.TWITTER))
            if(isPackageInstalled(getApplicationContext(), "com.twitter.android")) {
                Toast.makeText(getApplicationContext(), "First, install Twitter App on your device.", Toast.LENGTH_SHORT).show();
                return false;
            }
        if (selectedSocialMedia.contains(MainActivity.INSTAGRAM))
            if(isPackageInstalled(getApplicationContext(), "com.instagram.android")) {
                Toast.makeText(getApplicationContext(), "First, install Instagram App on your device.", Toast.LENGTH_SHORT).show();
                return false;
            }
        return true;
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if(imageViewImage != null)
            outState.putString("Image of imageView", imageViewImage);
        if(!statusEditText.getText().toString().equals(""))
            outState.putString("Text of status", statusEditText.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        imageViewImage = savedInstanceState.getString("Image of imageView");
        if(imageViewImage!=null)
            imageView.setImageURI(Uri.parse(imageViewImage));
        statusEditText.setText(savedInstanceState.getString("Text of status"));
    }

}





