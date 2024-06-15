package com.clankalliance.backbeta.utils;

import com.clankalliance.backbeta.entity.Word;
import com.clankalliance.backbeta.configuration.BloomFilterProperties;
import com.clankalliance.backbeta.repository.WordRepository;
import com.google.common.hash.Funnel;
import com.google.common.hash.Funnels;
import com.google.common.hash.Hashing;
import com.google.common.primitives.Longs;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class BloomFilter {

    private Long numBits;
    private Integer numHashFunctions;
    private Funnel<CharSequence> funnel = Funnels.stringFunnel(Charset.forName("UTF-8"));

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private WordRepository wordRepository;

    @Resource
    private BloomFilterProperties prop ;


    //布隆过滤器只初始化一次
    @PostConstruct
    public void initBloomFilter() {
            // 检查 Redis 中布隆过滤器是否已存在
            boolean bloomFilterExists = RedisUtils.hasKey("myBloomFilter",stringRedisTemplate);

            if (!bloomFilterExists) {
                this.numBits = optimalNumOfBits(prop.getExpectedInsertions(), prop.getFpp());
                this.numHashFunctions = optimalNumOfHashFunctions(prop.getExpectedInsertions(), numBits);

                // 假设从数据库中获取所有单词数据并存入布隆过滤器
                List<String> allDataFromMySQL = getAllDataFromMySQL();
                System.out.println("开始初始化数据");
                for (String data : allDataFromMySQL) {
                    putToBloomFilter(data);
                }
                System.out.println("初始化结束");
            } else {
                System.out.println("布隆过滤器已存在，无需初始化");
            }
    }

    // 将数据存入布隆过滤器
    public void putToBloomFilter(String data) {
        byte[] bytes = Hashing.murmur3_128().hashObject(data, funnel).asBytes();
        long hash1 = lowerEight(bytes);
        long hash2 = upperEight(bytes);

        long combinedHash = hash1;
        for (int i = 0; i < numHashFunctions; i++) {
            long bit = (combinedHash & Long.MAX_VALUE) % numBits;
            stringRedisTemplate.opsForValue().setBit("myBloomFilter", bit, true);
            combinedHash += hash2;
        }
    }

    // 判断数据是否在 MySQL 中的函数
    public boolean isInMySQL(String data) {
        // 进一步查询 MySQL 确认数据是否存在
        return existsInMySQL(data);
    }

    // 假设实现从 MySQL 中查询数据是否存在的方法
    public boolean existsInMySQL(String data) {
        return wordRepository.existsByWord(data);
    }

    public long lowerEight(byte[] bytes) {
        return Longs.fromBytes(bytes[7], bytes[6], bytes[5], bytes[4], bytes[3], bytes[2], bytes[1], bytes[0]);
    }

    public long upperEight(byte[] bytes) {
        return Longs.fromBytes(bytes[15], bytes[14], bytes[13], bytes[12], bytes[11], bytes[10], bytes[9], bytes[8]);
    }

    // 假设从 MySQL 获取所有数据的方法
    public List<String> getAllDataFromMySQL() {
        // 实现从 MySQL 中获取所有单词的方法
        List<Word> wordList = wordRepository.findAll();

        // 使用流式操作获取所有单词列表
        List<String> words = wordList.stream()
                .map(Word::getWord) // 获取每个Word对象的word字段值
                .collect(Collectors.toList());

        return words;
    }

    public long optimalNumOfBits(long expectedInsertions, double fpp) {
        if (fpp == 0) {
            fpp = Double.MIN_VALUE;
        }
        return (long) (-expectedInsertions * Math.log(fpp) / (Math.log(2) * Math.log(2)));
    }

    public static int optimalNumOfHashFunctions(long expectedInsertions, long numBits) {
        return Math.max(1, (int) Math.round((double) numBits / expectedInsertions * Math.log(2)));
    }

}
