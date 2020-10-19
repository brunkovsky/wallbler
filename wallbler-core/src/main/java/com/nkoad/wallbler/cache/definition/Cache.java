package com.nkoad.wallbler.cache.definition;

import com.nkoad.wallbler.core.WallblerItem;
import com.nkoad.wallbler.core.WallblerItems;
import org.json.JSONArray;

import java.util.List;

public interface Cache {
    void add(WallblerItems data);
    JSONArray getData(String socials, Integer limit);
    void setAccept(List<WallblerItem> wallblerItems);
    void removeFromCache(String socialMediaType, String feedName);
}
