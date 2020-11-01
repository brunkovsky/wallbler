package com.nkoad.wallbler.core.implementation.facebook;

import com.nkoad.wallbler.core.WallblerItem;

import java.util.Map;

public class FacebookWallblerItem extends WallblerItem {
    private String typeOfFeed;
    private String thumbnailUrl;  // link to the original image
    private Integer likedCount;
    private Integer commentsCount;
    private Integer sharedCount;

    public FacebookWallblerItem(Map<String, Object> feedProperties) {
        super(feedProperties);
        this.typeOfFeed = (String) feedProperties.get("config.typeOfFeed");
    }

    public String getTypeOfFeed() {
        return typeOfFeed;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public Integer getLikedCount() {
        return likedCount;
    }

    public void setLikedCount(Integer likedCount) {
        this.likedCount = likedCount;
    }

    public Integer getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(Integer commentsCount) {
        this.commentsCount = commentsCount;
    }

    public Integer getSharedCount() {
        return sharedCount;
    }

    public void setSharedCount(Integer sharedCount) {
        this.sharedCount = sharedCount;
    }

}
