package com.nkoad.wallbler.core;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class WallblerItemPack {
    private Date lastRefreshDate;
    private List<WallblerItem> data;

    public WallblerItemPack(List<WallblerItem> data) {
        this.lastRefreshDate = new Date();
        this.data = data;
    }

    public void addData(WallblerItem wallblerItem) {
        data.add(wallblerItem);
    }

    public Date getLastRefreshDate() {
        return lastRefreshDate;
    }

    public List<WallblerItem> getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WallblerItemPack that = (WallblerItemPack) o;
        return Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }
}
