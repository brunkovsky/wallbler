package com.nkoad.wallbler.core.implementation.instagram;

import com.nkoad.wallbler.core.WallblerItem;

import java.util.Map;

public class InstagramWallblerItem extends WallblerItem {
    private String thumbnailUrl;
    private String mediaType;

    public InstagramWallblerItem(Map<String, Object> feedProperties) {
        super(feedProperties);
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

}
