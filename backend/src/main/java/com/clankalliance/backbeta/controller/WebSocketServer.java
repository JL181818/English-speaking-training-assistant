package com.clankalliance.backbeta.controller;

import com.clankalliance.backbeta.entity.Dialog;
import com.clankalliance.backbeta.entity.TrainingData;
import com.clankalliance.backbeta.entity.User;
import com.clankalliance.backbeta.repository.DialogRepository;
import com.clankalliance.backbeta.repository.TrainingDataRepository;
import com.clankalliance.backbeta.repository.UserRepository;
import com.clankalliance.backbeta.response.CommonResponse;
import com.clankalliance.backbeta.service.AIService;
import com.clankalliance.backbeta.utils.RedisUtils;
import com.clankalliance.backbeta.utils.TokenUtil;
import com.clankalliance.backbeta.utils.Websocket.SocketDomain;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.clankalliance.backbeta.service.UserService.AI_USER;

//xxx.cn/websocket/123456
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

    /**
     * key: userId
     * value: list<dialog>
     */
    private static StringRedisTemplate redisTemplateUserRoom;

    @Resource
    public void setRedisTemplateUserRoom(StringRedisTemplate redisTemplateUserRoom){WebSocketServer.redisTemplateUserRoom = redisTemplateUserRoom;}

    private static AIService aiService;

    @Resource
    public void setAiService(AIService aiService){WebSocketServer.aiService = aiService;}

    private static DialogRepository dialogRepository;

    @Resource
    public void setDialogRepository(DialogRepository dialogRepository){WebSocketServer.dialogRepository = dialogRepository;}

    private static UserRepository userRepository;

    @Resource
    public void setUserRepository(UserRepository userRepository){WebSocketServer.userRepository = userRepository;}

    private static TrainingDataRepository trainingDataRepository;

    @Resource
    public void setTrainingDataRepository(TrainingDataRepository trainingDataRepository){WebSocketServer.trainingDataRepository = trainingDataRepository;}

    private static final Logger logger = LoggerFactory.getLogger(WebSocketServer.class);
    //在线客户端数目
    private static int onlineCount = 0;
    //Map用于存储已连接的客户端信息(打算用redis改进)
    private static final ConcurrentHashMap<String, SocketDomain> websocketMap = new ConcurrentHashMap<>();

    private Session session;

    private String userId = "";


    private boolean isAckSay = true;

    private boolean isAckCorr = true;

    private long startTimeSay;

    private long startTimeCorr;

    //关云鹏 2024.4.28
    //用常量维护超时时间
    private final long ACK_TIMEOUT = 5;

    //期望收到客户端的对话编号 小于该值代表为客户端过去重传的重复包，回复ack并不予处理
    private long sayPackageIdExcepted;

    //ai当前的消息序号
    private long aiCurrentPackageId;

    /*
    userId 若为用户，对应唯一一个房间号，一个房间号对应一个房间信息
    房间信息存储用户和AI的训练对话内容
    userId,AI对应不同的对话内容
    */

    private String modelTestCorr(String input){ return "AI纠错";}

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token){
        //用户连接上Websocket客户端后，会调用该函数
        CommonResponse response = tokenUtil.tokenCheck(token);
        this.session = session;
        if(!response.getSuccess()){
            sendMessage("loginFail");
            return;
        }
        String targetId = response.getMessage(); //message里存储用户Id
        if(!websocketMap.containsKey(targetId)){
            onlineCount ++;
        }
        this.userId = targetId;
        SocketDomain socketDomain = new SocketDomain();
        socketDomain.setSession(session);
        socketDomain.setUri(session.getRequestURI().toString());
        websocketMap.put(userId, socketDomain);
        isAckSay = true;
        logger.info("id为" + userId + "的用户连接，当前人数为" + onlineCount);
        //startTraining(targetId);  //
        //两个序号的初始化
        sayPackageIdExcepted = 1;
        aiCurrentPackageId = 1;
    }

    @OnClose
    public void onClose(){
        if(websocketMap.containsKey(userId)){
            websocketMap.remove(userId);
            onlineCount --;
            logger.info("id为" + userId + "的用户断开连接，当前人数为" + onlineCount);
        }

        //断开连接后，会把数据存到数据库
        List<Dialog> dialogs = RedisUtils.getList(userId,redisTemplateUserRoom,Dialog.class);
        //将dialog存入数据库

        dialogRepository.saveAll(dialogs);

        TrainingData trainingData = new TrainingData();
        trainingData.setUser(userRepository.findUserById(Long.parseLong(userId)).get());
        Date currentTimeUser = new Date();
        trainingData.setTime(currentTimeUser);
        trainingData.setDialogs(dialogs);

        //调用接口，获取score
        //将trainingData存入数据库
        trainingDataRepository.save(trainingData);

        redisTemplateUserRoom.delete(userId);

    }

    @OnMessage
    public void onMessage(String message, Session session){
        //ack#say#{AI消息序号}，用户向Websocket客户端发送消息，会调用该函数
        String[] request;
        request = message.split("#");

        //后续存储在MySQL里的时候，只需要判断dialog的sender是否为AI就可以吧

        if(request[0].equals("ack")){
            if(request[1].equals("say")){
                //格式ack#say#{AI消息序号} 发过去的回应被收到了
                isAckSay = true;//接收到了ack才能继续

            }else if(request[1].equals("corr")){

                isAckCorr = true;

            }else{
                throw new RuntimeException();
            }

        }else if(request[0].equals("say")&&isAckSay&&isAckCorr){
            //格式say#{用户消息序号}#{用户回应}，用户传来文本,需要调用大模型
            /*关云鹏 2024.4.28 增加一层对用户消息序号的验证
            *
            * */
            //将消息确认放最前面，避免后面的say与corr导致ack超时引起客户端重传
            //收到用户的say后向用户回确认
            String ackMessage = "ack#say#" + request[1];
            sendMessageTo(userId,ackMessage);
            if(Long.parseLong(request[1]) >= sayPackageIdExcepted){
                String messageToUser;

                //存储大模型纠错作为一个dialog
                String correction = modelTestCorr(request[2]);

                messageToUser = "corr#" + request[1] + "#"+correction;
                isAckCorr = false;
                sendMessageTo(userId,messageToUser);
                startTimeCorr = System.currentTimeMillis();
                //关云鹏 2024.4.28 用常量维护超时时间，避免Magic Number
                while(!isAckCorr && ((System.currentTimeMillis()-startTimeCorr) / 1000) > ACK_TIMEOUT){
                    //当没收到ack并且已经过了5秒了，超时重传
                    sendMessageTo(userId,messageToUser);
                    startTimeCorr = System.currentTimeMillis();
                }

                User user = new User();
                Optional <User> uop = userRepository.findUserById(Long.parseLong(userId));
                if(uop.isEmpty()){
                    sendMessage("用户不存在"); //这不能不存在吧，token都找到了，需要异常处理吗？
                }else{
                    user = uop.get();
                }


                //先把用户的文本存一个dialog
                redisStor(userId,user,request[2],correction);

                //存储大模型说的话返回结果为一个dialog
                //这里实际上要把redis中的数据拿出来给大模型，是否为直接转成字符串？
                //需要什么格式，下面仅是个测试
                //String content = modelTestSay("AI回复");

                /*
                 * 关云鹏 2024.4.28
                 * 接入大模型 纠错暂不接入，中期检查后再更新
                 * 针对你Redis的数据结构，对AI接口也做了修改
                 * aiCurrentPackageId代表当前发送的对话的序号，作为标识
                 * 避免重传可能引起的重复问题
                 * */
                List<Dialog> dialogs = RedisUtils.getList(userId,redisTemplateUserRoom,Dialog.class);
                String content = aiService.invokeModel(user, dialogs);

                redisStor(userId,AI_USER,content,"");

                //向用户发送AI回复
                messageToUser = "say#" + aiCurrentPackageId + "#" + content;
                sendMessageTo(userId,messageToUser);
                aiCurrentPackageId ++;
                /*
                 * 修改结束
                 * */
                isAckSay = false;
                startTimeSay = System.currentTimeMillis();
                //关云鹏 2024.4.28 用常量维护超时时间，避免Magic Number
                while(!isAckSay && ((System.currentTimeMillis()-startTimeSay)/1000) > ACK_TIMEOUT){
                    //当没收到ack并且已经过了5秒了
                    sendMessageTo(userId,messageToUser);
                    startTimeSay = System.currentTimeMillis();
                }
            }

        }
        if(!StringUtil.isNullOrEmpty(message)){
            logger.info("收到id为" + userId + "的用户发来消息：" + message);

        }
    }

    private void redisStor(String userId,User sender,String content,String correction){
        Date currentTimeUser = new Date();
        Dialog dialogUser = new Dialog();
        dialogUser.setSender(sender);
        dialogUser.setContent(content);
        dialogUser.setTime(currentTimeUser);
        dialogUser.setCorrection(correction);
        //没有userId这个键值，会自己创建一个空的吧
        RedisUtils.add(userId,dialogUser,redisTemplateUserRoom);
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
