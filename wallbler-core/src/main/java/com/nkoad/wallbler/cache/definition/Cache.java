package com.nkoad.wallbler.cache.definition;

import com.nkoad.wallbler.core.WallblerItem;
import com.nkoad.wallbler.core.WallblerItemPack;

import java.util.List;

public interface Cache {
    void add(String feedPid, WallblerItemPack data);
    List<WallblerItem> getData(String socials, Boolean accepted);
    WallblerItemPack get(String feedPid);
    void setAccept(List<WallblerItem> wallblerItems);
    void removeFromCache(String feedPid);
}
