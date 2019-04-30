package ir.markazandroid.advertiser.fragment;


import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.danikula.videocache.HttpProxyCacheServer;
import com.google.android.exoplayer2.C;
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
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.util.ArrayList;

import ir.markazandroid.advertiser.AdvertiserApplication;
import ir.markazandroid.advertiser.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class VideoFragment extends SelectiveFragment {

    private VideoStateChangeListener videoStateChangeListener;
    private ArrayList<File> videoUrl;
    private SimpleExoPlayer player;
    private int repeatMode;
    private int videoCount=0;


    @Override
    protected void start() {
        if (videoStateChangeListener==null)
            videoStateChangeListener= (VideoStateChangeListener) getActivity();
        videoUrl= (ArrayList<File>) getArguments().getSerializable(VIDEO_URL);
        repeatMode=getArguments().getInt(REPEAT_MODE,Player.REPEAT_MODE_OFF);

        player = newSimpleExoPlayer();

        playerView.setPlayer(player);

        MediaSource[] medias =new MediaSource[videoUrl.size()];
        for (int i = 0; i < videoUrl.size(); i++) {
            File url = videoUrl.get(i);
            medias[i] = newVideoSource(url);
        }

        ConcatenatingMediaSource concatenatedSource =
                new ConcatenatingMediaSource(medias);

        //player.setRepeatMode(repeatMode);

        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
        player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);

        player.prepare(concatenatedSource);

        player.addListener(new Player.DefaultEventListener() {
            /*@Override
            public void onLoadingChanged(boolean isLoading) {
                if (!isLoading)
                    player.setPlayWhenReady(true);
            }*/

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Log.e("player",playbackState+"");
                if (playbackState==Player.STATE_IDLE && !playWhenReady){
                    //           videoCount++;
                    videoStateChangeListener.onVideoFinish(VideoFragment.this);
                }
                if(playbackState==Player.STATE_ENDED)
                    videoStateChangeListener.onVideoFinish(VideoFragment.this);
                //     if (videoCount==videoUrl.size()) {
                //         videoStateChangeListener.onVideoFinish();
                //        videoCount=0;
                //   }
            }
            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Log.e("error","error");
                error.printStackTrace();
                player.setPlayWhenReady(false);
            }
        });

        player.setPlayWhenReady(true);
        Log.e("cycle","init");
    }

    @Override
    protected void stop() {
        player.release();
        Log.e("cycle","stop");
    }

    @Override
    protected void refresh() {
    //    stop();
     //   start();
    }

    public interface VideoStateChangeListener{
        void onVideoFinish(Fragment frag);
    }


    private final String VIDEO_URL = "video_url";
    private final String REPEAT_MODE = "repeat_mode";


    public VideoFragment() {
        Bundle bundle = new Bundle();
        setArguments(bundle);
    }

    public VideoFragment videoUrl(ArrayList<File> url){
        getArguments().putSerializable(VIDEO_URL,url);
        return this;
    }

    public VideoFragment repeatMode(int repeatMode){
        getArguments().putInt(REPEAT_MODE,repeatMode);
        return this;
    }

    public VideoFragment videoStateChangeListener(VideoStateChangeListener listener){
        videoStateChangeListener=listener;
        return this;
    }

    private SimpleExoPlayer newSimpleExoPlayer() {
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        LoadControl loadControl = new DefaultLoadControl();
        return ExoPlayerFactory.newSimpleInstance(getActivity(), trackSelector, loadControl);
    }

    private MediaSource newVideoSource(File url) {
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        String userAgent = Util.getUserAgent(getActivity(), "Advertiser");
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getActivity(), userAgent, bandwidthMeter);
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        return new ExtractorMediaSource(Uri.fromFile(url), dataSourceFactory, extractorsFactory, null, null);
    }

    private SimpleExoPlayerView playerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video, container, false);
        playerView =view.findViewById(R.id.player);
        return view;
    }

    protected HttpProxyCacheServer getProxy(){
        return ((AdvertiserApplication) getContext().getApplicationContext()).getProxy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (playerView!=null && playerView.getPlayer()!=null)
            playerView.getPlayer().release();
    }

}
