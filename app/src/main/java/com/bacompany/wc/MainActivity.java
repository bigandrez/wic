/*
Сделать уровень громкости рингтона и отвучки в %
Сделать кнопку стоп
режим про - сделать чтобы в "не про" был только встроенный рингтон
копирование библиотеки ffmpeg для android 6
звонок при завершении
русификация
проверить создание рингтонов когда потух экран
режим "все контакты"
 */

package com.bacompany.wc;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaCodec;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Telephony;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.netcompss.ffmpeg4android.GeneralUtils;
import com.netcompss.loader.LoadJNI;



public class MainActivity extends AppCompatActivity implements RewardedVideoAdListener {
    private MediaCodec codec;
    private TextView LV;
    private static final List<String> SUPPORTED_EXTENSION = Arrays.asList("mp3", "ogg", "wav", "m4a", "amr", "aac", "3gp", "mkv", "flac");
    public String TEMP_PATH;
    public String RINGTONE_PATH;
    public static String LogString="";


    public void MyLogVar(String s){

        int p1 = LogString.lastIndexOf('[');
        int p2 = LogString.lastIndexOf(']');

        if (p1<0 || p2<0) return;

        LogString = LogString.substring(0,p1+1) + s + LogString.substring(p2);
        LV.setText(Html.fromHtml(LogString), TextView.BufferType.SPANNABLE);
    }

    public void MyLog(String s){

//        String t = LV.getText().toString();
//        t += s;
//        LV.setText(t);

        LogString += s;
        LV.setText(Html.fromHtml(LogString), TextView.BufferType.SPANNABLE);
    }

    public void ButtonsEnabled(boolean b){

        if (!b)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        Button b1 = (Button) findViewById(R.id.start_conversion);
        Button b2 = (Button) findViewById(R.id.start_sample);
        Button b3 = (Button) findViewById(R.id.remove_contacts_ringtones);
        Button b4 = (Button) findViewById(R.id.stop);

        if (!b) {
            b1.setVisibility(View.GONE);
            b2.setVisibility(View.GONE);
            b3.setVisibility(View.GONE);
            b4.setVisibility(View.VISIBLE);
        } else {
            b1.setVisibility(View.VISIBLE);
            b2.setVisibility(View.VISIBLE);
            b3.setVisibility(View.VISIBLE);
            b4.setVisibility(View.GONE);
        }
        MyApplication myApp=MyApplication.getMyApp();
        myApp.stop_mode = false;
        if (b) myApp.creating_ringtone_mode=false;
        else myApp.creating_ringtone_mode=true;
    }

    public void MyLogInvalidate(){
        LV.postInvalidate();
    }
//    SharedPreferences sp;

    private void loadRewardedVideoAd() {
        MyApplication myApp=MyApplication.getMyApp();
        myApp.mAd.loadAd("ca-app-pub-5368195271545700/1838884074", new AdRequest.Builder().build());
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish(); // close this activity as oppose to navigating up

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setHomeAsUpIndicator(R.drawable.places_ic_clear);

        MyApplication myApp = MyApplication.getMyApp();

        TEMP_PATH = getCacheDir() + "/";
        RINGTONE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES).getPath() + "/";
        /*
        try {
            File dbdir = new File(RINGTONE_PATH);
            if (!dbdir.exists() && !dbdir.mkdirs())
                MyLog(String.format(getString(R.string.cant_create_path),RINGTONE_PATH));
        } catch (Throwable t) {
//            MyLog("Error: can't create path "+RINGTONE_PATH+"<br/>");
            MyLog(String.format(getString(R.string.cant_create_path),RINGTONE_PATH));
        }
*/
//        If Targeting Android 6 and above, you need to add runtime permissions:
//        Call the method: GeneralUtils.checkForPermissionsMAndAbove(Main.this, false);

        myApp.setactivity(this);
        myApp.sethandler(new Handler());

        if (!myApp.pro_mode) {
            myApp.mAd = MobileAds.getRewardedVideoAdInstance(this);
            myApp.mAd.setRewardedVideoAdListener(this);
            loadRewardedVideoAd();
        }


        MobileAds.initialize(getApplicationContext(), "ca-app-pub-5368195271545700~3376969677");

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest asRequest = new AdRequest.Builder().build();
        mAdView.loadAd(asRequest);
        mAdView.setAdListener(new AdListener() {
            public void onAdLeftApplication ()
            {
                Button b1 = (Button) findViewById(R.id.start_conversion);
                b1.setEnabled(true);
                b1.setText(R.string.start_creating_ringtones);
            }
        });

//        String date = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date());
//        MyLog("Log started at "+date+ "<br/>");


//        myApp.log("Initialize internal TTS<br/>");

//        sp = PreferenceManager.getDefaultSharedPreferences(this);
        File file = new File(getCacheDir().getAbsolutePath() + "/ffmpeglicense.lic");
        boolean r = file.delete();
        r = false;

//        DoGetContacts();

        boolean allow_get_contacts = true;
        List<String> permissions = new ArrayList<String>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_CONTACTS);
                allow_get_contacts = false;
            }
            if (checkSelfPermission(Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED)
                permissions.add(Manifest.permission.WAKE_LOCK);
            if (checkSelfPermission(Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED)
                permissions.add(Manifest.permission.WRITE_CONTACTS);
            if (checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)
                permissions.add(Manifest.permission.INTERNET);
            if (checkSelfPermission(Manifest.permission.WRITE_SETTINGS) != PackageManager.PERMISSION_GRANTED)
                permissions.add(Manifest.permission.WRITE_SETTINGS);
            if (checkSelfPermission(Manifest.permission.CHANGE_CONFIGURATION) != PackageManager.PERMISSION_GRANTED)
                permissions.add(Manifest.permission.CHANGE_CONFIGURATION);
            if (checkSelfPermission(Manifest.permission.MODIFY_AUDIO_SETTINGS) != PackageManager.PERMISSION_GRANTED)
                permissions.add(Manifest.permission.MODIFY_AUDIO_SETTINGS);
            String[] perm = new String[ permissions.size() ];
            permissions.toArray( perm );
            try {requestPermissions(perm, 1);} catch (Throwable t) {}
        }

        LV = (TextView) findViewById(R.id.LogView);
        LV.setMovementMethod(new ScrollingMovementMethod());

        if (myApp.mTts==null) {
            // Log preparing

            MyLog(getString(R.string.helptextpro));
            if (!myApp.pro_mode)
                MyLog(getString(R.string.helptextprolite));

            myApp.ru_tts_available = myApp.en_tts_available = false;
            myApp.mTts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    MyApplication myApp = (MyApplication) getApplication();
                    if (status != TextToSpeech.ERROR) {
                        myApp.en_tts_available = true;
                        if (myApp.mTts.isLanguageAvailable(Locale.US) >= 0) {
                            myApp.en_tts_available = true;
                            myApp.mTts.setLanguage(Locale.US);
                        }
                        myApp.log(getString(R.string.internal_tts_ok));
                    } else myApp.log(getString(R.string.internal_tts_error));
//                myApp.ButtonsEnabled(true);
                }
            });
            if (allow_get_contacts)
                getContacts();

            try {
                // path for android 6
                InputStream is = getResources().openRawResource(R.raw.libvideokit);
                String p2 = getApplicationContext().getFilesDir().getPath();
                p2 = p2.substring(0, p2.length() - 5);
                File f2 = new File(p2 + "zlib");
                if (!f2.exists())
                    f2.mkdirs();
                File f = new File(p2 + "zlib/libvideokit.so");
                if (!f.exists()) {
                    OutputStream out = new FileOutputStream(f);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = is.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.close();
                    is.close();
                }
            } catch (Throwable e) {}

        } else
            ButtonsEnabled(!myApp.creating_ringtone_mode);
        myApp.log("");

