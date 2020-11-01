package com.nkoad.wallbler.core.implementation.twitter;

import com.nkoad.wallbler.core.WallblerItem;

import java.util.Map;

public class TwitterWallblerItem extends WallblerItem {
    private String thumbnailUrl;  // link to the original image
    private Integer likedCount;
    private Integer sharedCount;

    public TwitterWallblerItem(Map<String, Object> feedProperties) {
        super(feedProperties);
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

    public Integer getSharedCount() {
        return sharedCount;
    }

    public void setSharedCount(Integer sharedCount) {
        this.sharedCount = sharedCount;
    }

}
