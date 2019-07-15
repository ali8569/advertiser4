package ir.markazandroid.advertiser.event;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import ir.markazandroid.advertiser.AdvertiserApplication;
import ir.markazandroid.advertiser.signal.Signal;
import ir.markazandroid.advertiser.signal.SignalManager;

public class PoliceEventReceiver extends BroadcastReceiver {

    private Context context;

    public static final String EVENT_TYPE_DEVICE_AUTHENTICATED = "EVENT_TYPE_DEVICE_AUTHENTICATED";
    public static final String EVENT_TYPE_NAME = "ir.markazandroid.police.event.BaseEvent.EVENT_TYPE_NAME";

    public static final String EVENT_TYPE_MIRROR_BLOCK = "EVENT_TYPE_MIRROR_BLOCK";
    public static final String EVENT_TYPE_MIRROR_UNBLOCK = "EVENT_TYPE_MIRROR_UNBLOCK";

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        Log.e("Event", intent.toString());
        switch (intent.getStringExtra(EVENT_TYPE_NAME)) {
            case EVENT_TYPE_DEVICE_AUTHENTICATED:
                onDeviceAuthenticatedEvent();
                break;

            case EVENT_TYPE_MIRROR_BLOCK:
                onMirrorBlockEvent();
                break;

            case EVENT_TYPE_MIRROR_UNBLOCK:
                onMirrorUnBlockEvent();
                break;
        }
    }

    private void onDeviceAuthenticatedEvent() {
        getSignalManager().sendMainSignal(new Signal(Signal.SIGNAL_DEVICE_AUTHENTICATED));
    }

    private void onMirrorBlockEvent() {
        Signal signal = new Signal("screen block", Signal.SIGNAL_SCREEN_BLOCK);
        getSignalManager().sendMainSignal(signal);
    }

    private void onMirrorUnBlockEvent() {
        Signal signal = new Signal("screen unblock", Signal.SIGNAL_SCREEN_UNBLOCK);
        getSignalManager().sendMainSignal(signal);
    }

    private SignalManager getSignalManager() {
        return ((AdvertiserApplication) context.getApplicationContext()).getSignalManager();
    }

}
