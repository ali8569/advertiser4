package ir.markazandroid.advertiser.util;

import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;

import ir.markazandroid.advertiser.fragment.ImageFragment;
import ir.markazandroid.advertiser.fragment.VideoFragment;
import ir.markazandroid.advertiser.object.Content;

/**
 * Coded by Ali on 10/21/2018.
 */
public class ContentAdapter {

    private FragmentManager fragmentManager;
    private ArrayList<Content> contents;
    private ArrayList<Fragment> fragments;
    private int containerId;
    private Timer timer;
    private Fragment currentFragment;
    private Handler handler;

    public ContentAdapter(Handler handler, FragmentManager fragmentManager, ArrayList<Content> contents, @IdRes int containerId) {
        this.fragmentManager = fragmentManager;
        this.contents = contents;
        this.containerId = containerId;
        this.handler=handler;
        init();
    }

    private void init(){

        fragments=new ArrayList<>(contents.size());

        for(Content content:contents){
            Fragment fragment;

            switch (content.getType()){
                case Content.TYPE_IMAGE:
                    fragment=new ImageFragment().image(content);break;

                case Content.TYPE_VIDEO:
                    fragment=new VideoFragment().videoUrl(new ArrayList<>(Arrays.asList(content.getFile())))
                            .videoStateChangeListener(this::onVideoFinish);break;

                    default: fragment=new Fragment();

            }
            fragments.add(fragment);
        }
    }

    public void start(){
        Iterator<Content> contentIterator = contents.iterator();
        if (contentIterator.hasNext()){
            schedule(contentIterator);
        }
    }

    private void schedule(Iterator<Content> contentIterator) {
        handler.post(() ->{
            if (timer!=null) timer.cancel();

            if (!contentIterator.hasNext()) {
                schedule(contents.iterator());
                return;
            }

            Content content = contentIterator.next();

            makeView(content);

            if (content.getType().equals(Content.TYPE_IMAGE) && contents.size()>1){
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        ContentAdapter.this.schedule(contentIterator);
                    }
                },content.getDuration()*1000);
            }
        });
    }

    private void makeView(Content content) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (currentFragment!=null){
            transaction.detach(currentFragment);
        }
        String name = generateFragmentTag(content);
        Fragment fragment = fragmentManager.findFragmentByTag(name);
        if (fragment != null) {
            transaction.attach(fragment);
        } else {
            fragment = fragments.get(contents.indexOf(content));
            transaction.add(containerId, fragment, generateFragmentTag(content));
        }
        currentFragment=fragment;
        transaction.commit();
    }

    private String generateFragmentTag(Content content){
        return containerId+"."+content.hashCode();
    }

    public void dispose(){
        if (timer!=null) timer.cancel();
        timer=null;
    }

    private void onVideoFinish(Fragment frag) {
        int index = fragments.indexOf(frag);
        index++;
        if (index==fragments.size())
            index=0;

        ListIterator<Content> iterator = contents.listIterator(index);
        schedule(iterator);
    }
}
