package com.nkoad.wallbler.core.implementation.facebook;

import java.util.Map;

public class FacebookPostsWallblerItem extends FacebookPhotosWallblerItem {
    private Integer sharedCount;

    public FacebookPostsWallblerItem(Map<String, Object> feedProperties) {
        super(feedProperties);
    }

    public Integer getSharedCount() {
        return sharedCount;
    }

    public void setSharedCount(Integer sharedCount) {
        this.sharedCount = sharedCount;
    }

}
