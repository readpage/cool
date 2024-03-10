package com.undraw.util.redis;

import cn.undraw.util.StrUtils;
import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.api.async.RedisAsyncCommands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis工具类
 */
@Component
public class RedisUtil<T> {
    @Autowired
    private RedisTemplate<String, T> redisTemplate;

    /**
     * 设置过期时间，以秒为单位
     * @param key
     * @param time
     * @return 返回true表示设置成功
     */
    public Boolean expireOfSeconds(String key, long time){
        if (time <= 0) {
            return false;
        }
        return redisTemplate.expire(key, time, TimeUnit.SECONDS);
    }

    /**
     * 设置过期时间，以小时为单位
     * @param key
     * @param time
     * @return 返回true表示设置成功
     */
    public Boolean expireOfHours(String key, long time){
        if (time <= 0) {
            return false;
        }
        return redisTemplate.expire(key,time, TimeUnit.HOURS);
    }

    /**
     * 获取过期时间
     * @return 时间(秒) 返回0代表为永久有效
     */
    public long expire(String key){
        return redisTemplate.getExpire(key,TimeUnit.SECONDS);
    }

    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
    public Boolean hasKey(String key){
        return redisTemplate.hasKey(key);
    }

    /**
     * 删除缓存
     * @param key 可以传一个或多个值
     */
    public boolean del(String... key) {
        Long n = 0L;
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                n = redisTemplate.delete(key[0]) == true ? 1L : 0L;
            } else {
                n = redisTemplate.delete((Collection<String>) CollectionUtils.arrayToList(key));
            }
        }
        return n > 0;
    }

    public boolean del(Collection<String> keys) {
        return redisTemplate.delete(keys) > 0;
    }

    /**
     * 模糊匹配删除key值
     * @param pattern
     */
    public boolean delByMatch(String pattern) {
        List<String> keys = scan(pattern);
        if (StrUtils.isNotEmpty(pattern)) {
            return del(keys);
        }
        return false;
    }

    /**
     * 获取 指定格式的所有key
     * 迭代执行 SCAN 0 MATCH {pattern} COUNT 10000
     *
     * @param pattern       匹配规则
     * @return 指定格式的所有key
     */
    public List<String> scan(String pattern) {
        //SCAN 0 MATCH {pattern} COUNT 10000
        return  redisTemplate.execute(connection -> {
            //scan 迭代遍历键，返回的结果可能会有重复，需要客户端去重复
            Set<String> redisKeys = new HashSet<>();
            //lettuce 原生api
            RedisAsyncCommands conn = (RedisAsyncCommands) connection.getNativeConnection();
            //游标
            ScanCursor curs = ScanCursor.INITIAL;
            try {
                //采用 SCAN 命令，迭代遍历所有key
                while (!curs.isFinished()) {
                    long count = 10000L;
                    ScanArgs args = ScanArgs.Builder.matches(pattern).limit(count);
                    RedisFuture<KeyScanCursor<byte[]>> future = conn.scan(curs, args);
                    KeyScanCursor<byte[]> keyCurs = future.get();
                    List<byte[]> ks = keyCurs.getKeys();
                    Set<String> set = ks.stream().map(bytes -> new String(bytes, StandardCharsets.UTF_8)).collect(Collectors.toSet());
                    redisKeys.addAll(set);
                    curs = keyCurs;
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            return new ArrayList<>(redisKeys);
        }, true);
    }



//    =============================================String=======================================================
    /**
     * 一般情况下缓存获取
     * @param key 键
     * @return 值
     */
    public T get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }


    /**
     * 一般情况下存入String
     * @param key
     * @param value
     * @return 返回true保存成功
     */
    public void set(String key, T value){
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 可加入秒为单位的时间
     * @param key
     * @param value
     * @param time 以秒为单位
     * @return
     */
    public void set(String key, T value, long time){
        if (time>0){
            redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
        } else {
            set(key, value);
        }
    }

    /**
     * 递增或递减
     *
     * @param key   键
     * @param delta 要增加几(大于0)/要减少几(小于0)
     * @return
     */
    public long incr(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递增或递减
     *
     * @param key   键
     * @param delta 要增加几(大于0)/要减少几(小于0)
     * @return
     */
    public long decr(String key, long delta) {
        return redisTemplate.opsForValue().decrement(key, delta);
    }
//========================================hash================================================

    /**
     * 获得hash的值
     * @param key 键 不能为空
     * @param item 项 不能为空
     * @return 获得的值
     */
    public Object hGet(String key, String item){
        return redisTemplate.opsForHash().get(key, item);
    }

    /**
     * 获取hash键中所有键值对
     * @param key 键不能为空
     * @return map键值集合
     */
    public Map<Object,Object> hMget(String key){
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 获取hash键中key的所有值
     * @param key
     * @return
     */
    public List<Object> hVals(String key) {
        return redisTemplate.opsForHash().values(key);
    }

    /**
     * HashSet
     * @param key 键
     * @param map 对应多个键值
     * @return true 成功 false 失败
     */
    public void hMset(String key, Map<String, T> map){
        redisTemplate.opsForHash().putAll(key, map);
    }

    /**
     * HashSet 并设置时间
     *
     * @param key  键
     * @param map  对应多个键值
     * @param time 时间(秒)
     * @return true成功 false失败
     */
    public void hMset(String key, Map<String, T> map, long time){
        redisTemplate.opsForHash().putAll(key,map);
        if (time > 0) {
            expireOfSeconds(key, time);
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @return true 成功 false失败
     */
    public void hSet(String key, String item, Object value) {
        redisTemplate.opsForHash().put(key, item, value);
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param item  项
     * @param value 值
     * @param time  时间(秒) 注意:如果已存在的hash表有时间,这里将会替换原有的时间
     * @return true 成功 false失败
     */
    public void hSet(String key, String item, Object value, long time) {
        redisTemplate.opsForHash().put(key, item, value);
        if (time > 0) {
            expireOfSeconds(key, time);
        }
    }

    /**
     * 迭代哈希表中的键值对
     * @param key
     * @param scanOptions
     * @return
     */
    public Cursor<Map.Entry<Object, Object>> hScan(String key, ScanOptions scanOptions) {
        return redisTemplate.opsForHash().scan(key, scanOptions);
    }

    /**
     * 迭代哈希表中的键值对
     * @param key
     * @return
     */
    public Cursor<Map.Entry<Object, Object>> hScan(String key) {
        return hScan(key, ScanOptions.NONE);
    }

    /**
     * 删除hash表中的值
     *
     * @param key  键 不能为null
     * @param item 项 可以使多个 不能为null
     */
    public void hDel(String key, Object... item) {
        redisTemplate.opsForHash().delete(key, item);
    }

    /**
     * 判断hash表中是否有该项的值
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return true 存在 false不存在
     */
    public boolean hHasKey(String key, String item) {
        return redisTemplate.opsForHash().hasKey(key, item);
    }

    /**
     * hash递增递减 如果不存在,就会创建一个 并把新增后的值返回
     *
     * @param key  键
     * @param item 项
     * @param by   要增加几(大于0)
     * @return
     */
    public Long hIncr(String key, String item, long by) {
        return redisTemplate.opsForHash().increment(key, item, by);
    }

    /**
     * hash递增递减 如果不存在,就会创建一个 并把新增后的值返回
     *
     * @param key  键
     * @param item 项
     * @param by   要增加几(大于0)
     * @return
     */
    public double hIncrByDouble(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, by);
    }

//==================================set=========================================
    /**
     * 根据key获取Set中的所有值
     * @param key 键
     * @return
     */
    public Set<T> sGet(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 根据value从一个set中查询,是否存在
     *
     * @param key   键
     * @param value 值
     * @return true 存在 false不存在
     */
    public boolean sHasKey(String key, T value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * 将数据放入set缓存
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public long sSet(String key, T... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    /**
     * 将set数据放入缓存
     *
     * @param key    键
     * @param values 值
     * @param time   时间(秒)
     * @return 成功存储个数
     */
    public long sSet(String key, T values,  long time) {
        Long count = redisTemplate.opsForSet().add(key, values);
        if (time > 0)
            expireOfSeconds(key, time);
        return count;
    }

    /**
     * 获取set缓存的长度
     *
     * @param key 键
     * @return
     */
    public long sSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    /**
     * 迭代Set中的键值对
     * @param key
     * @param scanOptions
     * @return
     */
    public Cursor<T> sScan(String key, ScanOptions scanOptions) {
        return redisTemplate.opsForSet().scan(key, scanOptions);
    }

    /**
     * 迭代Set中的键值对
     * @param key
     * @return
     */
    public Cursor<T> sScan(String key) {
        return sScan(key, ScanOptions.NONE);
    }

    /**
     * 移除值为value的
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 移除的个数
     */
    public long sRemove(String key, T... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    // ===============================List(列表)=================================

    /**
     * 获取list缓存的内容
     *
     * @param key   键
     * @param start 开始
     * @param end   结束 0 到 -1代表所有值
     * @return
     */
    public List <T> lGet(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * 获取list缓存所有的内容
     * @param key
     * @return
     */
    public List <T> lGetAll(String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    /**
     * 获取list缓存的长度
     *
     * @param key 键
     * @return
     */
    public long lSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    /**
     * 通过索引 获取list中的值
     *
     * @param key   键
     * @param index 索引 index>=0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
     * @return
     */
    public T lGetIndex(String key, long index) {
        return redisTemplate.opsForList().index(key, index);
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return
     */
    public void rSet(String key, T value) {
        redisTemplate.opsForList().rightPush(key, value);
    }

    public void lSet(String key, T value) {
        redisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return
     */
    public void rSet(String key, T value, long time) {
        redisTemplate.opsForList().rightPush(key, value);
        if (time > 0) {
            expireOfSeconds(key, time);
        }
    }
    public void lSet(String key, T value, long time) {
        redisTemplate.opsForList().leftPush(key, value);
        if (time > 0) {
            expireOfSeconds(key, time);
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return
     */
    public void lRSet(String key, List<T> value) {
        redisTemplate.opsForList().rightPushAll(key, value);
    }
    public void lLSet(String key, List<T> value) {
        redisTemplate.opsForList().leftPushAll(key, value);
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return
     */
    public void lRSet(String key, List<T> value, long time) {
        redisTemplate.opsForList().rightPushAll(key, value);
        if (time > 0)
            expireOfSeconds(key, time);
    }
    public void lLSet(String key, List<T> value, long time) {
        redisTemplate.opsForList().leftPushAll(key, value);
        if (time > 0)
            expireOfSeconds(key, time);
    }

    /**
     * 根据索引修改list中的某条数据
     *
     * @param key   键
     * @param index 索引
     * @param value 值
     * @return
     */
    public void lUpdateIndex(String key, long index, T value) {
        redisTemplate.opsForList().set(key, index, value);
    }

    /**
     * 移除N个值为value
     *
     * @param key   键
     * @param count 移除多少个
     * @param value 值
     * @return 移除的个数
     */
    public long lRemove(String key, long count, T value) {
        return redisTemplate.opsForList().remove(key, count, value);
    }
//    =========================================zset====================================

    /**
     * 获取个数
     * @param key 键
     * @return 返回值
     */
    public long zSize(String key){
        return redisTemplate.opsForZSet().size(key);
    }

    /**
     * 将数据放入zset缓存
     *
     * @param key    键
     * @param value 值
     * @return 成功个数
     */
    public boolean zAdd(String key, T value, int score) {
//        DefaultTypedTuple<String> tuple1 = new DefaultTypedTuple<String>("p2", 1.1);
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * 加分减分
     *
     * @param key 键
     * @return
     */
    public Double zIncrScore(String key, T value, int inc) {
        return redisTemplate.opsForZSet().incrementScore(key, value, inc);
    }

    /**
     * 移除值为value的
     *
     * @param key    键
     * @param value 值 可以是多个
     * @return 移除的个数
     */
    public long zSetRemove(String key, T... value) {
        return redisTemplate.opsForZSet().remove(key, value);
    }

    /**
     * 获取指定元素的排名，从小到大排序
     * @param key
     * @param value
     * @return
     */
    public long zRank(String key, T value){
        return redisTemplate.opsForZSet().rank(key, value);
    }

    /**
     * 获取指定元素的排名，从大到小排序
     * @param key
     * @param value
     * @return
     */
    public long zReverseRank(String key, T value){
        return redisTemplate.opsForZSet().reverseRank(key, value);
    }

    /**
     * 返回集合内元素的排名，以及分数（从小到大）
     * @param key
     * @param min max 最小值最大值
     * @return
     */
    public Set<ZSetOperations.TypedTuple<T>> zRangeWithScores(String key, long min, long max){
        return redisTemplate.opsForZSet().rangeWithScores(key, min, max);
    }

    /**
     * 返回集合内元素在指定分数范围内的排名（从小到大）
     * @param key
     * @param min max 最小值最大值
     * @return
     */
    public Set<T> zRangeByScore(String key, int min, int max){
        return redisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    /**
     * 返回集合内元素在指定分数范围内的排名（从小到大）
     * 带偏移量和个数
     * 从index下标开始，个位数为count
     * @param key
     * @param min max 最小值最大值
     * @return
     */
    public Set<T> zRangeByScore(String key, int min, int max, int index, int count){
        return redisTemplate.opsForZSet().rangeByScore(key, min, max, index, count);
    }

    /**
     * 返回集合内指定分数范围的成员个数
     * @param key
     * @param min max 最小值最大值
     * @return
     */
    public long zCount(String key, int min, int max){
        return redisTemplate.opsForZSet().count(key, min, max);
    }

    /**
     * 获得指定元素的分数
     * @param key
     * @param  value 指定元素
     * @return 分数
     */
    public Double zCount(String key, T value){
        return redisTemplate.opsForZSet().score(key, value);
    }

    /**
     * 删除指定索引范围的元素
     * @param key
     * @param  min max 指定范围
     * @return 分数
     */
    public long zRemoveRange(String key, int min, int max){
        return redisTemplate.opsForZSet().removeRange(key, min, max);
    }

    /**
     *删除指定分数范围内的元素
     * @param key
     * @param  min max 指定范围
     * @return 分数
     */
    public long zRemoveRangeByScore(String key, int min, int max){
        return redisTemplate.opsForZSet().removeRangeByScore(key, min, max);
    }

    /**
     *按照排名先后(从小到大)打印指定区间内的元素, -1为打印全部
     * @param key
     * @param  min max 指定范围
     * @return 分数
     */
    public Set<T> zRangeAll(String key, int min, int max){
        return redisTemplate.opsForZSet().range(key, min, max);
    }

}

