package websocket.bean;

import java.io.Serializable;

/**
 * Created by addy on 5/26/15.
 */
public class IncomingMessage implements Serializable {
    private String username;
    private String message;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
