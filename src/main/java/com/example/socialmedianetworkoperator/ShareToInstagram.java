package com.example.socialmedianetworkoperator;

import android.content.Intent;
import android.net.Uri;

public class ShareToInstagram {

    public Intent createStoryIntent(Story story){
        Intent storyIntent = new Intent("com.instagram.share.ADD_TO_STORY");
        storyIntent.setDataAndType(Uri.parse(story.getStoryImage()), "image/*");
        storyIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return storyIntent;
    }

    public Intent createPostIntent(SocialMediaStatus post){
        Intent postIntent = new Intent(Intent.ACTION_SEND);
        postIntent.setPackage("com.instagram.android");
        postIntent.setType("image/*");
        postIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(post.getStatusImage()));
        return postIntent;
    }
}
