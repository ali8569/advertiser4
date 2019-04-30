package ir.markazandroid.advertiser.signal;

import android.content.Context;
import android.os.Handler;

import java.util.concurrent.ConcurrentLinkedQueue;

import ir.mirrajabi.persiancalendar.PersianCalendarView;

/**
 * Coded by Ali on 29/07/2017.
 */

public class SignalManager {

    private Handler handler;
    private ConcurrentLinkedQueue<SignalReceiver> receivers;

    public SignalManager(Context context) {
        receivers = new ConcurrentLinkedQueue<>();
        handler = new Handler(context.getMainLooper());
    }

    public synchronized void sendMainSignal(final Signal signal) {
        handler.post(() -> sendSignal(signal));
    }


    public synchronized boolean sendSignal(Signal signal) {
        boolean caught = false;
        for (SignalReceiver receiver : receivers) {
            try {
                if (receiver.onSignal(signal)) caught = true;
            } catch (Exception e) {
                e.printStackTrace();
                //receivers.remove(receiver);
            }
        }
        return caught;
    }

    public void addReceiver(SignalReceiver receiver) {
        receivers.add(receiver);
    }

    public void removeReceiver(SignalReceiver receiver) {
        receivers.remove(receiver);
    }


}
