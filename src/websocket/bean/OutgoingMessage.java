package websocket.bean;

import java.util.List;

/**
 * Created by addy on 5/26/15.
 */
public class OutgoingMessage {
    private String username;
    private String message;
    private Long activeUsers;
    private Long noOfMsgs=0l;
    private List<User> leaderBoard=null;


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

    public Long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(Long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public Long getNoOfMsgs() {
        return noOfMsgs;
    }

    public void setNoOfMsgs(Long noOfMsgs) {
        this.noOfMsgs = noOfMsgs;
    }

    public List<User> getLeaderBoard() {
        return leaderBoard;
    }

    public void setLeaderBoard(List<User> leaderBoard) {
        this.leaderBoard = leaderBoard;
    }
}
