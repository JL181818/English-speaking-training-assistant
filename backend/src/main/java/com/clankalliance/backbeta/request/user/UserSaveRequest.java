package com.clankalliance.backbeta.request.user;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

//用户信息保存请求

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSaveRequest {

    @Id
    @JsonSerialize(using= ToStringSerializer.class)
    private long id;

    //用户名
    private String nickName;

    //第一次注册及修改手机时需要，手机验证码
    private String code;

    @NotBlank
    @Size(max = 50)
    private String password;

    @JsonSerialize(using= ToStringSerializer.class)
    private long phone;

    //男：false 女：true
    private Boolean gender;

    //电子邮箱
    private String email;

    private String researchDirection;

    private String token;
}
