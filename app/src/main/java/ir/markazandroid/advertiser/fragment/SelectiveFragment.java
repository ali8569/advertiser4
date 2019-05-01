package ir.markazandroid.advertiser.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import ir.markazandroid.advertiser.AdvertiserApplication;
import ir.markazandroid.advertiser.signal.Signal;

/**
 * Coded by Ali on 5/24/2018.
 */
public abstract class SelectiveFragment extends Fragment implements OnSelectListener {

    private boolean started = false;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((AdvertiserApplication) getActivity().getApplication()).getSignalManager().addReceiver(this::onSignal);
    }

    @Override
    public void onSelect() {
        Log.e("cycle " + toString(), "start");
        if (!started) {
            start();
            started = true;
        }
    }

    @Override
    public void onDeselect() {
        Log.e("cycle " + toString(), "stop");
        if (started) {
            stop();
            started = false;
        }
    }

    protected abstract void start();

    protected abstract void stop();

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    @Override
    public void onResume() {
        super.onResume();
        onSelect();
    }

    @Override
    public void onPause() {
        super.onPause();
        onDeselect();
    }

    protected void refresh() {
    }

    public boolean onSignal(Signal signal) {
        return signal.getType() == Signal.DOWNLOADER_FINISHED;
    }
}
