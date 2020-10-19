package com.nkoad.wallbler.core;

import java.util.Map;
import java.util.Objects;

public class WallblerItem {
    private int socialId;
    private String feedName;
    private String socialMediaType;
    private String title;
    private String description;
    private long date;
    private String url;             // link to the account
    private String linkToSMPage;    // link to the post
    private Boolean accepted;

    public WallblerItem(Map<String, Object> feedProperties) {
        this.feedName = (String) feedProperties.get("config.name");
        this.socialMediaType = ((String) feedProperties.get("service.pid")).split("\\.")[5];
        this.accepted = (boolean) feedProperties.get("config.acceptedByDefault");
    }

    public int getSocialId() {
        return socialId;
    }

    public String getFeedName() {
        return feedName;
    }

    public String getSocialMediaType() {
        return socialMediaType;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLinkToSMPage() {
        return linkToSMPage;
    }

    public void setLinkToSMPage(String linkToSMPage) {
        this.linkToSMPage = linkToSMPage;
    }

    public Boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(Boolean accepted) {
        this.accepted = accepted;
    }

    public void generateSocialId() {
        this.socialId = this.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WallblerItem that = (WallblerItem) o;
        return Objects.equals(title, that.title) &&
                Objects.equals(description, that.description) &&
                Objects.equals(date, that.date) &&
                Objects.equals(url, that.url) &&
                Objects.equals(linkToSMPage, that.linkToSMPage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description, date, url, linkToSMPage);
    }

    @Override
    public String toString() {
        return "SocialMediaItem{" +
                "socialId='" + socialId + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", date=" + date +
                ", url='" + url + '\'' +
                ", linkToSMPage='" + linkToSMPage + '\'' +
                ", accepted=" + accepted +
                '}';
    }

}
