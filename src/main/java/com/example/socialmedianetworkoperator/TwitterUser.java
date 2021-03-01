package com.example.socialmedianetworkoperator;

import java.io.Serializable;

public class TwitterUser implements Serializable {

    private String userId;
    private String username;
    private String accessToken;
    private String accessTokenSecret;
    private String consumerKey;
    private String consumerSecret;
    private String bearerToken;
    private String oauthCallback;

    public TwitterUser(){
        removeCredentials();
    }

    public void removeCredentials(){
        setUserId("");
        setUsername("");
        setAccessToken("");
        setAccessTokenSecret("");
        setConsumerKey("");
        setConsumerSecret("");
        setBearerToken("");
        setOauthCallback("");
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessTokenSecret() {
        return accessTokenSecret;
    }

    public void setAccessTokenSecret(String accessTokenSecret) {
        this.accessTokenSecret = accessTokenSecret;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public String getConsumerSecret() {
        return consumerSecret;
    }

    public void setConsumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
    }

    public String getBearerToken() {
        return bearerToken;
    }

    public void setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    public String getOauthCallback() {
        return oauthCallback;
    }

    public void setOauthCallback(String oauthCallback) {
        this.oauthCallback = oauthCallback;
    }
}
