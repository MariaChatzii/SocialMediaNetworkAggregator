package com.example.socialmedianetworkoperator;

import android.annotation.SuppressLint;
import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;

import com.facebook.login.widget.LoginButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity  {

    public static Integer TWITTER = 1;
    public static Integer FACEBOOK = 2;
    public static Integer INSTAGRAM = 3;

    CallbackManager callbackManager;
    LoginButton fbLoginBtn;
    CheckBox instaCheckBox;
    CheckBox fbCheckBox;
    CheckBox twitterCheckBox;
    Button postOnSMNBtn, searchByHashtagBtn, twitterLoginBtn;
    TwitterUser twitterUser;
    FacebookPage facebookPage;


    private static final String TWIT_OAUTH_CALLBACK = "https://www.google.com";


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        postOnSMNBtn = findViewById(R.id.createStatusOrStoryButton);
        searchByHashtagBtn = findViewById(R.id.searchByHashtagButton);
        twitterCheckBox = (CheckBox) findViewById(R.id.twitterCheckBox);
        fbCheckBox = (CheckBox) findViewById(R.id.facebookCheckBox);
        instaCheckBox = (CheckBox) findViewById(R.id.instaCheckBox);
        fbLoginBtn = (LoginButton) findViewById(R.id.fblogin_button);
        twitterLoginBtn = findViewById(R.id.twitterLoginBtn);

        postOnSMNBtn.setEnabled(false);

        callbackManager = CallbackManager.Factory.create();

        twitterUser = new TwitterUser();
        facebookPage = new FacebookPage();

        AccessTokenTracker fbTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken accessToken, AccessToken accessToken2) {
                if (accessToken2 == null) {
                    Log.d("FB", "User Logged Out.");
                    facebookPage = new FacebookPage();
                } else {
                    Log.d("FB", "User Logged In.");
                    GetFacebookPageInfo fbPageGenerator = new GetFacebookPageInfo(MainActivity.this, twitterUser);
                    fbPageGenerator.execute();
                }
            }
        };

        View.OnClickListener twitterLoginListener = v -> {
            twitterUser.setConsumerKey(BuildConfig.TwitterApiKey);
            twitterUser.setConsumerSecret(BuildConfig.TwitterApiSecretKey);
            twitterUser.setOauthCallback(TWIT_OAUTH_CALLBACK);

            TwitterRequestToken twitLogin = new TwitterRequestToken(MainActivity.this, twitterUser, facebookPage);
            twitLogin.execute();
        };

        View.OnClickListener twitterLogoutListener= v -> {
            twitterLoginBtn.setText("Log in with Twitter");
            twitterUser.removeCredentials();
            twitterLoginBtn.setOnClickListener(twitterLoginListener);
        };


        Intent intent = getIntent();
        if(intent.getStringExtra("Source") == null) { //Application just started
            twitterLoginBtn.setOnClickListener(twitterLoginListener);
            if(AccessToken.getCurrentAccessToken()!=null)
                new GetFacebookPageInfo(MainActivity.this,twitterUser).execute();
        }else if(intent.getStringExtra("Source").equals("TwitterBearerTokenRequest.class")){ //Intent coming from TwitterBearerTokenRequest with user credentials
            facebookPage = (FacebookPage) intent.getSerializableExtra("FacebookPage");
            twitterUser = (TwitterUser) intent.getSerializableExtra("UserWithVerifiedCredentials");
            twitterLoginBtn.setText("Log out");
            twitterLoginBtn.setOnClickListener(twitterLogoutListener);
        }else if(intent.getStringExtra("Source").equals("GetFacebookPageInfo.class")){
            facebookPage = (FacebookPage) intent.getSerializableExtra("FacebookPage");
            twitterUser = (TwitterUser) intent.getSerializableExtra("TwitterUser");
            if(!twitterUser.getUsername().equals("")){
                twitterLoginBtn.setText("Log out");
                twitterLoginBtn.setOnClickListener(twitterLogoutListener);
            }else{
                twitterLoginBtn.setOnClickListener(twitterLoginListener);
            }
        }

        twitterCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked)
                postOnSMNBtn.setEnabled(true);
            else {
                if(!fbCheckBox.isChecked() && !twitterCheckBox.isChecked())
                    postOnSMNBtn.setEnabled(false);
            }
         });

        fbCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked)
                postOnSMNBtn.setEnabled(true);
            else {
                if (!instaCheckBox.isChecked() && !twitterCheckBox.isChecked())
                    postOnSMNBtn.setEnabled(false);
            }
        });

        instaCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked)
                postOnSMNBtn.setEnabled(true);
            else {
                if(!fbCheckBox.isChecked() && !twitterCheckBox.isChecked())
                postOnSMNBtn.setEnabled(false);
            }
        });

        View.OnClickListener createPostListener = view -> {
            ArrayList<Integer> selectedSocialMedia = selectedSM();
            startPostActivity(selectedSocialMedia);
        };

        View.OnClickListener searchByHashtagListener = v -> {
            if(!twitterUser.getUsername().equals(""))
                startHashtagSearchActivity(twitterUser);
            else {
                Toast toast = Toast.makeText(MainActivity.this, "First, login to Twitter!", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
            }
        };

        postOnSMNBtn.setOnClickListener(createPostListener);
        searchByHashtagBtn.setOnClickListener(searchByHashtagListener);
    }

    public void startPostActivity(ArrayList<Integer> selectedSocialMedia){
        Intent intent = new Intent(MainActivity.this, PostActivity.class);
        intent.putExtra("TwitterUser", twitterUser);
        intent.putExtra("FacebookPage", facebookPage);
        intent.putIntegerArrayListExtra("SelectedSocialMedia",selectedSocialMedia);
        intent.putExtra("Source","MainActivity.class");

        startActivity(intent);
    }

    public void startHashtagSearchActivity(TwitterUser user){
        Intent intent = new Intent(MainActivity.this, HashtagSearchActivity.class);
        intent.putExtra("TwitterUser", user);
        intent.putExtra("FacebookPage",facebookPage);
        intent.putExtra("Source","MainActivity.class");

        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode,resultCode,data);
    }

    public ArrayList<Integer> selectedSM(){
        ArrayList<Integer> checkedSMlist = new ArrayList<>();
        if(twitterCheckBox.isChecked())
            checkedSMlist.add(TWITTER);
        if(fbCheckBox.isChecked())
            checkedSMlist.add(FACEBOOK);
        if(instaCheckBox.isChecked())
            checkedSMlist.add(INSTAGRAM);
        return checkedSMlist;
    }


}