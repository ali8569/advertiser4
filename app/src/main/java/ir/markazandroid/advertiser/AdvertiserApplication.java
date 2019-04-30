package ir.markazandroid.advertiser;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.ndk.CrashlyticsNdk;
import com.danikula.videocache.HttpProxyCacheServer;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;
import ir.markazandroid.advertiser.activity.MainActivity;
import ir.markazandroid.advertiser.aidl.PoliceBridge;
import ir.markazandroid.advertiser.aidl.PoliceHandlerHelper;
import ir.markazandroid.advertiser.db.DataBase;
import ir.markazandroid.advertiser.downloader.AppUpdater;
import ir.markazandroid.advertiser.downloader.RecordDownloader;
import ir.markazandroid.advertiser.hardware.PortReader;
import ir.markazandroid.advertiser.network.JSONParser.Parser;
import ir.markazandroid.advertiser.network.NetworkClient;
import ir.markazandroid.advertiser.object.Content;
import ir.markazandroid.advertiser.object.ErrorObject;
import ir.markazandroid.advertiser.object.ExtrasObject;
import ir.markazandroid.advertiser.object.FieldError;
import ir.markazandroid.advertiser.object.Phone;
import ir.markazandroid.advertiser.object.Record;
import ir.markazandroid.advertiser.object.RecordOptions;
import ir.markazandroid.advertiser.object.User;
import ir.markazandroid.advertiser.object.Version;
import ir.markazandroid.advertiser.signal.Signal;
import ir.markazandroid.advertiser.signal.SignalManager;
import ir.markazandroid.advertiser.signal.SignalReceiver;
import ir.markazandroid.advertiser.util.PreferencesManager;
import ir.markazandroid.advertiser.util.Utils;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

import static android.content.pm.ApplicationInfo.FLAG_LARGE_HEAP;

/**
 * Coded by Ali on 01/02/2018.
 */

public class AdvertiserApplication extends Application implements SignalReceiver{

    public static final String SHARED_PREFRENCES = "shared_pref";

    private NetworkClient networkClient;
    private User user;
    private SignalManager signalManager;
    private Parser parser;
    private PreferencesManager preferencesManager;
    private DataBase dataBase;
    private HttpProxyCacheServer proxy;
    private RecordDownloader recordDownloader;
    private boolean isInternetConnected = false;
    private String frontActivity;
    private PortReader portReader;
    private PoliceBridge policeBridge;

    private boolean touchDisabled;

    public NetworkClient getNetworkClient(){
        if (networkClient==null){
            networkClient=new NetworkClient(this);
        }
        return networkClient;
    }

    public SignalManager getSignalManager() {
        if (signalManager == null) signalManager = new SignalManager(this);
        return signalManager;
    }

    public HttpProxyCacheServer getProxy() {
        if (proxy==null) {
            proxy = new HttpProxyCacheServer.Builder(this)
                    .build();
        }
        return proxy;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        getPoliceBridge();
        //installApp();
        Fabric.with(this, new Crashlytics(), new CrashlyticsNdk());
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/advertiser/image");
        myDir.mkdirs();
        Picasso picasso =new Picasso.Builder(this)
                .downloader(new OkHttp3Downloader(myDir,200_000_000))
                .build();
        Picasso.setSingletonInstance(picasso);
        getSignalManager().addReceiver(this);
        FirebaseMessaging.getInstance().subscribeToTopic("all");

        CalligraphyConfig.Builder config=new CalligraphyConfig.Builder()
                .setFontAttrId(R.attr.fontPath);

        if (!Locale.getDefault().getLanguage().equals("en")){
            config.setDefaultFontPath("font/BYekan.ttf");
        }

        CalligraphyConfig.initDefault(config.build());

        AppUpdater updater = new AppUpdater(this);
        updater.start();
        //updater.installApp();
        //updater.start();

        Intent intent = new Intent(this,KeepAliveService.class);
        startService(intent);



        //getPortReader().init();

        //TODO Port Reader
        getPortReader().start();

        if (getPreferencesManager().getArduinoOnOffTime()!=null){
            String command = getPreferencesManager().getArduinoOnOffTime();
            setArdunoTime(command);
        }


        //getSocketManager().connect();

        //locationMgr=new LocationMgr(this);
        //locationMgr.start();

        //sensorMeter=new SensorMeter(this);
        //sensorMeter.init();

       /* try {
            WebSockTest webSockTest = new WebSockTest(getParser(),getNetworkClient().getClient());
            webSockTest.send();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }*/
    }

    private boolean setArdunoTime(String command){
        //17:0:9:0
        String[] times = command.split(":");
        Calendar calendar = Calendar.getInstance();
        int nowHour = calendar.get(Calendar.HOUR_OF_DAY);
        int nowMinute= calendar.get(Calendar.MINUTE);
        int now = Integer.parseInt(String.format(Locale.US,"%02d%02d",nowHour,nowMinute));
        int off = Integer.parseInt(times[0]+times[1]);
        int on = Integer.parseInt(times[2]+times[3]);

        if (on==off && on==0){
            return getPortReader().write(generateArduinoTime(command));
        }
        if (on==off) return false;

        if ((off<on && now >= off && now < on) || (off>on && !(now >= on && now < off))) {
            int rem = 60-nowMinute;
            if (rem<=5){
                nowHour+=1;
                nowMinute=5-rem;
            }
            else nowMinute+=5;

            if (Integer.parseInt(String.format(Locale.US,"%02d%02d",nowHour,nowMinute))>=on && Integer.parseInt(String.format(Locale.US,"%02d%02d",nowHour,nowMinute))-on<6){
                return getPortReader().write(generateArduinoTime(command));
            }
            command=nowHour+":"+nowMinute+":"+times[2]+":"+times[3];
            new Handler(getMainLooper()).post(()->Toast.makeText(this,"Off Time, turning off 5 minute",Toast.LENGTH_LONG).show());
            return getPortReader().write(generateArduinoTime(command));

        }
        return getPortReader().write(generateArduinoTime(command));
    }


