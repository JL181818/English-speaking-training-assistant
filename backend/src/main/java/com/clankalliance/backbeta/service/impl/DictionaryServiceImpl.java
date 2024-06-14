package com.clankalliance.backbeta.service.impl;

import com.clankalliance.backbeta.entity.Word;
import com.clankalliance.backbeta.repository.WordRepository;
import com.clankalliance.backbeta.response.CommonResponse;
import com.clankalliance.backbeta.service.DictionaryService;
import com.clankalliance.backbeta.utils.RedisUtils;
import com.clankalliance.backbeta.utils.TokenUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class DictionaryServiceImpl implements DictionaryService {

    @Resource
    private TokenUtil tokenUtil;

    @Resource
    private WordRepository wordRepository;

    /**
     * key: w#{word}
     * value: Word(Entity)
     */
    @Resource
    private StringRedisTemplate RedisTemplateWord;

    private static Long WORD_EXPIRE_TIME;

    private final TimeUnit EXPIRE_TIME_TYPE = TimeUnit.MILLISECONDS;

    @Value("${wordUpdate.expireTime}")
    public void setWordExpireTime(Long time){WORD_EXPIRE_TIME = time;}

    private Word handleGetWord(String word){
        String key = "w#" + word;
        if(RedisUtils.hasKey(key, RedisTemplateWord)){
            RedisTemplateWord.expire(key, (long)(WORD_EXPIRE_TIME * (1 + Math.random())), EXPIRE_TIME_TYPE);
            return RedisUtils.getObject(key, RedisTemplateWord, Word.class);
        }else{
            Optional<Word> wop = wordRepository.findById(word);
            if(wop.isEmpty())
                return null;
            Word wordEntity = wop.get();
            RedisUtils.add(key, wordEntity, RedisTemplateWord);
            RedisTemplateWord.expire(key, (long)(WORD_EXPIRE_TIME * (1 + Math.random())), EXPIRE_TIME_TYPE);
            return wordEntity;
        }
    }

    public CommonResponse saveWord(String word, String trans, String example, String exampleTrans){
        Word wordEntity = new Word(word, trans, example, exampleTrans);
        try{
            wordRepository.save(wordEntity);
        }catch (Exception e){
            return CommonResponse.errorResponse(e.toString());
        }
        return CommonResponse.successResponse("success");
    }

    public CommonResponse getWord(String token, String word){
        CommonResponse response = tokenUtil.tokenCheck(token);
        if(!response.getLoginValid())
            return CommonResponse.errorResponse("登录失效", response);
        Word wordEntity = handleGetWord(word);
        response.setContent(wordEntity);
        if(wordEntity != null){
            return CommonResponse.successResponse("查找成功", response);
        }else{
            return CommonResponse.errorResponse("找不到单词", response);
        }
    }

}
