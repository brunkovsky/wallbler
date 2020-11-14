package com.nkoad.wallbler.core;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class WallblerItem implements Serializable {
    private String lastRefreshDate;
    private int socialId;
    private String feedName;
    private String socialMediaType;
    private String title;
    private String description;
    private String date;
    private String url;             // link to the account
    private String linkToSMPage;    // link to the post
    private Boolean accepted;

    public WallblerItem() {
    }

    public WallblerItem(Map<String, Object> feedProperties) {
        this.feedName = (String) feedProperties.get("config.name");
        this.socialMediaType = ((String) feedProperties.get("service.pid")).split("\\.")[5];
        this.accepted = (boolean) feedProperties.get("config.acceptedByDefault");
    }

    public String getLastRefreshDate() {
        return lastRefreshDate;
    }

    public void setLastRefreshDate(Date lastRefreshDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        this.lastRefreshDate = sdf.format(lastRefreshDate);
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

    public String getDate() {
        return date;
    }

    public void setDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        this.date = sdf.format(date);
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
        generateSocialId();
    }

    public Boolean isAccepted() {
        return accepted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WallblerItem that = (WallblerItem) o;
        return Objects.equals(linkToSMPage, that.linkToSMPage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(linkToSMPage);
    }

    private void generateSocialId() {
        this.socialId = this.hashCode();
    }

}
