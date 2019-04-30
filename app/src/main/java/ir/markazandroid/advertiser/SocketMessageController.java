package ir.markazandroid.advertiser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import ir.markazandroid.advertiser.Message;
import ir.markazandroid.advertiser.WebSockTest;
import ir.markazandroid.advertiser.network.JSONParser.Parser;

/**
 * Coded by Ali on 2/5/2019.
 */
public class SocketMessageController {

    public interface MessageResponseListener{
        void onResponse(Message response);
    }

    private WebSockTest socket;

    private ConcurrentHashMap<String,Message> messageMap;
    private Parser parser;
    private ConcurrentHashMap<String,Timer> timers;
    private ConcurrentHashMap<String,MessageResponseListener> responseListenerMap;

    public SocketMessageController(WebSockTest socket, Parser parser) {
        this.socket = socket;
        this.parser=parser;
        messageMap=new ConcurrentHashMap<>();
        timers=new ConcurrentHashMap<>();
        responseListenerMap=new ConcurrentHashMap<>();
        socket.addMessageListener(this::onResponse);
    }

    public String sendMessage(String msg,MessageResponseListener responseListener){
        Message message = new Message();
        message.setMessage(msg);
        message.setMessageId(UUID.randomUUID().toString());
        socket.send(parser.get(message).toString());
        messageMap.put(message.getMessageId(),message);
        Timer timer = new Timer();
        timer.schedule(new TimeOutTimerTask(message.getMessageId()),60_000);
        timers.put(message.getMessageId(),timer);
        responseListenerMap.put(message.getMessageId(),responseListener);
        return message.getMessageId();
    }

    public void sendOutMessage(Message outputMessage){
        socket.send(parser.get(outputMessage).toString());
    }

    private void onResponse( Message message){
        if (messageMap.containsKey(message.getMessageId())){
            responseListenerMap.get(message.getMessageId()).onResponse(message);
            finishMessage(message.getMessageId());
        }
    }

    public void finishMessage(String messageId){
        Timer timer = timers.get(messageId);
        timers.remove(messageId);
        if (timer!=null) timer.cancel();
        responseListenerMap.remove(messageId);
        messageMap.remove(messageId);

    }

    private class TimeOutTimerTask extends TimerTask{

        private String messageId;

        private TimeOutTimerTask(String messageId) {
            this.messageId = messageId;
        }

        @Override
        public void run() {
            finishMessage(messageId);
        }
    }
}
