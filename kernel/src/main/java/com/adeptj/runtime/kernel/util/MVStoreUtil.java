package com.adeptj.runtime.kernel.util;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

public final class MVStoreUtil {

    private MVStoreUtil() {
    }

    public static String getValue(String storeName, String mapName, String key) {
        try (MVStore store = MVStore.open(storeName)) {
            MVMap<String, String> mvMap = store.openMap(mapName);
            return mvMap.get(key);
        }
    }
}
