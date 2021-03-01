package com.example.socialmedianetworkoperator;

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

public class GetInstagramStatusesRequest extends AsyncTask<String, Void, List<SocialMediaStatus>> {

    private final StatusesAdapter adapter;
    private final String statusType;
    private final FacebookPage facebookPage;

    private final static String GET_INSTA_POSTS_LOG = "GetInstaPosts";
    public final static String HTTP_GET = "GET";

    private final static String FB_GRAPH_URL = "https://graph.facebook.com/";
    private final static String INSTA_OEMBED_URL = "/instagram_oembed?url=";
    private final static String INSTA_LOGO = "https://instagram-brand.com/wp-content/themes/ig-branding/assets/images/ig-logo-email.png";

    private static final String  INSTA_POSTS_MAX_LIMIT = "50";
    private static final String REQUESTED_FIELDS = "id,caption,media_type,media_url,timestamp,like_count,comments_count,permalink";



    public GetInstagramStatusesRequest(StatusesAdapter adapter, String statusType,FacebookPage facebookPage){
        this.adapter = adapter;
        this.statusType = statusType;
        this.facebookPage = facebookPage;
    }

    @Override
    protected List<SocialMediaStatus> doInBackground(String... hashtags) {
        String selectedHashtag = hashtags[0];

        String instaUserId = getInstaUserId();
        String hashtagId = getHashtagId(selectedHashtag, instaUserId);

        return getInstaStatuses(hashtagId,instaUserId);
    }

    @Override
    protected void onPostExecute(List<SocialMediaStatus> instagramStatuses) {
        Log.d(GET_INSTA_POSTS_LOG, "Finished with getting Instagram statuses request");
        if (!instagramStatuses.isEmpty()){
            adapter.setData(instagramStatuses);
        }else{
            Log.d(GET_INSTA_POSTS_LOG,"List with Instagram statuses is empty.");
        }
    }

    private String getInstaUserId(){
        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .build();

        String urlOfRequest = FB_GRAPH_URL+facebookPage.getPageId()+"?fields=instagram_business_account&access_token="+ AccessToken.getCurrentAccessToken().getToken();
        Request request = new RequestCreation().createRequest(HTTP_GET, urlOfRequest,null,null,null);

        try {
            Response response = client.newCall(request).execute();
            if(!response.isSuccessful()) {
                Log.d(GET_INSTA_POSTS_LOG, "Something went wrong getting insta userId. Response code was " + response.code());
                return "";
            }else {
                JSONObject jsonObj = new JSONObject(response.body().string());
                return jsonObj.getJSONObject("instagram_business_account").getString("id");
            }
        } catch (IOException | JSONException e) {
            Log.e(GET_INSTA_POSTS_LOG, "Error happened getting insta userId request!", e);
            return "";
        }
    }

    private String getHashtagId(String hashtag, String instaUserId){
        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .build();
        String urlOfRequest = FB_GRAPH_URL+"ig_hashtag_search?user_id="+instaUserId+"&q="+hashtag+"&access_token="+AccessToken.getCurrentAccessToken().getToken();
        Request request = new RequestCreation().createRequest(HTTP_GET,urlOfRequest,null,null,null);

        try {
            Response response = client.newCall(request).execute();
            if(!response.isSuccessful()) {
                Log.d(GET_INSTA_POSTS_LOG, "Something went wrong getting insta hashtagId. Response code was " + response.code());
            }else if(response.body()!=null){
                return new JSONObject(response.body().string()).getJSONArray("data")
                        .getJSONObject(0).getString("id");
            }
            return "";
        } catch (IOException | JSONException e) {
            Log.e(GET_INSTA_POSTS_LOG, "Error happened getting insta hashtagId request!", e);
            return "";
        }

    }

    private List<SocialMediaStatus> getInstaStatuses(String instaHashtagId, String instaUserId){
        List<SocialMediaStatus> instaStatusList = new ArrayList<>();

        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .build();

        String urlOfRequest = FB_GRAPH_URL+instaHashtagId+"/"+statusType+"?user_id="+instaUserId+"&limit="+ INSTA_POSTS_MAX_LIMIT
                +"&access_token="+ AccessToken.getCurrentAccessToken().getToken()+"&fields="+REQUESTED_FIELDS;
        Request request = new RequestCreation().createRequest(HTTP_GET,urlOfRequest,null,null,null);
        try {
            Response response = client.newCall(request).execute();
            if(!response.isSuccessful()) {
                Log.d(GET_INSTA_POSTS_LOG, "Something went wrong getting insta "+statusType+".Response code was " + response.code());
            }else if(response.body()!=null){
                JSONArray topPostsArray= new JSONObject(response.body().string()).getJSONArray("data");
                for(int i=0;i< topPostsArray.length(); i++) {
                    JSONObject statusInfo = topPostsArray.getJSONObject(i);

                    if(statusInfo.getString("media_type").equals("VIDEO")) //Don't show insta post with video
                        return instaStatusList;
                    String statusId = statusInfo.getString("id");
                    String caption=null;
                    if(statusInfo.has("caption"))
                        caption = statusInfo.getString("caption");
                    String imageUrl = null;
                    if(statusInfo.has("media_url"))
                        imageUrl = statusInfo.getString("media_url");
                    String creationDate = statusInfo.getString("timestamp");
                    Integer likesCount = statusInfo.getInt("like_count");
                    String permalink = statusInfo.getString("permalink");

                    String username = getPostCreatorUsername(permalink);

                    SocialMediaStatus status = new SocialMediaStatus(caption, null);
                    status.setSocialMedia("Instagram");
                    status.setStatusId(statusId);
                    status.setSocialMediaLogo(INSTA_LOGO);
                    status.setCreatorName("@"+username);
                    status.setCreatorUsername(""); //Username of post author cannot be gained
                    if(imageUrl!=null) status.setStatusImage(imageUrl);
                    status.setCreationDate(creationDate);
                    status.setLikesCount(likesCount);

                    instaStatusList.add(status);
                }
            }
            return  instaStatusList;
        } catch (IOException | JSONException e) {
            Log.e(GET_INSTA_POSTS_LOG, "Error happened getting insta "+statusType+" statuses response!", e);
            return instaStatusList;
        }
    }

    private String getPostCreatorUsername(String permalink){
        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1,TimeUnit.MINUTES)
                .readTimeout(1,TimeUnit.MINUTES)
                .build();

        String urlOfRequest = FB_GRAPH_URL+INSTA_OEMBED_URL+permalink+"&access_token="+AccessToken.getCurrentAccessToken().getToken();
        Request request = new RequestCreation().createRequest(HTTP_GET,urlOfRequest,null,null,null);

        try {
            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()){
                Log.d(GET_INSTA_POSTS_LOG, "Something went wrong getting insta post creator's username. Response code was " + response.code());
                return "";
            }else {
                JSONObject json = new JSONObject(response.body().string());
                return json.getString("author_name");
            }
        }catch(IOException | JSONException e) {
            Log.e(GET_INSTA_POSTS_LOG, "Error happened getting post creator's username!", e);
        }
        return  null;
    }
}