/*

        String[] perm = new String[1];
        boolean r1 = true;
        boolean r2 = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            r1 = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
            r2 = false;
        if (!r1 && !r2)
            perm = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_CONTACTS};
        else
            if (!r1)
                perm = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
            else
                if (!r2)
                    perm = new String[]{Manifest.permission.READ_CONTACTS};
        if (!r1 || !r2)
        {
            try {
                requestPermissions(perm, 1);
            } catch (Throwable t) {
            }
        }
        if (r2) getContacts();
*/



        //float ver = Float.parseFloat(""+Build.VERSION.RELEASE);


    }

    public void OnAdlayerClick(View view) {
        Button b1 = (Button) findViewById(R.id.start_conversion);
        b1.setEnabled(true);
        b1.setText(R.string.start_creating_ringtones);
    }

    @Override
    public void onRewarded(RewardItem reward) {
        Toast.makeText(this, "onRewarded! currency: " + reward.getType() + "  amount: " +
                reward.getAmount(), Toast.LENGTH_SHORT).show();
        MyApplication myApp=MyApplication.getMyApp();
        myApp.try_pro_mode = true;
        myApp.try_pro_mode_enable = false;
        myApp.log(myApp.getString(R.string.pro_mode_activated));
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
//        Toast.makeText(this, "onRewardedVideoAdLeftApplication",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdClosed() {
//        Toast.makeText(this, "onRewardedVideoAdClosed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int errorCode) {
        MyApplication myApp=MyApplication.getMyApp();
        myApp.try_pro_mode_enable = false;
//        Toast.makeText(this, "onRewardedVideoAdFailedToLoad", Toast.LENGTH_SHORT).show();
        loadRewardedVideoAd();
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        MyApplication myApp=MyApplication.getMyApp();
        if (!myApp.try_pro_mode_enable && !myApp.try_pro_mode) {
            Toast.makeText(this, getString(R.string.try_pro_mode_enabled), Toast.LENGTH_SHORT).show();
            myApp.log(myApp.getString(R.string.try_pro_mode_enabled)+"<br/>");
        }
        myApp.try_pro_mode_enable = true;

//        Toast.makeText(this, "onRewardedVideoAdLoaded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdOpened() {
  //      Toast.makeText(this, "onRewardedVideoAdOpened", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoStarted() {
//        Toast.makeText(this, "onRewardedVideoStarted", Toast.LENGTH_SHORT).show();
    }

    private String getSDcardPath()
    {
        String exts =  Environment.getExternalStorageDirectory().getPath();
        String sdCardPath = null;
        try
        {
            FileReader fr = new FileReader(new File("/proc/mounts"));
            BufferedReader br = new BufferedReader(fr);
            String line;
            while((line = br.readLine())!=null)
            {
                if(line.contains("secure") || line.contains("asec"))
                    continue;
                if(line.contains("fat"))
                {
                    String[] pars = line.split("\\s");
                    if(pars.length<2)
                        continue;
                    if(pars[1].equals(exts))
                        continue;
                    sdCardPath =pars[1];
                    break;
                }
            }
            fr.close();
            br.close();
            return sdCardPath;
        }
        catch (Exception e)
        {
            //e.printStackTrace();
            //textInfo.setText(e.toString());
        }
        return sdCardPath;
    }

    public void SaveFile (String filePath, String FileContent)
    {
        //Создание объекта файла.
        File fhandle = new File(filePath);
        try
        {
            //Если нет директорий в пути, то они будут созданы:
            if (!fhandle.getParentFile().exists())
                fhandle.getParentFile().mkdirs();
            //Если файл существует, то он будет перезаписан:
            fhandle.createNewFile();
            FileOutputStream fOut = new FileOutputStream(fhandle);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.write(FileContent);
            myOutWriter.close();
            fOut.close();
        }
        catch (IOException e)
        {
            //e.printStackTrace();
            //textInfo.setText("Path " + filePath + ", " + e.toString());
        }
    }

    public static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }


    private void DoGetContacts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            try {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS/*, Manifest.permission.READ_EXTERNAL_STORAGE*/}, 1);
            } catch (Throwable t) {}
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            ButtonsEnabled(false);
            Runnable runnable = new Runnable() {
                public void run() {
                    try{
                        MyApplication myApp=MyApplication.getMyApp();
                        ((MainActivity)myApp.act).getContacts();
                    } catch (Throwable t) {

                    }
                }
            };
            Thread thread = new Thread(runnable);
            thread.start();
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        MyApplication myApp = MyApplication.getMyApp();
        int i=0;
        for (i=0;i<permissions.length;i++) {
            if (permissions[i].equals(Manifest.permission.READ_CONTACTS)) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        ((MainActivity) myApp.act).getContacts();
                    } catch (Throwable t) {

                    }
                } else {
                    myApp.log(getString(R.string.contacts_permission_required) + "<br/>");
                }
            }
            if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                    myApp.log(getString(R.string.external_permission_required) + "<br/>");
            }
        }
    }

    public String ActivateRingtone(String title, String filename){
        String ret = "";
        try {
            File k = new File(filename);

            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, k.getAbsolutePath());
            values.put(MediaStore.MediaColumns.TITLE, title);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
            values.put(MediaStore.MediaColumns.SIZE, k.length());
            values.put(MediaStore.Audio.Media.ARTIST, R.string.app_name);
            values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
            values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
            values.put(MediaStore.Audio.Media.IS_ALARM, false);
            values.put(MediaStore.Audio.Media.IS_MUSIC, false);

            Uri uri = MediaStore.Audio.Media.getContentUriForPath(k.getAbsolutePath());

            getContentResolver().delete(
                    uri,
                    MediaStore.MediaColumns.DATA + "=\""
                            + k.getAbsolutePath() + "\"", null);
            Uri newUri = getContentResolver().insert(uri, values);
            ret = newUri.toString();
        } catch (Throwable t) {

        }
        return ret;
    }

    public void setContactRingtone(String id, String uri) {
        try {
            Uri lookupUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, id);
            ContentValues localContentValues = new ContentValues();
            localContentValues.put(ContactsContract.Data.RAW_CONTACT_ID, id);
            localContentValues.put(ContactsContract.Data.CUSTOM_RINGTONE, uri);
            getContentResolver().update(lookupUri, localContentValues,null, null);
        } catch (Throwable t) {

        }
    }

    public static void getContacts() {

//        setContactRingtone("481","");
        try{
            Thread.sleep(1000);
        } catch (Throwable t) {

        }

        MyApplication myApp=MyApplication.getMyApp();
        myApp.ButtonsEnabled(false);
        myApp.contact_list = new ArrayList<>();


        //Связываемся с контактными данными и берем с них значения id контакта, имени контакта и его номера:
        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String _CUSTOM_RINGTONE = ContactsContract.Contacts.CUSTOM_RINGTONE;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

        String [][] ar;

        ContentResolver contentResolver = myApp.getContentResolver();

        Cursor cursor = contentResolver.query(CONTENT_URI, null,null, null, null);
        float i=0;

        //Запускаем цикл обработчик для каждого контакта:
        int cc = cursor.getCount();
        if (cc>500)
            myApp.log(myApp.getString(R.string.many_contacts_warning));
        myApp.log(myApp.getString(R.string.loading_contacts)+"<br/>");
        if (cc > 0) {

            //Если значение имени и номера контакта больше 0 (то есть они существуют) выбираем
            //их значения в приложение привязываем с соответствующие поля "Имя" и "Номер":
            while (cursor.moveToNext()) {
                String contact_id = cursor.getString(cursor.getColumnIndex( _ID ));
                String ringtone = cursor.getString(cursor.getColumnIndex( _CUSTOM_RINGTONE ));
                String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));
                if (ringtone == null) ringtone="";

                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER)));

                //Получаем имя:
