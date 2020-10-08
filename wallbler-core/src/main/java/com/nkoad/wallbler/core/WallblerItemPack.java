package com.nkoad.wallbler.core;

import java.util.Date;
import java.util.Objects;
import java.util.Set;

public class WallblerItemPack {
    private Date lastRefreshDate;
    private Set<WallblerItem> data;

    public WallblerItemPack(Set<WallblerItem> data) {
        this.lastRefreshDate = new Date();
        this.data = data;
    }

    public WallblerItemPack(Set<WallblerItem> data, Date date) {
        this.lastRefreshDate = date;
        this.data = data;
    }

    public void addData(WallblerItem wallblerItem) {
        data.add(wallblerItem);
    }

    public Date getLastRefreshDate() {
        return lastRefreshDate;
    }

    public Set<WallblerItem> getData() {
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
