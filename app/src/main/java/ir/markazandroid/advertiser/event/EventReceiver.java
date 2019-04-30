package ir.markazandroid.advertiser.event;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import ir.markazandroid.advertiser.AdvertiserApplication;
import ir.markazandroid.advertiser.signal.Signal;
import ir.markazandroid.advertiser.signal.SignalManager;

public class EventReceiver extends BroadcastReceiver {

    private Context context;

    public static final String EVENT_TYPE_DEVICE_AUTHENTICATED="EVENT_TYPE_DEVICE_AUTHENTICATED";
    public static final String EVENT_TYPE_NAME="ir.markazandroid.police.event.BaseEvent.EVENT_TYPE_NAME";

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context=context;
        Log.e("Event",intent.toString());
        switch (intent.getStringExtra(EVENT_TYPE_NAME)){
            case EVENT_TYPE_DEVICE_AUTHENTICATED:
                onDeviceAuthenticatedEvent();break;
        }
    }

    private void onDeviceAuthenticatedEvent(){
        getSignalManager().sendMainSignal(new Signal(Signal.SIGNAL_DEVICE_AUTHENTICATED));
    }

    private SignalManager getSignalManager(){
        return ((AdvertiserApplication)context.getApplicationContext()).getSignalManager();
    }
}
