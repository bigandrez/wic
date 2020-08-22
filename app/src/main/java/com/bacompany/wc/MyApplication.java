package com.bacompany.wc;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.ads.reward.RewardedVideoAd;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by andre on 20.02.2017.
 */


public class MyApplication extends Application {
    private static MyApplication myapp;
    public static MyApplication getMyApp() {
        return MyApplication.myapp;
    }
    private static Context myappcontext;
    public static Context getMyAppContext() {
        return MyApplication.myappcontext;
    }
    public  class MyRunnable implements  Runnable{
        private String logstr;
        public void OutLog(String s){
            logstr = s;
        }
        @Override
        public void run() {
            ((MainActivity)act).MyLog(logstr);
        }
    }
    public  class MyRunnable3 implements  Runnable{
        private String logstr;
        public void OutLog(String s){
            logstr = s;
        }
        @Override
        public void run() {
            ((MainActivity)act).MyLogVar(logstr);
        }
    }
    public  class MyRunnable2 implements  Runnable{
        private boolean be;
        public void ButtonsEnabled(boolean b){be = b;}
        @Override
        public void run() {
            ((MainActivity)act).ButtonsEnabled(be);
        }
    }
    public static AppCompatActivity act;
    private Handler acth;
    public void setactivity(AppCompatActivity a){
        act = a;
    }
    public void sethandler(Handler a){
        acth = a;
    }
    public void log(String s) {
        MyRunnable r = new MyRunnable();
        r.OutLog(s);
        acth.post(r);
    }
    public void logvar(String s) {
        MyRunnable3 r = new MyRunnable3();
        r.OutLog(s);
        acth.post(r);
    }
    public void ButtonsEnabled(boolean be) {
        MyRunnable2 r = new MyRunnable2();
        r.ButtonsEnabled(be);
        acth.post(r);
    }

    @Override
    public void onCreate() {
        myapp = this;
        myappcontext = getApplicationContext();
        ReparseSettings();
        try_pro_mode=false;
        try_pro_mode_enable=false;
        pro_mode=true;
        stop_mode=false;
        creating_ringtone_mode=false;
    }


    public void ReparseSettings() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        prefix_for_incoming_call = prefs.getString("prefix_for_incoming_call",getString(R.string.prefix_for_incoming_call_value));
        suffix_for_incoming_call = prefs.getString("suffix_for_incoming_call",getString(R.string.suffix_for_incoming_call_value));
/*
        tts_mode=0;
        Resources res = getResources();
        String[] planets = res.getStringArray(R.array.use_tts);

        if (prefs.getString("select_tts",planets[0]).equals(planets[1]))
            tts_mode=1;
        if (prefs.getString("select_tts",planets[0]).equals(planets[2]))
            tts_mode=2;
*/
        tts_mode =Integer.parseInt(prefs.getString("select_tts","0"));
        ringtone_freq =Integer.parseInt(prefs.getString("select_freq","8000"));

        locale = prefs.getString("select_locale",Locale.getDefault().toString());

        Uri currentRintoneUri = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
        Ringtone currentringtone = RingtoneManager.getRingtone(this, currentRintoneUri);
        String currenttitle = currentringtone.getTitle(this);
        ringtone_uri = prefs.getString("ringtone_for_calling",RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_RINGTONE).toString());
        Ringtone ringtone = RingtoneManager.getRingtone(this, Uri.parse(ringtone_uri));
        String title = ringtone.getTitle(this);

        if (title.indexOf(currenttitle)>=0)
            ringtone_uri = currentRintoneUri.toString();


        only_changed_contacts = prefs.getBoolean("only_changed_contacts",true);
//        forced_transliteration_to_english = prefs.getBoolean("forced_transliteration_to_english",false);
        timeout_before_speech = Integer.parseInt(prefs.getString("timeout_before_speech","3"));
        ringtone_duration = Integer.parseInt(prefs.getString("ringtone_duration","25"));
    }
    public RewardedVideoAd mAd;
    // 0 - id
    // 1 - имя
    // 2 - путь к исходному рингтону
    // 3 - требует ли контакт обновления
    public static ArrayList<ArrayList<String>>  contact_list;
    public boolean ru_tts_available;
    public boolean en_tts_available;
    public String prefix_for_incoming_call;
    public String suffix_for_incoming_call;
    public int tts_mode;
    public int ringtone_freq;
    public String ringtone_uri;
    public boolean only_changed_contacts;
//    public boolean forced_transliteration_to_english;
    public int timeout_before_speech;
    public int ringtone_duration;
    public String locale;
    public boolean try_pro_mode;
    public boolean try_pro_mode_enable;
    public boolean pro_mode;
    public boolean stop_mode;
    public boolean creating_ringtone_mode;
    public TextToSpeech mTts;
}
