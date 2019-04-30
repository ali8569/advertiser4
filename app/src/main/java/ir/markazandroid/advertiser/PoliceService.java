package ir.markazandroid.advertiser;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import ir.markazandroid.advertiser.AdvertiserApplication;
import ir.markazandroid.advertiser.R;
import ir.markazandroid.advertiser.activity.MainActivity;
import ir.markazandroid.advertiser.activity.PoliceActivity;
import ir.markazandroid.advertiser.signal.Signal;
import ir.markazandroid.advertiser.signal.SignalManager;

public class PoliceService extends Service {

    private Timer timer;
    private boolean isStarted=false;
    private static final int ONGOING_NOTIFICATION_ID=123456;
    private SignalManager signalManager;
    private IBinder mBinder;

    public PoliceService() {
       mBinder = new MyBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        signalManager=((AdvertiserApplication)getApplication()).getSignalManager();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        doWork();
        return Service.START_STICKY;
    }

    private void doWork() {
        if (isStarted) return;

        Log.e("Service","started");


        timer=new Timer();

        setWorkingNotification();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                signalManager.sendMainSignal(new Signal(Signal.START_MAIN_ACTIVITY));
            }
        },0,10_000);

        isStarted=true;
    }

    private void setWorkingNotification() {
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this,getPackageName())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Police is Working...")
                .setTicker("Started Police Service")
                .setWhen(System.currentTimeMillis())
                .setOngoing(true);


        Intent manager = new Intent(this, PoliceActivity.class);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, manager, 0);
        notification.setContentIntent(resultPendingIntent);

        startForeground(ONGOING_NOTIFICATION_ID, notification.build());
    }

    public void stopAndRelease(){
        Log.e("Service","stoped");
        if (timer!=null)
            timer.cancel();

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this,getPackageName())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Police is Stopped...")
                .setTicker("Police is Stopped")
                .setWhen(System.currentTimeMillis())
                .setOngoing(true);
        Intent manager = new Intent(this, PoliceActivity.class);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, manager, 0);
        notification.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(ONGOING_NOTIFICATION_ID, notification.build());
        isStarted=false;
    }

    @Override
    public boolean stopService(Intent name) {
        stopAndRelease();
        return super.stopService(name);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopAndRelease();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class MyBinder extends Binder {
        public PoliceService getService() {
            return PoliceService.this;
        }
    }

    public boolean isStarted() {
        return isStarted;
    }
}