//                if (hasPhoneNumber > 0)
                    if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0)
                {
                    /*
                    Cursor pCur = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                            new String[]{contact_id}, null);
                    String phoneNo = "";
                    if (pCur.moveToNext())
                        phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        */
                    ArrayList<String> al = new ArrayList<>();
                    al.add("" + contact_id);
                    al.add(name);
                    al.add(""+ringtone);
                    if (ringtone.equals(""))
                        al.add("true");
                    else
                        al.add("false");
//                    al.add(phoneNo);
                    myApp.contact_list.add(al);
                    i++;
                    if (Math.round(i/(10.f))*10==Math.round(i))
                        myApp.logvar(""+Math.round(i));
                    try{Thread.sleep(1);} catch (Throwable t) {}
                }
            }
        }
        myApp.logvar(myApp.getString(R.string.green_ok));

        myApp.log(myApp.getString(R.string.preparing_contacts)+"<br/>");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(myApp);


        try{
            ArrayList<ArrayList<String>>  old_contact_list;
            String s = prefs.getString("contactlist", "");
            if (s.equals(""))
                old_contact_list = myApp.contact_list;
            else
                old_contact_list = (ArrayList<ArrayList<String>>)ObjectSerializer.deserialize(s);

//            String s = prefs.getString("contactlist", ObjectSerializer.serialize( myApp.contact_list));
//            old_contact_list = (ArrayList<ArrayList<String>>)ObjectSerializer.deserialize(s);

            i=0.f;

            for (ArrayList<String> o: myApp.contact_list) {
                String id = o.get(0);
                String name = o.get(1);
                for (ArrayList<String> l: old_contact_list) {
                    if (!l.get(0).equals(id)) continue;
                    if (!l.get(1).equals(name)) {
                        o.set(1, name);
                        o.set(3, "true");
                    }
                    if (l.size()>3 && (!l.get(3).equals("false")))
                        o.set(3, "true");
                    break;
                }
                i++;
                if (Math.round(i/10.f)*10==Math.round(i))
                    myApp.logvar(""+Math.round(i));
            }

            int c=0;
            for (ArrayList<String> o: myApp.contact_list) {
                if (!o.get(3).equals("false")) c++;
            }
            myApp.log(String.format(myApp.getString(R.string.need_update_contacts),myApp.contact_list.size(),c));
            myApp.log(myApp.getString(R.string.helptextbuttons));

//            MyLog(""+c+" contact items need to update<br/>");
        } catch (IOException e){
            myApp.log("IOException in getContacts<br/>");
        } catch(ClassNotFoundException c) {
            myApp.log("ClassNotFoundException in getContacts<br/>");
        }
        myApp.logvar(myApp.getString(R.string.green_ok));

        myApp.ButtonsEnabled(true);
