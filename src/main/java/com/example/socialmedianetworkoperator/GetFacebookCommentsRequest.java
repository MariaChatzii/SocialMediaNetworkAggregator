package com.example.socialmedianetworkoperator;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.facebook.AccessToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GetFacebookCommentsRequest extends AsyncTask<SocialMediaStatus, Void, List<SocialMediaStatus>> {

    private final static String FB_COMMENTS = "GetFacebookComments";
    private final static String FB_GRAPH_URL = "https://graph.facebook.com/";
    private final static String HTTP_GET = "GET";

    private final StatusesAdapter adapter;
    private final Context context;
    private ProgressDialog dialog;

    public GetFacebookCommentsRequest(Context context,StatusesAdapter adapter){
        this.adapter = adapter;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        dialog = ProgressDialog.show(context,"Please Wait","Loading...");
    }

    @Override
    protected List<SocialMediaStatus> doInBackground(SocialMediaStatus... facebookStatuses) {
        SocialMediaStatus selectedStatus = facebookStatuses[0];

        Log.d(FB_COMMENTS,"Facebook status ID:"+selectedStatus.getStatusId());

        return getFacebookStatusComments(selectedStatus.getStatusId());

    }

    @Override
    protected void onPostExecute(List<SocialMediaStatus> comments) {
        if(dialog.isShowing()) {
            dialog.dismiss();
        }
        Log.d(FB_COMMENTS, "Finished with Facebook getting Comments Request");
        if (!comments.isEmpty()){
            adapter.setData(comments);
        }else{
            Log.d(FB_COMMENTS,"There are not comments on this status post.");
        }
    }

    private List<SocialMediaStatus> getFacebookStatusComments(String statusId){
        List<SocialMediaStatus> comments = new ArrayList<>();

        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .build();


        String urlOfRequest = FB_GRAPH_URL+statusId+"/comments?fields=id,from,message,created_time,attachment&access_token="+ AccessToken.getCurrentAccessToken().getToken();
        Request request = new RequestCreation().createRequest(HTTP_GET,urlOfRequest,null,null,null);

        try {
            Response response = client.newCall(request).execute();
            if(!response.isSuccessful())
                Log.d(FB_COMMENTS, "Something went wrong getting Facebook comments. Response code was "+ response.code());
            else {
                JSONArray dataArray = new JSONObject(response.body().string()).getJSONArray("data");

                for(int i=0;i<dataArray.length();i++){
                    JSONObject commentObj = dataArray.getJSONObject(i);
                    SocialMediaStatus comment = new SocialMediaStatus(commentObj.getString("message"),null);
                    comment.setCreationDate(commentObj.getString("created_time"));

                    if(commentObj.has("attachment")) {

                        JSONObject attachmentObj = commentObj.getJSONObject("attachment");
                        comment.setStatusImage(attachmentObj.getJSONObject("media").getJSONObject("image").getString("src"));
                    }

                    if(commentObj.has("from")){
                        comment.setCreatorName(commentObj.getJSONObject("from").getString("name"));
                    }else {
                        comment.setCreatorName("Unavailable Name");
                    }
                    comment.setCreatorUsername("");

                    comments.add(comment);
                }
            }
            return comments;
        } catch (IOException | JSONException e) {
            Log.e(FB_COMMENTS, "Error happened getting Facebook comments!", e);
            return comments;
        }

    }

}
