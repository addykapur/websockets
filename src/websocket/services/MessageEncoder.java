package websocket.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import websocket.bean.OutgoingMessage;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import java.util.Date;

/**
 * Created by addy on 5/26/15.
 */
public class MessageEncoder implements Encoder.Text<OutgoingMessage> {

    @Override
    public String encode(OutgoingMessage outgoingMessage) throws EncodeException {


        Gson gson = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .create();
        String json=gson.toJson(outgoingMessage);

        System.out.println(new Date() + " encode:" + json);
        return json;
    }

    @Override
    public void init(EndpointConfig ec) {
        System.out.println("MessageEncoder - init method called");
    }

    @Override
    public void destroy() {
        System.out.println("MessageEncoder - destroy method called");
    }

}