package com.clankalliance.backbeta.controller;

import com.clankalliance.backbeta.entity.Dialog;
import com.clankalliance.backbeta.entity.TrainingData;
import com.clankalliance.backbeta.entity.User;
import com.clankalliance.backbeta.entity.relation.TrainingDataDialogs;
import com.clankalliance.backbeta.redisDataBody.DialogDataBody;
import com.clankalliance.backbeta.repository.DialogRepository;
import com.clankalliance.backbeta.repository.TrainingDataRepository;
import com.clankalliance.backbeta.repository.UserRepository;
import com.clankalliance.backbeta.repository.relationRepository.TrainingDataDialogsRepository;
import com.clankalliance.backbeta.response.CommonResponse;
import com.clankalliance.backbeta.service.AIService;
import com.clankalliance.backbeta.utils.RedisUtils;
import com.clankalliance.backbeta.utils.SnowFlake;
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
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.clankalliance.backbeta.service.UserService.AI_USER;

@Component
@ServerEndpoint("/websocket/{token}")
public class WebSocketServer {

    private final String AI_HELLO_MESSAGE = "Hello, I'm SpeakSpark, your oral English speaking training assistant. What can I help you?";

    private static TokenUtil tokenUtil;
    /**
     * WebSocket为多对象的
     * @serverEndpoint下的@Resource注解会失效
     * 故采用set方法注入
     */
    @Resource
    public void setTokenUtil(TokenUtil tokenUtil){WebSocketServer.tokenUtil = tokenUtil;}


    private static SnowFlake snowFlake;

    @Resource
    public void setSnowFlake(SnowFlake snowFlake){WebSocketServer.snowFlake = snowFlake;}

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

    private static TrainingDataDialogsRepository trainingDataDialogsRepository;

    @Resource
    public void setTrainingDataDialogsRepository(TrainingDataDialogsRepository trainingDataDialogsRepository){WebSocketServer.trainingDataDialogsRepository = trainingDataDialogsRepository;}

    private static TrainingDataRepository trainingDataRepository;

    @Resource
    public void setTrainingDataRepository(TrainingDataRepository trainingDataRepository){WebSocketServer.trainingDataRepository = trainingDataRepository;}

    private static final Logger logger = LoggerFactory.getLogger(WebSocketServer.class);
    //在线客户端数目
    private static int onlineCount = 0;
    //Map用于存储已连接的客户端信息(打算用redis改进)
    private static final ConcurrentHashMap<String, SocketDomain> websocketMap = new ConcurrentHashMap<>();

    private Session session;

    private Long userId = 0L;


    //关云鹏 2024.4.28
    //用常量维护超时时间 单位: 毫秒
    public final long ACK_TIMEOUT = 5000;

    //期望收到客户端的对话编号 小于该值代表为客户端过去重传的重复包，回复ack并不予处理
    private long sayPackageIdExcepted;

    //ai当前的消息序号
    private long aiCurrentPackageId;

    //该房间对应的用户
    private User currentUser;

    /*
    userId 若为用户，对应唯一一个房间号，一个房间号对应一个房间信息
    房间信息存储用户和AI的训练对话内容
    userId,AI对应不同的对话内容
    */


