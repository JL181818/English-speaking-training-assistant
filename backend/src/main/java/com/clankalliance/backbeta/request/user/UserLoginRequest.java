package com.clankalliance.backbeta.request.user;

//用户登录请求对象

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginRequest {

    private Long phone;

    private String password;

    private String randstr;

    private String ticket;
}
