package com.clankalliance.backbeta.controller;

import java.io.IOException;

public class AckWaitingThread extends Thread{

    private static final Long RESEND_LIMIT = 30L;

    WebSocketServer webSocketServer;

    String sendContent;

    Long resendTime;

    AckWaitingThread(WebSocketServer webSocketServer, String sendContent){
        this.webSocketServer = webSocketServer;
        this.sendContent = sendContent;
        resendTime = 0L;
    }

    public void run(){
        try{
            while (true){
                if(resendTime > RESEND_LIMIT){
                    webSocketServer.closeConnection();
                    break;
                }
                resendTime ++;
                webSocketServer.sendMessage(sendContent);
                sleep(webSocketServer.ACK_TIMEOUT);
            }
        }catch (InterruptedException | IOException ignored){}
    }
}
