package com.nkoad.wallbler.core;

import java.util.Date;
import java.util.Objects;

public abstract class WallblerItem {
    private int socialId;
    private String socialMediaType;
    private String title;
    private String description;
    private Date date;
    private String url;             // link to the account
    private String linkToSMPage;    // link to the post
    private Boolean accepted;

    public WallblerItem(String socialMediaType) {
        this.socialMediaType = socialMediaType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getLinkToSMPage() {
        return linkToSMPage;
    }

    public void setLinkToSMPage(String linkToSMPage) {
        this.linkToSMPage = linkToSMPage;
    }

    public String getSocialMediaType() {
        return socialMediaType;
    }

    public Boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(Boolean accepted) {
        this.accepted = accepted;
    }

    public int getSocialId() {
        return socialId;
    }

    public void generateSocialId() {
        this.socialId = this.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WallblerItem that = (WallblerItem) o;
        return Objects.equals(socialMediaType, that.socialMediaType) &&
                Objects.equals(title, that.title) &&
                Objects.equals(description, that.description) &&
                Objects.equals(date, that.date) &&
                Objects.equals(url, that.url) &&
                Objects.equals(linkToSMPage, that.linkToSMPage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(socialMediaType, title, description, date, url, linkToSMPage);
    }

//    @Override
//    public String toString() {
//        return "SocialMediaItem{" +
//                "socialId='" + socialId + '\'' +
//                ", socialMediaType='" + socialMediaType + '\'' +
//                ", title='" + title + '\'' +
//                ", description='" + description + '\'' +
//                ", date=" + date +
//                ", url='" + url + '\'' +
//                ", linkToSMPage='" + linkToSMPage + '\'' +
//                ", accepted=" + accepted +
//                '}';
//    }

    @Override
    public String toString() {
        return "SMI{" +
                "id='" + socialId + '\'' +
                ", title='" + title + '\'' +
                ", accepted=" + accepted +
                '}';
    }

}
