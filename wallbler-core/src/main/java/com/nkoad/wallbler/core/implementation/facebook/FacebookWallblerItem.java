package com.nkoad.wallbler.core.implementation.facebook;

import com.nkoad.wallbler.core.WallblerItem;

public class FacebookWallblerItem extends WallblerItem {
    private String typeOfFeed;
    private String thumbnailUrl;  // link to the original image
    private Integer likedCount;
    private Integer sharedCount;
    private Integer commentsCount;


    public FacebookWallblerItem() {
        super("facebook");
    }

    public String getTypeOfFeed() {
        return typeOfFeed;
    }

    public void setTypeOfFeed(String typeOfPost) {
        this.typeOfFeed = typeOfPost;
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

    public Integer getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(Integer commentsCount) {
        this.commentsCount = commentsCount;
    }

}
