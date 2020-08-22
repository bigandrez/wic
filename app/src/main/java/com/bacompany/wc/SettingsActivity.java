package com.bacompany.wc;


import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.view.MenuItem;

import java.util.List;
import java.util.Locale;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(null);
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//        setupActionBar();
        MainPreferenceFragment f = new MainPreferenceFragment();
        f.act = this;
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, f)
                .commit();


//        f.findPreference("select_tts").setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    public void UpdateSummary(Preference preference, Object value){
        MyApplication myApp = (MyApplication) getApplication();

        MainPreferenceFragment f = (MainPreferenceFragment)getFragmentManager().findFragmentById(android.R.id.content);

        if (preference!=null) {
            if (preference.getKey().equals("try_pro_mode")){
                finish();
                if (myApp.mAd.isLoaded()) {
                    myApp.mAd.show();
                }
                return;

            }

            if (preference.getKey().equals("select_tts")) ((ListPreference) f.findPreference("select_tts")).setValue(value.toString());
            if (preference.getKey().equals("select_freq")) ((ListPreference) f.findPreference("select_freq")).setValue(value.toString());
            if (preference.getKey().equals("select_locale")) ((ListPreference) f.findPreference("select_locale")).setValue(value.toString());
            if (preference.getKey().equals("prefix_for_incoming_call")) ((EditTextPreference) f.findPreference("prefix_for_incoming_call")).setText(value.toString());
            if (preference.getKey().equals("suffix_for_incoming_call")) ((EditTextPreference) f.findPreference("suffix_for_incoming_call")).setText(value.toString());
            if (preference.getKey().equals("timeout_before_speech")) ((EditTextPreference) f.findPreference("timeout_before_speech")).setText(value.toString());
            if (preference.getKey().equals("ringtone_duration")) ((EditTextPreference) f.findPreference("ringtone_duration")).setText(value.toString());
//            if (preference.getKey().equals("ringtone_for_calling")) ((RingtonePreference) f.findPreference("ringtone_for_calling"))..setText(value.toString());
            if (preference.getKey().equals("only_changed_contacts")) ((CheckBoxPreference) f.findPreference("only_changed_contacts")).setChecked( value.toString().equals("true") ? true : false);
//            if (preference.getKey().equals("forced_transliteration_to_english")) ((CheckBoxPreference) f.findPreference("forced_transliteration_to_english")).setChecked( value.toString().equals("true") ? true : false);
        }

        myApp.ReparseSettings();
        if (preference!=null && preference.getKey().equals("ringtone_for_calling")) {
            Uri currentRintoneUri = RingtoneManager.getActualDefaultRingtoneUri(myApp.getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
            Ringtone currentringtone = RingtoneManager.getRingtone(this, currentRintoneUri);
            String currenttitle = currentringtone.getTitle(this);
            Ringtone ringtone = RingtoneManager.getRingtone(this, Uri.parse(value.toString()));
            String title = ringtone.getTitle(this);

            if (title.indexOf(currenttitle)>=0)
                myApp.ringtone_uri = currentRintoneUri.toString();
            else
                myApp.ringtone_uri = value.toString();
        }

        f.findPreference("select_tts").setSummary(getString(R.string.current_value) + " \""+((ListPreference)f.findPreference("select_tts")).getEntry()+"\"");
        f.findPreference("select_freq").setSummary(getString(R.string.current_value) + " "+((ListPreference)f.findPreference("select_freq")).getEntry()+"Hz");
        f.findPreference("select_locale").setSummary(getString(R.string.current_value) + " \""+((ListPreference)f.findPreference("select_locale")).getEntry()+"\"");
        f.findPreference("prefix_for_incoming_call").setSummary(getString(R.string.current_value) + " \""+myApp.prefix_for_incoming_call+"\"");
        f.findPreference("suffix_for_incoming_call").setSummary(getString(R.string.current_value) + " \""+myApp.suffix_for_incoming_call+"\"");
        f.findPreference("timeout_before_speech").setSummary(getString(R.string.current_value) + " "+myApp.timeout_before_speech+" "+getString(R.string.seconds));
        f.findPreference("ringtone_duration").setSummary(getString(R.string.current_value) + " "+myApp.ringtone_duration+" "+getString(R.string.seconds));
        if (f.findPreference("try_pro_mode")!=null)
            ((CheckBoxPreference)f.findPreference("try_pro_mode")).setChecked(myApp.try_pro_mode);


        Ringtone ringtone = RingtoneManager.getRingtone(this, Uri.parse(myApp.ringtone_uri));
        String title = ringtone.getTitle(this);
        f.findPreference("ringtone_for_calling").setSummary(getString(R.string.current_value) + " \""+title+"\"");

        findViewById(android.R.id.content).postInvalidate();


    }
    private  Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
