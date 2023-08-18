package com.adeptj.runtime.kernel.util;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import static com.adeptj.runtime.kernel.Constants.H2_MAP_ADMIN_CREDENTIALS;
import static com.adeptj.runtime.kernel.Constants.MV_CREDENTIALS_STORE;

public final class MVStoreUtil {

    private MVStoreUtil() {
    }

    public static String getValue(String storeName, String mapName, String key) {
        try (MVStore store = MVStore.open(storeName)) {
            MVMap<String, String> mvMap = store.openMap(mapName);
            return mvMap.get(key);
        }
    }

    public static String getPassword(String username) {
        return getValue(MV_CREDENTIALS_STORE, H2_MAP_ADMIN_CREDENTIALS, username);
    }
}
