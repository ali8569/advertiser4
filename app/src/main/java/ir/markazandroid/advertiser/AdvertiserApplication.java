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

import java.io.File;
import java.io.IOException;

import io.fabric.sdk.android.Fabric;
import ir.markazandroid.advertiser.activity.GoldActivity;
import ir.markazandroid.advertiser.activity.MainActivity;
import ir.markazandroid.advertiser.activity.authentication.LoginActivity;
import ir.markazandroid.advertiser.db.DataBase;
import ir.markazandroid.advertiser.downloader.AppUpdater;
import ir.markazandroid.advertiser.downloader.GoldDownloader;
import ir.markazandroid.advertiser.downloader.RecordDownloader;
import ir.markazandroid.advertiser.hardware.SensorMeter;
import ir.markazandroid.advertiser.network.JSONParser.Parser;
import ir.markazandroid.advertiser.network.NetworkClient;
import ir.markazandroid.advertiser.object.Content;
import ir.markazandroid.advertiser.object.ErrorObject;
import ir.markazandroid.advertiser.object.ExtrasObject;
import ir.markazandroid.advertiser.object.FieldError;
import ir.markazandroid.advertiser.object.GoldEntity;
import ir.markazandroid.advertiser.object.GoldListContainer;
import ir.markazandroid.advertiser.object.Phone;
import ir.markazandroid.advertiser.object.Record;
import ir.markazandroid.advertiser.object.RecordOptions;
import ir.markazandroid.advertiser.object.User;
import ir.markazandroid.advertiser.object.Version;
import ir.markazandroid.advertiser.signal.Signal;
import ir.markazandroid.advertiser.signal.SignalManager;
import ir.markazandroid.advertiser.signal.SignalReceiver;
import ir.markazandroid.advertiser.util.LocationMgr;
import ir.markazandroid.advertiser.util.PreferencesManager;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

import static android.content.pm.ApplicationInfo.FLAG_LARGE_HEAP;

/**
 * Coded by Ali on 01/02/2018.
 */

public class AdvertiserApplication extends Application implements SignalReceiver {

    public static final String SHARED_PREFRENCES = "shared_pref";

    private NetworkClient networkClient;
    private User user;
    private SignalManager signalManager;
    private Parser parser;
    private PreferencesManager preferencesManager;
    private DataBase dataBase;
    private HttpProxyCacheServer proxy;
    private RecordDownloader recordDownloader;
    private GoldDownloader goldDownloader;
    private boolean isInternetConnected = false;
    private SensorMeter sensorMeter;
    private LocationMgr locationMgr;
    private String frontActivity;
    private boolean igonrePoliceSignal = false;
    private boolean isGoldShowing = false;
    private String goldTitle;

    public NetworkClient getNetworkClient() {
        if (networkClient == null) {
            networkClient = new NetworkClient(this);
        }
        return networkClient;
    }

    public SignalManager getSignalManager() {
        if (signalManager == null) signalManager = new SignalManager(this);
        return signalManager;
    }

