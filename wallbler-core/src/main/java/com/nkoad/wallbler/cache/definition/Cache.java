package com.nkoad.wallbler.cache.definition;

import com.nkoad.wallbler.core.WallblerItemPack;

public interface Cache {
    void add(String feedPid, WallblerItemPack data);
    WallblerItemPack get(String feedPid);
}
