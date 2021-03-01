package com.example.socialmedianetworkoperator;

import android.app.ProgressDialog;
import android.content.Context;
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
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GetTwitterFriendsCommentsRequest extends AsyncTask<SocialMediaStatus, Void, List<SocialMediaStatus>> {

    private final static String TWIT_FRIENDS_REPLIES = "TwitterFriendsReplies";

    private final static String GET_FRIENDS_TWIT_URL = "https://api.twitter.com/1.1/friends/ids.json";
    private final static String GET_REPLIES_TWIT_URL = "https://api.twitter.com/2/tweets/search/recent?query=conversation_id:";
    public final static String HTTP_GET = "GET";

    private ProgressDialog dialog;
    private final StatusesAdapter adapter;
    private final TwitterUser user;
    private final Context context;

    public GetTwitterFriendsCommentsRequest(Context context,StatusesAdapter adapter, TwitterUser user){
        this.adapter = adapter;
        this.user = user;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        dialog = ProgressDialog.show(context,"Please Wait","Loading...");
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected List<SocialMediaStatus> doInBackground(SocialMediaStatus... twitterStatuses) {
        SocialMediaStatus selectedStatus = twitterStatuses[0];
        List<String> userFriends = getTwitterFriends();

        Log.d(TWIT_FRIENDS_REPLIES,"Tweet Id:"+selectedStatus.getStatusId());

        List<SocialMediaStatus> friendsReplies = new ArrayList<>();
        if(!userFriends.isEmpty())
            return getFriendRepliesTwitter(userFriends,selectedStatus.getStatusId());
        return friendsReplies;
    }

    @Override
    protected void onPostExecute(List<SocialMediaStatus> friendsReplies) {
        if(dialog.isShowing()) {
            dialog.dismiss();
        }
        Log.d(TWIT_FRIENDS_REPLIES,"Finished with getting comments on this status request");
        if(!friendsReplies.isEmpty())
            adapter.setData(friendsReplies);
        else{
            Log.d(TWIT_FRIENDS_REPLIES,"The Accounts that users follow did not replied to this tweet.");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private List<String> getTwitterFriends(){
        List<String> twitterFriends= new ArrayList<>();

        //Generate authorization Header
        Map<String,String> requestParams = new HashMap<>();
        String oauthHeader = new OAuthHeaderGenerator(user.getConsumerKey(), user.getConsumerSecret(), user.getAccessToken(), user.getAccessTokenSecret(),null)
                .generateOAuthHeader(HTTP_GET,GET_FRIENDS_TWIT_URL,requestParams).toString();

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new RequestCreation().createRequest(HTTP_GET,GET_FRIENDS_TWIT_URL,null,"Authorization",oauthHeader);

        try {
            Response response = client.newCall(request).execute();
            if(!response.isSuccessful()) {
                Log.d(TWIT_FRIENDS_REPLIES, "Something went wrong getting user twitter friends. Response code was " + response.code());
            }else {
                JSONArray friendsArray = new JSONObject(response.body().string()).getJSONArray("ids");
                for(int i =0;i<friendsArray.length();i++)
                    twitterFriends.add(friendsArray.get(i).toString());
            }
            return twitterFriends;
        } catch (IOException | JSONException e) {
            Log.e(TWIT_FRIENDS_REPLIES, "Error happened getting user twitter friends request!", e);
            return twitterFriends;
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private List<SocialMediaStatus> getFriendRepliesTwitter(List<String> friendsList, String tweetId){
        List<SocialMediaStatus> commentsList = new ArrayList<>();

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        String urlOfRequest = GET_REPLIES_TWIT_URL+tweetId+"&user.fields=name,username&tweet.fields=created_at" +
                "&expansions=attachments.media_keys,author_id&media.fields=url&max_results=100";
        Request request = new RequestCreation().createRequest(HTTP_GET,urlOfRequest,null,"Authorization","Bearer "+ user.getBearerToken());

        try {
            Response response = client.newCall(request).execute();
            if(!response.isSuccessful())
                Log.d(TWIT_FRIENDS_REPLIES, "Something went wrong getting Tweet's replies. Response code was "+ response.code());
            else {
                JSONObject jsonResponse = new JSONObject(response.body().string());

                //First check if tweet has not replies
                if(new JSONObject(jsonResponse.getString("meta")).getString("result_count").equals("0"))
                    return commentsList;

                JSONArray repliesArray = jsonResponse.getJSONArray("data");
                for(int i =0;i<repliesArray.length();i++){
                    JSONObject reply = repliesArray.getJSONObject(i);

                    if(friendsList.contains(reply.getString("author_id"))){
                        SocialMediaStatus comment = new SocialMediaStatus
                                ("Replying to "+ removeTweetUrlFromText(reply.getString("text")),null);

                        comment.setCreationDate(reply.getString("created_at"));
                        String author_id = reply.getString("author_id");

                        JSONObject includesObj = jsonResponse.getJSONObject("includes");
                        JSONArray usersArray = includesObj.getJSONArray("users");
                        for(int j=0;j<usersArray.length();j++){
                            JSONObject userObj = usersArray.getJSONObject(j);
                            if(userObj.getString("id").equals(author_id)){
                                comment.setCreatorUsername("@"+userObj.getString("username"));
                                comment.setCreatorName(userObj.getString("name"));
                                break;
                            }
                        }

                        if(reply.has("attachments")){
                            JSONObject attachments = reply.getJSONObject("attachments");
                            String mediaKey = attachments.getJSONArray("media_keys").get(0).toString();
                            JSONArray mediaArray = includesObj.getJSONArray("media");
                            for(int k=0;k<mediaArray.length();k++){
                                JSONObject mediaObj =  mediaArray.getJSONObject(k);
                                if(mediaObj.getString("media_key").equals(mediaKey) && mediaObj.has("url")) {
                                    comment.setStatusImage(mediaObj.getString("url"));
                                    break;
                                }
                            }
                        }
                        commentsList.add(comment);
                    }
                }
            }
            return commentsList;
        } catch (IOException | JSONException e) {
            Log.e(TWIT_FRIENDS_REPLIES, "Error happened getting Tweet's replies request!", e);
            return commentsList;
        }

    }

    private String removeTweetUrlFromText(String string){ // remove the url that links to the tweet
        String newString=string;
        int count=0;
        String[] words = string.split("\\s+");
        for( String word : words){
            if (count == words.length-1 && word.startsWith("https://t.co")){
                newString = newString.replace(word,"");
                break;
            }
            count++;
        }

        return newString;
    }
}
