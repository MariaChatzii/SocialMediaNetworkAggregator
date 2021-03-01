package com.example.socialmedianetworkoperator;

import android.os.AsyncTask;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FacebookPostStatusRequest extends AsyncTask<SocialMediaStatus,Void,String> {
    public static final String FB_LOG = "Facebook";
    private final static String FB_GRAPH_URL = "https://graph.facebook.com/";

    private final OkHttpClient client;
    private String statusTopost;
    private String imageToPost;
    private File imageFileToPost;
    private final FacebookPage facebookPage;

    public FacebookPostStatusRequest(FacebookPage facebookPage){
        client = new OkHttpClient().newBuilder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .build();
        this.facebookPage = facebookPage;
    }

    @Override
    protected String doInBackground(SocialMediaStatus... posts) {
        SocialMediaStatus post = posts[0];
        statusTopost = post.getStatusText();
        imageToPost = post.getStatusImage();
        imageFileToPost = post.getStatusImgRealPath();

        uploadStatus();

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        Log.d(FB_LOG,"Facebook upload status procedures are done!");
    }

    private void uploadStatus() {

        RequestBody body;
        String urlOfRequest = FB_GRAPH_URL+"/"+facebookPage.getPageId();

        if (imageToPost != null) { //Status with image
            urlOfRequest = urlOfRequest+"/photos?message="+statusTopost+"&access_token="+facebookPage.getPageAccessToken();
            String imageFileName = imageFileToPost.getName();

            MediaType mediaType = MediaType.parse("text/plain");
            body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("source", imageFileName,
                            RequestBody.create(MediaType.parse("application/octet-stream"),
                                    imageFileToPost))
                    .build();
        }else {
            urlOfRequest = urlOfRequest+"/feed?access_token="+facebookPage.getPageAccessToken();
            body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("message",statusTopost)
                    .build();
        }

        Request request = new RequestCreation().createRequest("POST",urlOfRequest,body,null,null);

        try {
            Response response = client.newCall(request).execute();
            if(!response.isSuccessful())
                Log.d(FB_LOG,"Something went wrong. Response code was " +response.code());
        } catch (IOException e) {
            Log.d(FB_LOG,"Error happened getting response",e);
        }
    }


}
