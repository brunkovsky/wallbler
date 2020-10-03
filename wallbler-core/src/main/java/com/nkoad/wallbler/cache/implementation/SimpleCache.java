package com.nkoad.wallbler.cache.implementation;

import com.nkoad.wallbler.cache.definition.Cache;
import com.nkoad.wallbler.core.WallblerItemPack;
import org.osgi.service.component.annotations.Component;

import java.util.HashMap;
import java.util.Map;

@Component(name = "SimpleCache", service = Cache.class)
public class SimpleCache implements Cache {
    private Map<String, WallblerItemPack> cache = new HashMap<>();

    @Override
    public void add(String feedPid, WallblerItemPack data) {
        cache.put(feedPid, data);
    }

    @Override
    public WallblerItemPack get(String feedPid) {
        return cache.get(feedPid);
    }
}