/*
        Map<String, ?> allEntries = prefs.getAll();
        allEntries.clear();

        try{
            prefs.edit().putString( "contactlist", ObjectSerializer.serialize( myApp.contact_list) ).commit();
        } catch (IOException e) {

        }

        try{
            String s = prefs.getString("contactlist", ObjectSerializer.serialize( myApp.contact_list));
            old_contact_list = (ArrayList<ArrayList<String>>)ObjectSerializer.deserialize(s);
        } catch (IOException e){
        } catch(ClassNotFoundException c) {
        }
*/
    }

    public void SaveContactList(){
        MyApplication myApp=(MyApplication)getApplication();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        try{
            prefs.edit().putString( "contactlist", ObjectSerializer.serialize( myApp.contact_list) ).commit();
        } catch (IOException e){
            MyLog("IOException in SaveContactList<br/>");
        }
    }

    private String getUriExtension(Uri uri) {
        String extension = null;
// Еще нужна кнопка "настройки" - не у всех телефонов такая аппаратная кнопка есть
// Здесь нужно уточнить. Несколько телефонов дают разный type для mp3 рингтона
        String type = MyApplication.getMyAppContext().getContentResolver().getType(uri);
        if (type != null) {
            if (type.equals("audio/mpeg") || type.equals("application/mpeg") || type.equals("audio/mp3")) {
                extension = "mp3";
            } else if (type.equals("audio/mp4") || type.equals("application/mp4")) {
                extension = "m4a";
            } else if (type.equals("audio/x-wav") || type.equals("application/x-wav")) {
                extension = "wav";
            } else if (type.equals("audio/ogg") || type.equals("application/ogg")) {
                extension = "ogg";
            } else if (type.equals("audio/amr") || type.equals("application/amr")) {
                extension = "amr";
            } else if (type.equals("audio/aac") || type.equals("application/aac")) {
                extension = "aac";
            } else if (type.equals("audio/3gpp") || type.equals("application/3gpp")) {
                extension = "3gp";
            } else if (type.equals("audio/x-matroska") || type.equals("application/x-matroska")) {
                extension = "mkv";
            } else if (type.equals("audio/flac") || type.equals("application/x-flac") || type.equals("application/flac") || type.equals("application/x-flac")) {
                extension = "flac";
            } else {
                MyApplication myApp=(MyApplication)getApplication();
                myApp.log("Unknown ringtone type - \""+type+"\". Using mp3 type<br/>");
                extension = "mp3";
            }
        } else {
            String filePath = uri.toString();
            if (filePath.startsWith("file://")) {
                int lastIndex = filePath.lastIndexOf(".");
                if ((lastIndex > -1) && (lastIndex != filePath.length())) {
                    extension = filePath.substring(lastIndex + 1).toLowerCase();
                }
            }
        }

        if (SUPPORTED_EXTENSION.contains(extension)) {
            return extension;
        } else {
            return null;
        }
    }

    private String UriToFile(Uri uri) {
        MyApplication myApp=MyApplication.getMyApp();
        String filename = "";

        myApp.log(myApp.getString(R.string.create_source_ringtone_file)+"<br/>");
        if (uri.toString().isEmpty()) {
            myApp.logvar(myApp.getString(R.string.green_empty));
            return "";
        }

        try {
            String ext = getUriExtension(uri);
            if (ext==null || ext.isEmpty()) return "";
//            filename = getCacheDir() + "/ringtone."+ext;
            filename = TEMP_PATH + "ringtone."+ext;
            InputStream in = getContentResolver().openInputStream(uri);
            File file = new File(filename);

            if (megabytesAvailable(file)<0.5f){
                myApp.log(myApp.getString(R.string.not_enough_space));
                return "";
            }

            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
            out.close();
            in.close();

            long length = file.length();
            if (length<=1000)
                myApp.logvar(myApp.getString(R.string.red_error));
            else
                myApp.logvar("<font color=\"#00b000\">"+ file.getName() + "</font> <font color=\"#0000b0\">" + length/1024+"kb</font>");

            if (length<=1000) filename="";

        } catch (Exception e) {
            myApp.log("Exception in UriToFile<br/>");
            myApp.logvar(myApp.getString(R.string.red_error));
            return "";
        }
/*
        if (filename!="") myApp.log("Created source ringtone file is "+filename+"<br/>");
        else myApp.log(getString(R.string.error_create_speech));
*/
        return filename;
    }

    public String DownloadSpeech(String text) {
        MyApplication myApp=MyApplication.getMyApp();
        String filename = "";
        try {
            myApp.log(myApp.getString(R.string.try_use_external_tts)+"<br/>");
//            myApp.log("Locale is \""+myApp.locale+"\"<br/>");


            String TEXT_TO_SPEECH_SERVICE = "http://translate.google.com/translate_tts";
            String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:11.0) " + "Gecko/20100101 Firefox/11.0";
            text = URLEncoder.encode(text, "utf-8");

            String strUrl = TEXT_TO_SPEECH_SERVICE + "?" + "tl=" + myApp.locale + "&client=tw-ob" + "&q=" + text;
            URL url = new URL(strUrl);

            // Etablish connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // Get method
            connection.setRequestMethod("GET");
            connection.setReadTimeout(10000);
            // Set User-Agent to "mimic" the behavior of a web browser. In this
            // example, I used my browser's info
            connection.addRequestProperty("User-Agent", USER_AGENT);
//            connection.setDoOutput(false);
            connection.connect();

            // Get content
            BufferedInputStream bufIn =
                    new BufferedInputStream(connection.getInputStream());
            byte[] buffer = new byte[1024];
            int n;
            ByteArrayOutputStream bufOut = new ByteArrayOutputStream();
            while ((n = bufIn.read(buffer)) > 0) {
                bufOut.write(buffer, 0, n);
            }

            filename = TEMP_PATH + "tts.mp3";
            File output = new File(filename);
            if (megabytesAvailable(output)<0.5f){
                myApp.logvar(myApp.getString(R.string.red_error));
                myApp.log(myApp.getString(R.string.not_enough_space));
                return "";
            }


            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(output));

            // Done, save data;
            out.write(bufOut.toByteArray());
            out.flush();
        }   catch (Throwable e) {
            myApp.logvar(myApp.getString(R.string.red_error));
        }

        File file = new File(filename);
        long length = file.length();

        if (length<=1000)
            myApp.logvar(myApp.getString(R.string.red_error));
        else
            myApp.logvar("<font color=\"#00b000\">" + file.getName() + "</font> <font color=\"#0000b0\">" + length/1024+"kb</font>");


        if (length<=1000) {
            myApp.log(myApp.getString(R.string.error_download_speech));
            filename = "";
        }
        return filename;
    }

    public String CreateSpeech(String text){
        String filename = "";
        MyApplication myApp=MyApplication.getMyApp();
        if (myApp.stop_mode) return "";
        try {

            myApp.log(myApp.getString(R.string.try_use_local_tts));

            Locale locale = new Locale(myApp.locale);
            if (myApp.mTts.setLanguage(locale)<0){
                myApp.logvar(myApp.getString(R.string.red_error));
                myApp.log("Setting locale \""+myApp.locale+"\" failed<br/>");
                return "";
            }

            filename = TEMP_PATH + "speech.wav";

            HashMap<String, String> myHashRender = new HashMap();
            String wakeUpText = text;
            myHashRender.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ringtonespeech");
            myApp.mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    MyApplication myApp=MyApplication.getMyApp();
//                    myApp.log("TTS started<br/>");
                }

                @Override
                public void onDone(String utteranceId) {
                    MyApplication myApp=MyApplication.getMyApp();
                    myApp.log("<br/>");
                }

                @Override
                public void onError(String utteranceId) {
                    MyApplication myApp=MyApplication.getMyApp();
                    myApp.log("Error in TTS<br/>");
                }
            });

            File tf = new File(filename);
            if (megabytesAvailable(tf)<0.5f){
                myApp.logvar(myApp.getString(R.string.red_error));
                myApp.log(myApp.getString(R.string.not_enough_space));
                return "";
            }
            tf.delete();

            myApp.mTts.synthesizeToFile(wakeUpText, myHashRender, filename);

            int i=0;
            while (myApp.mTts.isSpeaking()) {
                if (myApp.stop_mode){
                    myApp.mTts.stop();
                    myApp.logvar(myApp.getString(R.string.red_cancel));
                    return "";
                }
                Thread.sleep(1000);
                i++;
                myApp.logvar(""+i);
            }

            File file = new File(filename);
            long length = file.length();

            if (length<=1000)
                myApp.logvar(myApp.getString(R.string.red_error));
            else
                myApp.logvar("<font color=\"#00b000\">" + file.getName() + "</font> <font color=\"#0000b0\">" + length/1024+"kb</font>");

            if (length <= 1000) filename = "";

        } catch (Exception e) {
            myApp.log("Exception in GetSpeech<br/>");
            myApp.logvar(myApp.getString(R.string.red_error));
            filename = "";
        }
        /*
        if (!filename.isEmpty())
            myApp.log("Created speech file is " + filename + "<br/>");
        else
            myApp.log("Failed for create speech file<br/>");
            */
        return filename;

    }

    public String GetSpeech(String text){
        if (text==null || text.isEmpty()) return "";
        MyApplication myApp=MyApplication.getMyApp();
        String filename = "";
        int number_of_attempts=3;
        if (myApp.tts_mode!=2) {
            while (number_of_attempts-->0) {
                filename = CreateSpeech(text);
                if (!filename.isEmpty()) break;
                if (myApp.stop_mode) break;
                myApp.log("Wait two seconds<br/>");
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {}
            }
        }
        if (myApp.tts_mode!=1 && filename.isEmpty() && myApp.stop_mode!=true) {
            number_of_attempts=3;
            while (number_of_attempts-->0) {
                filename = DownloadSpeech(text);
                if (myApp.stop_mode) break;
                if (!filename.isEmpty())
                    break;
            }
        }
        return filename;
   }

    public void copyFile(String s, String d)  {
        try {
            File src = new File(s);
            File dst = new File(d);
            FileInputStream inStream = new FileInputStream(src);
            FileOutputStream outStream = new FileOutputStream(dst);
            FileChannel inChannel = inStream.getChannel();
            FileChannel outChannel = outStream.getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
            inStream.close();
            outStream.close();
        } catch (Throwable e) {

        }
    }

    public boolean CreateRingtone(Uri ringtone_uri, String contactname, String outfile){
        MyApplication myApp=MyApplication.getMyApp();
        final String ringtone_file = UriToFile(ringtone_uri);
//        if (ringtone_file==null || ringtone_file.isEmpty()) return false;
        final String out_file = outfile;

        /*
        File tf = new File(out_file);
        if (megabytesAvailable(tf)<0.5f){
            myApp.log(getString(R.string.not_enough_space));
            return false;
        }
        tf.delete();
*/
        final String speech_file = GetSpeech(contactname);
        if (speech_file==null || speech_file.isEmpty()) return false;


/*

        String p2 = getApplicationContext().getFilesDir().getPath();
        p2 = p2.substring(0,p2.length()-5);
        {
            File f = new File(p2 + "lib2");
            f.mkdirs();
        }
*/

//        copyFile("/storage/sdcard0/libvideokit.so", "/data/user/0/com.bacompany.wc/files/libvideokit.so");
//        copyFile("/storage/sdcard0/libloader-jni.so", "/data/user/0/com.bacompany.wc/files/libloader-jni.so");
//        copyFile("/storage/sdcard0/liblicense-jni.so", "/data/user/0/com.bacompany.wc/files/liblicense-jni.so");

        myApp.log(myApp.getString(R.string.waiting_for_create_ringtone)+"<br/>");

        Thread hffmpeg = new Thread() {
            public void run() {
                MyApplication myApp=MyApplication.getMyApp();
                GeneralUtils.checkForPermissionsMAndAbove(myApp.act, true);
                LoadJNI vk = new LoadJNI();

                // Исправляем ошибку, когда на внешней SD карте нет папки рингтонов
                try {
                    new File(out_file.substring(0,out_file.lastIndexOf("/"))).mkdirs();
                } catch (Throwable e) {
                    myApp.log("Exception on create ringtone directory<br/>");
                    myApp.logvar(myApp.getString(R.string.red_error));
                    return;
                }


                try {
                    Context a = MyApplication.getMyAppContext();
                    File b = a.getFilesDir();
                    String c = b.getAbsolutePath();
                    String workFolder = getApplicationContext().getFilesDir().getAbsolutePath();
                    workFolder = getCacheDir().getAbsolutePath();
 //                   String[] complexCommand2 = {"ffmpeg","-y", "-i", ringtone_file, out_file};

                    String[] complexCommand;
                    if (!ringtone_file.isEmpty()) {
                        String[] c1 = {"ffmpeg", "-y", "-t", "3.0", "-i", ringtone_file,
                                "-i", speech_file,
                                "-ss", "" + myApp.timeout_before_speech, "-i", ringtone_file,
                                "-filter_complex",
                                "[0][1]concat=v=0:a=1[a1];[a1][2]concat=v=0:a=1[out]", "-map", "[out]", "-ar", "" + myApp.ringtone_freq, "-t",
                                "" + myApp.ringtone_duration, "-ac", "1", out_file};
                        vk.run(c1 , workFolder , MyApplication.getMyAppContext());
                    } else {
                        String[] c2 = {"ffmpeg", "-y",
                                "-i", speech_file, "-ar", "" + myApp.ringtone_freq, "-t",
                                "" + myApp.ringtone_duration, "-ac", "1", out_file};
                        vk.run(c2 , workFolder , MyApplication.getMyAppContext());
                    }


                } catch (Throwable e) {
                    myApp.log("Exception in CreateRingtone<br/>");
                    myApp.logvar(myApp.getString(R.string.red_error));
                }
            }
        };

        hffmpeg.start();
        try {
            int i=0;
            while (hffmpeg.isAlive()) {
                if (myApp.stop_mode){
                    hffmpeg.interrupt();
                    hffmpeg = null;
                    myApp.logvar(myApp.getString(R.string.red_cancel));
                    return false;
                }

                Thread.sleep(1000);
                i++;
                myApp.logvar(""+i);
            }
        } catch (Throwable e) {
            myApp.log("Exception in CreateRingtone<br/>");
            myApp.logvar(myApp.getString(R.string.red_error));
            return false;
        }
        try {
            File f = new File(outfile);
            long length = f.length();

            if (length<=1000)
                myApp.logvar(myApp.getString(R.string.red_error));
            else
                myApp.logvar("<font color=\"#00b000\">" + f.getName() + "</font> <font color=\"#0000b0\">" + length/1024+"kb</font>");

            if (length<=1000) {
                return false;
            }
        } catch (Throwable e) {
            myApp.logvar(myApp.getString(R.string.red_error));
            myApp.log("Exception in CreateRingtone<br/>");
            return false;

        }
/*
        myApp.logvar(myApp.getString(R.string.green_ok));
        myApp.log("Created ringtone file is "+outfile+"<br/>");
*/
        return true;
        //getExternalStoragePublicDirectory(),
    }

    public static boolean isNumeric(String str)
    {
        for (char c : str.toCharArray())
        {
            if (!Character.isDigit(c) && c!='+') return false;
        }
        return true;
    }

    public String PrepareContact(String contactname){
        if (!isNumeric(contactname.trim())) return contactname;
        return contactname.substring(0,2)+" "+contactname.substring(2,5)+" "+contactname.substring(5,8)+" "+contactname.substring(8,10)+" "+contactname.substring(10,12); // error 
    }

    public void OnSampleClick(View view) {
/*
        if (mAd.isLoaded()) {
            mAd.show();
        }
*/
        ButtonsEnabled(false);
        Runnable runnable = new Runnable() {
            public void run() {
                MyApplication myApp=MyApplication.getMyApp();
                myApp.log("<br/>");
//                Uri currentRintoneUri = RingtoneManager.getActualDefaultRingtoneUri(myApp.getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
                Uri currentRintoneUri = Uri.parse(myApp.ringtone_uri);
                String ringtonefilename = TEMP_PATH + "sampleringtone.mp3";

                String contacttext = "Ivan";

                for (ArrayList<String> object: myApp.contact_list) {
                    if (object.get(1).length()>contacttext.length())
                        contacttext =object.get(1);
                }

                contacttext = myApp.prefix_for_incoming_call + " " + PrepareContact(contacttext) + " " + myApp.suffix_for_incoming_call;
                myApp.log(String.format(myApp.getString(R.string.sel_speech),contacttext));

                boolean r = CreateRingtone(currentRintoneUri, contacttext, ringtonefilename);
                if (r==false) {
                    if (myApp.stop_mode)
                        myApp.log(myApp.getString(R.string.cancel_create_ringtone));
                    else
                        myApp.log(myApp.getString(R.string.error_create_ringtone));
                    myApp.ButtonsEnabled(true);
                    return;
                }

                myApp.log(myApp.getString(R.string.play_created_file)+"<br/><br/>");
                MediaPlayer mp = new MediaPlayer();
                try {
                    mp.setDataSource(ringtonefilename);
                    mp.prepare();
                    mp.start();
                } catch (Exception e) {
                    myApp.log("Exception when playing sample ringtone<br/>");
                    e.printStackTrace();
                }
                myApp.ButtonsEnabled(true);
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public String GetExistRingtone(String ringtonefilename){
        MyApplication myApp=MyApplication.getMyApp();
        try {
            File f = new File(ringtonefilename);
            if (!f.exists()) return "";

            Uri uri = MediaStore.Audio.Media.getContentUriForPath(f.getAbsolutePath());


            Cursor cursor = getContentResolver().query(uri,
                    new String[] {MediaStore.MediaColumns._ID}, MediaStore.MediaColumns.DATA + " = \""+f.getAbsolutePath()+"\"",null,null);
            while (cursor.moveToNext()) {
                    return uri.toString()+"/"+cursor.getString(0);
            }
        } catch (Throwable e) {
            myApp.log("Exception in GetExistRingtone");
        }
/*

        RingtoneManager ringtoneManager = new RingtoneManager(myApp.act);
        ringtoneManager.setType(RingtoneManager.TYPE_RINGTONE);
        Cursor cursor = ringtoneManager.getCursor();
        while (cursor.moveToNext()) {
            String a = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
            String b = cursor.getString(RingtoneManager.URI_COLUMN_INDEX);
            String c = cursor.getString(RingtoneManager.ID_COLUMN_INDEX);
            if (a.substring(0,4).equals("ZZZ_"+contacttext))
                return ringtoneManager.getRingtoneUri(cursor.getPosition()).toString();
        }
*/
        return "";
    }

    public void OnStopClick(View view) {
        MyApplication myApp = MyApplication.getMyApp();
        myApp.stop_mode=true;
    }

    public void OnStartClick(View view) {

        AlertDialog.Builder altBx = new AlertDialog.Builder(this);
        altBx.setTitle(getString(R.string.are_you_sure));
        altBx.setMessage(getString(R.string.do_you_want_to_create_ringtones));

        altBx.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which) {
                ButtonsEnabled(false);
                final Runnable runnable = new Runnable() {
                    public void run() {
                        MyApplication myApp = MyApplication.getMyApp();
                        myApp.ButtonsEnabled(false);
                        Uri currentRintoneUri = Uri.parse(myApp.ringtone_uri);
                        int c = 0;
                        int cc=0;
                        for (ArrayList<String> o : myApp.contact_list) {
                            if (!o.get(3).equals("false")) c++;
                        }
                        cc=c;
                        if (c==0)
                            myApp.log(myApp.getString(R.string.create_not_need));

                        for (ArrayList<String> o : myApp.contact_list)
//                            ArrayList<String> o = myApp.contact_list.get(13);
                        {
                            if (!o.get(3).equals("true")) continue;
                            myApp.log("<br/>"+String.format(myApp.getString(R.string.remains_to_process_contacts)+"<br/>",c));
                            String id = o.get(0);
                            String contacttext = (myApp.prefix_for_incoming_call + " " + PrepareContact(o.get(1)) + " " + myApp.suffix_for_incoming_call).trim();
                            myApp.log(String.format(myApp.getString(R.string.create_ringtone_for_contact_name)+"<br/>",PrepareContact(o.get(1))));
                            String ringtonefilename = RINGTONE_PATH + "ZZZ_" + contacttext.hashCode() + ".mp3";
                            String ringtone_uri = GetExistRingtone(ringtonefilename);
                            if (ringtone_uri.equals("")) {

                                boolean r = CreateRingtone(currentRintoneUri, contacttext, ringtonefilename);
                                if (r == false) {
                                    if (myApp.stop_mode)
                                        myApp.log(myApp.getString(R.string.cancel_create_ringtone));
                                    else
                                        myApp.log(myApp.getString(R.string.error_create_ringtone));
                                    break;
                                }
                                ringtone_uri = ActivateRingtone("ZZZ_"+contacttext,ringtonefilename);
                            }
                            myApp.log(myApp.getString(R.string.ring_to_cont));
                            o.set(2, ringtone_uri.toString());
                            o.set(3, "false");
                            setContactRingtone(id, ringtone_uri.toString());
                            //SaveContactList();
                            c--;
                            try{Thread.sleep(100);} catch (Throwable t) {}

                        }
                        SaveContactList();
                        myApp.ButtonsEnabled(true);
                        if (cc!=0)
                            myApp.log(myApp.getString(R.string.create_complete));
                    }
                };
                Thread thread = new Thread(runnable);
                thread.start();
            }
        });
        altBx.setNeutralButton(getString(R.string.no), new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                //show any message
            }

        });
        altBx.show();


