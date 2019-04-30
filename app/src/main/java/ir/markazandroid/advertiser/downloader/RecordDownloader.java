package ir.markazandroid.advertiser.downloader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import ir.markazandroid.advertiser.AdvertiserApplication;
import ir.markazandroid.advertiser.db.DataBase;
import ir.markazandroid.advertiser.object.Content;
import ir.markazandroid.advertiser.object.Record;
import ir.markazandroid.advertiser.signal.Signal;
import ir.markazandroid.advertiser.signal.SignalManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;

/**
 * Coded by Ali on 5/1/2018.
 */
public class RecordDownloader {

    public void setProcessMonitor(ProcessMonitor processMonitor) {
        this.processMonitor = processMonitor;
    }

    public void setRecord(Record record) {
        this.record = record;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public boolean isFailed() {
        return failed;
    }

    public interface ProcessMonitor{
        void onProcess(String status,int proccess);
        void onFinish();
    }

    private Context context;
    private Record record;
    private ProcessMonitor processMonitor;
    private boolean isCancelled=false;
    private String imageDirectory;
    private String videoDirectory;
    private String soundDirectory;
    private String userDirectory;
    private String content1Directory,content3Directory,content2Directory;
    private String tempDir;
    private ArrayList<Link> videos;
    private ArrayList<Link> sounds;
    private ArrayList<Link> images;
    private ArrayList<Link> content1,content2,content3;

    private boolean isFinished=true;
    private boolean failed=false;
    private ThreadPoolExecutor executor;

    public RecordDownloader(Context context) {
        this.context = context;
        executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }


    private String mkdirs(String path){
        File f = new File(path);
        f.mkdirs();
        return f.toString();
    }

    public void init() {
        failed=false;
        isFinished=false;
        isCancelled=false;
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/advertiser/record_"+record.getRecordId());
        imageDirectory=mkdirs(myDir.toString()+"/image");
        videoDirectory=mkdirs(myDir.toString()+"/video");
        soundDirectory=mkdirs(myDir.toString()+"/sound");
        content1Directory=mkdirs(myDir.toString()+"/content1");
        content2Directory=mkdirs(myDir.toString()+"/content2");
        content3Directory=mkdirs(myDir.toString()+"/content3");
        userDirectory=mkdirs(root+"/advertiser/userFiles");
        tempDir=mkdirs(root+"/advertiser/temp");
        processMonitor.onProcess("دریافت اطلاعات از پایگاه داده...",0);
        videos=getLinks(record.getVideoArray(),videoDirectory);
        sounds=getLinks(record.getSoundArray(),soundDirectory);
        images=getLinksImage(record.getPhotosArrayObject());

        content1=getLinksContent(record.getExtras().getContents1(),content1Directory);
        content2=getLinksContent(record.getExtras().getContents2(),content2Directory);
        content3=getLinksContent(record.getExtras().getContents3(),content3Directory);

        start();
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    private ArrayList<Link> getLinks(ArrayList<String> urls,String directory) {
        ArrayList<Link> links= new ArrayList<>();
        for (String url:urls){
            links.add(getLink(url,directory));
        }
        return links;
    }

    private Link getLink(String url,String directory) {
        SQLiteDatabase db = ((AdvertiserApplication)context.getApplicationContext()).getDataBase().getWritableDatabase();
        Cursor c = db.query(DataBase.LinkTable.TABLE_NAME,new String[]{DataBase.LinkTable.ETAG},
                DataBase.LinkTable.LINK+"=?",
                new String[]{url},null,null,null);
        String ETag = null;
        if (c.moveToFirst()) ETag=c.getString(c.getColumnIndex(DataBase.LinkTable.ETAG));
        c.close();
        if (ETag!=null) {
            File file = new File(directory+"/"+RecordDownloader.extractFilename(url));
            if (!file.exists()){
                ETag=null;
                db.delete(DataBase.LinkTable.TABLE_NAME,DataBase.LinkTable.LINK+"=?",new String[]{url});
            }
        }
        return new Link(url,ETag);
    }

    private ArrayList<Link> getLinksImage(ArrayList<Record.Image> urls) {
        ArrayList<Link> links= new ArrayList<>();
        for (Record.Image url:urls){
            links.add(getLink(url.getImageUrl(),imageDirectory));
        }
        return links;
    }

    private ArrayList<Link> getLinksContent(ArrayList<Content> urls,String directory) {
        ArrayList<Link> links= new ArrayList<>();
        if(urls==null) return links;
        for (Content url:urls){
            links.add(getLink(url.getImageUrl(),directory));
        }
        return links;
    }

    private void start(){
        DownloadRunnable downloadRunnable = new DownloadRunnable();
        executor.execute(downloadRunnable);
    }

    public static String extractFilename(String fileUrl){
        return fileUrl.substring(fileUrl.lastIndexOf('/')+1);
    }

    public void setCancelled() {
        isCancelled = true;
        executor.shutdownNow();
        executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }

    private class DownloadRunnable implements Runnable{

        @Override
        public void run() {
            try {
                processMonitor.onProcess("چک کردن فایل های کاربر...",0);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                checkUserFiles(images,imageDirectory);
                checkUserFiles(videos,videoDirectory);
                checkUserFiles(sounds,soundDirectory);
                checkUserFiles(content1,content1Directory);
                checkUserFiles(content2,content2Directory);
                checkUserFiles(content3,content3Directory);
                processMonitor.onProcess("چک کردن فایل های دانلود شده...",0);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                checkNetFiles(images,imageDirectory);
                checkNetFiles(videos,videoDirectory);
                checkNetFiles(sounds,soundDirectory);
                checkNetFiles(content1,content1Directory);
                checkNetFiles(content2,content2Directory);
                checkNetFiles(content3,content3Directory);
                processMonitor.onProcess("بررسی فایل ها موفقیت آمیز بود.",100);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                processMonitor.onFinish();
            }catch (Exception e){
                e.printStackTrace();
                failed=true;
            }
            finally {
                isFinished=true;
                getSignalManager().sendMainSignal(new Signal("",Signal.DOWNLOADER_FINISHED));
            }
        }
    }

    private void checkNetFiles(ArrayList<Link> links,String directory){
        for (Link link:links){
            processMonitor.onProcess(extractFilename(link.getLink()),0);
            if (isCancelled) return;
            if (link.ETag==null || !link.ETag.equals("local")){
                downloadFile(link,directory,false);
            }
            while (link.ETag==null){
                if (isCancelled) return;
                //processMonitor.onProcess("به ارتباط اینترنت نیاز است...");
                processMonitor.onProcess("فایل \""+extractFilename(link.getLink())+"\" پیدا نشد، ارتباط اینترنت را چک کنید. در حال تلاش مجدد...",-1);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                downloadFile(link,directory,true);
            }
        }
    }

    private static String getPathUrl(String url){
        return url.substring(0,url.lastIndexOf("/")+1);
    }

    private static String getEncodedUrl(String url){
        try {
            String s = getPathUrl(url)+URLEncoder.encode(extractFilename(url),"UTF-8");
            Log.e("url",s);
            return s;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void downloadFile(Link link, String directory,boolean force) {
        if (!isNetworkConnected() && !force){
            return;
        }
        Log.e("link",link.getLink());
        Request.Builder request = new Request.Builder()
                .url(link.getLink())
                .get();
        File tempFile = new File(tempDir,extractFilename(link.getLink()));
        boolean isResume = tempFile.exists();

        if (isResume){
            request.addHeader("Range", "bytes=" + String.valueOf(tempFile.length()) + "-");
        }
        Response response=null;
        try {
            response =getClient().newCall(request.build()).execute();
            getSignalManager().sendMainSignal(new Signal("NET",Signal.DOWNLOADER_NETWORK));
            if (isCancelled) return;
            if (response.isSuccessful()){
                if (!response.header("ETag","").equals(link.getETag())){
                    doDownload(response.body(),
                            new File(directory+"/"+extractFilename(link.getLink())),
                            isResume?tempFile.length()+response.body().contentLength():response.body().contentLength()
                            ,isResume);
                    link.ETag=response.header("ETag");
                    saveToDb(link);
                }
                else {
                    Log.e("File: "+extractFilename(link.getLink()),"Correct");
                }
            }
            else {
                Log.e("File:"+extractFilename(link.getLink()),"download failed status:"+response.code());
            }
        } catch (IOException e) {
            getSignalManager().sendMainSignal(new Signal("NO_NET",Signal.DOWNLOADER_NO_NETWORK));
            e.printStackTrace();
            Log.e("File"+extractFilename(link.getLink()),"download failed");
        }
        finally {
            if (response!=null)
                response.close();
        }
    }

    private synchronized void doDownload(ResponseBody body, File out, long len,boolean isResume) throws IOException {
        byte[] buffer = new byte[1024];
        int length;
        int lastChange;
        long total=0;
        double max =  len/1024f/1024f;
        double dled = 0;
        File tempFile = new File(tempDir,out.getName());
        BufferedSource source = body.source();

        /*if(isResume)
            source.skip(tempFile.length());*/

        BufferedInputStream in = new BufferedInputStream(body.byteStream());
        FileOutputStream outputStream;

        if (isResume) {
            outputStream = new FileOutputStream(tempFile, true);
            total=tempFile.length();
        } else {
            outputStream = new FileOutputStream(tempFile, false);
        }

        try {
            while ((length = in.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
                total+=length;
                double downloaded = total/1024f/1024f;
                if (downloaded-dled>=0.1 || downloaded==0 || downloaded==max){
                    dled=downloaded;
                    //Log.e("Downloaded",out.getName()+"... "+downloaded+"/"+max+"--"+(int)((downloaded/max)*100f));
                    processMonitor.onProcess(out.getName()+"... "+downloaded+"/"+max,(int)((downloaded/max)*100f));
                }
            }

            outputStream.flush();
            outputStream.close();
            moveFileTo(tempFile,out);
        }finally {
            outputStream.flush();
            outputStream.close();
        }


    }

    private OkHttpClient getClient(){
        return ((AdvertiserApplication)context.getApplicationContext()).getNetworkClient().getClient();
    }

    private void checkUserFiles(ArrayList<Link> links,String directory){
        for (Link link:links){
            checkUserFile(link,directory);
        }
    }

    private void checkUserFile(Link link,String directory){
        if (isCancelled) return;
        File userFile = new File(userDirectory+"/"+extractFilename(link.getLink()));
        if (userFile.exists()) {
            processMonitor.onProcess(extractFilename(link.getLink())+"...",0);
            if (copyFileToFolder(userFile, directory)) {
                link.ETag = "local";
                saveToDb(link);
                processMonitor.onProcess(extractFilename(link.getLink())+"... OK",100);
            }
        }
    }


    private boolean copyFileToFolder(File from, String toFolder){
        try {
            FileUtils.copyFileToDirectory(from,new File(toFolder));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void saveToDb(Link link){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DataBase.LinkTable.LINK,link.getLink());
        contentValues.put(DataBase.LinkTable.ETAG,link.getETag());
        SQLiteDatabase db = ((AdvertiserApplication)context.getApplicationContext()).getDataBase().getWritableDatabase();
        db.delete(DataBase.LinkTable.TABLE_NAME,DataBase.LinkTable.LINK+"=?",new String[]{link.link});
        db.insert(DataBase.LinkTable.TABLE_NAME,null,contentValues);
    }

    private boolean moveFileTo(File from, File to){
        try {
            FileUtils.moveFile(from,to);
            return true;
        } catch (FileExistsException e){
            try {
                FileUtils.forceDelete(to);
                return moveFileTo(from,to);
            } catch (IOException e1) {
                e1.printStackTrace();
                return false;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    protected SignalManager getSignalManager(){
        return ((AdvertiserApplication) context.getApplicationContext()).getSignalManager();
    }

    private boolean isNetworkConnected(){
        return ((AdvertiserApplication) context.getApplicationContext()).isInternetConnected();
    }

    private static class Link{
        private String link;
        private String ETag;

        public Link(String link, String ETag) {
            this.link = link;
            this.ETag = ETag;
        }

        public String getLink() {
            return link;
        }

        public String getETag() {
            return ETag;
        }

    }
}
