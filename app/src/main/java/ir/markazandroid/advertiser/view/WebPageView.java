package ir.markazandroid.advertiser.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

import ir.markazandroid.advertiser.AdvertiserApplication;
import ir.markazandroid.advertiser.R;
import ir.markazandroid.advertiser.network.JSONParser.Parser;
import ir.markazandroid.advertiser.network.JSONParser.annotations.JSON;
import ir.markazandroid.advertiser.signal.Signal;
import ir.markazandroid.advertiser.signal.SignalReceiver;
import ir.markazandroid.advertiser.util.Utils;

/**
 * Coded by Ali on 6/25/2019.
 */
public class WebPageView extends FrameLayout implements SignalReceiver {

    public static final int STATUS_IDEAL = 1;
    public static final int STATUS_LOADING = 2;
    public static final int STATUS_FAILED = 3;

    private WebView webView;
    private Button go;
    private ImageView webviewBack, webviewForward, webviewHome;
    private EditText addressBar;
    private ProgressBar webviewProgressBar;
    private String homeUrl;
    private boolean shouldRefreshOnNet, weAreOnHome;
    private String currentUrl;
    private Parser parser;
    private WebViewExtras extras;
    private Timer goBackToHomeTimer;
    private Handler handler;
    private int status = STATUS_IDEAL;
    private View errorView;
    private Button reloadButton;

    @SuppressLint("ClickableViewAccessibility")
    public WebPageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View innerView = LayoutInflater.from(context).inflate(R.layout.widget_web_page_view, this, false);
        addView(innerView);


        handler = new Handler(context.getMainLooper());

        webView = findViewById(R.id.webView);
        addressBar = findViewById(R.id.url);
        go = findViewById(R.id.go);
        webviewBack = findViewById(R.id.webViewBack);
        webviewForward = findViewById(R.id.webViewForward);
        webviewHome = findViewById(R.id.webViewHome);
        webviewProgressBar = findViewById(R.id.webViewProgressBar);
        errorView = findViewById(R.id.error);
        reloadButton = findViewById(R.id.reload);
        ((AdvertiserApplication) context.getApplicationContext()).getSignalManager().addReceiver(this);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);

                Log.e("WebView", "onLoadResource");
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                status = STATUS_LOADING;
                super.onPageStarted(view, url, favicon);
                currentUrl = url;
                webLoading(true);
                status = STATUS_LOADING;

                Log.e("WebView", "Started");

                showErrorPage(false);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (status == STATUS_LOADING) {
                    status = STATUS_IDEAL;
                    shouldRefreshOnNet = false;
                }

                webLoading(false);

                Log.e("WebView", "Finished");

                //showErrorPage(false);
            }


            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.e("WebView", "Error out");
                onError(errorCode);
            }

            @TargetApi(android.os.Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
                // Redirect to deprecated method, so you can use it in all SDK versions
                Log.e("WebView", "Error in");
                if (req.isForMainFrame())
                    onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);

        go.setOnClickListener(v -> {
            String u = addressBar.getText().toString();
            goToPage(u);
        });

        webviewBack.setOnClickListener(v -> {
            if (webView.canGoBack())
                if (isNetConnected())
                    webView.goBack();
                else
                    onError(WebViewClient.ERROR_CONNECT);

        });
        webviewForward.setOnClickListener(v -> {
            if (webView.canGoForward())
                if (isNetConnected())
                    webView.goForward();
                else
                    onError(WebViewClient.ERROR_CONNECT);

        });
        webviewHome.setOnClickListener(v -> {
            gotoHomePage();
        });

        reloadButton.setOnClickListener(v -> reloadPage());

        setOnTouchListener((v, event) -> {


            if (goBackToHomeTimer != null)
                goBackToHomeTimer.cancel();

            goBackToHomeTimer = new Timer();
            goBackToHomeTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    //if (!weAreOnHome)
                    handler.post(WebPageView.this::gotoHomePage);
                }
            }, 1 * 60 * 1000);

            Log.d("WebView", "Touch");
            return false;
        });
    }

    private void showErrorPage(boolean isError) {
        Log.e("WebView", "showError " + isError);
        if (isError) {
            errorView.setAlpha(1);
            errorView.setVisibility(VISIBLE);
            // Utils.fadeVisible(errorView,500);
        } else
            Utils.fadeFade(errorView, 500, true);
    }

    private void webLoading(boolean isLoading) {
        if (isLoading) {
            Utils.fade(webviewProgressBar, webviewHome, 500);
            Utils.fadeFade(webviewBack, 500, true);
            Utils.fadeFade(webviewForward, 500, true);
        } else {
            Utils.fade(webviewHome, webviewProgressBar, 500);
            Utils.fadeVisible(webviewBack, 500);
            Utils.fadeVisible(webviewForward, 500);
        }
    }

    public void init(String homeUrl) {
        this.homeUrl = homeUrl;
        gotoHomePage();
    }

    private void gotoHomePage() {
        webView.clearHistory();
        goToPage(homeUrl);
        weAreOnHome = true;
    }

    private void goToPage(String url) {
        currentUrl = url;
        if (isNetConnected())
            webView.loadUrl(fixUrl(url));
        else
            onError(WebViewClient.ERROR_CONNECT);
    }

    private void onError(int errorCode) {
        // Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
        switch (errorCode) {
            case WebViewClient.ERROR_CONNECT:
            case WebViewClient.ERROR_FAILED_SSL_HANDSHAKE:
            case WebViewClient.ERROR_HOST_LOOKUP:
            case WebViewClient.ERROR_IO:
            case WebViewClient.ERROR_TIMEOUT:
            case WebViewClient.ERROR_UNKNOWN:
                shouldRefreshOnNet = true;
        }

        status = STATUS_FAILED;
        webLoading(false);
        showErrorPage(true);


    }

    private void reloadPage() {
        if (currentUrl != null)
            goToPage(currentUrl);
    }

    private boolean isNetConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
            if (ni != null)
                return ni.isConnected();
        }
        return false;
    }

    public void dispose() {
        ((AdvertiserApplication) getContext().getApplicationContext()).getSignalManager().removeReceiver(this);
        if (goBackToHomeTimer != null)
            goBackToHomeTimer.cancel();

    }

    private String fixUrl(String urlLink) {
        if (!urlLink.startsWith("http://") && !urlLink.startsWith("https://")) {
            urlLink = "http://" + urlLink;
        }
        return urlLink;
    }


    @Override
    public boolean onSignal(Signal signal) {
        if (signal.getType() == Signal.DOWNLOADER_NETWORK && shouldRefreshOnNet)
            reloadPage();

        return false;
    }

    public static class WebViewExtras implements Serializable {
        private String homeUrl;

        @JSON
        public String getHomeUrl() {
            return homeUrl;
        }

        public void setHomeUrl(String homeUrl) {
            this.homeUrl = homeUrl;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        // ((AdvertiserApplication)getContext().getApplicationContext()).getSignalManager().removeReceiver(this);
        dispose();
    }
}
