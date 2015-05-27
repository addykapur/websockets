package websocket.bean;

import java.io.Serializable;

/**
 * Created by addy on 5/27/15.
 */
public class User implements Serializable,Comparable<User> {
    private String nickname;
    private Long noOfMsgs=0l;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Long getNoOfMsgs() {
        return noOfMsgs;
    }

    public void setNoOfMsgs(Long noOfMsgs) {
        this.noOfMsgs = noOfMsgs;
    }

    public int compareTo(User other)   {
        //we want reverse order
        return other.getNoOfMsgs().compareTo(noOfMsgs);
    }
}
