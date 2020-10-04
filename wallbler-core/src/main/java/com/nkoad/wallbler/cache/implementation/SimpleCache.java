package com.nkoad.wallbler.cache.implementation;

import com.nkoad.wallbler.cache.definition.Cache;
import com.nkoad.wallbler.core.WallblerItem;
import com.nkoad.wallbler.core.WallblerItemPack;
import org.osgi.service.component.annotations.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component(name = "SimpleCache", service = Cache.class)
public class SimpleCache implements Cache {
    private Map<String, WallblerItemPack> cache = new HashMap<>();

    @Override
    public void add(String feedPid, WallblerItemPack data) {
        cache.put(feedPid, data);
    }

    @Override
    public List<WallblerItem> getAll() {
        List<WallblerItem> collect = cache
                .values()
                .stream()
                .map(WallblerItemPack::getData)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        return collect;
    }

    @Override
    public WallblerItemPack get(String feedPid) {
        return cache.get(feedPid);
    }

    @Override
    public void removeFromCache(String feedPid) {
        cache.remove(feedPid);
    }
}
