package com.clankalliance.backbeta.request.user;

public class TokenCheckRequest {

    //前后端token校验请求只需要传token一个参数
    //单独作为一个请求对象

    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "TokenCheckRequest{" +
                "token='" + token + '\'' +
                '}';
    }
}
