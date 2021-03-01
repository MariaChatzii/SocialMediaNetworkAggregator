package com.example.socialmedianetworkoperator;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class StatusInfoActivity extends AppCompatActivity{

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_info);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        View includedLayout = findViewById(R.id.listview_selected_item);
        TextView username = includedLayout.findViewById(R.id.username);
        TextView name = includedLayout.findViewById(R.id.name);
        TextView bodyText = includedLayout.findViewById(R.id.statusText);
        TextView creationDate = includedLayout.findViewById(R.id.postCreationDate);
        ImageView bodyImage = includedLayout.findViewById(R.id.postImage);
        ImageView logo = includedLayout.findViewById(R.id.socialMediaLogo);
        TextView likesCount = findViewById(R.id.likesCount);
        TextView sharesCount = findViewById(R.id.sharesCount);
        TextView likesTextView = findViewById(R.id.likesTextView);
        likesTextView.setVisibility(INVISIBLE);
        ListView commentsListView = findViewById(R.id.commentsListView);



        Intent intent = getIntent();
        SocialMediaStatus status =  (SocialMediaStatus) intent.getSerializableExtra("SelectedStatus");
        TwitterUser user = (TwitterUser) intent.getSerializableExtra("TwitterUser");

        username.setText(status.getCreatorUsername());
        name.setText(status.getCreatorName());
        creationDate.setText(status.getCreationDate());
        bodyText.setText(status.getStatusText());
        if(status.getStatusImage()!=null)
            Picasso.get().load(status.getStatusImage()).into(bodyImage);
        Picasso.get().load(status.getSocialMediaLogo()).into(logo);
        sharesCount.setText(status.getSharesCount()+"");


        if(status.getSocialMedia().equals("Twitter")) {
            likesTextView.setVisibility(VISIBLE);
            likesCount.setText(status.getLikesCount()+"");
        }

        StatusesAdapter commentsAdapter =
                new StatusesAdapter(this,
                        R.layout.statuses_list_record,
                        new ArrayList<>(),
                        commentsListView
                );

        if(status.getSocialMedia().equals("Twitter")) {
            new GetTwitterFriendsCommentsRequest(StatusInfoActivity.this,commentsAdapter,user).execute(status);
        }else if(status.getSocialMedia().equals("Facebook")) {
            new GetFacebookCommentsRequest(StatusInfoActivity.this,commentsAdapter).execute(status);
        }
    }
}