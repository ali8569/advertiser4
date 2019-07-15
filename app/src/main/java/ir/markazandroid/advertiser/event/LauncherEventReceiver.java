package ir.markazandroid.advertiser.event;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import ir.markazandroid.advertiser.AdvertiserApplication;
import ir.markazandroid.advertiser.signal.Signal;
import ir.markazandroid.advertiser.signal.SignalManager;

public class LauncherEventReceiver extends BroadcastReceiver {

    private Context context;

    public static final String EVENT_TYPE_NAME = "ir.markazandroid.launcher.event.BaseEvent.EVENT_TYPE_NAME";

    public static final String EVENT_TYPE_LAUNCHING_3PARTY_APP = "EVENT_TYPE_LAUNCHING_3PARTY_APP";

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        Log.e("Event", intent.toString());
        switch (intent.getStringExtra(EVENT_TYPE_NAME)) {
            case EVENT_TYPE_LAUNCHING_3PARTY_APP:
                launching3partyApp();
                break;
        }
    }

    private void launching3partyApp() {
        getSignalManager().sendMainSignal(new Signal(Signal.SIGNAL_LAUNCHING_3PARTY_APP));
    }

    private SignalManager getSignalManager() {
        return ((AdvertiserApplication) context.getApplicationContext()).getSignalManager();
    }

}
