package com.clankalliance.backbeta.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.clankalliance.backbeta.entity.Dialog;
import com.clankalliance.backbeta.entity.TrainingData;
import com.clankalliance.backbeta.entity.User;
import com.clankalliance.backbeta.redisDataBody.DialogDataBody;
import com.clankalliance.backbeta.request.model.InvokeModelRequest;
import com.clankalliance.backbeta.service.AIService;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

@Service
public class AIServiceImpl implements AIService {


    private static String MODEL_BASE_URL;

    @Value("${modelConnection.requestBaseUrl}")
    public void setModelBaseUrl(String url){MODEL_BASE_URL = url;}

    @Override
    public String invokeModel(User user, List<DialogDataBody> dialogs) {
        InvokeModelRequest request = new InvokeModelRequest(user, dialogs);

        // 创建httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        //创建请求对象
        HttpPost httpPost = new HttpPost( MODEL_BASE_URL + "/v1/chat/completions");

        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(request);


        StringEntity entity = null;
        try {
            entity = new StringEntity(jsonObject.toString());
        } catch (UnsupportedEncodingException e) {
            return e.toString();
        }
        //指定请求编码方式
        entity.setContentEncoding("utf-8");
        //数据格式
        entity.setContentType("application/json");
        httpPost.setEntity(entity);

        //发送请求
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
        } catch (IOException e) {
            return e.toString();
        }

        //解析返回结果
        int statusCode = response.getStatusLine().getStatusCode();

        HttpEntity entity1 = response.getEntity();

        String body = null;
        try {
            body = EntityUtils.toString(entity1);
        } catch (IOException e) {
            return e.toString();
        }
        if(statusCode != 200){
            return "出现错误：" + statusCode + " body: " + body;
        }
        //关闭资源
        try {
            response.close();
            httpClient.close();
        } catch (IOException e) {
            return e.toString();
        }
        String result = body.substring(body.lastIndexOf("\"content\":") + 11, body.indexOf('\"', body.lastIndexOf("\"content\":") + 11));
        return result;


    }

}