    public HttpProxyCacheServer getProxy() {
        if (proxy == null) {
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

    public LocationMgr getLocationMgr() {
        return locationMgr;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        //if (true) throw new RuntimeException();
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/advertiser/image");
        myDir.mkdirs();
        Picasso picasso = new Picasso.Builder(this)
                .downloader(new OkHttp3Downloader(myDir, 200_000_000))
                .build();
        Picasso.setSingletonInstance(picasso);
        getSignalManager().addReceiver(this);
        FirebaseMessaging.getInstance().subscribeToTopic("all");

        CalligraphyConfig.Builder config = new CalligraphyConfig.Builder()
                .setFontAttrId(R.attr.fontPath);
        config.setDefaultFontPath("font/BYekan.ttf");

        CalligraphyConfig.initDefault(config.build());

        try {
            AppUpdater updater = new AppUpdater(this);
            updater.start();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(this, PoliceService.class);
        startService(intent);

        //locationMgr=new LocationMgr(this);
        //locationMgr.start();

        //sensorMeter=new SensorMeter(this);
        //sensorMeter.init();

        //Console console = new Console();
        //console.start();
        //console.write("pm install -d -r /storage/emulated/0/advertiser/app.apk;su -e monkey -p ir.markazandroid.advertiser -c android.intent.category.LAUNCHER 1");
        //console.write("ls");
        //console.write("top");
    }

    static int calculateMemoryCacheSize(Context context) {
        ActivityManager am = getService(context, ACTIVITY_SERVICE);
        boolean largeHeap = (context.getApplicationInfo().flags & FLAG_LARGE_HEAP) != 0;
        int memoryClass = largeHeap ? am.getLargeMemoryClass() : am.getMemoryClass();
        // Target ~15% of the available heap.
        Log.e("MMCache:", (1024L * 1024L * memoryClass / 6) + "");
        return (int) (1024L * 1024L * memoryClass / 6);
    }

    static <T> T getService(Context context, String service) {
        return (T) context.getSystemService(service);
    }

    public Parser getParser() throws NoSuchMethodException {
        if (parser == null) {
            parser = new Parser();
            parser.addClass(Record.class);
            parser.addClass(Record.Image.class);
            parser.addClass(Record.SubTitle.class);
            parser.addClass(User.class);
            parser.addClass(Phone.class);
            parser.addClass(ErrorObject.class);
            parser.addClass(FieldError.class);
            parser.addClass(Version.class);
            parser.addClass(RecordOptions.class);
            parser.addClass(ExtrasObject.class);
            parser.addSubClass(Content.class);
            parser.addClass(GoldEntity.class);
            parser.addClass(GoldListContainer.class);
        }
        return parser;
    }

    public DataBase getDataBase() {
        if (dataBase == null) dataBase = new DataBase(this);
        return dataBase;
    }

    @Override
    public boolean onSignal(Signal signal) {
        if (signal.getType() == Signal.SIGNAL_LOGIN) {
            // setUser((User) signal.getExtras());
            Log.e(AdvertiserApplication.this.toString(), "login signal recived " /*+ user.getUsername()*/);
            return true;
        } else if (signal.getType() == Signal.SIGNAL_LOGOUT) {
            Log.e(AdvertiserApplication.this.toString(), "logout signal recived ");
            getNetworkClient().deleteCookies();
            DeleteToken deleteToken = new DeleteToken();
            deleteToken.execute();
            setUser(null);
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        } else if (signal.getType() == Signal.START_MAIN_ACTIVITY) {
            if (getFrontActivity() == null && !igonrePoliceSignal) {
                Intent intent = new Intent(this, isGoldShowing ? GoldActivity.class : MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
        if (signal.getType() == Signal.DOWNLOADER_NO_NETWORK) {
            isInternetConnected = false;
        } else if (signal.getType() == Signal.DOWNLOADER_NETWORK) {
            isInternetConnected = true;
        } else if ((signal.getType() & Signal.ENABLE_POLICE) != 0) {
            Log.e(AdvertiserApplication.this.toString(), "enable police signal " /*+ user.getUsername()*/);
            igonrePoliceSignal = false;
        } else if ((signal.getType() & Signal.DISABLE_POLICE) != 0) {
            Log.e(AdvertiserApplication.this.toString(), "disable police signal " /*+ user.getUsername()*/);
            igonrePoliceSignal = true;
        } else if (signal.getType() == Signal.SIGNAL_SCREEN_BLOCK) {
            showGold();
            return true;
        } else if (signal.getType() == Signal.SIGNAL_SCREEN_UNBLOCK) {
            hideGold();
            return true;
        } else if (signal.getType() == Signal.GOLD_TITLE_LOADED) {
            goldTitle = (String) signal.getExtras();
            return true;
        }

        return false;

    }

    private void hideGold() {
        isGoldShowing = false;

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void showGold() {
        isGoldShowing = true;

        Intent intent = new Intent(this, GoldActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public PreferencesManager getPreferencesManager() {
        if (preferencesManager == null)
            preferencesManager = new PreferencesManager(getSharedPreferences(SHARED_PREFRENCES, MODE_PRIVATE));
        return preferencesManager;
    }

    public RecordDownloader getRecordDownloader() {
        if (recordDownloader == null) {
            recordDownloader = new RecordDownloader(this);
        }
        return recordDownloader;
    }

    public GoldDownloader getGoldDownloader() {
        if (goldDownloader == null) {
            goldDownloader = new GoldDownloader(this);
        }
        return goldDownloader;
    }

    public boolean isInternetConnected() {
        return isInternetConnected;
    }

    public SensorMeter getSensorMeter() {
        return sensorMeter;
    }

    public void setSensorMeter(SensorMeter sensorMeter) {
        this.sensorMeter = sensorMeter;
    }

    public String getFrontActivity() {
        return frontActivity;
    }

    public void setFrontActivity(String frontActivity) {
        this.frontActivity = frontActivity;
    }

    public String getGoldTitle() {
        return goldTitle;
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
        sensorMeter.close();
        locationMgr.stop();
    }
}