/*
        GeneralUtils.checkForPermissionsMAndAbove(this, true);
        LoadJNI vk = new LoadJNI();
        try {
            String workFolder = getApplicationContext().getFilesDir().getAbsolutePath();
//            String[] complexCommand = {"ffmpeg","-i", "/storage/extSdCard/temp.wav", "/storage/extSdCard/temp.mp3"};

            String[] complexCommand2 = {"ffmpeg","-y", "-i", "content://media/internal/audio/media/1", "/storage/extSdCard/ringtone.mp3"};


            String[] complexCommand = {"ffmpeg","-y", "-t", "0.2", "-i", "/storage/extSdCard/z1.mp3",
                    "-i", "/storage/extSdCard/z2.wav",
                    "-ss", "0.2", "-i", "/storage/extSdCard/z1.mp3",
                    "-filter_complex",
                    "[0][1]concat=v=0:a=1[a1];[a1][2]concat=v=0:a=1[out]", "-map", "[out]", "-ar", "22050", "-t", "0.9", "-ac", "1", "/storage/extSdCard/muxed.mp3"};
            vk.run(complexCommand2 , workFolder , getApplicationContext());
            MyLog("ffmpeg4android finished successfully");
        } catch (Throwable e) {
            MyLog("ffmpeg4android exception "+getStackTrace(e));
            Log.e("test", "vk run exception.", e);
        }

*/
        //mTts.speak("sample of text",                TextToSpeech.QUEUE_FLUSH, null);

