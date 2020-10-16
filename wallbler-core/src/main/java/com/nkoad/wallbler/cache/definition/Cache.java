package com.nkoad.wallbler.cache.definition;

import com.nkoad.wallbler.core.WallblerItem;
import com.nkoad.wallbler.core.WallblerItemPack;
import org.json.JSONArray;

import java.util.List;

public interface Cache {
    void add(String feedPid, WallblerItemPack data);
    JSONArray getData(String socials, Boolean accepted);
    void setAccept(List<WallblerItem> wallblerItems);
    void removeFromCache(String feedPid);
}
