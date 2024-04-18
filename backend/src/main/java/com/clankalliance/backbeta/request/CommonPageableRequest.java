package com.clankalliance.backbeta.request;

public class CommonPageableRequest {

    private String token;

    //意义可变的一个属性，可视使用情景用来传递token或对象属性，如名称等
    private String identity;

    private int pageNum;

    private int size;

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    @Override
    public String toString() {
        return "CommonPageableRequest{" +
                "token='" + token + '\'' +
                ", identity='" + identity + '\'' +
                ", pageNum=" + pageNum +
                ", size=" + size +
                '}';
    }
}
