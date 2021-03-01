package com.example.socialmedianetworkoperator;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TwitterBearerTokenRequest extends AsyncTask<TwitterUser,Void,TwitterUser> {

    private static final String HTTP_POST = "POST";
    private static final String GET_BEARER_URL = "https://api.twitter.com/oauth2/token?grant_type=client_credentials";
    private static final String BEARER_LOG = "TwitterBearer";

    private final FacebookPage facebookPage;
    private final Context context;

    public TwitterBearerTokenRequest(Context context, FacebookPage facebookPage){
        this.context = context;
        this.facebookPage = facebookPage;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected TwitterUser doInBackground(TwitterUser... users) {
        TwitterUser twitterUser = users[0];

        return setUserBearerToken(twitterUser);
    }

    @Override
    protected void onPostExecute(TwitterUser user) {
        Intent intent = new Intent(context,MainActivity.class);
        intent.putExtra("UserWithVerifiedCredentials", user);
        intent.putExtra("FacebookPage", facebookPage);
        intent.putExtra("Source","TwitterBearerTokenRequest.class");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private TwitterUser setUserBearerToken(TwitterUser user){

        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .build();

        //Generate basic Authentication Header
        String basicAuthHeader = "Basic "+ Base64.getEncoder().encodeToString((user.getConsumerKey() + ":" + user.getConsumerSecret()).getBytes());

        //Request Twitter Bearer Token
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, "");
        Request request = new RequestCreation().createRequest(HTTP_POST,GET_BEARER_URL,body,"Authorization",basicAuthHeader);

        try {
            Response response = client.newCall(request).execute();
            if(response.isSuccessful()) {
                user.setBearerToken(new JSONObject(response.body().string()).getString("access_token"));
            }else {
                Log.d(BEARER_LOG, "Something went wrong getting Twitter Bearer Token. Response code was " + response.code());
            }
        }catch (IOException | JSONException e) {
            Log.e(BEARER_LOG, "Error happened getting response to Twitter Bearer Token request!", e);
        }
        return user;
    }
}
