package com.example.socialmedianetworkoperator;

import java.io.File;
import java.io.Serializable;

public class SocialMediaStatus implements Serializable {


    private String statusId;
    private String socialMedia;
    private String socialMediaLogo;
    private String creatorUsername;
    private String creatorName;
    private String creationDate;
    private final String statusText;

    private Integer likesCount;
    private Integer sharesCount; //how many times the post was shared by others.

    private String statusImage;
    private File statusImgRealPath;

    public SocialMediaStatus(String statusText, String statusImage){

        this.statusText = statusText;
        this.statusImage = statusImage;
        statusImgRealPath = null;
        likesCount = 0;
        sharesCount = 0;
    }

    public String getStatusId() {
        return statusId;
    }

    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }

    public String getSocialMedia() {
        return socialMedia;
    }

    public void setSocialMedia(String socialMedia) {
        this.socialMedia = socialMedia;
    }

    public String getSocialMediaLogo() {
        return socialMediaLogo;
    }

    public void setSocialMediaLogo(String socialMediaLogo) {
        this.socialMediaLogo = socialMediaLogo;
    }

    public Integer getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(Integer likesCount) {
        this.likesCount = likesCount;
    }

    public Integer getSharesCount() {
        return sharesCount;
    }

    public void setSharesCount(Integer sharesCount) {
        this.sharesCount = sharesCount;
    }

    public String getCreatorUsername() {
        return creatorUsername;
    }

    public void setCreatorUsername(String creatorUsername) {
        this.creatorUsername = creatorUsername;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public void setStatusImage(String statusImage) {
           this.statusImage = statusImage;
    }

    public File getStatusImgRealPath() {
        return statusImgRealPath;
    }

    public void setStatusImgRealPath(File statusImgRealPath) { this.statusImgRealPath = statusImgRealPath; }

    public String getStatusText() {
        return statusText;
    }

    public String getStatusImage() {
        return statusImage;
    }
}
