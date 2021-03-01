package com.example.socialmedianetworkoperator;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.facebook.AccessToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GetFacebookPageInfo extends AsyncTask<Object,Void,FacebookPage> {

    private final static String FB_GRAPH_URL = "https://graph.facebook.com/";
    private final static String FB_PAGE_INFO = "GetFacebookPageInfo";

    private final Context context;
    private ProgressDialog dialog;
    private final TwitterUser user;

    public GetFacebookPageInfo(Context context,TwitterUser user){
        this.context = context;
        this.user = user;
    }

    @Override
    protected void onPreExecute() {
        dialog = ProgressDialog.show(context,"Please Wait","Loading...");
    }

    @Override
    protected FacebookPage doInBackground(Object[] objects) {
        return getPageData();
    }

    @Override protected void onPostExecute(FacebookPage page) {
        if(dialog.isShowing()) {
            dialog.dismiss();
        }

        Log.d(FB_PAGE_INFO,"Finished with facebook page info request");

        if(!page.getPageId().equals("")){
            Intent intent = new Intent(context,MainActivity.class);
            intent.putExtra("FacebookPage", page);
            intent.putExtra("TwitterUser",user);
            intent.putExtra("Source","GetFacebookPageInfo.class");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(intent);
        }
    }

    private FacebookPage getPageData(){

        FacebookPage page = new FacebookPage();

        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .build();

        String urlOfRequest = FB_GRAPH_URL+"/me/accounts?access_token="+ AccessToken.getCurrentAccessToken().getToken(); //using user AccessToken
        Request request = new RequestCreation().createRequest("GET", urlOfRequest,null,null,null);

        try {
            Response response = client.newCall(request).execute();
            if(response.isSuccessful()){
                JSONArray accountDataArray = new JSONObject(response.body().string()).getJSONArray("data");

                //Taking the id of the first page user owns
                JSONObject accountDataObject = accountDataArray.getJSONObject(0);

                String fbPageAccessTokenString = accountDataObject.getString("access_token");
                String fbPageId = accountDataObject.getString("id");

                page.setPageAccessToken(fbPageAccessTokenString);
                page.setPageId(fbPageId);
            }else{
                Log.d(FB_PAGE_INFO,"Something went wrong. Response code was " +response.code());
            }
            return page;
        } catch (IOException | JSONException e) {
            Log.d(FB_PAGE_INFO,"Error happened getting response",e);
            return page;
        }
    }
}
