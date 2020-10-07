package com.nkoad.wallbler.cache.implementation;

import com.nkoad.wallbler.cache.definition.Cache;
import com.nkoad.wallbler.core.WallblerItem;
import com.nkoad.wallbler.core.WallblerItemPack;
import org.osgi.service.component.annotations.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(name = "SimpleCache", service = Cache.class)
public class SimpleCache implements Cache {
    private Map<String, WallblerItemPack> cache = new HashMap<>();

    /*@Override
    public void add(String feedPid, WallblerItemPack data) {
        WallblerItemPack wallblerItemPack = cache.get(feedPid);
        if (wallblerItemPack == null) {
            cache.put(feedPid, data);
        } else {
            List<WallblerItem> cacheCurrent = new ArrayList<>(wallblerItemPack.getData());
            List<WallblerItem> result = new ArrayList<>();
            for (WallblerItem datum : data.getData()) {
                for (WallblerItem wallblerItem : cacheCurrent) {
                    if (datum.getSocialId() != wallblerItem.getSocialId()) {
                        result.add(wallblerItem);
                    }
                }
            }
            cache.remove(feedPid);
            cache.put(feedPid, new WallblerItemPack(result, data.getLastRefreshDate()));
        }
    }*/

    @Override
    public void add(String feedPid, WallblerItemPack data) {
        WallblerItemPack wallblerItemPack = cache.get(feedPid);
        if (wallblerItemPack == null) {
            cache.put(feedPid, data);
        } else {
            List<WallblerItem> cacheCurrent = new ArrayList<>(wallblerItemPack.getData());
            List<WallblerItem> result = new ArrayList<>();
            for (WallblerItem datum : data.getData()) {
                for (WallblerItem wallblerItem : cacheCurrent) {
                    if (datum.getSocialId() != wallblerItem.getSocialId()) {
                        result.add(wallblerItem);
                    }
                }
            }
            cache.remove(feedPid);
            cache.put(feedPid, new WallblerItemPack(result, data.getLastRefreshDate()));
        }
    }

    @Override
    public List<WallblerItem> getData(String socials, Boolean accepted) {
        Stream<WallblerItem> result = getWallblerItems()
                .filter(a -> socials == null || socials.contains(a.getSocialMediaType()))
                .filter(a -> accepted == null || a.isAccepted() == accepted);

        return result.collect(Collectors.toList());
    }

    @Override
    public WallblerItemPack get(String feedPid) {
        return cache.get(feedPid);
    }

    @Override
    public void setAccept(Integer socialId, boolean accept) {
        getWallblerItems()
                .filter(a -> a.getSocialId() == socialId)
                .forEach(a -> {
                    a.setAccepted(accept);
                });
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