    private String generateArduinoTime(String onOffTime){
        return Utils.getNowForArduino() + onOffTime+"#";
    }


    static int calculateMemoryCacheSize(Context context) {
        ActivityManager am = getService(context, ACTIVITY_SERVICE);
        boolean largeHeap = (context.getApplicationInfo().flags & FLAG_LARGE_HEAP) != 0;
        int memoryClass = largeHeap ? am.getLargeMemoryClass() : am.getMemoryClass();
        // Target ~15% of the available heap.
        Log.e("MMCache:", (1024L * 1024L * memoryClass / 6)+"");
        return (int) (1024L * 1024L * memoryClass / 6);
    }

    static <T> T getService(Context context, String service) {
        return (T) context.getSystemService(service);
    }

    public Parser getParser() {
        if (parser==null) {
            try {
                parser = new Parser();
                parser.addClass(ErrorObject.class);
                parser.addClass(Record.class);
                parser.addClass(Record.Image.class);
                parser.addClass(Record.SubTitle.class);
                parser.addClass(User.class);
                parser.addClass(Phone.class);
                parser.addClass(FieldError.class);
                parser.addClass(Version.class);
                parser.addClass(RecordOptions.class);
                parser.addClass(ExtrasObject.class);
                parser.addSubClass(Content.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

        }
        return parser;
    }

    public DataBase getDataBase(){
        if (dataBase==null) dataBase=new DataBase(this);
        return dataBase;
    }

    @Override
    public boolean onSignal(Signal signal) {
        if (signal.getType() == Signal.SIGNAL_LOGIN) {
           // setUser((User) signal.getExtras());
            Log.e(AdvertiserApplication.this.toString(), "login signal received " /*+ user.getUsername()*/);
            return true;
        } else if (signal.getType() == Signal.SIGNAL_LOGOUT) {
            Log.e(AdvertiserApplication.this.toString(), "logout signal received ");
            getNetworkClient().deleteCookies();
            DeleteToken deleteToken = new DeleteToken();
            deleteToken.execute();
            setUser(null);
            return true;
        }
        else if (signal.getType()==Signal.START_MAIN_ACTIVITY){
            if (getFrontActivity()==null) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
        else if (signal.getType()==Signal.OPEN_SOCKET_HEADER_RECEIVED){
        }else if (signal.getType()==Signal.DOWNLOADER_NO_NETWORK){
            isInternetConnected=false;
        }else if (signal.getType()==Signal.DOWNLOADER_NETWORK){
            isInternetConnected=true;
        }
        return false;
    }

    public PreferencesManager getPreferencesManager() {
        if (preferencesManager==null) preferencesManager=new PreferencesManager(getSharedPreferences(SHARED_PREFRENCES,MODE_PRIVATE));
        return preferencesManager;
    }

    public RecordDownloader getRecordDownloader() {
        if (recordDownloader==null){
            recordDownloader=new RecordDownloader(this);
        }
        return recordDownloader;
    }

    public boolean isInternetConnected() {
        return isInternetConnected;
    }


    public String getFrontActivity() {
        return frontActivity;
    }

    public void setFrontActivity(String frontActivity) {
        this.frontActivity = frontActivity;
    }


    public PortReader getPortReader() {
        if (portReader==null) portReader=new PortReader(this);
        return portReader;
    }

    public boolean isTouchDisabled() {
        return touchDisabled;
    }

    public void setTouchDisabled(boolean touchDisabled) {
        this.touchDisabled = touchDisabled;
    }

    public PoliceBridge getPoliceBridge() {
        if (policeBridge==null){
            policeBridge=new PoliceHandlerHelper(this,getSignalManager());
        }
        return policeBridge;
    }

    private static class DeleteToken extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                FirebaseInstanceId.getInstance().deleteInstanceId();
                FirebaseInstanceId.getInstance().getToken();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    protected void finalize() {
        getSignalManager().removeReceiver(this);
        if (portReader!=null)
            portReader.close();
    }


    public void installApp(){
        //Log.e("Neshoooon bede","Done");
        try {
            Log.e("Done","Done");
            Process proc = Runtime.getRuntime().exec("su ls");
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
           // BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
          //  writer.write("");//;su monkey -p ir.markazandroid.advertiser -c android.intent.category.LAUNCHER 1
            //writer.flush();
            String s;
            while ((s=reader.readLine())!=null){
                Log.e("Console out","out= "+s);
            }
            //proc.waitFor();
            proc.waitFor();
            reader.close();
            //Toast.makeText(this,"Bars are enabled",Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("Console Error","out= "+e.getMessage());
            e.printStackTrace();
        }
    }
}
