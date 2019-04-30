package ir.markazandroid.advertiser;

import android.app.ActivityManager;
import android.app.Application;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
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

import org.json.JSONException;
import org.json.JSONObject;

import io.fabric.sdk.android.Fabric;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import ir.markazandroid.advertiser.activity.MainActivity;
import ir.markazandroid.advertiser.activity.authentication.LoginActivity;
import ir.markazandroid.advertiser.db.DataBase;
import ir.markazandroid.advertiser.downloader.AppUpdater;
import ir.markazandroid.advertiser.downloader.RecordDownloader;
import ir.markazandroid.advertiser.hardware.PortReader;
import ir.markazandroid.advertiser.hardware.SensorMeter;
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
import ir.markazandroid.advertiser.object.WebSocketConfiguration;
import ir.markazandroid.advertiser.signal.Signal;
import ir.markazandroid.advertiser.signal.SignalManager;
import ir.markazandroid.advertiser.signal.SignalReceiver;
import ir.markazandroid.advertiser.util.LocationMgr;
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
    private SensorMeter sensorMeter;
    private LocationMgr locationMgr;
    private String frontActivity;
    private WebSockTest socketManager;
    private SocketMessageController socketMessageController;
    private Console console;
    private PortReader portReader;

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

    public LocationMgr getLocationMgr() {
        return locationMgr;
    }

    @Override
    public void onCreate() {
        super.onCreate();
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

        Intent intent = new Intent(this,PoliceService.class);
        startService(intent);

        getSocketManager().addMessageListener(this::onSocketMessage);

        //getPortReader().init();

        //TODO Port Reader
        getPortReader().start();

        if (getPreferencesManager().getArduinoOnOffTime()!=null){
            String command = getPreferencesManager().getArduinoOnOffTime();
            setArdunoTime(command);
        }

        for (String cmd:getPreferencesManager().getStartupCommands()){
            Message message = new Message();
            message.setMessage(cmd);
            message.setMessageId("STARTUP");
            message.setType("STARTUP");
            message.setTime(System.currentTimeMillis());
            onSocketMessage(message);
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
        int now = Integer.parseInt(String.format("%02d%02d",nowHour,nowMinute));
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

            if (Integer.parseInt(String.format("%02d%02d",nowHour,nowMinute))>=on && Integer.parseInt(String.format("%02d%02d",nowHour,nowMinute))-on<6){
                return getPortReader().write(generateArduinoTime(command));
            }
            command=nowHour+":"+nowMinute+":"+times[2]+":"+times[3];
            new Handler(getMainLooper()).post(()->Toast.makeText(this,"Off Time, turning off 5 minute",Toast.LENGTH_LONG).show());
            return getPortReader().write(generateArduinoTime(command));

        }
        return getPortReader().write(generateArduinoTime(command));
    }

    public static void main(String[] args) {
        //17:0:9:0
        String[] times = "17:00:17:36".split(":");
        Calendar calendar = Calendar.getInstance();
        int nowHour = 17 /*calendar.get(Calendar.HOUR_OF_DAY)*/;
        int nowMinute= 30 /*calendar.get(Calendar.MINUTE)*/;
        int now = Integer.parseInt(String.format("%02d%02d",nowHour,nowMinute));
        int off = Integer.parseInt(times[0]+times[1]);
        int on = Integer.parseInt(times[2]+times[3]);

        if (on==off && on==0){
            System.out.println("ds");
            return;
        }
        if (on==off){
            System.out.println("dss");
            return;
        };

        if ((off<on && now >= off && now < on) || (off>on && !(now >= on && now < off))) {
            int rem = 60-nowMinute;
            if (rem<=5){
                nowHour+=1;
                nowMinute=5-rem;
            }
            else nowMinute+=5;

            if (Integer.parseInt(String.format("%02d%02d",nowHour,nowMinute))>=on && Integer.parseInt(String.format("%02d%02d",nowHour,nowMinute))-on<6){
                System.out.println(on);
                System.out.println(Integer.parseInt(String.format("%02d%02d",nowHour,nowMinute)));
                System.out.println("default");
                return;
            }
            System.out.println("offTime");
            System.out.println("off");
            return;

            //command=nowHour+":"+nowMinute+":"+times[2]+":"+times[3];
            //new Handler(getMainLooper()).post(()->Toast.makeText(this,"Off Time, turning off 5 minute",Toast.LENGTH_LONG).show());

        }
        System.out.println("default2");

    }

    public void onSocketMessage(Message message){

        if (message.getMessage()==null) return;

        if (message.getMessage().startsWith("terminal ")){
            Message outputMessage = new Message();
            outputMessage.setMessageId(message.getMessageId());
            outputMessage.setType(Message.RESPONSE);
            getConsole().executeAsync(message.getMessage().substring("terminal ".length(), message.getMessage().length())
                    , (resultCode, output) -> {
                        outputMessage.setTime(System.currentTimeMillis());
                        outputMessage.setMessage("Process exit code="+resultCode+"\r\n"+output);
                        getSocketMessageController().sendOutMessage(outputMessage);
                    });
        }
        else if (message.getMessage().startsWith("arduino ")){
            //T:HH:MM:SS:DD:MM:YY:HH:MM:HH:MM#
            String command = message.getMessage().substring("arduino ".length(), message.getMessage().length());
            if(command.startsWith("setTime ")){
                String onOffTime = command.substring("setTime ".length(), command.length());
                //command = generateArduinoTime(onOffTime);
                getPreferencesManager().setArduinoOnOffTime(onOffTime);
                Message outputMessage = new Message();
                outputMessage.setMessageId(message.getMessageId());
                outputMessage.setType(Message.RESPONSE);
                outputMessage.setSuccess(setArdunoTime(onOffTime));
                outputMessage.setTime(System.currentTimeMillis());
                outputMessage.setMessage("non");
                getSocketMessageController().sendOutMessage(outputMessage);
            }

        }
        else if (message.getMessage().startsWith("system ")){
            String command = message.getMessage().substring("system ".length(), message.getMessage().length());
            Message outputMessage = new Message();
            outputMessage.setMessageId(message.getMessageId());
            outputMessage.setType(Message.RESPONSE);
            outputMessage.setTime(System.currentTimeMillis());
            outputMessage.setMessage("non");
            outputMessage.setSuccess(true);

            if (command.startsWith("set ")){
                String ts = command.substring("set ".length(), command.length());
                getPreferencesManager().addStartupCommand(ts);
                outputMessage.setMessage("Done");
            }
            else if (command.startsWith("unset ")){
                String ts = command.substring("unset ".length(), command.length());
                getPreferencesManager().removeStartupCommand(ts);
                outputMessage.setMessage("Done");
            }
            else{
                switch (command){
                    case "disconnect": getSocketManager().disconnect(); break;
                    case "disable input": getPreferencesManager().addStartupCommand("system disable input"); disableInput(); break;
                    case "enable input": getPreferencesManager().removeStartupCommand("system disable input"); enableInput(); break;
                    default:
                        outputMessage.setSuccess(false);
                        outputMessage.setMessage("Unknown System Command \""+command+"\"");
                }
            }
            getSocketMessageController().sendOutMessage(outputMessage);
        }

        else{
            Message outputMessage = new Message();
            outputMessage.setMessageId(message.getMessageId());
            outputMessage.setType(Message.RESPONSE);
            outputMessage.setTime(System.currentTimeMillis());
            switch (message.getMessage()){
                case "hi":
                    outputMessage.setMessage("Hi"); break;

                case "ready":
                    outputMessage.setMessage("YESSIR"); break;

                default:
                    outputMessage.setMessage("Unknown Message \""+message.getMessage()+"\""); break;

            }
            //T:HH:MM:SS:DD:MM:YY:HH:MM:HH:MM#
            getSocketMessageController().sendOutMessage(outputMessage);

        }
    }

    private void enableInput() {
        //just reboot
        delayedReboot();

    }

    private void delayedReboot(){
        //String cmd = "rm /dev/input/*";
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                getConsole().write("reboot");
            }
        },120_000);
        new Handler(getMainLooper()).post(()->Toast.makeText(this,"rebooting in 2 minutes",Toast.LENGTH_LONG).show());

    }

    private void disableInput() {

        //String cmd = "rm /dev/input/*";
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                getConsole().write("rm /dev/input/*");
                new Handler(getMainLooper()).post(()->Toast.makeText(AdvertiserApplication.this,"touch disabled",Toast.LENGTH_LONG).show());
                setTouchDisabled(true);
            }
        },120_000);
        new Handler(getMainLooper()).post(()->Toast.makeText(this,"Disabling touch in 2 minutes",Toast.LENGTH_LONG).show());
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
                parser.addClass(Message.class);
                parser.addSubClass(WebSocketConfiguration.class);
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
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
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
            getSocketManager().connect();
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

    public WebSockTest getSocketManager() {
        if (socketManager==null) {
            socketManager=new WebSockTest(getNetworkClient(), parser);
        }
        return socketManager;
    }

    public SocketMessageController getSocketMessageController() {
        if (socketMessageController==null) socketMessageController= new SocketMessageController(getSocketManager(),getParser());
        return socketMessageController;
    }

    public Console getConsole() {
        if (console==null) console=new Console();
        return console;
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
        if (sensorMeter!=null)
            sensorMeter.close();
        if (locationMgr!=null)
            locationMgr.stop();
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
