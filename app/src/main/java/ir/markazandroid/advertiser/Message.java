package ir.markazandroid.advertiser;


import java.io.Serializable;

import ir.markazandroid.advertiser.network.JSONParser.annotations.JSON;

/**
 * Created by Ali on 1/29/2019.
 */
public class Message implements Serializable {

    public static final String PING="ping";
    public static final String PONG="pong";
    public static final String ACK="ack";
    public static final String RESPONSE="response";
    public static final String MESSAGE="message";
    public static final String COMMAND="command";
    public static final String CONFIGURATION="configuration";
    public static final String CONNECTED="connected";

    private String type;
    private long time;
    private String message;
    private String messageId;
    private boolean success;


    @JSON
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @JSON
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    @JSON
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JSON
    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @JSON
    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
