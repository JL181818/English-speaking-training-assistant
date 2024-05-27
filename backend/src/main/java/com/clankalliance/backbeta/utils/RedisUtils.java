package com.clankalliance.backbeta.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class RedisUtils {

    /**
     * 数据缓存至redis
     *
     * @param key
     * @param value
     * @return
     */
    public static  <V> void add(String key, V value, StringRedisTemplate targetMap) {
        try {
            if(value != null){
                if(hasKey(key, targetMap)){
                    delete(key, targetMap);
                }
                String temp = JSON.toJSONString(value);
                targetMap.opsForValue().set(key, temp);
            }
        } catch (Exception e) {
            throw new RuntimeException("数据缓存至redis失败");
        }
    }
    /**
     * 从redis中获取缓存数据，转成对象
     *
     * @param key   must not be {@literal null}.
     * @param clazz 对象类型
     * @return
     */
    public static <V> V getObject(String key, StringRedisTemplate targetMap, Class<V> clazz) {
        String value = get(key, targetMap);
        V result = null;
        if (value != null && !value.equals("")) {
            result = JSONObject.parseObject(value, clazz);
        }
        return result;
    }

    /**
     * 从redis中获取缓存数据，转成list
     *
     * @param key   must not be {@literal null}.
     * @param clazz 对象类型
     * @return
     */
    public static <V> List<V> getList(String key, StringRedisTemplate targetMap, Class<V> clazz) {
        String value = get(key, targetMap);
        List<V> result = Collections.emptyList();
        if (value != null && !value.equals("")) {
            result = JSONArray.parseArray(value, clazz);
        }
        return result;
    }
    /**
     * 功能描述：Get the value of {@code key}.
     *
     * @param key must not be {@literal null}.
     * @return java.lang.String
     * @date 2021/9/19
     **/
    public static String get(String key, StringRedisTemplate targetMap) {
        String value;
        try {
            value = targetMap.opsForValue().get(key);
        } catch (Exception e) {
            throw new RuntimeException("从redis缓存中获取缓存数据失败");
        }
        return value;
    }
    /**
     * 删除key
     */
    public static void delete(String key, StringRedisTemplate targetMap) {
        targetMap.delete(key);
    }

    /**
     * 批量删除key
     */
    public static void delete(Collection<String> keys, StringRedisTemplate targetMap) {
        targetMap.delete(keys);
    }
    /**
     * 是否存在key
     */
    public static Boolean hasKey(String key, StringRedisTemplate targetMap) {
        return targetMap.hasKey(key);
    }

    /**
     * 作为set使用 添加字符串
     * @param setName
     * @param targetMap
     * @param value
     */
    public static void setAdd(String setName, RedisTemplate<String,String> targetMap, String value){
        targetMap.opsForSet().add(setName, value);
    }

    /**
     * 返回set大小
     * @param setName
     * @param targetMap
     * @return
     */
    public static Long getSetSize(String setName, RedisTemplate<String,String> targetMap){
        return targetMap.opsForSet().size(setName);
    }

    /**
     * set中随机弹出
     * @param setName
     * @param targetMap
     * @return
     */
    public static String setPop(String setName, RedisTemplate<String,String> targetMap){
        return targetMap.opsForSet().pop(setName);
    }

    /**
     * set中删除指定元素
     * @param setName
     * @param targetMap
     * @param value
     */
    public static void setDel(String setName, RedisTemplate<String,String> targetMap, String value){
        targetMap.opsForSet().remove(setName, value);
    }

    /**
     * set是否存在指定元素
     * @param setName
     * @param targetMap
     * @param value
     * @return
     */
    public static boolean setContains(String setName, RedisTemplate<String,String> targetMap, String value){
        return Objects.equals(targetMap.opsForSet().isMember(setName, value), Boolean.TRUE);
    }

}
