package com.example.socialmedianetworkoperator;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TwitterUserCredentialsRequest extends AsyncTask<HashMap<String,String>, Void, TwitterUser> {
    private static final String TWIT_ACCESS_TOKENS = "TwitterGetAccessTokens";
    private static final String VERIFY_CREDENTIALS_URL = "https://api.twitter.com/1.1/account/verify_credentials.json";
    private static final String ACCESS_TOKEN_URL ="https://api.twitter.com/oauth/access_token";


    private final OkHttpClient client;
    private final Context context;
    private final TwitterUser user;
    private final FacebookPage facebookPage;

    public TwitterUserCredentialsRequest(Context context, TwitterUser user, FacebookPage facebookPage){
        client = new OkHttpClient().newBuilder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .build();
        this.context = context;
        this.user = user;
        this.facebookPage = facebookPage;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected TwitterUser doInBackground(HashMap<String,String>... hashMaps) {
        HashMap<String,String> oauthInfoHashMap = hashMaps[0];
        String oauthToken = oauthInfoHashMap.get("oauth_token");
        String verifier = oauthInfoHashMap.get("oauth_verifier");

        getUserCredentials(verifier, oauthToken);
        if(!isUserCredentialsVerified(user))
            user.removeCredentials();
        return user;
    }

    @Override
    protected void onPostExecute(TwitterUser user) {
        Log.d(TWIT_ACCESS_TOKENS,"Finished with user credentials request");
        if(!user.getUserId().equals("")){
            new TwitterBearerTokenRequest(context, facebookPage).execute(user);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void getUserCredentials(String verifier, String oauthToken){

        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "");

        String urlOfRequest = ACCESS_TOKEN_URL+"?oauth_token="+oauthToken+"&oauth_verifier="+verifier;

        Request request = new RequestCreation().createRequest("POST",urlOfRequest,body,"Content-Type","application/x-www-form-urlencoded");

        try {
            Response response = client.newCall(request).execute();
            if(response.isSuccessful()) {
                String[] splitResponse = response.body().string().split("&");

                String access_token = splitResponse[0].replaceAll("oauth_token=","");
                String access_token_secret = splitResponse[1].replaceAll("oauth_token_secret=","");
                String user_id = splitResponse[2].replaceAll("user_id=","");
                String username = splitResponse[3].replaceAll("screen_name=","");

                user.setUserId(user_id);
                user.setUsername("@"+username);
                user.setAccessToken(access_token);
                user.setAccessTokenSecret(access_token_secret);

            }else{
                Log.d(TWIT_ACCESS_TOKENS, "Something went wrong. Response code was " +response.code());
            }
        } catch (IOException e) {
            Log.d(TWIT_ACCESS_TOKENS,"Error happened getting response",e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private boolean isUserCredentialsVerified(TwitterUser user){

        Log.d(TWIT_ACCESS_TOKENS,"TOKEN:"+user.getAccessToken());

        //Generate Authorization header
        Map<String,String> requestParams = new HashMap<>();
        String header = new OAuthHeaderGenerator(user.getConsumerKey(), user.getConsumerSecret(), user.getAccessToken(), user.getAccessTokenSecret(),null)
                .generateOAuthHeader("GET",VERIFY_CREDENTIALS_URL,requestParams).toString();
        Request request = new RequestCreation().createRequest("GET",VERIFY_CREDENTIALS_URL,null,"Authorization",header);


        try {
            Response response = client.newCall(request).execute();
            if(response.isSuccessful()) {
                JSONObject credentialsObj = new JSONObject(response.body().string());
                String user_id = credentialsObj.getString("id_str");
                String username = "@"+credentialsObj.getString("screen_name");

                if(user_id.equals(user.getUserId()) && username.equals(user.getUsername()))
                    return true;
                else
                    Log.d(TWIT_ACCESS_TOKENS,"User Credentials are not valid.");
            }else{
                Log.d("VERIFY_CREDENTIALS", "Something went wrong. Response code was " +response.code());
            }
        } catch (IOException | JSONException e) {
            Log.d(TWIT_ACCESS_TOKENS,"Error happened getting response",e);
        }
        return false;
    }
}
