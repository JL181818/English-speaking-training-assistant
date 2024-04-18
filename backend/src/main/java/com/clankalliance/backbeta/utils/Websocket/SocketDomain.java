package com.clankalliance.backbeta.utils.Websocket;

import lombok.Data;

import javax.websocket.Session;

@Data
public class SocketDomain {

    private Session session;

    private String uri;

}
