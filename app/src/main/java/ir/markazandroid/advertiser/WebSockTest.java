package ir.markazandroid.advertiser;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

import ir.markazandroid.advertiser.network.JSONParser.Parser;
import ir.markazandroid.advertiser.network.NetworkClient;
import ir.markazandroid.advertiser.object.WebSocketConfiguration;
import rx.Observer;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.client.StompClient;
import ua.naiksoftware.stomp.client.StompMessage;

/**
 * Coded by Ali on 1/29/2019.
 */
public class WebSockTest {


    public interface MessageReceiver{
        void onReceive(Message payload) throws Exception;
    }

    private StompClient mStompClient;
    private NetworkClient networkClient;
    private CopyOnWriteArraySet<MessageReceiver> messageReceivers;
    private Parser parser;
    private volatile long lastPongTimestamp,lastMessageTimestamp;
    private Timer timeoutTimer,pingTimer,toConnectTimeoutTimer;
    private volatile WebSocketConfiguration configuration;
    private volatile boolean isConnecting;


    public WebSockTest(NetworkClient networkClient, Parser parser) {
        this.networkClient=networkClient;
        this.parser = parser;
        messageReceivers=new CopyOnWriteArraySet<>();
    }


    public synchronized void connect(){
        if (mStompClient!=null){
            if ((mStompClient.isConnected() || isConnecting)){
                Log.e("STOMP",mStompClient.isConnected()+" lol"+mStompClient.isConnecting());
                return;
            }
        }
        tryToConnect();

        Log.e("socket","trying to over");
        Map<String,String> headers = new HashMap<>();
        /*StringBuilder value= new StringBuilder();
        for (Cookie cookie:networkClient.getCookie()){
            value.append(cookie.name()).append("=").append(cookie.value()).append("; ");
        }
        value.delete(value.length()-2,value.length()-1);
        headers.put("Cookie",value.toString());
*/
        mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://harajgram.ir/advertiserv4/socket/phone",headers,networkClient.getClient());
        mStompClient.lifecycle().subscribe(lifecycleEvent -> {
            switch (lifecycleEvent.getType()) {

                case OPENED:
                    endConnectTimeout();
                    Log.d("STOMP", "Stomp connection opened");
                    break;

                case ERROR:
                    endConnectTimeout();
                    disconnect();
                    connect();
                    Log.e("STOMP", "Error", lifecycleEvent.getException());
                    break;

                case CLOSED:
                    endConnectTimeout();
                    disconnect();
                    Log.d("STOMP", "Stomp connection closed");
                    break;
            }
        });
        Log.e("socket","trying to connect");
        mStompClient.connect();

        Log.e("socket","trying to subscribe");
        mStompClient.topic("/user/queue/phone").subscribe(new Observer<StompMessage>() {
            @Override
            public void onCompleted() {


            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(StompMessage stompMessage) {
                Log.e("WebSocket", stompMessage.getPayload());

                try {
                    Message message = parser.get(Message.class,new JSONObject(stompMessage.getPayload()));

                    if (Message.PONG.equals(message.getType())){
                        lastPongTimestamp=System.currentTimeMillis();
                    }
                    else if (Message.CONFIGURATION.equals(message.getType())){
                        lastMessageTimestamp=System.currentTimeMillis();
                        WebSocketConfiguration configuration = parser.get(WebSocketConfiguration.class,new JSONObject(stompMessage.getPayload()));
                        setConfig(configuration);
                        sendAckMessage(message.getMessageId());
                    }
                    else {
                        lastMessageTimestamp=System.currentTimeMillis();
                        sendAckMessage(message.getMessageId());
                        for (MessageReceiver receiver : messageReceivers) {
                            try {
                                receiver.onReceive(message);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        Message message = new Message();
        message.setTime(System.currentTimeMillis());
        message.setMessage("Connected");
        message.setType(Message.CONNECTED);
        message.setMessageId(UUID.randomUUID().toString());
        send(parser.get(message).toString());
        //lastMessageTimestamp=System.currentTimeMillis();

        Log.e("socket","done");
    }

    private void tryToConnect(){
        isConnecting=true;
        if (toConnectTimeoutTimer!=null){
            toConnectTimeoutTimer.cancel();
        }
        toConnectTimeoutTimer=new Timer();
        toConnectTimeoutTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.e("STOMP","Connecting Timed Out");
                disconnect();
                isConnecting=false;
            }
        },35_000);
    }

    private void endConnectTimeout(){
        if (toConnectTimeoutTimer!=null){
            toConnectTimeoutTimer.cancel();
        }
        toConnectTimeoutTimer=null;
        isConnecting=false;
        Log.e("STOMP","TimeOut Timer Ended");
    }

    private void setConfig(WebSocketConfiguration configuration) {
        if (timeoutTimer!=null)
            timeoutTimer.cancel();

        if (pingTimer!=null)
            pingTimer.cancel();

        this.configuration=configuration;

        timeoutTimer=new Timer();
        timeoutTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (System.currentTimeMillis()-lastMessageTimestamp>=configuration.getNoMessageTimeout() ||
                        (System.currentTimeMillis()-lastMessageTimestamp>=configuration.getNoPongTimeout() &&
                                System.currentTimeMillis()-lastPongTimestamp>=configuration.getNoPongTimeout()) ){
                    disconnect();
                }
            }
        },configuration.getNoPongTimeout(),1000);

        pingTimer=new Timer();
        pingTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendPing();
            }
        },0,configuration.getPingTime());

    }

    private void sendPing() {
        if (mStompClient==null) return;
        Message message = new Message();
        message.setTime(System.currentTimeMillis());
        message.setMessage("non");
        message.setType(Message.PING);
        message.setMessageId(UUID.randomUUID().toString());
        mStompClient.send("/ping",parser.get(message).toString()).subscribe();
    }

    private void sendAckMessage(String messageId) {
        Message message = new Message();
        message.setTime(System.currentTimeMillis());
        message.setMessage("non");
        message.setType(Message.ACK);
        message.setMessageId(messageId);
        send(parser.get(message).toString());
    }

    public synchronized void disconnect(){
        try {
            Log.e("WebSocket","Disconnected");
            doDisconnect();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }



    private void doDisconnect() throws Exception {
        if (timeoutTimer!=null) timeoutTimer.cancel();
        timeoutTimer=null;

        if (pingTimer!=null) pingTimer.cancel();
        pingTimer=null;

        if (mStompClient!=null) mStompClient.disconnect();
        mStompClient=null;

        lastPongTimestamp=0;
        lastMessageTimestamp=0;
    }


    public void addMessageListener(MessageReceiver receiver){
        messageReceivers.add(receiver);
    }

    public void removeMessageListener(MessageReceiver receiver){
        messageReceivers.remove(receiver);
    }

    public void send(String message){
        if (mStompClient==null) return;
        mStompClient.send("/sendToUser",message).subscribe();
    }


}
