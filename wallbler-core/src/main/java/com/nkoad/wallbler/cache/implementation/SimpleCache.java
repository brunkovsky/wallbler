package com.nkoad.wallbler.cache.implementation;

import com.nkoad.wallbler.cache.definition.Cache;
import com.nkoad.wallbler.core.WallblerItem;
import com.nkoad.wallbler.core.WallblerItemPack;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(name = "SimpleCache", service = Cache.class)
public class SimpleCache implements Cache {
    private Map<String, WallblerItemPack> cache = new HashMap<>();

    @Override
    public void add(String feedPid, WallblerItemPack data) {
        WallblerItemPack wallblerItemPack = cache.get(feedPid);
        if (wallblerItemPack == null) {
            cache.put(feedPid, data);
        } else {
            Set<WallblerItem> result = new HashSet<>();
            for (WallblerItem toInputItem : data.getData()) {
                for (WallblerItem wallblerItemInCacheItem : wallblerItemPack.getData()) {
                    if (toInputItem.getSocialId() == wallblerItemInCacheItem.getSocialId()) {
                        toInputItem.setAccepted(wallblerItemInCacheItem.isAccepted());
                    }
                    result.add(toInputItem);
                }
            }
            cache.put(feedPid, new WallblerItemPack(result));
        }
    }

    @Override
    public JSONArray getData(String socials, Boolean accepted) {
//        Stream<WallblerItem> result = getWallblerItems()
//                .filter(a -> socials == null || socials.contains(a.getSocialMediaType()))
//                .filter(a -> accepted == null || a.isAccepted() == accepted);
//
//        return result.collect(Collectors.toList());
        return null;
    }

    @Override
    public void setAccept(List<WallblerItem> wallblerItems) {
        System.out.println(wallblerItems);
        for (WallblerItem wallblerItem : wallblerItems) {
            for (WallblerItem item : getWallblerItems().collect(Collectors.toList())) {
                if (wallblerItem.getSocialId() == item.getSocialId()) {
                    item.setAccepted(wallblerItem.isAccepted());
                }
            }
        }
    }

    @Override
    public void removeFromCache(String feedPid) {
        cache.remove(feedPid);
    }

    private Stream<WallblerItem> getWallblerItems() {
        return cache
                .values()
                .stream()
                .map(WallblerItemPack::getData)
                .flatMap(Collection::stream);
    }

}
