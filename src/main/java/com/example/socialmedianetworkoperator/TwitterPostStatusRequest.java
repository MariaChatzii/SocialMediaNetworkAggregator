package com.example.socialmedianetworkoperator;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TwitterPostStatusRequest extends AsyncTask<SocialMediaStatus,Void,String> {

    public static final String TWITTER_LOG = "Twitter";

    private final String UPDATE_STATUS_URL = "https://api.twitter.com/1.1/statuses/update.json";
    private final String UPDLOAD_IMAGE_URL = "https://upload.twitter.com/1.1/media/upload.json";

    private final OkHttpClient client;
    private String statusTopost;
    private String imageToPost;
    private File imageFileToPost;
    private final TwitterUser user;

    public TwitterPostStatusRequest(TwitterUser user){
        client = new OkHttpClient().newBuilder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .build();
        this.user = user;
    }

    @Override
    protected void onPostExecute(String result) {
        Log.d(TWITTER_LOG,"Twitter upload status procedures are done!");
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected String doInBackground(SocialMediaStatus... statuses) {
        SocialMediaStatus status = statuses[0];
        statusTopost = status.getStatusText();
        imageToPost = status.getStatusImage();
        imageFileToPost = status.getStatusImgRealPath();

        postStatus();

        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void postStatus(){
        //Generate authorization Header
        StringBuilder oauthHeader;
        Map<String, String> requestParams = new HashMap<>();
        OAuthHeaderGenerator headerGenerator = new OAuthHeaderGenerator(user.getConsumerKey(),user.getConsumerSecret(),user.getAccessToken(),user.getAccessTokenSecret(),null);

        if(imageToPost!=null) { //Status with image
            //First upload media on Twitter Server
            oauthHeader = headerGenerator.generateOAuthHeader("POST",UPDLOAD_IMAGE_URL, requestParams);

            String mediaId = uploadMedia(oauthHeader.toString());
            if(mediaId != null) { // Uploading media succeed
                //Make header for creating status with this uploaded media
                requestParams.put("media_ids", mediaId);
                oauthHeader = headerGenerator.generateOAuthHeader("POST", UPDATE_STATUS_URL, requestParams);

                String urlOfRequest = UPDATE_STATUS_URL + "?" + "media_ids" + "=" + mediaId;
                uploadStatus(urlOfRequest, oauthHeader.toString());
            }
        }else {//Status without media
            oauthHeader = headerGenerator.generateOAuthHeader("POST", UPDATE_STATUS_URL, requestParams);
            uploadStatus(UPDATE_STATUS_URL, oauthHeader.toString());
        }
    }

    private String uploadMedia(String oauthHeader){
        String imageFileName = imageFileToPost.getName();

        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("media",imageFileName,
                        RequestBody.create(MediaType.parse("application/octet-stream"), imageFileToPost))
                .build();
        Request request = new RequestCreation().createRequest("POST", UPDLOAD_IMAGE_URL, body, "Authorization",oauthHeader);

        try {
            Response response = client.newCall(request).execute();
            if(isResponseSuccessful(response)){
                return new JSONObject(response.body().string()).getString("media_id");
            }
        } catch (IOException | JSONException e) {
            Log.d(TWITTER_LOG,"Error happened getting response",e);
        }
        return null;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void uploadStatus(String urlOfRequest, String oauthHeader){
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("status",statusTopost)
                .build();
        Request request = new RequestCreation().createRequest("POST", urlOfRequest, body, "Authorization",oauthHeader);

        try {
            Response response = client.newCall(request).execute();
            isResponseSuccessful(response);
        }catch (IOException e) {
            Log.d(TWITTER_LOG,"Error happened getting response",e);
        }
    }

    public boolean isResponseSuccessful (Response response) {
            if (!response.isSuccessful()) {
                Log.d(TWITTER_LOG, "Something went wrong. Response code was " + response.code());
                return false;
            }
            return true;
    }
}
