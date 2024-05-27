package com.clankalliance.backbeta.controller;

public class AckWaitingThread extends Thread{

    WebSocketServer webSocketServer;

    String sendContent;

    AckWaitingThread(WebSocketServer webSocketServer, String sendContent){
        this.webSocketServer = webSocketServer;
        this.sendContent = sendContent;
    }

    public void run(){
        try{
            while (true){
                webSocketServer.sendMessage(sendContent);
                sleep(webSocketServer.ACK_TIMEOUT);
            }
        }catch (InterruptedException ignored){}
    }
}
