package com.undraw.handler;

import java.util.concurrent.ConcurrentHashMap;

public class ContextHolder {
    private static final ConcurrentHashMap<String, Object> tables = new ConcurrentHashMap<>();
}
