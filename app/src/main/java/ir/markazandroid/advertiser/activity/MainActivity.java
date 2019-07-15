package ir.markazandroid.advertiser.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ir.markazandroid.advertiser.AdvertiserApplication;
import ir.markazandroid.advertiser.BuildConfig;
import ir.markazandroid.advertiser.R;
import ir.markazandroid.advertiser.downloader.RecordDownloader;
import ir.markazandroid.advertiser.fragment.ImageFragment;
import ir.markazandroid.advertiser.fragment.OnSelectListener;
import ir.markazandroid.advertiser.fragment.VideoFragment;
import ir.markazandroid.advertiser.network.OnResultLoaded;
import ir.markazandroid.advertiser.object.Content;
import ir.markazandroid.advertiser.object.ErrorObject;
import ir.markazandroid.advertiser.object.ExtrasObject;
import ir.markazandroid.advertiser.object.Record;
import ir.markazandroid.advertiser.object.RecordOptions;
import ir.markazandroid.advertiser.object.RssFeedModel;
import ir.markazandroid.advertiser.signal.Signal;
import ir.markazandroid.advertiser.signal.SignalReceiver;
import ir.markazandroid.advertiser.util.ContentAdapter;
import ir.markazandroid.advertiser.util.Utils;
import ir.markazandroid.advertiser.view.MQTextView;
import ir.markazandroid.advertiser.view.WebPageView;

public class MainActivity extends BaseActivity implements SignalReceiver, VideoFragment.VideoStateChangeListener {

    TextView subtitle, downloaderStats, version, dateText;
    private ProgressBar downloaderProgressStats;
    Toolbar toolbar;
    TextClock clock;
    FragmentPagerAdapter adapterViewPager;
    ViewPager.OnPageChangeListener pageChangeListener;
    CountDownTimer imageCountDownTimer, subtitleCountDownTimer;
    ViewPager viewPager;
    ImageView logo, blockView;
    SimpleExoPlayer soundPlayer;
    Timer autoFetch;
    boolean isShowing = false;
    private boolean isDownloaderStarted = false;
    private int id;
    private static final int RequestPermissionCode = 10;
    private BroadcastReceiver m_timeChangedReceiver;
    private ArrayList<ContentAdapter> contentAdapters;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //Log.e("Neshoooon bede","bede");
        //((AdvertiserApplication)getApplication()).installApp();

        //TODO decor
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LOW_PROFILE;
        decorView.setSystemUiVisibility(uiOptions);

        //hideBars();

        setContentView(R.layout.activity_main);

        initMainLayout();


