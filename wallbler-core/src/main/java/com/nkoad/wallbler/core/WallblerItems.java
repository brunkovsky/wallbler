package com.nkoad.wallbler.core;

import java.util.Date;
import java.util.List;

public class WallblerItems {
    private long lastRefreshDate;
    private List<WallblerItem> data;

    public WallblerItems(List<WallblerItem> data) {
        this.data = data;
        lastRefreshDate = new Date().getTime();
    }

    public long getLastRefreshDate() {
        return lastRefreshDate;
    }

    public List<WallblerItem> getData() {
        return data;
    }
}
