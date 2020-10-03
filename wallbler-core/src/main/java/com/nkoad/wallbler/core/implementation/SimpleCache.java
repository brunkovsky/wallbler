package com.nkoad.wallbler.core.implementation;

import com.nkoad.wallbler.core.WallblerItemPack;
import org.osgi.service.component.annotations.Component;

import java.util.HashMap;
import java.util.Map;

@Component(name = "SimpleCache", service = SimpleCache.class)
public class SimpleCache {

    private Map<String, WallblerItemPack> cache = new HashMap<>(); //key = feed pid

    public void add(String feedPid, WallblerItemPack postData) {
        cache.put(feedPid, postData);
//        System.out.println("cash now is: " + cache);
    }

    public void remove(String feedPid) {
        cache.remove(feedPid);
//        System.out.println("cash now is: " + cache);
    }
}
