package ir.markazandroid.advertiser.downloader;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ir.markazandroid.advertiser.object.GoldEntity;
import ir.markazandroid.advertiser.signal.Signal;

/**
 * Coded by Ali on 2/8/2019.
 */
public class GoldDownloader extends RecordDownloader {

    private List<GoldEntity> golds;
    private ArrayList<Link> goldLinks;
    private String goldDirectory;

    public GoldDownloader(Context context) {
        super(context);
    }


    @Override
    public void init() {
        setBoolsToStart();
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/advertiser/gold");
        goldDirectory = mkdirs(myDir.toString());
        tempDir = mkdirs(root + "/advertiser/goldtemp");

        processMonitor.onProcess("دریافت اطلاعات از پایگاه داده...");

        goldLinks = getLinksGold();

        start();
    }

    protected ArrayList<Link> getLinksGold() {
        ArrayList<Link> links = new ArrayList<>();
        if (golds == null) return links;
        for (GoldEntity url : golds) {
            links.add(getLink(url.getPhotoUrl(), goldDirectory));
        }
        return links;
    }

    protected void start() {
        DownloadRunnable downloadRunnable = new DownloadRunnable();
        executor.execute(downloadRunnable);
    }

    public void setGolds(List<GoldEntity> golds) {
        this.golds = golds;
    }

    protected class DownloadRunnable implements Runnable {

        @Override
        public void run() {
            try {
                processMonitor.onProcess("چک کردن فایل های دانلود شده...");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                checkNetFiles(goldLinks, goldDirectory);
                processMonitor.onProcess("بررسی فایل ها موفقیت آمیز بود.");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!isCancelled)
                    processMonitor.onFinish();
            } catch (Exception e) {
                e.printStackTrace();
                failed = true;
            } finally {
                isFinished = true;
                if (!isCancelled)
                    getSignalManager().sendMainSignal(new Signal("", Signal.GOLD_DOWNLOADER_FINISHED));
            }
        }
    }

}
