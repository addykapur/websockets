package websocket.endpoint;

/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import com.google.gson.Gson;
import websocket.bean.IncomingMessage;
import websocket.bean.OutgoingMessage;
import websocket.services.MessageEncoder;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

@ServerEndpoint(value = "/chat3",encoders = { MessageEncoder.class })
public class Chat3 {

    private static final Logger log = Logger.getLogger(Chat3.class.getName());

    private static final String GUEST_PREFIX = "Guest";
    private static final AtomicInteger connectionIds = new AtomicInteger(0);
    private static final Set<Chat3> connections =
            new CopyOnWriteArraySet<>();

    private String nickname;
    private Session session;

    public Chat3() {
        nickname = GUEST_PREFIX + connectionIds.getAndIncrement();
    }


    @OnOpen
    public void start(Session session) {
        this.session = session;
        connections.add(this);
        String message = String.format("* %s %s", nickname, "has joined.");
        broadcast(nickname,message);
    }


    @OnClose
    public void end() {
        connections.remove(this);
        String message = String.format("* %s %s",
                nickname, "has disconnected.");
        broadcast(nickname,message);
    }


    @OnMessage
    public void incoming(String json) {

//        System.out.println(new Date()+" received: "+json);
        Gson gson=new Gson();
        IncomingMessage incomingMessage=gson.fromJson(json,IncomingMessage.class);
        nickname=incomingMessage.getUsername();

        // Never trust the client
        String filteredMessage = filter(incomingMessage.getMessage());
        broadcast(nickname,filteredMessage);
    }




    @OnError
    public void onError(Throwable t) throws Throwable {
        log.log(Level.SEVERE, "Chat Error: " + t.toString(), t);
    }


    private static void broadcast(String username,String msg) {
//        System.out.println("broadcast: " + msg+" for "+username);

        final OutgoingMessage outgoingMessage=new OutgoingMessage();
        outgoingMessage.setMessage(msg);
        outgoingMessage.setUsername(username);
        outgoingMessage.setActiveUsers((long) connections.size());

        for (Chat3 client : connections) {
            try {
                synchronized (client) {

                    client.session.getBasicRemote().sendObject(outgoingMessage);
                }
            } catch (IOException e) {
                log.log(Level.FINE, "Chat Error: Failed to send message to client", e);
                connections.remove(client);
                try {
                    client.session.close();
                } catch (IOException e1) {
                    // Ignore
                }
                String message = String.format("* %s %s",
                        client.nickname, "has been disconnected.");
                broadcast(username,message);
            } catch (EncodeException e2)   {
                e2.printStackTrace();
            }
        }
    }

    /**
     * Filter the specified message string for characters that are sensitive
     * in HTML.  This avoids potential attacks caused by including JavaScript
     * codes in the request URL that is often reported in error messages.
     *
     * @param message The message string to be filtered
     */
    public static String filter(String message) {

        if (message == null)
            return (null);

        char content[] = new char[message.length()];
        message.getChars(0, message.length(), content, 0);
        StringBuilder result = new StringBuilder(content.length + 50);
        for (int i = 0; i < content.length; i++) {
            switch (content[i]) {
                case '<':
                    result.append("&lt;");
                    break;
                case '>':
                    result.append("&gt;");
                    break;
                case '&':
                    result.append("&amp;");
                    break;
                case '"':
                    result.append("&quot;");
                    break;
                default:
                    result.append(content[i]);
            }
        }
        return (result.toString());

    }
}
