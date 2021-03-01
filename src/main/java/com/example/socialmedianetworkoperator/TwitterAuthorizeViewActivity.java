package com.example.socialmedianetworkoperator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.HashMap;

public class TwitterAuthorizeViewActivity extends AppCompatActivity {

    private static final String TWITTER_AUTH = "TwitterWebView";
    private WebView oauthView;
    private String oauthToken;
    private HashMap<String,String> oauthInfoHashMap;
    private TwitterUser user;
    private FacebookPage facebookPage;
    private boolean isRedirectedToCallback=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter_login_web_view);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        oauthView = (WebView) findViewById(R.id.webview);


        WebSettings webSettings = oauthView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        Intent intent = getIntent();
        if(intent!=null) {
            oauthInfoHashMap = (HashMap<String, String>) intent.getSerializableExtra("oauthRequestTokens");
            oauthToken = oauthInfoHashMap.get("oauth_token");
            user = (TwitterUser) intent.getSerializableExtra("TwitterUser");
            facebookPage = (FacebookPage) intent.getSerializableExtra("FacebookPage");
        }

        oauthView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                Log.d(TWITTER_AUTH,"Page Finished with url:"+url);

                if(isOauthCallbackUrl(url, user.getOauthCallback()) && !isRedirectedToCallback) {
                    isRedirectedToCallback = true;
                    String oauth_verifier = getOauthVerifier(url, oauthToken);
                    oauthInfoHashMap.put("oauth_verifier",oauth_verifier);
                    if(oauth_verifier!=null) {
                        TwitterUserCredentialsRequest credentials = new TwitterUserCredentialsRequest(TwitterAuthorizeViewActivity.this, user, facebookPage);
                        credentials.execute(oauthInfoHashMap);
                    }
                }
            }
        });

      oauthView.loadUrl(oauthInfoHashMap.get("AuthorizationURL"));
    }

    @Override
    public void onBackPressed() {
        if(oauthView.canGoBack())
            oauthView.goBack();
        else
            super.onBackPressed();
    }

    private boolean isOauthCallbackUrl(String url, String oauth_callback){
        return url.startsWith(oauth_callback);
    }

    private String getOauthVerifier(String urlString, String requestToken){
        Uri url = Uri.parse(urlString);
        if(requestToken.equals(url.getQueryParameter("oauth_token"))){
            return url.getQueryParameter("oauth_verifier");
        }else{
            Log.d(TWITTER_AUTH,"Token does not match the request token");
            return null;
        }
    }
}