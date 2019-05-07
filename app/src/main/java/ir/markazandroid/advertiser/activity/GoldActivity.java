package ir.markazandroid.advertiser.activity;

import android.os.Bundle;
import android.util.Log;

import ir.markazandroid.advertiser.R;
import ir.markazandroid.advertiser.fragment.GoldViewerFragment;
import ir.markazandroid.advertiser.signal.Signal;
import ir.markazandroid.advertiser.signal.SignalReceiver;

public class GoldActivity extends BaseActivity implements SignalReceiver {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gold);
        getSignalManager().addReceiver(this);

        if (savedInstanceState == null)
            getSupportFragmentManager().beginTransaction().add(R.id.goldViewerFragmentContainer, new GoldViewerFragment()).commit();
    }

    @Override
    public boolean onSignal(Signal signal) {

        Log.i("Signal on MainActivity", signal.getMsg() == null ? "" : signal.getMsg());
        switch (signal.getType()) {
            case Signal.SIGNAL_LOGOUT:
                finish();
                return true;

        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getSignalManager().removeReceiver(this);
    }
}
