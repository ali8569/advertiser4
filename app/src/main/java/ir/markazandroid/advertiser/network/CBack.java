package ir.markazandroid.advertiser.network;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import ir.markazandroid.advertiser.AdvertiserApplication;
import ir.markazandroid.advertiser.signal.Signal;
import ir.markazandroid.advertiser.signal.SignalManager;
import ir.markazandroid.advertiser.signal.SignalReceiver;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Coded by Ali on 01/04/2017.
 */
abstract class CBack implements Callback,SignalReceiver {

    private State state;
    private boolean isCancelled = false;
    private Context mContext;
    private Handler handler;
    private String tag;
    private boolean isRaw=false;


    public CBack(Context context, State state, String tag) {
        this.state = state;
        this.tag = tag;
        state.state = State.IS_RUNNING;
        mContext = context;
        handler = new Handler(context.getMainLooper());
        getSignalManager().addReceiver(this);
    }

    CBack(Context context, String tag) {
        mContext = context;
        handler = new Handler(context.getMainLooper());
        this.tag = tag;
        getSignalManager().addReceiver(this);
    }

    CBack(Context context, String tag,boolean isRaw) {
        this(context,tag);
        this.isRaw=isRaw;
    }

    public State getState() {
        return state;
    }


    public boolean isCancelled() {
        return isCancelled;
    }

    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }

    @Override
    public void onFailure(Call call, final IOException e) {
        getSignalManager().removeReceiver(this);
        if (!isCancelled) {
            try {
                fail(e);
            } finally {
                if (state != null)
                    state.state = State.FAILED;
                getSignalManager().sendMainSignal(new Signal("NO_NET", Signal.DOWNLOADER_NO_NETWORK));
                getSignalManager().sendMainSignal(new Signal("Failed", Signal.SIGNAL_RESPONSE));
                /*handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "اشکال در ارتصال به اینترنت.", Toast.LENGTH_LONG).show();
                    }
                });*/
            }
        }
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        getSignalManager().sendMainSignal(new Signal("NET", Signal.DOWNLOADER_NETWORK));
        /*for (String h:response.headers().names()){
            Log.e("Head","name:"+h+"  value="+response.header(h));
        }*/
        if (response.headers().names().contains("openSocket"))
            getSignalManager().sendMainSignal(new Signal("Open Socket", Signal.OPEN_SOCKET_HEADER_RECEIVED));
        try {
            if (!isCancelled) {
                getSignalManager().sendMainSignal(new Signal("Response", Signal.SIGNAL_RESPONSE));
                try {
                    if (!isSuccessfull(response.code())) return;
                    if(isRaw){
                        result(response.body().byteStream());
                        return;
                    }

                    String s = response.body().string();
                    Log.e("Response", s);
                    if (s.startsWith("{")) {
                        JSONObject object = new JSONObject(s);
                        if (!isSuccessfull(response.code(), object)) return;
                        result(object);
                    } else if (s.startsWith("[")){
                        JSONArray array = new JSONArray(s);
                        if (!isSuccessfull(response.code(), array)) return;
                        result(array);
                    }
                    result(s);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (state != null) {
                    state.state = State.DONE;
                }
            }
        } finally {
            getSignalManager().removeReceiver(this);
            response.close();
        }
    }

    @Override
    public boolean onSignal(Signal signal) {
        if ((signal.getType() & Signal.SIGNAL_VIEW_DESTROYED)!=0
                && signal.getMsg().equals(tag)){
            setCancelled(true);
            Log.d("CBack","request for "+signal.getMsg()+" callBack canceled.");
            return true;
        }
        return false;
    }

    public void result(JSONObject response) throws JSONException {
    }

    public void result(JSONArray response) throws JSONException {
    }

    public void result(String response) {
    }

    /**
     * the stream must be used in the current thread
     * because after this method returns the stream closes
     * @param responseBody
     */
    public void result(InputStream responseBody) {

    }

    public void fail(IOException e) {
    }

    public boolean isSuccessfull(int code, JSONObject response) {
        return true;
    }

    public boolean isSuccessfull(int code, JSONArray response) {
        return true;
    }

    public boolean isSuccessfull(int code) {
        if (code == 403 || code == 401) {
            getSignalManager().sendMainSignal(new Signal("Logout", Signal.SIGNAL_LOGOUT));
            Log.e(toString(), "logout signal sent");
            return false;
        }
        return true;
    }
    private SignalManager getSignalManager(){
        return ((AdvertiserApplication) mContext.getApplicationContext())
                .getSignalManager();
    }

    public boolean isRaw() {
        return isRaw;
    }

    public void setRaw(boolean raw) {
        isRaw = raw;
    }
}