//            MyApplication myApp = (MyApplication) getApplication();
//            myApp.ReparseSettings();
    //        preference.getContext().getApplicationContext().g
  //          UpdateSummary();
/*
            if (preference.getKey().equals("prefix_for_incoming_call"))
                myApp.prefix_for_incoming_call = stringValue;
            if (preference.getKey().equals("suffix_for_incoming_call"))
                myApp.suffix_for_incoming_call = stringValue;
            if (preference.getKey().equals("select_tts")) {

                ListPreference l = (ListPreference)preference;
                myApp.tts_mode = l.findIndexOfValue(stringValue);
            }
            if (preference.getKey().equals("ringtone_for_calling")) {
                RingtonePreference l = (RingtonePreference)preference;
                myApp.ringtone_uri = stringValue;
            }
*/


return true;
/*
            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
*/
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
/*
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));

    }
*/


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
//        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || MainPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class MainPreferenceFragment extends PreferenceFragment /*implements SharedPreferences.OnSharedPreferenceChangeListener*/ {
        public SettingsActivity act;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);
            bindPreferenceSummaryToValue(findPreference("select_locale"));
            bindPreferenceSummaryToValue(findPreference("select_tts"));
            bindPreferenceSummaryToValue(findPreference("prefix_for_incoming_call"));
            bindPreferenceSummaryToValue(findPreference("suffix_for_incoming_call"));
            bindPreferenceSummaryToValue(findPreference("ringtone_for_calling"));
            bindPreferenceSummaryToValue(findPreference("only_changed_contacts"));
            bindPreferenceSummaryToValue(findPreference("timeout_before_speech"));
            bindPreferenceSummaryToValue(findPreference("ringtone_duration"));
            bindPreferenceSummaryToValue(findPreference("try_pro_mode"));

            ListPreference l = (ListPreference) findPreference("select_locale");
            if (l.getValue().equals("en_US"))
                l.setValue(Locale.getDefault().toString());

            ((SettingsActivity)this.getActivity()).UpdateSummary(null,null);

            MyApplication myApp = (MyApplication) getActivity().getApplication();
            if (myApp.try_pro_mode_enable && !myApp.try_pro_mode)
                findPreference("try_pro_mode").setEnabled(true);
            if (myApp.pro_mode || myApp.try_pro_mode) {
                PreferenceScreen screen = getPreferenceScreen();
                if (findPreference("try_pro_mode")!=null)
                    screen.removePreference(findPreference("try_pro_mode"));
            }
            if (!myApp.pro_mode){
                findPreference("prefix_for_incoming_call").setEnabled(false);
                findPreference("prefix_for_incoming_call").setSummary(getString(R.string.pro_mode_required));
                findPreference("suffix_for_incoming_call").setEnabled(false);
                findPreference("suffix_for_incoming_call").setSummary(getString(R.string.pro_mode_required));
                findPreference("select_freq").setEnabled(false);
                findPreference("select_freq").setSummary(getString(R.string.pro_mode_required)+" (8000Hz)");
            }
            if (!myApp.pro_mode && !myApp.try_pro_mode){
                findPreference("ringtone_for_calling").setEnabled(false);
                findPreference("ringtone_for_calling").setSummary(getString(R.string.pro_lite_mode_required));
                findPreference("timeout_before_speech").setEnabled(false);
                findPreference("timeout_before_speech").setSummary(getString(R.string.pro_lite_mode_required));
                findPreference("ringtone_duration").setEnabled(false);
                findPreference("ringtone_duration").setSummary(getString(R.string.pro_lite_mode_required));
            }

        }
        private  Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                act.UpdateSummary(preference, value);
                return true;
            }
        };
        private void bindPreferenceSummaryToValue(Preference preference) {
            // Set the listener to watch for value changes.
            preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        }
    }
}
