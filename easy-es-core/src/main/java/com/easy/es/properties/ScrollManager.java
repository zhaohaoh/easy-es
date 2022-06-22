//package com.framework.es.properties;
//
//import com.framework.es.core.ScrollHandler;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.ArrayBlockingQueue;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.LinkedBlockingDeque;
//import java.util.concurrent.LinkedBlockingQueue;
//
//public class ScrollManager {
//    private static final Map<String, LinkedBlockingQueue<ScrollHandler<?>>> SCROLL_HANDLER_MAP = new ConcurrentHashMap<>();
//
//    public static void putScroll(String key, ScrollHandler<?> scrollHandler) {
//        LinkedBlockingQueue<ScrollHandler<?>> scrollHandlerQueue = SCROLL_HANDLER_MAP.computeIfAbsent(key, queue -> new LinkedBlockingQueue<>(10));
//        scrollHandlerQueue.add(scrollHandler);
//    }
//
//    public static <T> ScrollHandler<T> getScroll(String key, Class<T> tClass) {
//        LinkedBlockingQueue<ScrollHandler<?>> scrollHandlerLinkedBlockingQueue = SCROLL_HANDLER_MAP.get(key);
//        return (ScrollHandler<T>) scrollHandlerLinkedBlockingQueue.poll();
//    }
//}
