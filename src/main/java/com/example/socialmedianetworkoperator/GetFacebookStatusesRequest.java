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

public class GetFacebookStatusesRequest extends AsyncTask<String, Void, List<SocialMediaStatus>> {

    private final static String FB_PAGE_STATUSES = "GetFacebookPageStatuses";
    public final static String HTTP_GET = "GET";
    private final static String FB_GRAPH_URL = "https://graph.facebook.com/";
    private static final String FACEBOOK_LOGO = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSmyfCiUpkiFKJ0p9PCMlkU2kzDclt8GWU7zQ&usqp=CAU";

    private final StatusesAdapter adapter;
    private final FacebookPage facebookPage;

    public GetFacebookStatusesRequest(StatusesAdapter adapter,FacebookPage facebookPage){
        this.adapter = adapter;
        this.facebookPage = facebookPage;
    }

    @Override
    protected List<SocialMediaStatus> doInBackground(String... hashtags) {
        String selectedHashtag = hashtags[0];

        return getFacebookPageStatuses(selectedHashtag);
    }

    @Override
    protected void onPostExecute(List<SocialMediaStatus> facebookPageStatuses) {
        Log.d(FB_PAGE_STATUSES,"Finished with Facebook Page getting statuses Request");
        if(!facebookPageStatuses.isEmpty()){
            adapter.setData(facebookPageStatuses);
        }else{
            Log.d(FB_PAGE_STATUSES,"List with Facebook statuses is empty.");
        }
    }

    private List<SocialMediaStatus> getFacebookPageStatuses(String hashtag){
        List<SocialMediaStatus> pageStatuses = new ArrayList<>();

        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .build();

        String urlOfRequest = FB_GRAPH_URL+facebookPage.getPageId()+"/feed?fields=from,message,created_time,attachments,shares&limit=100&access_token="+ AccessToken.getCurrentAccessToken().getToken();
        Request request = new RequestCreation().createRequest(HTTP_GET, urlOfRequest,null,null,null);

        try {
            Response response = client.newCall(request).execute();
            if(!response.isSuccessful()) {
                Log.d(FB_PAGE_STATUSES, "Something went wrong getting Facebook statuses. Response code was " + response.code());
            }else{
                JSONArray dataArray = new JSONObject(response.body().string()).getJSONArray("data");
                for(int i=0;i<dataArray.length();i++) {
                    JSONObject dataObject = dataArray.getJSONObject(i);

                    String statusText = null;
                    if(dataObject.has("message"))
                        statusText = dataObject.getString("message");

                    if(statusText!=null && statusText.toLowerCase().contains(hashtag.toLowerCase())) {
                        SocialMediaStatus pageStatus = new SocialMediaStatus(statusText,null);

                        pageStatus.setCreationDate(dataObject.getString("created_time"));
                        pageStatus.setStatusId(dataObject.getString("id"));
                        pageStatus.setCreatorName(dataObject.getJSONObject("from").getString("name"));
                        pageStatus.setCreatorUsername("");

                        if(dataObject.has("shares"))
                            pageStatus.setSharesCount(Integer.parseInt(dataObject.getJSONObject("shares").getString("count")));

                        if(dataObject.has("attachments")) {
                            JSONArray dataAttachmentArray = dataObject.getJSONObject("attachments").getJSONArray("data");
                            for(int j=0;j<dataAttachmentArray.length();j++) {
                                JSONObject dataAttachmentObject = dataAttachmentArray.getJSONObject(j);
                                if(dataAttachmentObject.getString("type").equals("photo")){
                                    pageStatus.setStatusImage(dataAttachmentObject.getJSONObject("media").getJSONObject("image").getString("src"));
                                }
                            }
                        }
                        pageStatus.setSocialMedia("Facebook");
                        pageStatus.setSocialMediaLogo(FACEBOOK_LOGO);

                        pageStatuses.add(pageStatus);
                    }

                }
            }
            return pageStatuses;
        } catch (IOException | JSONException e) {
            Log.e(FB_PAGE_STATUSES, "Error happened with Facebook getting Page Statuses response!", e);
            return pageStatuses;
        }
    }

}
