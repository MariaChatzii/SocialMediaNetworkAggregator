package com.example.socialmedianetworkoperator;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TwitterTrendHashtagsRequest extends AsyncTask<Object, Void, ArrayList<String>> {

    public final static String HTTP_METHOD = "GET";
    private final static String TWIT_HASHTAGS = "TwitterHashtagsRest";
    private final static String TRENDS_URL = "https://api.twitter.com/1.1/trends/place.json"; //Returns 50 trending topics
    private final static String WOEID_GREECE = "23424833"; //GREECE WOEID

    private final TwitterUser user;
    private final FacebookPage facebookPage;
    private final OkHttpClient client;
    private final Context context;
    private ProgressDialog dialog;

    public TwitterTrendHashtagsRequest(Context context, TwitterUser user, FacebookPage facebookPage){
        client = new OkHttpClient().newBuilder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .build();
        this.user = user;
        this.context = context;
        this.facebookPage = facebookPage;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected ArrayList<String> doInBackground(Object[] objects) {
        return getHashtagTrends();
    }

    @Override
    protected void onPreExecute() {
        dialog = ProgressDialog.show(context,"Please Wait","Loading...");
    }

    @Override
    protected void onPostExecute(ArrayList<String> hashtagTrends) {
        if(dialog.isShowing()) {
            dialog.dismiss();
        }
        Log.d(TWIT_HASHTAGS,"Finished with trending hashtag requests");

        if(!hashtagTrends.isEmpty()){
            Intent intent = new Intent(context, HashtagSearchActivity.class);
            intent.putExtra("Source","TwitterTrendHashtagsRequest.class");
            intent.putExtra("TwitterUser", user);
            intent.putExtra("FacebookPage",facebookPage);
            intent.putStringArrayListExtra("HashtagTrends", hashtagTrends);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(intent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public ArrayList<String> getHashtagTrends(){
        ArrayList<String> hashtagTrends = new ArrayList<>();

        //Generate authorization Header
        Map<String,String> requestParams =  new HashMap<>();
        requestParams.put("id", WOEID_GREECE);
        String oauthHeader = new OAuthHeaderGenerator(user.getConsumerKey(),user.getConsumerSecret(),user.getAccessToken(),user.getAccessTokenSecret(),null)
                .generateOAuthHeader(HTTP_METHOD, TRENDS_URL, requestParams).toString();

        String urlOfRequest = TRENDS_URL +"?id="+ WOEID_GREECE;

        Request request = new RequestCreation().createRequest(HTTP_METHOD,urlOfRequest,null,"Authorization",oauthHeader);

        try {
            Response response = client.newCall(request).execute();
            if(response.isSuccessful()) {
               JSONArray jsonArray = new JSONArray(response.body().string());
               JSONObject jsonObject = jsonArray.getJSONObject(0);
               JSONArray trendsArray = jsonObject.getJSONArray("trends");
               for (int i = 0; i < trendsArray.length(); i++) {
                   String name = trendsArray.getJSONObject(i).getString("name");
                   if (name.startsWith("#"))
                       hashtagTrends.add(name.replace("#",""));
               }
            }
            else{
                Log.d(TWIT_HASHTAGS, "Something went wrong. Response code was " +response.code());
            }
            return hashtagTrends;
        }catch (IOException | JSONException e) {
            Log.e(TWIT_HASHTAGS, "Error happened getting response to twitter hashtag trends request!", e);
            return hashtagTrends;
        }
    }
}
