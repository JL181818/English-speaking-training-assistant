package com.clankalliance.backbeta.controller;

import com.clankalliance.backbeta.entity.User;
import com.clankalliance.backbeta.response.CommonResponse;
import com.clankalliance.backbeta.service.UserService;
import com.clankalliance.backbeta.utils.RedisUtils;
import com.clankalliance.backbeta.utils.TokenUtil;
import com.clankalliance.backbeta.utils.Websocket.SocketDomain;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/websocket/{token}")
public class WebSocketServer {

    private static TokenUtil tokenUtil;

    /**
     * WebSocket为多对象的
     * @serverEndpoint下的@Resource注解会失效
     * 故采用set方法注入
     */
    @Resource
    public void setTokenUtil(TokenUtil tokenUtil){WebSocketServer.tokenUtil = tokenUtil;}

//    Redis的一个引入范例
//    /**
//     * key: id
//     * value: roomCode
//     */
//    private static StringRedisTemplate RedisTemplateIdRoomCode;
//
//    @Resource
//    public void setRedisTemplateIdRoomCode(StringRedisTemplate redisTemplateIdRoomCode){WebSocketServer.RedisTemplateIdRoomCode = redisTemplateIdRoomCode;}


    private static UserService userService;

    @Resource
    public void setUserService(UserService userService){WebSocketServer.userService = userService;}

    private static final Logger logger = LoggerFactory.getLogger(WebSocketServer.class);
    //在线客户端数目
    private static int onlineCount = 0;
    //Map用于存储已连接的客户端信息(打算用redis改进)
    private static ConcurrentHashMap<String, SocketDomain> websocketMap = new ConcurrentHashMap<>();

    private Session session;

    private String userId = "";

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token){
        //TODO: 用户连接上Websocket客户端后，会调用该函数
        CommonResponse response = tokenUtil.tokenCheck(token);
        this.session = session;
        if(!response.getSuccess()){
            sendMessage("loginFail");
            return;
        }
        String targetId = response.getMessage();
        if(!websocketMap.containsKey(targetId)){
            onlineCount ++;
        }
        this.userId = targetId;
        SocketDomain socketDomain = new SocketDomain();
        socketDomain.setSession(session);
        socketDomain.setUri(session.getRequestURI().toString());
        websocketMap.put(userId, socketDomain);
        logger.info("id为" + userId + "的用户连接，当前人数为" + onlineCount);
    }

    @OnClose
    public void onClose(){
        if(websocketMap.containsKey(userId)){
            websocketMap.remove(userId);
            onlineCount --;
            logger.info("id为" + userId + "的用户断开连接，当前人数为" + onlineCount);
        }
        //TODO: 用户断开连接Websocket客户端后，会调用该函数

    }

    @OnMessage
    public void onMessage(String message, Session session){
        //TODO: 用户向Websocket客户端发送消息，会调用该函数
        if(!StringUtil.isNullOrEmpty(message)){
            logger.info("收到id为" + userId + "的用户发来消息：" + message);
        }
    }


    private void sendMessage(String obj){
        synchronized (session){
            this.session.getAsyncRemote().sendText(obj);
        }
    }

    private void sendMessageTo(String userId,String obj){
        SocketDomain socketDomain = websocketMap.get(userId);
        try {
            if(socketDomain !=null){
                socketDomain.getSession().getAsyncRemote().sendText(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    private void sendMessageToAllExpectSelf(String message, Session fromSession) {
        for(Map.Entry<String, SocketDomain> client : websocketMap.entrySet()){
            Session toSession = client.getValue().getSession();
            if( !toSession.getId().equals(fromSession.getId())&&toSession.isOpen()){
                toSession.getAsyncRemote().sendText(message);
                logger.info("服务端发送消息给"+client.getKey()+":"+message);
            }
        }
    }

    private void sendMessageToAll(String message){
        for(Map.Entry<String, SocketDomain> client : websocketMap.entrySet()){
            Session toSeesion = client.getValue().getSession();
            if(toSeesion.isOpen()){
                toSeesion.getAsyncRemote().sendText(message);
                logger.info("服务端发送消息给"+client.getKey()+":"+message);
            }
        }
    }
    //给外部调用的方法接口
    public void sendAll(String Message){
        sendMessageToAll(Message);
    }
}
