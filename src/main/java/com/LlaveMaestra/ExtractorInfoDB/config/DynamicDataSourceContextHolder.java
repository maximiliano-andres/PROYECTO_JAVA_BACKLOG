package com.LlaveMaestra.ExtractorInfoDB.config;

import lombok.extern.slf4j.Slf4j;

/**
 * Gestiona el contexto del DataSource dinámico usando ThreadLocal.
 */
@Slf4j
public class DynamicDataSourceContextHolder {

    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();

    public static void setDataSourceKey(String key) {
        log.debug("Setting DataSource key to: {}", key);
        CONTEXT_HOLDER.set(key);
    }

    public static String getDataSourceKey() {
        return CONTEXT_HOLDER.get();
    }

    public static void clearDataSourceKey() {
        CONTEXT_HOLDER.remove();
    }
}
