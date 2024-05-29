package com.clankalliance.backbeta.service.impl;

import com.clankalliance.backbeta.entity.User;
import com.clankalliance.backbeta.repository.UserRepository;
import com.clankalliance.backbeta.response.CommonResponse;
import com.clankalliance.backbeta.service.TencentService;
import com.clankalliance.backbeta.service.UserService;
import com.clankalliance.backbeta.utils.SnowFlake;
import com.clankalliance.backbeta.utils.StatusManipulateUtilsWithRedis.ManipulateUtilRedis;
import com.clankalliance.backbeta.utils.TokenUtil;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private TokenUtil tokenUtil;

    @Resource
    private UserRepository userRepository;

    @Resource
    private TencentService tencentService;

    @Resource
    private ManipulateUtilRedis ManipulateUtil;

    @Resource
    private SnowFlake snowFlake;



    @PostConstruct
    public void init(){
        userRepository.save(AI_USER);
        userRepository.save(TEST_USER);
    }

    /**
     * 手机登录 发送验证码方法
     * @param phone 手机号
     * @return
     */
    @Override
    public CommonResponse handlePhoneLogin(long phone){
        CommonResponse response = new CommonResponse();
        Optional<User> uop = userRepository.findUserByPhone(phone);
        User user;
        String code;
        if(uop.isPresent()){
            user = uop.get();
            code = ManipulateUtil.updatePhoneStatus(String.valueOf(user.getId()), String.valueOf(phone));
        }else{
            //用户不存在：用R作为占位符
            code = ManipulateUtil.updatePhoneStatus("R", String.valueOf(phone));
        }
        tencentService.sendSms(String.valueOf(phone),code);
        response.setSuccess(true);
        response.setMessage("已发送验证码");
        return response;
    }

    /**
     * 手机登录 验证验证码 生成token
     * @param phone 手机号
     * @param code 验证码
     * @return
     */
    @Override
    public CommonResponse handleCodeLogin(long phone, String code){
        CommonResponse response = tokenUtil.phoneCodeCheck(phone,code);
        if(!response.getLoginValid())
            return response;
        if(response.getMessage().equals("R")){
            User newUser = new User(snowFlake.nextId(), "默认用户名", phone, false, new ArrayList<>());
            try{
                userRepository.save(newUser);
                return CommonResponse.successResponse("注册成功", response);
            }catch (Exception r){
                return CommonResponse.errorResponse("保存失败", response, r);
            }
        }else{
            return CommonResponse.successResponse("登录成功", response);
        }
    }

    /**
     * 获取用户信息
     * @param token
     * @return
     */
    @Override
    public CommonResponse handleGetInfo(String token){
        CommonResponse response = tokenUtil.tokenCheck(token);
        if(!response.getLoginValid())
            return response;
        Optional<User> uop = userRepository.findUserById(Long.parseLong(response.getMessage()));
        if(uop.isEmpty()){
            return CommonResponse.errorResponse("用户不存在", response);
        }
        User user = uop.get();
        response.setContent(user);
        response.setSuccess(true);
        response.setMessage("查找成功");
        return response;
    }

}