/*
        final String utteranceId = "myTestingId";
        File destinationFile = new File(android.os.Environment.getExternalStorageDirectory(), utteranceId + ".wav");
        mTts.synthesizeToFile("sample of text", null, destinationFile, utteranceId);
*/
/*
        HashMap<String, String> myHashRender = new HashMap();
        String wakeUpText = "Звонит Глухих Андрей Иванович";
//        String destFileName = getSDcardPath()+"/temp/temp.wav";
            String destFileName = getExternalFilesDir(Environment.DIRECTORY_RINGTONES)+"/temp.wav";

        //String destFileName = android.os.Environment.getExternalStorageDirectory()+"/temp/temp.wav";
        //SaveFile(destFileName, "Этот текст сохранен на External Storage");
        myHashRender.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "id");
        mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                MyApplication myApp=(MyApplication)getApplication();
                myApp.log("Starting TTS<br/>");
            }
            @Override
            public void onDone(String utteranceId) {
                MyApplication myApp=(MyApplication)getApplication();
                myApp.log("Finished TTS<br/>");
            }
            @Override
            public void onError(String utteranceId) {
                MyApplication myApp=(MyApplication)getApplication();
                myApp.log("Error in TTS<br/>");
            }
        });
        MyLog("TTS start<br/>");
        mTts.synthesizeToFile(wakeUpText, myHashRender, destFileName);
*/
    }

    public void OnRemoveClick(View view){
        AlertDialog.Builder altBx = new AlertDialog.Builder(this);
        altBx.setTitle(getString(R.string.are_you_sure));
        altBx.setMessage(getString(R.string.do_you_want_to_remove_ringtones));
//        altBx.setIcon(R.drawable.logo);

        altBx.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which) {
                MyApplication myApp=MyApplication.getMyApp();

//                Uri currentRintoneUri = RingtoneManager.getActualDefaultRingtoneUri(myApp.getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
/*
                RingtoneManager ringtoneManager = new RingtoneManager(myApp.act);
                ringtoneManager.setType(RingtoneManager.TYPE_ALL);
                Cursor cursor = ringtoneManager.getCursor();
                while (cursor.moveToNext()) {
                    String a = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
                    String b = cursor.getString(RingtoneManager.URI_COLUMN_INDEX);
                    if (a.substring(0,4).equals("ZZZ_") || a.equals("Звонит Мама"))
                        getContentResolver().delete(
                                Uri.parse(b),
                                MediaStore.MediaColumns.TITLE + "=\"" + a + "\"", null);
                }
*/

/*
                getContentResolver().delete(
                        Uri.parse("content://media/internal/audio/media"),
                        MediaStore.MediaColumns.TITLE + " like \"ZZZ_%\"", null);
*/

                File f = new File(RINGTONE_PATH);

                File[] files=f.listFiles();

                if (files!=null) for(int i=0; i<files.length; i++){
                    if (!files[i].getName().substring(0,4).equals("ZZZ_")) continue;

                    Uri uri = MediaStore.Audio.Media.getContentUriForPath(files[i].getAbsolutePath());

                    getContentResolver().delete(
                            uri,
                            MediaStore.MediaColumns.DATA + "=\""
                                    + files[i].getAbsolutePath() + "\"", null);

                    files[i].delete();
                }


                for (ArrayList<String> o : myApp.contact_list) {
                    String r = o.get(2);
                    if (r.isEmpty()) continue;
                    String id = o.get(0);
                    setContactRingtone(id,"");
                    o.set(2,"");
                    o.set(3,"true");
                }
                SaveContactList();
                myApp.log(myApp.getString(R.string.ringtones_removed));
            }
        });
        altBx.setNeutralButton(getString(R.string.no), new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                //show any message
            }

        });
        altBx.show();
    }

     @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
         Button b4 = (Button) findViewById(R.id.stop);
         if (b4.getVisibility()==b4.VISIBLE) return false;
        if(keyCode==KeyEvent.KEYCODE_MENU){
            startActivity(new Intent(this, SettingsActivity.class));
        }
         if(keyCode==KeyEvent.KEYCODE_BACK){
             moveTaskToBack(true);
             finishAffinity();
             android.os.Process.killProcess(android.os.Process.myPid());
             System.exit(1);
            finish();
         }

        return super.onKeyDown(keyCode, event);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        return super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home/back button
            case android.R.id.home:
                moveTaskToBack(true);
                finishAffinity();
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
                finish();
                break;
            default:
                Button b4 = (Button) findViewById(R.id.stop);
                if (b4.getVisibility()==b4.VISIBLE) return false;
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }
        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public static float megabytesAvailable(File file) {
        long bytesAvailable=0;
        try {
            boolean f = false;
            if (!file.exists() && !(f = file.createNewFile()))
                return 0;
            StatFs stat = new StatFs(file.getPath());
            if (Build.VERSION.SDK_INT >= 18) {
                bytesAvailable = getAvailableBytes(stat);
            } else {
                //noinspection deprecation
                bytesAvailable = stat.getBlockSize() * stat.getAvailableBlocks();
            }
            if (f) file.delete();
        } catch (Throwable e) {
        }
        return bytesAvailable / (1024.f * 1024.f);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private static long getAvailableBytes(StatFs stat) {
        return stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
    }
/*
    public void encodeToAAC(){
        final String LOGTAG = "Encode";
        final String COMPRESSED_AUDIO_FILE_MIME_TYPE = "audio/mp4a-latm";
        final int KEY_CHANNEL_COUNT = 2;
        final int COMPRESSED_AUDIO_FILE_BIT_RATE = 96000; // 96kbps
        final int SAMPLING_RATE = 44100;
        final int CODEC_TIMEOUT_IN_MS = 5000;
        final int BUFFER_SIZE = 88200;
        Boolean mStop = false;
        MediaCodec codec;
        try {
            String filePath = Environment.getExternalStorageDirectory()+"/My Folder/song.raw";
            File inputFile = new File(filePath);
            FileInputStream fis = new FileInputStream(inputFile);
            File outputFile = new File(Environment.getExternalStorageDirectory()+"/My Folder/song.mp4");
            if (outputFile.exists()) outputFile.delete();
            MediaMuxer mux = new MediaMuxer(outputFile.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            MediaFormat outputFormat = MediaFormat.createAudioFormat(COMPRESSED_AUDIO_FILE_MIME_TYPE, SAMPLING_RATE, KEY_CHANNEL_COUNT);
            outputFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            outputFormat.setInteger(MediaFormat.KEY_BIT_RATE, COMPRESSED_AUDIO_FILE_BIT_RATE);
            codec = MediaCodec.createEncoderByType(COMPRESSED_AUDIO_FILE_MIME_TYPE);
            codec.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            codec.start();
            ByteBuffer[] codecInputBuffers = codec.getInputBuffers();
            ByteBuffer[] codecOutputBuffers = codec.getOutputBuffers();
            MediaCodec.BufferInfo outBuffInfo = new MediaCodec.BufferInfo();
            byte[] tempBuffer = new byte[BUFFER_SIZE];
            boolean hasMoreData = true;
            double presentationTimeUs = 0;
            int audioTrackIdx = 0;
            int totalBytesRead = 0;
            int percentComplete;
            do {
                int inputBufIndex = 0;
                while (inputBufIndex != -1 && hasMoreData) {
                    inputBufIndex = codec.dequeueInputBuffer(CODEC_TIMEOUT_IN_MS);
                    if (inputBufIndex >= 0) {
                        ByteBuffer dstBuf = codecInputBuffers[inputBufIndex];
                        dstBuf.clear();
                        int bytesRead = fis.read(tempBuffer, 0, dstBuf.limit());
                        if (bytesRead == -1) { // -1 implies EOS
                            hasMoreData = false;
                            codec.queueInputBuffer(inputBufIndex, 0, 0, (long) presentationTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        } else {
                            totalBytesRead += bytesRead;
                            dstBuf.put(tempBuffer, 0, bytesRead);
                            codec.queueInputBuffer(inputBufIndex, 0, bytesRead, (long) presentationTimeUs, 0);
                            presentationTimeUs = 1000000l * (totalBytesRead / 2) / SAMPLING_RATE;
                        }
                    }
                }
// Drain audio
                int outputBufIndex = 0;
                while (outputBufIndex != MediaCodec.INFO_TRY_AGAIN_LATER) {
                    outputBufIndex = codec.dequeueOutputBuffer(outBuffInfo, CODEC_TIMEOUT_IN_MS);
                    if (outputBufIndex >= 0) {
                        ByteBuffer encodedData = codecOutputBuffers[outputBufIndex];
                        encodedData.position(outBuffInfo.offset);
                        encodedData.limit(outBuffInfo.offset + outBuffInfo.size);
                        if ((outBuffInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0 && outBuffInfo.size != 0) {
                            codec.releaseOutputBuffer(outputBufIndex, false);
                        } else {
                            mux.writeSampleData(audioTrackIdx, codecOutputBuffers[outputBufIndex], outBuffInfo);
                            codec.releaseOutputBuffer(outputBufIndex, false);
                        }
                    } else if (outputBufIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        outputFormat = codec.getOutputFormat();
                        Log.v(LOGTAG, "Output format changed - " + outputFormat);
                        audioTrackIdx = mux.addTrack(outputFormat);
                        mux.start();
                    } else if (outputBufIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                        Log.e(LOGTAG, "Output buffers changed during encode!");
                    } else if (outputBufIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
// NO OP
                    } else {
                        Log.e(LOGTAG, "Unknown return code from dequeueOutputBuffer - " + outputBufIndex);
                    }
                }
                percentComplete = (int) Math.round(((float) totalBytesRead / (float) inputFile.length()) * 100.0);
                Log.v(LOGTAG, "Conversion % - " + percentComplete);
            } while (outBuffInfo.flags != MediaCodec.BUFFER_FLAG_END_OF_STREAM && !mStop);
            fis.close();
            mux.stop();
            mux.release();
            Log.v(LOGTAG, "Compression done ...");
        } catch (FileNotFoundException e) {
            Log.e(LOGTAG, "File not found!", e);
        } catch (IOException e) {
            Log.e(LOGTAG, "IO exception!", e);
        }
        mStop = false;
// Notify UI thread...
    }
    */
}


/*

// The Uri used to look up a contact by phone number
final Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, "012-345-6789");
// The columns used for `Contacts.getLookupUri`
final String[] projection = new String[] {
        Contacts._ID, Contacts.LOOKUP_KEY
};
// Build your Cursor
final Cursor data = getContentResolver().query(lookupUri, projection, null, null, null);
data.moveToFirst();
try {
    // Get the contact lookup Uri
    final long contactId = data.getLong(0);
    final String lookupKey = data.getString(1);
    final Uri contactUri = Contacts.getLookupUri(contactId, lookupKey);
    if (contactUri == null) {
        // Invalid arguments
        return;
    }

    // Get the path of ringtone you'd like to use
    final String storage = Environment.getExternalStorageDirectory().getPath();
    final File file = new File(storage + "/AudioRecorder", "hello.mp4");
    final String value = Uri.fromFile(file).toString();

    // Apply the custom ringtone
    final ContentValues values = new ContentValues(1);
    values.put(Contacts.CUSTOM_RINGTONE, value);
    getContentResolver().update(contactUri, values, null, null);
} finally {
    // Don't forget to close your Cursor
    data.close();
}
 */