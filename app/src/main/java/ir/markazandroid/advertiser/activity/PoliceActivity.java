package ir.markazandroid.advertiser.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Button;

import ir.markazandroid.advertiser.PoliceService;
import ir.markazandroid.advertiser.R;

public class PoliceActivity extends BaseActivity implements ServiceConnection {

    private Button start,stop;
    private PoliceService policeService;
    private Intent starterIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_police);
        start=findViewById(R.id.start);
        stop=findViewById(R.id.stop);
        starterIntent=new Intent(this,PoliceService.class);

        start.setOnClickListener((v) -> {
            startService(starterIntent);
            start.setEnabled(false);
            stop.setEnabled(true);
        });

        stop.setOnClickListener((v) -> {
            if (policeService!=null){
                policeService.stopAndRelease();
                stop.setEnabled(false);
                start.setEnabled(true);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent= new Intent(this, PoliceService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        PoliceService.MyBinder b = (PoliceService.MyBinder) binder;
        policeService = b.getService();
        start.setEnabled(!policeService.isStarted());
        stop.setEnabled(policeService.isStarted());
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        policeService = null;
        start.setEnabled(false);
        stop.setEnabled(false);
    }
}
