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
import websocket.bean.User;
import websocket.services.MessageEncoder;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

@ServerEndpoint(value = "/chat4",encoders = { MessageEncoder.class })
public class Chat4 {

    private static final Logger log = Logger.getLogger(Chat4.class.getName());

    private static final int MAX_LEADERS=20;
    private static final String GUEST_PREFIX = "Guest";
    private static final AtomicInteger connectionIds = new AtomicInteger(0);
    private static final Set<Chat4> connections =
            new CopyOnWriteArraySet<>();
    //private static final SortedSet<User> allUsers=Collections.synchronizedSortedSet(new TreeSet());
    private static List<User> allUsers = new ArrayList<>();

    private User user=new User();
    private Session session;

    public Chat4() {
        user.setNickname(GUEST_PREFIX + connectionIds.getAndIncrement());
    }


    @OnOpen
    public void start(Session session) {
        this.session = session;
        connections.add(this);
        allUsers.add(user);

        String message = String.format("* %s %s", user.getNickname(), "has joined.");
        broadcast(user.getNickname(),message);
    }


    @OnClose
    public void end() {
        connections.remove(this);
        String message = String.format("* %s %s",
                user.getNickname(), "has disconnected.");
        broadcast(user.getNickname(),message);
    }


    @OnMessage
    public void incoming(String json) {

        System.out.println(new Date()+" received: "+json);
        Gson gson=new Gson();
        IncomingMessage incomingMessage=gson.fromJson(json,IncomingMessage.class);
        if(incomingMessage.getUsername()!=null)
            user.setNickname(incomingMessage.getUsername());
        user.setNoOfMsgs(user.getNoOfMsgs()+1l);//increment counters
        // Never trust the client
        String filteredMessage = filter(incomingMessage.getMessage());
        broadcast(user.getNickname(),filteredMessage);
    }




    @OnError
    public void onError(Throwable t) throws Throwable {
        log.log(Level.SEVERE, "Chat Error: " + t.toString(), t);
    }


    private static void broadcast(String username,String msg) {
        System.out.println("broadcast: " + msg);
        Collections.sort(allUsers);
        List<User> leaderList=new ArrayList<>();
        int i=0;
        for(User u:allUsers)    {
            System.out.println(u.getNickname()+" = "+u.getNoOfMsgs());
            if(i<MAX_LEADERS)
                leaderList.add(u);
            i++;
        }

        OutgoingMessage outgoingMessage=new OutgoingMessage();
        outgoingMessage.setUsername(username);
        outgoingMessage.setMessage(msg);
        outgoingMessage.setActiveUsers((long) connections.size());
        outgoingMessage.setLeaderBoard(leaderList);

        for (Chat4 client : connections) {
            try {
                synchronized (client) {
                    outgoingMessage.setNoOfMsgs(client.user.getNoOfMsgs());
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
                        client.user.getNickname(), "has been disconnected.");
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