    private Hashtable<Long, AckWaitingThread> waitingMissions;

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token){
        waitingMissions = new Hashtable<>();
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
        this.userId = Long.parseLong(targetId);

        Optional <User> uop = userRepository.findUserById(userId);
        currentUser = uop.get();

        SocketDomain socketDomain = new SocketDomain();
        socketDomain.setSession(session);
        socketDomain.setUri(session.getRequestURI().toString());
        websocketMap.put(String.valueOf(userId), socketDomain);
        logger.info("id为" + userId + "的用户连接，当前人数为" + onlineCount);
        //startTraining(targetId);  //
        //两个序号的初始化
        sayPackageIdExcepted = 1;
        aiCurrentPackageId = 1;

        RedisUtils.add(String.valueOf(currentUser.getId()),new ArrayList<>(),redisTemplateUserRoom);

        sendMessageWithResend("token#" + aiCurrentPackageId + "#" + response.getToken());

        aiCurrentPackageId ++;
    }

    @OnClose
    public void onClose(){
        if(websocketMap.containsKey(userId)){
            websocketMap.remove(userId);
            onlineCount --;
            logger.info("id为" + userId + "的用户断开连接，当前人数为" + onlineCount);
        }

        for(Map.Entry<Long, AckWaitingThread> t: waitingMissions.entrySet()){
            t.getValue().interrupt();
        }
        waitingMissions.clear();

        //断开连接后，会把数据存到数据库

        if(!RedisUtils.hasKey(String.valueOf(userId), redisTemplateUserRoom)){
            return;
        }

        List<DialogDataBody> dialogsRaw = RedisUtils.getList(String.valueOf(userId),redisTemplateUserRoom,DialogDataBody.class);
        RedisUtils.delete(String.valueOf(userId), redisTemplateUserRoom);
        List<Dialog> dialogs = new ArrayList<>();
        if(dialogsRaw != null){
            for(DialogDataBody d: dialogsRaw){
                if(d.getSenderId().equals(AI_USER.getId())){
                    dialogs.add(d.toDialog(AI_USER));
                }else{
                    dialogs.add(d.toDialog(currentUser));
                }
            }
            //关云鹏 5.11: 空的训练不需要存
            if(dialogs.size() == 0)
                return;
            //将dialog存入数据库

            dialogRepository.saveAll(dialogs);
        }

        User user = userRepository.findUserById(userId).get();

        TrainingData trainingData = new TrainingData();
        //关云鹏 5.11: 训练数据初始化需指定id
        trainingData.setId("" + snowFlake.nextId());
        trainingData.setUser(user);
        Date currentTimeUser = new Date();
        trainingData.setTime(currentTimeUser);
        trainingData.setDialogs(dialogs);

        //调用接口，获取score
        //将trainingData存入数据库
        trainingDataRepository.save(trainingData);

        for(Dialog d: dialogs){
            trainingDataDialogsRepository.save(new TrainingDataDialogs(trainingData.getId(), d.getId()));
        }

        redisTemplateUserRoom.delete(String.valueOf(userId));

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
                try{
                    Long ackPackageId = Long.parseLong(request[2]);
                    waitingMissions.get(ackPackageId).interrupt();
                    waitingMissions.remove(ackPackageId);
                }catch (Exception ignored){}

                if(aiCurrentPackageId == 2){
                    //关云鹏 5.11 新增: AI开场白(固定)
                    String openingMessage = "say#" + aiCurrentPackageId + "#" + AI_HELLO_MESSAGE;
                    sendMessageWithResend(openingMessage);
                    redisStor(AI_USER.getId(), AI_HELLO_MESSAGE);
                    aiCurrentPackageId ++;
                }

            }else{
                System.out.println("前端发送错误: " + message);
                throw new RuntimeException();
            }

        }else if(request[0].equals("say")){
            //格式say#{用户消息序号}#{用户回应}#{用户回应更正}#{评分}，用户传来文本,需要调用大模型
            /*关云鹏 2024.4.28 增加一层对用户消息序号的验证
            *
            * */
            //将消息确认放最前面，避免后面的say与corr导致ack超时引起客户端重传
            //收到用户的say后向用户回确认
            String content = request[2];
            String correction = request[3];
            Double score = 0.0;
            try{
                score = Double.parseDouble(request[4]);
            }catch (Exception ignored){};
            String ackMessage = "ack#say#" + request[1];
            sendMessageTo(String.valueOf(userId),ackMessage);
            if(Long.parseLong(request[1]) >= sayPackageIdExcepted){
                //TODO: 考虑序号小的包比序号大的包来的晚(但这种情况似乎不太可能)
                sayPackageIdExcepted ++;
                String messageToUser;

                //存储大模型纠错作为一个dialog

                //先把用户的文本存一个dialog
                redisStor(userId,content,correction, score);

                /*
                 * 关云鹏 2024.4.28
                 * 接入大模型 纠错暂不接入，中期检查后再更新
                 * 针对你Redis的数据结构，对AI接口也做了修改
                 * aiCurrentPackageId代表当前发送的对话的序号，作为标识
                 * 避免重传可能引起的重复问题
                 * */
//                List<DialogDataBody> dialogs = RedisUtils.getList(String.valueOf(userId),redisTemplateUserRoom,DialogDataBody.class);
                String contentAI = aiService.invokeModel(content);

                redisStor(AI_USER.getId(),contentAI);

                //向用户发送AI回复
                messageToUser = "say#" + aiCurrentPackageId + "#" + contentAI;
                sendMessageWithResend(messageToUser);
                aiCurrentPackageId ++;
                /*
                 * 修改结束
                 * */
            }

        }
        if(!StringUtil.isNullOrEmpty(message)){
            logger.info("收到id为" + userId + "的用户发来消息：" + message);
        }
    }

    private void sendMessageWithResend(String content){
        AckWaitingThread ackWaitingThread = new AckWaitingThread(this, content);
        waitingMissions.put(aiCurrentPackageId, ackWaitingThread);
        ackWaitingThread.start();
    }

    private void redisStor(Long userId,String content,String correction, Double score){
        String key = String.valueOf(currentUser.getId());
        List<DialogDataBody> list = RedisUtils.getList(key, redisTemplateUserRoom, DialogDataBody.class);
        Date currentTimeUser = new Date();
        DialogDataBody dialogUser = new DialogDataBody();
        dialogUser.setId("" + snowFlake.nextId());
        dialogUser.setSenderId(userId);
        dialogUser.setContent(content);
        dialogUser.setTime(currentTimeUser);
        dialogUser.setCorrection(correction);
        dialogUser.setScore(score);
        list.add(dialogUser);
        //没有userId这个键值，会自己创建一个空的吧
        RedisUtils.add(key,list,redisTemplateUserRoom);
    }

    private void redisStor(Long userId,String content){
        redisStor(userId, content, "", 0.0);
    }

    public void sendMessage(String obj){
        synchronized (session){
            this.session.getAsyncRemote().sendText(obj);
        }
    }

    public void closeConnection() throws IOException {
        synchronized (session){
            this.session.close();
        }
    }

    public void sendMessageTo(String userId,String obj){
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
