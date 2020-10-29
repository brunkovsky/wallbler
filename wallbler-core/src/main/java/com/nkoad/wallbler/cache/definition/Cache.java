package com.nkoad.wallbler.cache.definition;

import com.nkoad.wallbler.core.WallblerItem;
import org.json.JSONArray;

import java.util.List;
import java.util.Set;

public interface Cache {
    void add(Set<WallblerItem> data);
    JSONArray getAllData(String socials, Integer limit);
    JSONArray getAcceptedData(String socials, Integer limit);
    JSONArray getNonAcceptedData(String socials, Integer limit);
    void setAccept(List<WallblerItem> wallblerItems);
    void deletePostsByFeedName(String socialMediaType, String feedName);
}
