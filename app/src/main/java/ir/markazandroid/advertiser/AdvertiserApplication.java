package ir.markazandroid.advertiser;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.danikula.videocache.HttpProxyCacheServer;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;
import ir.markazandroid.advertiser.activity.MainActivity;
import ir.markazandroid.advertiser.aidl.PoliceBridge;
import ir.markazandroid.advertiser.aidl.PoliceHandlerHelper;
import ir.markazandroid.advertiser.db.DataBase;
import ir.markazandroid.advertiser.downloader.RecordDownloader;
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
        Fabric.with(this, new Crashlytics());
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


        Intent intent = new Intent(this,KeepAliveService.class);
        startService(intent);



        //getPortReader().init();



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
