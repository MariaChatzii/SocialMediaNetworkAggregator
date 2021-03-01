package com.example.socialmedianetworkoperator;

import java.io.Serializable;

public class FacebookPage implements Serializable {

    private String pageId;
    private String pageAccessToken;

    public FacebookPage(){
        pageId = "";
        pageAccessToken = "";
    }

    public String getPageId() {
        return pageId;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    public String getPageAccessToken() {
        return pageAccessToken;
    }

    public void setPageAccessToken(String pageAccessToken) {
        this.pageAccessToken = pageAccessToken;
    }
}