        getSignalManager().addReceiver(this);
        getSignalManager().sendMainSignal(new Signal(Signal.SIGNAL_ENABLE_KEEP_ALIVE));

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, RequestPermissionCode);
        } else {
            startInit();
        }
    }

    private void hideBars() {
        try {
            //REQUIRES ROOT
            //Build.VERSION_CODES vc = new Build.VERSION_CODES();
            //Build.VERSION vr = new Build.VERSION();
            String /*ProcID = "79";*/ //HONEYCOMB AND OLDER

                    //v.RELEASE  //4.0.3
                    //if(vr.SDK_INT >= vc.ICE_CREAM_SANDWICH){
                    ProcID = "42"; //ICS AND NEWER
            //}


            //REQUIRES ROOT
            Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", "service call activity " + ProcID + " s16 com.android.systemui"}); //WAS 79
            proc.waitFor();

        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showBars() {
        try {
            Process proc = Runtime.getRuntime().exec(new String[]{
                    "su", "startservice", "-n", "com.android.systemui/.SystemUIService"});
            proc.waitFor();
            Toast.makeText(this, "Bars are enabled", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Button backToMenuButton;


    private void initMainLayout() {
        subtitle = findViewById(R.id.subtitle);
        subtitle.setSelected(true);

        backToMenuButton = findViewById(R.id.backToMenuButton);
        backToMenuButton.setOnClickListener(v -> {
            exitAdvertising();
        });
        backToMenuButton.setVisibility(View.GONE);

        downloaderStats = findViewById(R.id.downlaoderStat);
        downloaderProgressStats = findViewById(R.id.downlaoderStatBar);
        downloaderProgressStats.setMax(100);

        viewPager = findViewById(R.id.vpPager);

        logo = findViewById(R.id.logo);
        toolbar = findViewById(R.id.toolbar);

        /*logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSignalManager().sendMainSignal(new Signal("Logout", Signal.SIGNAL_LOGOUT));
            }
        });*/
        clock = findViewById(R.id.clock);
        dateText = findViewById(R.id.dateText);
        version = findViewById(R.id.version);
        version.setText(BuildConfig.VERSION_NAME);
        dateText.setOnClickListener(v -> {
            //socket.send();
            Toast.makeText(MainActivity.this, "date", Toast.LENGTH_SHORT).show();
            //finish();
            //return true;
        });

        version.setOnLongClickListener(v -> {
            finish();
            return true;
        });
    }

    //2 part
    private WebPageView webPageView;

    void init2PartLayer(boolean isHorizontal) {
        if (isHorizontal)
            setContentView(R.layout.horizontal_twopart_activity_main);
        else
            setContentView(R.layout.vertical_twopart_activity_main);
        initMainLayout();

        webPageView = findViewById(R.id.webViewWidget);


    }


    //3 part
    private ImageView staticImage;

    private void init3PartLayer() {
        setContentView(R.layout.horizontal_3part_activity_main);
        initMainLayout();
        staticImage = findViewById(R.id.static_image);

    }

    // 4 part 
    private WebView weatherWebView;
    private WebView currencyWebView;

    private void init4Part1Layer() {
        setContentView(R.layout.horizontal_4part_activity_main_1);
        initMainLayout();
        weatherWebView = findViewById(R.id.weatherWebView);
        currencyWebView = findViewById(R.id.currencyWebView);

        weatherWebView.setWebViewClient(new WebViewClient() {
            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                // Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
            }

            @TargetApi(android.os.Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
                // Redirect to deprecated method, so you can use it in all SDK versions
                onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
            }
        });
        weatherWebView.getSettings().setJavaScriptEnabled(true);
        weatherWebView.loadUrl("http://mohammaddalvi.ir/weather");

        currencyWebView.setWebViewClient(new WebViewClient() {
            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                // Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
            }

            @TargetApi(android.os.Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
                // Redirect to deprecated method, so you can use it in all SDK versions
                onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
            }
        });
        currencyWebView.getSettings().setJavaScriptEnabled(true);
        currencyWebView.loadUrl("http://www.zcast.ir/");

    }

    // 4 parts
    private TextView rssFeedTextView;

    private void init4Part2Layer() {
        setContentView(R.layout.horizontal_4part_activity_main_2);
        initMainLayout();
        weatherWebView = findViewById(R.id.weatherWebView);
        rssFeedTextView = findViewById(R.id.rssFeed);

        weatherWebView.setWebViewClient(new WebViewClient() {
            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                // Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
            }

            @TargetApi(android.os.Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
                // Redirect to deprecated method, so you can use it in all SDK versions
                onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
            }
        });
        weatherWebView.getSettings().setJavaScriptEnabled(true);
        weatherWebView.loadUrl("http://mohammaddalvi.ir/weather");
    }

    private void init4PartVerticalLayer() {
        setContentView(R.layout.vertical_4part_activity_main);
        initMainLayout();
        staticImage = findViewById(R.id.static_image);
        weatherWebView = findViewById(R.id.weatherWebView);

        weatherWebView.setWebViewClient(new WebViewClient() {
            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                // Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
            }

            @TargetApi(android.os.Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
                // Redirect to deprecated method, so you can use it in all SDK versions
                onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
            }
        });

        weatherWebView.getSettings().setJavaScriptEnabled(true);
        weatherWebView.loadUrl("http://mohammaddalvi.ir/weather");
    }

    //Doctor
    private WebView tvWebView;

    private void initVerticalDoctor() {
        setContentView(R.layout.vertical_doctor);
        initMainLayout();
        staticImage = findViewById(R.id.static_image);
        weatherWebView = findViewById(R.id.weatherWebView);

        weatherWebView.setWebViewClient(new WebViewClient() {
            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                // Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
            }

            @TargetApi(android.os.Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
                // Redirect to deprecated method, so you can use it in all SDK versions
                onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
            }
        });

        weatherWebView.getSettings().setJavaScriptEnabled(true);
        weatherWebView.loadUrl("http://mohammaddalvi.ir/weather");

        tvWebView = findViewById(R.id.tvWebView);

        tvWebView.setWebViewClient(new WebViewClient() {
            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                // Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
            }

            @TargetApi(android.os.Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
                // Redirect to deprecated method, so you can use it in all SDK versions
                onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
            }
        });
        tvWebView.getSettings().setJavaScriptEnabled(true);

        tvWebView.loadUrl("http://live.irib.ir");

    }

    void initDoctorLayout() {
        setContentView(R.layout.doctor_activity_main);
        initMainLayout();
        staticImage = findViewById(R.id.static_image);
        rssFeedTextView = findViewById(R.id.rssFeed);
        tvWebView = findViewById(R.id.tvWebView);

        tvWebView.setWebViewClient(new WebViewClient() {
            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                // Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
            }

            @TargetApi(android.os.Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
                // Redirect to deprecated method, so you can use it in all SDK versions
                onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
            }
        });
        tvWebView.getSettings().setJavaScriptEnabled(true);

        tvWebView.loadUrl("http://live.irib.ir");
    }


    private void initSpecialLayout() {
        setContentView(R.layout.special_activity_main);
        initMainLayout();
        rssFeedTextView = findViewById(R.id.rssFeed);
    }

    private String fixUrl(String urlLink) {
        if (!urlLink.startsWith("http://") && !urlLink.startsWith("https://")) {
            urlLink = "http://" + urlLink;
        }
        return urlLink;
    }

    private void initDate(final String calendarType) {
        dateText.setText(Utils.getDateString(System.currentTimeMillis(), calendarType));
        IntentFilter s_intentFilter = new IntentFilter();
        s_intentFilter.addAction(Intent.ACTION_TIME_TICK);
        s_intentFilter.addAction(Intent.ACTION_DATE_CHANGED);
        s_intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        s_intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        m_timeChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                dateText.setText(Utils.getDateString(System.currentTimeMillis(), calendarType));
            }
        };
        registerReceiver(m_timeChangedReceiver, s_intentFilter);
    }

    private void startInit() {

        //initHardware();

        //((AdvertiserApplication)getApplicationContext()).getLocationMgr().start();

        loadRecord();
        //fetchRecords();

        startAutoFetch();

        //if (checkIfAuthenticated())
        //startAutoFetch();

        /*SerialPort serialPort=null;
        if (SerialPort.getCommPorts().length>0)
            serialPort = SerialPort.getCommPorts()[0];
        else
            Toast.makeText(this,"No Port Found!",Toast.LENGTH_LONG).show();

        if (serialPort!=null){
            PortReader portReader = new PortReader(serialPort,this);
            ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
            threadPoolExecutor.execute(portReader);
        }
        else
            Toast.makeText(this,"Port is Null!",Toast.LENGTH_LONG).show();*/

    }

    private boolean checkIfAuthenticated() {
        return ((AdvertiserApplication)getApplication()).getPoliceBridge().getAuthenticationDetails()!=null;
    }

    private void initHardware() {
        //ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        //threadPoolExecutor.execute(portReader);
        //ttyS2
        // Log.e("fdfd","started running hw");
    }


    private void startAutoFetch() {

        Log.e("AutoStart","started");

        if (autoFetch!=null) autoFetch.cancel();

        autoFetch = new Timer();
        autoFetch.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (checkIfAuthenticated())
                    fetchAutoRecords();
            }
        }, 0, 30000);
    }

    private void stopAutoFetch() {

        Log.e("AutoStart","stopped");

        if (autoFetch!=null) autoFetch.cancel();
    }

    @Override
    public void onRequestPermissionsResult(int RC, String[] per, int[] PResult) {
        switch (RC) {
            case RequestPermissionCode:
                if (PResult.length > 0 && PResult[0] == PackageManager.PERMISSION_GRANTED) {
                    startInit();
                } else {
                    Toast.makeText(MainActivity.this, "Permission Canceled, Now your application cannot access GPS.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void fetchRecords() {
        subtitle.setText("در حال اتصال به اینترنت...");
        getNetworkManager().getRecords(new OnResultLoaded.ActionListener<ArrayList<Record>>() {
            @Override
            public void onSuccess(final ArrayList<Record> result) {
                runOnUiThread(() -> {
                    subtitle.setText("");
                    checkRecord(result);
                });
            }

            @Override
            public void onError(final ErrorObject error) {
                runOnUiThread(() -> {
                    if (error.getStatus() == 410) {
                        Intent intent = new Intent(MainActivity.this, UnAssignedActivity.class);
                        startActivity(intent);
                        finish();
                    } else if (error.getStatus() == 401) {
                        getSignalManager().sendMainSignal(new Signal("Logout", Signal.SIGNAL_LOGOUT));
                    }
                });
            }

            @Override
            public void failed(Exception e) {
                runOnUiThread(() -> {
                    subtitle.setText("");
                    loadRecord();
                    Toast.makeText(MainActivity.this, "اشکال در اتصال به اینترنت.", Toast.LENGTH_LONG).show();
                });
                // e.printStackTrace();
            }
        });
    }

    private void fetchAutoRecords() {
        getNetworkManager().getRecords(new OnResultLoaded.ActionListener<ArrayList<Record>>() {
            @Override
            public void onSuccess(final ArrayList<Record> result) {
                runOnUiThread(() -> checkAutoRecord(result));
            }

            @Override
            public void onError(final ErrorObject error) {
                runOnUiThread(() -> {
                    if (error.getStatus() == 410) {
                        Intent intent = new Intent(MainActivity.this, UnAssignedActivity.class);
                        startActivity(intent);
                        finish();
                    } else if (error.getStatus() == 401) {
                        getSignalManager().sendMainSignal(new Signal("Logout", Signal.SIGNAL_LOGOUT));
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

    private void checkRecord(ArrayList<Record> record) {
        if (record.isEmpty()) {
            getDataBase().setRecord(null);
        } else {
            getDataBase().setRecord(record.get(0));
            loadRecord();
        }
    }

    private void checkAutoRecord(ArrayList<Record> record) {
        Record presentRecord = getDataBase().getRecord();
        if (record.isEmpty()) {
            getDataBase().setRecord(null);
            if (presentRecord != null)
                restartActivity();
        } else {
            if (presentRecord == null || record.get(0).getLatestEditTime() > presentRecord.getLatestEditTime()) {
                getDataBase().setRecord(record.get(0));
                restartActivity();
                //loadRecord();
            }
        }

    }

    private void loadRecord() {
        final Record record = getDataBase().getRecord();
        if (record != null) {
            if (getRecorddownloader().isFinished()) {
                RecordDownloader recordDownloader = getRecorddownloader();
                recordDownloader.setRecord(record);
                recordDownloader.setProcessMonitor(new RecordDownloader.ProcessMonitor() {
                    @Override
                    public void onProcess(String status, int proccess) {
                        runOnUiThread(() -> {
                            //  Log.e("status",status);
                            if (proccess > 100) downloaderProgressStats.setProgress(100);
                            else if (proccess > 0)
                                downloaderProgressStats.setProgress(proccess);

                            downloaderStats.setText(status);
                        });
                    }

                    @Override
                    public void onFinish() {
                        runOnUiThread(() -> {
                            downloaderProgressStats.setVisibility(View.GONE);
                            downloaderStats.setVisibility(View.GONE);
                            if (soundPlayer == null)
                                setupAndStartAudio(record);
                        });
                    }
                });
                recordDownloader.init();
                isDownloaderStarted = true;
            } else
                getRecorddownloader().setCancelled();

            parseRecord(record);
            showRecord(record);
        }
    }

    public static void parseRecord(Record record) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/advertiser/record_" + record.getRecordId());
        String imageDirectory = new File(myDir.toString() + "/image").getPath();
        String videoDirectory = new File(myDir.toString() + "/video").getPath();
        String soundDirectory = new File(myDir.toString() + "/sound").getPath();

        record.setVideoFiles(assign(record.getVideoArray(), videoDirectory));
        record.setSoundFiles(assign(record.getSoundArray(), soundDirectory));
        assignImage(record.getPhotosArrayObject(), imageDirectory);

    }

    public static ArrayList<File> assign(ArrayList<String> links, String directory) {
        ArrayList<File> videos = new ArrayList<>();
        for (String link : links) {
            videos.add(new File(directory + "/" + RecordDownloader.extractFilename(link)));
        }
        return videos;
    }

    public static ArrayList<File> assign(String link, String directory) {
        ArrayList<File> videos = new ArrayList<>();
        videos.add(new File(directory + "/" + RecordDownloader.extractFilename(link)));
        return videos;
    }

    public static void assignImage(ArrayList<Record.Image> links, String directory) {
        for (Record.Image link : links) {
            link.setFile(new File(directory + "/" + RecordDownloader.extractFilename(link.getImageUrl())));
        }
    }

    public static void assignImage(Record.Image link, String directory) {
        link.setFile(new File(directory + "/" + RecordDownloader.extractFilename(link.getImageUrl())));
    }

    public static void assignContent(ArrayList<Content> links, String directory) {
        for (Content link : links) {
            link.setFile(new File(directory + "/" + RecordDownloader.extractFilename(link.getImageUrl())));
        }
    }


    private void showRecord(final Record record) {
        isShowing = true;


        initLayout(record.getLayoutType());
        initOptions(record.getOptions());
        initExtras(record);

        Picasso.get()
                .load(record.getIcon())
                .fit()
                .into(logo);

        setupAndStartAudio(record);

        if (record.getSubTitleArrayObject() != null && !record.getSubTitleArrayObject().isEmpty()) {
            Record.SubTitle subTitle = record.getSubTitleArrayObject().get(0);
            if (record.getSubTitleArrayObject().size() == 1) {
                subtitle.setText(subTitle.getSubTitle());
                subtitle.setTextSize(subTitle.getFontSize());
            } else {
                startSubtitleTimer(record, subTitle);
            }
        }
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager(), record);
        viewPager.setAdapter(adapterViewPager);
        viewPager.removeOnPageChangeListener(pageChangeListener);
        pageChangeListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (imageCountDownTimer != null) imageCountDownTimer.cancel();
                String name = makeFragmentName(id, position);
                int lastPosition = position - 1;
                if (lastPosition < 0) lastPosition = adapterViewPager.getCount() - 1;
                String lastname = makeFragmentName(id, lastPosition);
                OnSelectListener lastFragment = (OnSelectListener) getSupportFragmentManager().findFragmentByTag(lastname);
                OnSelectListener fragment = (OnSelectListener) getSupportFragmentManager().findFragmentByTag(name);
                if (lastFragment != null) lastFragment.onDeselect();
                if (fragment != null) fragment.onSelect();
                if (fragment instanceof VideoFragment) {
                    if (soundPlayer != null)
                        soundPlayer.setPlayWhenReady(false);
                } else {
                    if (soundPlayer != null)
                        soundPlayer.setPlayWhenReady(true);
                }
                if (record.getVideoFiles().isEmpty()) {
                    startTimer(record, position);
                } else {
                    if (position != 0) {
                        startTimer(record, position - 1);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };
        viewPager.addOnPageChangeListener(pageChangeListener);
        if (record.getVideoFiles().isEmpty() && !record.getPhotosArrayObject().isEmpty())
            startTimer(record, 0);
        else {
            if (soundPlayer != null)
                soundPlayer.setPlayWhenReady(false);
        }
    }

    private void showContent(int resId, ArrayList<Content> contents) {
        if (contentAdapters == null) contentAdapters = new ArrayList<>();
        ContentAdapter contentAdapter = new ContentAdapter(new Handler(getMainLooper()), getSupportFragmentManager(), contents, resId);
        contentAdapter.start();
        contentAdapters.add(contentAdapter);
    }

    private void initLayout(String layoutType) {

        switch (layoutType) {
            case "main":
                break;
            case "vertical_2part":
                init2PartLayer(false);
                break;
            case "horizontal_2part":
                init2PartLayer(true);
                break;
            case "horizontal_3part":
                init3PartLayer();
                break;
            case "horizontal_4part_1":
                init4Part1Layer();
                break;
            case "horizontal_4part_2":
                init4Part2Layer();
                break;
            case "vertical_4part":
                init4PartVerticalLayer();
                break;
            case "vertical_doctor":
                initVerticalDoctor();
                break;
            case "horizontal_doctor":
                initDoctorLayout();
                break;
            case "horizontal_special":
                initSpecialLayout();
                break;

        }
    }

    private void initExtras(Record record) {

        switch (record.getLayoutType()) {
            case "main":
                break;
            case "vertical_2part":
            case "horizontal_2part":
                init2PartExtras(record.getExtras());
                break;
            case "horizontal_3part":
                init3PartExtras(record.getExtras());
                break;
            case "horizontal_4part_1":
                init4Part1Extras(record.getExtras());
                break;
            case "horizontal_4part_2":
                init4Part2Extras(record.getExtras());
                break;
            case "vertical_4part":
                init4PartVerticalExtras(record.getExtras());
                break;
            case "vertical_doctor":
                initDoctorVerticalExtras(record.getExtras());
                break;
            case "horizontal_doctor":
                initDoctorExtras(record.getExtras());
                break;
            case "horizontal_special":
                initSpecialExtras(record.getExtras(), record.getRecordId());
                break;
        }
    }


    private void init2PartExtras(ExtrasObject extrasObject) {
        if (extrasObject != null) {
            if (extrasObject.getWebViewUrl() != null && !extrasObject.getWebViewUrl().isEmpty()) {
                webPageView.init(extrasObject.getWebViewUrl());
            }
        }
    }

    private void init3PartExtras(ExtrasObject extrasObject) {
        if (extrasObject != null) {
            Picasso.get().load(extrasObject.getResumePhotoUrl())
                    .fit()
                    .noFade()
                    .into(staticImage);
        }
    }


    private void init4Part1Extras(ExtrasObject extrasObject) {
        if (extrasObject != null) {

            if (extrasObject.getCurrencyUrl() != null && !extrasObject.getCurrencyUrl().isEmpty()) {
                currencyWebView.loadUrl(extrasObject.getCurrencyUrl());
            }
            if (extrasObject.getWeatherUrl() != null && !extrasObject.getWeatherUrl().isEmpty()) {
                weatherWebView.loadUrl(extrasObject.getWeatherUrl());
            }
        }
    }

    private void init4Part2Extras(final ExtrasObject extrasObject) {
        if (extrasObject != null) {

            if (extrasObject.getRssFeedUrl() != null && !extrasObject.getRssFeedUrl().isEmpty()) {
                getNetworkManager().loadRssFeed(extrasObject.getRssFeedUrl(), new OnResultLoaded<List<RssFeedModel>>() {
                    @Override
                    public void loaded(List<RssFeedModel> result) {
                        final String text = TextUtils.join(" -- ", result);
                        runOnUiThread(() -> {
                            rssFeedTextView.setTextSize(extrasObject.getRssFeedTextSize());
                            rssFeedTextView.setText(text);
                            rssFeedTextView.requestFocus();
                            rssFeedTextView.setSelected(true);
                        });
                    }

                    @Override
                    public void failed(Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            if (extrasObject.getWeatherUrl() != null && !extrasObject.getWeatherUrl().isEmpty()) {
                weatherWebView.loadUrl(extrasObject.getWeatherUrl());
            }
        }
    }

    private void init4PartVerticalExtras(ExtrasObject extrasObject) {
        if (extrasObject != null) {
            Picasso.get().load(extrasObject.getResumePhotoUrl())
                    .fit()
                    .noFade()
                    .into(staticImage);
        }
    }

    private void initDoctorVerticalExtras(ExtrasObject extras) {
        init4PartVerticalExtras(extras);
        if (extras.getTvUrl() != null && !extras.getTvUrl().isEmpty()) {
            tvWebView.loadUrl(extras.getTvUrl());
        }
    }

    private void initDoctorExtras(final ExtrasObject extrasObject) {
        if (extrasObject != null) {
            Picasso.get().load(extrasObject.getResumePhotoUrl())
                    .fit()
                    .noFade()
                    .into(staticImage);

            if (extrasObject.getTvUrl() != null && !extrasObject.getTvUrl().isEmpty()) {
                tvWebView.loadUrl(extrasObject.getTvUrl());
            }

            if (extrasObject.getRssFeedUrl() != null && !extrasObject.getRssFeedUrl().isEmpty()) {
                getNetworkManager().loadRssFeed(extrasObject.getRssFeedUrl(), new OnResultLoaded<List<RssFeedModel>>() {
                    @Override
                    public void loaded(List<RssFeedModel> result) {
                        final String text = TextUtils.join(" -- ", result);
                        runOnUiThread(() -> {
                            rssFeedTextView.setTextSize(extrasObject.getRssFeedTextSize());
                            rssFeedTextView.setText(text);
                            rssFeedTextView.requestFocus();
                            rssFeedTextView.setSelected(true);
                        });
                    }

                    @Override
                    public void failed(Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    private void initSpecialExtras(final ExtrasObject extrasObject, int recordId) {
        if (extrasObject != null) {
            if (extrasObject.getRssFeedUrl() != null && !extrasObject.getRssFeedUrl().isEmpty()) {
                getNetworkManager().loadRssFeed(extrasObject.getRssFeedUrl(), new OnResultLoaded<List<RssFeedModel>>() {
                    @Override
                    public void loaded(List<RssFeedModel> result) {
                        final String text = TextUtils.join(" -- ", result);
                        runOnUiThread(() -> {
                            rssFeedTextView.setTextSize(extrasObject.getRssFeedTextSize());
                            rssFeedTextView.setText(text);
                            rssFeedTextView.requestFocus();
                            rssFeedTextView.setSelected(true);
                        });
                    }

                    @Override
                    public void failed(Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/advertiser/record_" + recordId);

            if (extrasObject.getContents1() != null) {
                String content1Directory = new File(myDir.toString() + "/content1").getPath();
                assignContent(extrasObject.getContents1(), content1Directory);
                showContent(R.id.content1, extrasObject.getContents1());
            }

            if (extrasObject.getContents2() != null) {
                String content2Directory = new File(myDir.toString() + "/content2").getPath();
                assignContent(extrasObject.getContents2(), content2Directory);
                showContent(R.id.content2, extrasObject.getContents2());
            }

            if (extrasObject.getContents3() != null) {
                String content3Directory = new File(myDir.toString() + "/content3").getPath();
                assignContent(extrasObject.getContents3(), content3Directory);
                showContent(R.id.content3, extrasObject.getContents3());
            }
        }
    }

    private void initOptions(final RecordOptions options) {
        if (options != null) {
            toolbar.setVisibility(options.getLogoVisible() ? View.VISIBLE : View.GONE);
            subtitle.setVisibility(options.getSubtitleVisible() ? View.VISIBLE : View.GONE);
            clock.setVisibility(options.getClockVisible() ? View.VISIBLE : View.GONE);
            clock.setTextSize(options.getTimeTextSize());
            dateText.setVisibility(options.getDateVisible() ? View.VISIBLE : View.GONE);
            dateText.setTextSize(options.getTimeTextSize());
            //backToMenuButton.setVisibility(options.getBackToMenuVisible() ? View.VISIBLE : View.INVISIBLE);

            logo.post(() -> {
                logo.getLayoutParams().height = Utils.dpToPx(MainActivity.this, options.getLogoSize());
                logo.getLayoutParams().width = Utils.dpToPx(MainActivity.this, options.getLogoSize());
            });

            initDate(options.getCalendarType());
        }
    }

    private void setupAndStartAudio(Record record) {
        if (!record.getSoundFiles().isEmpty()) {
            if (soundPlayer != null) soundPlayer.release();

            soundPlayer = newSimpleExoPlayer();
            MediaSource[] medias = new MediaSource[record.getSoundFiles().size()];
            for (int i = 0; i < record.getSoundFiles().size(); i++) {
                medias[i] = newVideoSource(record.getSoundFiles().get(i));
            }

            ConcatenatingMediaSource concatenatedSource =
                    new ConcatenatingMediaSource(medias);
            soundPlayer.prepare(concatenatedSource);

            soundPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);

            soundPlayer.addListener(new Player.DefaultEventListener() {
                @Override
                public void onPlayerError(ExoPlaybackException error) {
                    soundPlayer.release();
                    soundPlayer = null;
                }
            });


            if (!record.getVideoFiles().isEmpty())
                soundPlayer.setPlayWhenReady(true);
        }
    }

    private void startSubtitleTimer(final Record record, final Record.SubTitle subTitle) {
        subtitle.setText(subTitle.getSubTitle());
        subtitle.setTextSize(subTitle.getFontSize());
        if (subtitleCountDownTimer != null) subtitleCountDownTimer.cancel();
        subtitleCountDownTimer = new CountDownTimer(subTitle.getDuration() * 1000, subTitle.getDuration() * 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                final MQTextView mqTextView = (MQTextView) subtitle;
                if (!mqTextView.getMarqueeStatus())
                    goToNextSubtitle(record, subTitle);
                else if (mqTextView.getListener() == null) {
                    mqTextView.setListener(() -> {
                        goToNextSubtitle(record, subTitle);
                        mqTextView.setListener(null);
                    });
                }
            }
        };
        subtitleCountDownTimer.start();
    }

    private void goToNextSubtitle(Record record, Record.SubTitle subTitle) {
        int position = record.getSubTitleArrayObject().indexOf(subTitle);
        if (position + 1 == record.getSubTitleArrayObject().size())
            startSubtitleTimer(record, record.getSubTitleArrayObject().get(0));
        else startSubtitleTimer(record, record.getSubTitleArrayObject().get(position + 1));
    }

    private void startTimer(Record record, int position) {
        Record.Image image = record.getPhotosArrayObject().get(position);
        if (imageCountDownTimer != null) imageCountDownTimer.cancel();
        imageCountDownTimer = new CountDownTimer(image.getDuration() * 1000, image.getDuration() * 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                int position = viewPager.getCurrentItem();
                if (position + 1 == viewPager.getAdapter().getCount()) {
                    viewPager.setCurrentItem(0, true);
                } else {
                    viewPager.setCurrentItem(position + 1, true);
                }
            }
        };
        imageCountDownTimer.start();
    }

    @Override
    public boolean onSignal(Signal signal) {
        Log.i("Signal on MainActivity", signal.getMsg() == null ? "" : signal.getMsg());
        switch (signal.getType()) {

            case Signal.SIGNAL_REFRESH_RECORDS:
                // fetchRecords();
                restartActivity();
                return true;

            case Signal.SIGNAL_SCREEN_BLOCK:
                blockView();
                return true;

            case Signal.SIGNAL_SCREEN_UNBLOCK:
                unblockView();
                return true;

            case Signal.DOWNLOADER_FINISHED:
                if (!isDownloaderStarted)
                    if (getRecorddownloader().isCancelled() || getRecorddownloader().isFailed()) {
                        loadRecord();
                    }
                return true;

            case Signal.SIGNAL_CONNECTED_TO_POLICE:
            case Signal.SIGNAL_DEVICE_AUTHENTICATED:
                startAutoFetch();
                return true;

            case Signal.SIGNAL_DISCONNECTED_FROM_POLICE:
                stopAutoFetch();
                return true;

            case Signal.SIGNAL_LAUNCHING_3PARTY_APP:
                exitAdvertising();
        }
        return false;
    }

    private void exitAdvertising() {
        getSignalManager().sendMainSignal(new Signal(Signal.SIGNAL_DISABLE_KEEP_ALIVE));
        finish();
    }

    private void unblockView() {
        ViewGroup container = (ViewGroup) downloaderStats.getRootView();
        if (blockView == null) {
            blockView = (ImageView) getLayoutInflater().inflate(R.layout.block_view, container, false);
            container.addView(blockView);
            container.bringChildToFront(blockView);
        }
        blockView.setVisibility(View.GONE);
        //Toast.makeText(this,"UnBlocked",Toast.LENGTH_SHORT).show();
    }

    private void blockView() {
        ViewGroup container = (ViewGroup) downloaderStats.getRootView();
        if (blockView == null) {
            blockView = (ImageView) getLayoutInflater().inflate(R.layout.block_view, container, false);
            container.addView(blockView);
            container.bringChildToFront(blockView);
        }
        blockView.setVisibility(View.VISIBLE);
        //Toast.makeText(this,"Blocked",Toast.LENGTH_SHORT).show();
    }

    private void restartActivity() {
        getRecorddownloader().setCancelled();
        Log.e("restarted", "restarted");
        finish();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void restartActivityLol() {
        finish();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        getSignalManager().removeReceiver(this);
        super.onDestroy();
        if (soundPlayer != null) soundPlayer.release();
        if (imageCountDownTimer != null) imageCountDownTimer.cancel();
        if (subtitleCountDownTimer != null) subtitleCountDownTimer.cancel();
        //if (portReader!=null) portReader.close();
        if (autoFetch != null) {
            autoFetch.cancel();
            autoFetch.purge();
            autoFetch = null;
        }
        if (m_timeChangedReceiver != null)
            unregisterReceiver(m_timeChangedReceiver);

        if (contentAdapters != null) {
            for (ContentAdapter adapter : contentAdapters) {
                adapter.dispose();
            }
        }

        if (webPageView != null)
            webPageView.dispose();

        try {
            // if (!getRecorddownloader().isFinished())

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onVideoFinish(Fragment frag) {
        if (viewPager.getAdapter().getCount() != viewPager.getCurrentItem() + 1) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
        }
        if (viewPager.getAdapter().getCount() == 1) {
            String name = makeFragmentName(id, 0);
            OnSelectListener fragment = (OnSelectListener) getSupportFragmentManager().findFragmentByTag(name);
            if (fragment != null) fragment.onDeselect();
            if (fragment != null) fragment.onSelect();
        }

    }

    private SimpleExoPlayer newSimpleExoPlayer() {
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        LoadControl loadControl = new DefaultLoadControl();
        return ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);
    }

    private MediaSource newVideoSource(File file) {
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        String userAgent = Util.getUserAgent(this, "Advertiser");
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, userAgent, bandwidthMeter);
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        return new ExtractorMediaSource(Uri.fromFile(file), dataSourceFactory, extractorsFactory, null, null);
    }

    public class MyPagerAdapter extends FragmentPagerAdapter {

        private Record record;

        public MyPagerAdapter(FragmentManager fragmentManager, Record record) {
            super(fragmentManager);
            this.record = record;
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            int count = 0;
            if (record.getPhotosArrayObject() != null)
                count += record.getPhotosArrayObject().size();
            if (!record.getVideoArray().isEmpty()) count += 1;
            return count;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            id = container.getId();
            return super.instantiateItem(container, position);
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                if (!record.getVideoFiles().isEmpty()) {
                    return new VideoFragment()
                            .videoUrl(record.getVideoFiles())
                            .repeatMode(getCount() > 1 ? Player.REPEAT_MODE_OFF : Player.REPEAT_MODE_ALL);
                } else
                    return new ImageFragment().image(record.getPhotosArrayObject().get(0));
            } else if (record.getVideoFiles().isEmpty()) {
                return new ImageFragment().image(record.getPhotosArrayObject().get(position));
            } else {
                return new ImageFragment().image(record.getPhotosArrayObject().get(position - 1));
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return "Page " + position;
        }

    }

    private static String makeFragmentName(int viewId, long id) {
        return "android:switcher:" + viewId + ":" + id;
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
