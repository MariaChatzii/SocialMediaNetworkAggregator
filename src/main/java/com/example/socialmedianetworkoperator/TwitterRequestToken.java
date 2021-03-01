package com.example.socialmedianetworkoperator;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TwitterRequestToken extends AsyncTask<Object, Void, HashMap<String,String>> {

    private final OkHttpClient client;

    private static final String REQUEST_TOKEN_URL = "https://api.twitter.com/oauth/request_token";
    private static final String AUTHORIZATION_URL = "https://api.twitter.com/oauth/authorize?oauth_token=";

    private static final String TWIT_LOGIN= "TwitterLogin";

    private final Context context;
    private final TwitterUser user;
    private final FacebookPage facebookPage;

    public TwitterRequestToken(Context context, TwitterUser user, FacebookPage facebookPage){
        client = new OkHttpClient().newBuilder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .build();
        this.context = context.getApplicationContext();
        this.user = user;
        this.facebookPage = facebookPage;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected HashMap<String,String> doInBackground(Object...objects) {

        return getOauthRequestToken();
    }

    @Override
    protected void onPostExecute(HashMap<String,String> requestTokens) {
        Log.d(TWIT_LOGIN,"Finished with request tokens");

        if(!requestTokens.isEmpty()) {
            Intent webViewIntent = new Intent(context, TwitterAuthorizeViewActivity.class);
            webViewIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            webViewIntent.putExtra("oauthRequestTokens", requestTokens);
            webViewIntent.putExtra("TwitterUser", user);
            webViewIntent.putExtra("FacebookPage", facebookPage);
            webViewIntent.putExtra("Source","TwitterLogin.class");

            context.startActivity(webViewIntent);
        }
    }

   @RequiresApi(api = Build.VERSION_CODES.N)
   private HashMap<String,String> getOauthRequestToken(){
       HashMap<String,String> requestTokenResponse = new HashMap<>();

       Map<String,String> reqParams = new HashMap<>();
       String header = new OAuthHeaderGenerator(user.getConsumerKey(),user.getConsumerSecret(),null,null,user.getOauthCallback())
               .generateOAuthHeader("POST", REQUEST_TOKEN_URL,reqParams).toString();

       MediaType mediaType = MediaType.parse("text/plain");
       RequestBody body = RequestBody.create(mediaType, "");
       Request request = new RequestCreation().createRequest("POST", REQUEST_TOKEN_URL,body,"Authorization",header);

       try {
           Response response = client.newCall(request).execute();
           if(response.isSuccessful()) {
               String[] splitResponse = response.body().string().split("&");
               String oauthCallbackConfirmed = splitResponse[2].replaceAll("oauth_callback_confirmed=", "");
               if (oauthCallbackConfirmed.equals("true")) {
                   String oauth_token = splitResponse[0].replaceAll("oauth_token=", "");
                   String oauth_token_secret = splitResponse[1].replaceAll("oauth_token_secret=", "");

                   requestTokenResponse.put("oauth_token", oauth_token);
                   requestTokenResponse.put("oauth_token_secret", oauth_token_secret);
                   requestTokenResponse.put("AuthorizationURL", AUTHORIZATION_URL +oauth_token);

               } else {
                   Log.d(TWIT_LOGIN, "Oauth callback is not confirmed!");
               }
           }else{
               Log.d(TWIT_LOGIN, "Something went wrong. Response code was " +response.code());
           }
       } catch (IOException e) {
           Log.d(TWIT_LOGIN,"Error happened getting response",e);
       }
       return requestTokenResponse;
   }
}
