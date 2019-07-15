package ir.markazandroid.advertiser;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import ir.markazandroid.advertiser.activity.KeepAliveActivity;
import ir.markazandroid.advertiser.signal.Signal;
import ir.markazandroid.advertiser.signal.SignalManager;
import ir.markazandroid.advertiser.signal.SignalReceiver;

public class KeepAliveService extends Service implements SignalReceiver {

    private Timer timer;
    private boolean isStarted=false;
    private static final int ONGOING_NOTIFICATION_ID=123456;
    private SignalManager signalManager;
    private IBinder mBinder;

    public KeepAliveService() {
       mBinder = new MyBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        signalManager=((AdvertiserApplication)getApplication()).getSignalManager();
        signalManager.addReceiver(this);
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


        Intent manager = new Intent(this, KeepAliveActivity.class);

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
        Intent manager = new Intent(this, KeepAliveActivity.class);

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
        signalManager.removeReceiver(this);
        stopAndRelease();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onSignal(Signal signal) {
        if (signal.getType() == Signal.SIGNAL_DISABLE_KEEP_ALIVE)
            stopAndRelease();
        return true;
    }

    public class MyBinder extends Binder {
        public KeepAliveService getService() {
            return KeepAliveService.this;
        }
    }

    public boolean isStarted() {
        return isStarted;
    }
}
