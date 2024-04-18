package com.clankalliance.backbeta.utils.StatusManipulateUtilsWithRedis;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

import static com.clankalliance.backbeta.utils.RedisUtils.*;

/**
 * 基于redis实现用户登陆状态的管理
 * 能够持久化维持登陆状态
 * 更新及自动回收用户状态，并自动生成身份验证用的验证码与token
 * 身份过期时间设置整合进了application.yml 便于后期维护
 * 减少对数据库的查询
 */
@Slf4j
@Component
public class ManipulateUtilRedis {

    private static long STATUS_EXPIRE_TIME;

    private static long PHONE_EXPIRE_TIME;

    private final TimeUnit EXPIRE_TIME_TYPE = TimeUnit.MILLISECONDS;

    /**
     * key: token
     * value: id
     */
    @Resource
    private StringRedisTemplate RedisTemplateTokenId;

    /**
     * key: id
     * value: token
     */
    @Resource
    private StringRedisTemplate RedisTemplateIdToken;

    /**
     * key: phone
     * value: phoneStatus
     */
    @Resource
    private StringRedisTemplate RedisTemplatePhoneStatus;

    @Value("${clankToken.statusExpireTime}")
    public void setStatusExpireTime(long time){
        STATUS_EXPIRE_TIME = time;
    }

    @Value("${clankToken.phoneExpireTime}")
    public void setPhoneExpireTime(long time){
        PHONE_EXPIRE_TIME = time;
    }

    /**
     * 设置登陆状态过期时间
     */
    public Boolean expire(String key, StringRedisTemplate targetMap) {
        return targetMap.expire(key, STATUS_EXPIRE_TIME, EXPIRE_TIME_TYPE);
    }

    /**
     * 设置手机验证码过期时间
     */
    public Boolean expirePhone(String key, StringRedisTemplate targetMap) {
        return targetMap.expire(key, PHONE_EXPIRE_TIME, EXPIRE_TIME_TYPE);
    }

    /**
     * 根据token查询登录状态
     * 若存在节点且没过期，则返回状态,并删除原节点。若不存在或过期，返回空节点
     * @param token 查询目标的token
     * @return
     */
    public String findStatusByToken(String token){
        if(token == null)
            return null;
        String id = null;
        if(hasKey(token, RedisTemplateTokenId)){
            id = getObject(token, RedisTemplateTokenId, String.class);
        }
        return id;
    }


    /**
     * 新增状态，根据用户id自动更新登录状态
     * @param id 用户id
     */
    public String updateStatus(String id){
        if(id == null)
            return null;
        String oldToken;
        long updateTime = System.currentTimeMillis();
        String newToken = DigestUtils.sha1Hex(id + updateTime);
        if(hasKey(id, RedisTemplateIdToken)){
            oldToken = getObject(id, RedisTemplateIdToken, String.class);
            delete(oldToken, RedisTemplateTokenId);
        }
        add(id, newToken, RedisTemplateIdToken);
        add(newToken, id, RedisTemplateTokenId);
        expire(newToken, RedisTemplateTokenId);
        expire(id, RedisTemplateIdToken);
        return newToken;
    }

    /**
     * 新增手机验证码状态
     * @param id
     * @param phone
     * @return
     */
    public String updatePhoneStatus(String id, String phone){
        if(id == null || phone == null)
            return null;
        PhoneStatusNode statusNode = new PhoneStatusNode(id);
        add(phone, statusNode, RedisTemplatePhoneStatus);
        expirePhone(phone, RedisTemplatePhoneStatus);
        return statusNode.getVerifyCode();
    }

    /**
     * 根据手机与验证码返回id
     * @param phone
     * @param code
     * @return id或null
     */
    public String getIdByPhone(String phone, String code){
        if(phone == null || code == null || !hasKey(phone, RedisTemplatePhoneStatus))
            return null;
        PhoneStatusNode statusNode = getObject(phone, RedisTemplatePhoneStatus, PhoneStatusNode.class);
        if(code.equals(statusNode.getVerifyCode())){
            return statusNode.getUserId();
        }else{
            return null;
        }
    }

}
