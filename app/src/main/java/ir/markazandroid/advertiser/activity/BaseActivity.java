package ir.markazandroid.advertiser.activity;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import com.danikula.videocache.HttpProxyCacheServer;

import ir.markazandroid.advertiser.AdvertiserApplication;
import ir.markazandroid.advertiser.db.DataBase;
import ir.markazandroid.advertiser.downloader.RecordDownloader;
import ir.markazandroid.advertiser.network.NetworkManager;
import ir.markazandroid.advertiser.network.NetworkMangerImp;
import ir.markazandroid.advertiser.signal.Signal;
import ir.markazandroid.advertiser.signal.SignalManager;
import ir.markazandroid.advertiser.util.PreferencesManager;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Coded by Ali on 02/02/2018.
 */

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((AdvertiserApplication)getApplicationContext()).setFrontActivity(getClass().getName());
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((AdvertiserApplication)getApplicationContext()).setFrontActivity(null);
    }

    PreferencesManager getPreferencesManager(){
        return  ((AdvertiserApplication) getApplication()).getPreferencesManager();
    }

    private NetworkManager networkManager;


    protected NetworkManager getNetworkManager() {
        if (networkManager==null){
            networkManager= new NetworkMangerImp.NetworkManagerBuilder()
                    .from(this)
                    .tag(toString())
                    .build();
        }
        return networkManager;
    }

    protected HttpProxyCacheServer getProxy(){
        return ((AdvertiserApplication) getApplication()).getProxy();
    }

    protected SignalManager getSignalManager(){
        return ((AdvertiserApplication) getApplication()).getSignalManager();
    }

    protected DataBase getDataBase(){
        return ((AdvertiserApplication) getApplication()).getDataBase();
    }

    protected RecordDownloader getRecorddownloader(){
        return ((AdvertiserApplication) getApplication()).getRecordDownloader();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        getSignalManager().sendSignal(new Signal(toString(), Signal.SIGNAL_ACTIVITY_DESTROYED));
    }
}
