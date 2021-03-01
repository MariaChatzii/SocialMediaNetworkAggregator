package com.example.socialmedianetworkoperator;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GetTweetsRequest extends AsyncTask<String, Void, List<SocialMediaStatus>> {

    private final static String TWITTER_LOGO = "https://cdn4.iconfinder.com/data/icons/social-media-icons-the-circle-set/48/twitter_circle-512.png";
    private final static String GET_TWEETS_URL = "https://api.twitter.com/1.1/search/tweets.json";

    private final static String GET_TWEETS_LOG = "GetTweets";
    public final static String HTTP_GET = "GET";

    private static final String TWEETS_MAX_COUNT = "100";
    private static final String TWIT_RESULT_TYPE = "mixed";

    private final StatusesAdapter adapter;
    private final TwitterUser user;

    public GetTweetsRequest(StatusesAdapter adapter, TwitterUser user){
        this.adapter = adapter;
        this.user = user;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected List<SocialMediaStatus> doInBackground(String... hashtags) {
        String selectedHashtag = hashtags[0];
        return getTweets(selectedHashtag);
    }

    @Override
    protected void onPostExecute(List<SocialMediaStatus> tweetsList) {
        Log.d(GET_TWEETS_LOG,"Finished with getting tweets request");
        if(!tweetsList.isEmpty()) {
            adapter.setData(tweetsList);
        }else{
            Log.d(GET_TWEETS_LOG,"List with Twitter statuses is empty.");
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private List<SocialMediaStatus> getTweets(String hashtag) {
        List<SocialMediaStatus> tweetsList = new ArrayList<>();

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        String urlOfRequest = GET_TWEETS_URL+"?count="+TWEETS_MAX_COUNT+"&result_type="+TWIT_RESULT_TYPE+"&tweet_mode=extended&q=%23"+hashtag;
        Request request = new RequestCreation().createRequest(HTTP_GET, urlOfRequest, null, "Authorization","Bearer "+user.getBearerToken());

        try {
            Response response = client.newCall(request).execute();
            if(!response.isSuccessful()){
                Log.d(GET_TWEETS_LOG, "Something went wrong getting tweets. Response code was "+ response.code());
                return null;
            }
            else {
                JSONArray jsonArray = new JSONObject(response.body().string()).getJSONArray("statuses");
                for(int i=0; i< jsonArray.length(); i++) {
                    JSONObject tweetJsonObject = jsonArray.getJSONObject(i);

                    if (tweetJsonObject.getString("in_reply_to_status_id_str").equals("null")) {   //check if tweet is a reply to another tweet
                        String tweetId = tweetJsonObject.getString("id");
                        String date = tweetJsonObject.getString("created_at");
                        String fullText = tweetJsonObject.getString("full_text");
                        String statusText = removeTweetUrlFromText(fullText);
                        String username = tweetJsonObject.getJSONObject("user").getString("screen_name");
                        String name = tweetJsonObject.getJSONObject("user").getString("name");
                        String image = null;

                        if (tweetJsonObject.getJSONObject("entities").has("media")) {
                            JSONArray mediaArray = tweetJsonObject.getJSONObject("entities").getJSONArray("media");
                            String media = mediaArray.getJSONObject(0).getString("media_url");
                            image = getImagePicassoFormat(media);
                        }
                        Integer retweetCounts = tweetJsonObject.getInt("retweet_count");
                        Integer likesCounts = tweetJsonObject.getInt("favorite_count");


                        SocialMediaStatus status = new SocialMediaStatus(statusText, null);
                        status.setStatusId(tweetId);
                        status.setSocialMedia("Twitter");
                        status.setSocialMediaLogo(TWITTER_LOGO);
                        status.setCreationDate(date);
                        status.setCreatorUsername("@" + username);
                        status.setCreatorName(name);
                        if (image != null) status.setStatusImage(image);
                        status.setSharesCount(retweetCounts);
                        status.setLikesCount(likesCounts);

                        tweetsList.add(status);
                    }
                }
            }
            return tweetsList;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Log.e(GET_TWEETS_LOG, "Error happened getting tweets request!", e);
            return tweetsList;
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

    private String getImagePicassoFormat(String imageStringUri){
        return imageStringUri.replaceAll("http://","https://");
    }
}
