package ir.markazandroid.advertiser.downloader;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Timer;
import java.util.TimerTask;

import ir.markazandroid.advertiser.AdvertiserApplication;
import ir.markazandroid.advertiser.BuildConfig;
import ir.markazandroid.advertiser.Console;
import ir.markazandroid.advertiser.Manifest;
import ir.markazandroid.advertiser.network.JSONParser.Parser;
import ir.markazandroid.advertiser.network.NetStatics;
import ir.markazandroid.advertiser.object.Version;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Coded by Ali on 5/24/2018.
 */
public class AppUpdater {
    private Context context;
    private Timer timer;
    private OkHttpClient client;
    private Parser parser;
    private Handler handler;
    private boolean firstTime=true;
    private boolean isDownloading;

    public AppUpdater(Context context) {
        this.context = context;
        this.client = ((AdvertiserApplication)context).getNetworkClient().getClient();
        this.parser=  ((AdvertiserApplication)context).getParser();
        handler=new Handler(context.getMainLooper());
    }

    public void start(){
        stop();
        timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                makeRequest();
                firstTime=false;
            }
        },0,60*1000);
    }

    private void makeRequest() {
        if (!isNetworkConnected() && !firstTime) return;
        if (isDownloading) return;
        Request request = new Request.Builder()
                .url(NetStatics.VERSION)
                .get()
                .build();
        Response response=null;
        try {
            response = client.newCall(request).execute();
            Version version = parser.get(Version.class,new JSONObject(response.body().string()));
            if (version.getVersion()> BuildConfig.VERSION_CODE) {
                downloadNewApp(version.getUrl());
            }
        } catch (Exception e) {
            isDownloading=false;
            e.printStackTrace();
        }finally {
            if (response!=null)
                response.close();
        }
    }

    private void downloadNewApp(String url) throws Exception {
        isDownloading=true;
        handler.post(() -> Toast.makeText(context,"نسخه جدید برنامه موجود است. درحال دانلود...",Toast.LENGTH_LONG).show());
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Response response=null;
        try {
            response = client.newCall(request).execute();
            saveAndInstall(response.body().byteStream());
        } finally {
            if (response!=null)
                response.close();
        }
    }

    private void saveAndInstall(InputStream inputStream) throws Exception {
        File dest = new File(Environment.getExternalStorageDirectory()+"/advertiser/app.apk");
        try {
            FileUtils.forceDelete(dest);
        }catch (Exception e){}
        FileUtils.copyToFile(inputStream,dest);
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(
                Uri.fromFile(dest),
                //FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".downloader.AppUpdater$GenericFileProvider", dest),
                "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        handler.post(() -> {
            installUpdate();
            //context.startActivity(intent);
            stop();
        });
    }

    public void stop() {
        if (timer!=null) timer.cancel();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        stop();
    }

    private boolean isNetworkConnected(){
        return ((AdvertiserApplication) context.getApplicationContext()).isInternetConnected();
    }

    private boolean isTouchDisabled(){
        return ((AdvertiserApplication) context.getApplicationContext()).isTouchDisabled();
    }

    private void installUpdate() {
        String path = "/storage/emulated/0/advertiser/app.apk";
        File file = new File("/storage/emulated/0/advertiser/app.apk");
        if (!file.exists())
            path="/storage/emulated/legacy/advertiser/app.apk";

        Console console = new Console();
        //console.start();
        if (isTouchDisabled())
            console.update("pm install -d -r "+path+";reboot");
        else
            console.update("pm install -d -r "+path+";su -e monkey -p ir.markazandroid.advertiser -c android.intent.category.LAUNCHER 1");
        //console.write("");

    }

    public static class GenericFileProvider extends FileProvider {

    }



}
