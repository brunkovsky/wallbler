package com.nkoad.wallbler.core.implementation.facebook;

import com.nkoad.wallbler.core.WallblerItem;

public class FacebookWallblerItem extends WallblerItem {
    private String typeOfFeed;

    public FacebookWallblerItem() {
        super("facebook");
    }

    public String getTypeOfFeed() {
        return typeOfFeed;
    }

    public void setTypeOfFeed(String typeOfPost) {
        this.typeOfFeed = typeOfPost;
    }
}
