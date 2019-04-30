package ir.markazandroid.advertiser.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import ir.markazandroid.advertiser.R;
import ir.markazandroid.advertiser.network.OnResultLoaded;
import ir.markazandroid.advertiser.object.ErrorObject;
import ir.markazandroid.advertiser.object.Record;
import ir.markazandroid.advertiser.signal.Signal;
import ir.markazandroid.advertiser.signal.SignalReceiver;

public class UnAssignedActivity extends BaseActivity{

    Timer autoFetch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_un_assigned);
        startAutoFetch();
    }

    private void startAutoFetch() {

        autoFetch=new Timer();
        autoFetch.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                fetchAutoRecords();
            }
        },12000, 1500);
    }

    private void fetchAutoRecords() {
        getNetworkManager().getRecords(new OnResultLoaded.ActionListener<ArrayList<Record>>() {
            @Override
            public void onSuccess(final ArrayList<Record> result) {
                runOnUiThread(() -> {
                    finish();
                    Intent intent = new Intent(UnAssignedActivity.this,MainActivity.class);
                    startActivity(intent);
                });
            }

            @Override
            public void onError(final ErrorObject error) {
                runOnUiThread(() -> {
                    if (error.getStatus()==401){
                        getSignalManager().sendMainSignal(new Signal("Logout", Signal.SIGNAL_LOGOUT));
                        finish();
                    }
                });
            }

            @Override
            public void failed(Exception e) {
                /*runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                      //  loadRecord();
                    }
                });*/
                //e.printStackTrace();
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        autoFetch.cancel();
    }
}
