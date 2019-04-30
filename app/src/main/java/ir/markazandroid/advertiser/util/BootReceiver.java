package ir.markazandroid.advertiser.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ir.markazandroid.advertiser.PoliceService;

/**
 * Created by Ali on 23/01/2017.
 */
public class BootReceiver extends BroadcastReceiver {
    public static final int Starter_activity=1254786;
    @Override
    public void onReceive(Context context, Intent arg) {
        Intent intent = new Intent(context,PoliceService.class);
        context.startService(intent);
    }
}
