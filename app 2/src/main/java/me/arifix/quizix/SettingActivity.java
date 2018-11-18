package me.arifix.quizix;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import me.arifix.quizix.Utils.Config;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import java.util.Locale;

import me.arifix.quizix.Database.AppDatabase;
import me.arifix.quizix.Database.DatabaseCall;
import me.arifix.quizix.Utils.Common;
import me.arifix.quizix.Utils.SharedPref;
import me.arifix.quizix.Utils.Utils;

import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SettingActivity extends AppCompatActivity {
    private static final String TAG = "SettingActivity";
    private static final boolean SHOW_INTERSTITIAL_AD = true;

    private boolean isPurchased;

    private SharedPref sharedPref;
    private InterstitialAd interstitialAds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Fabric Crashlytics SDK
        Fabric.with(this, new Crashlytics());

        setContentView(R.layout.activity_setting);


        // Initialize Shared Preference
        sharedPref = SharedPref.getPreferences(this);


        //changing language
//        Button btnEng = findViewById(R.id.btn_english);
//        Button btnAr = findViewById(R.id.btn_arabic);
//
//        btnEng.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                sharedPref.setBoolData("language",false);
//                finish();
//                startActivity(new Intent(SettingActivity.this,HomeActivity.class));
//                setLanguage("en");
//            }
//        });
//        btnAr.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                sharedPref.setBoolData("language",true);
//
//                finish();
//                startActivity(new Intent(SettingActivity.this,HomeActivity.class));
//                setLanguage("ar");
//            }
//        });

        //end

        // Set ActionBar Title & Add back icon on ActionBar
        setTitle(getString(R.string.settings));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Center ActionBar Title
        Utils.centerActionbarTitle(this);

        // Initialize Logger Library
        Logger.addLogAdapter(new AndroidLogAdapter());

        // Initialize Butterknife Library
        ButterKnife.bind(this);

        // Initialize Calligraphy Library with default Font
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/OpenSans-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        // Check if Purchased(Premium)
        isPurchased = sharedPref.getBoolData(Config.IS_PURCHASED, false);

        // Block Application if no Internet Connection & Internet-only Mode Activated
        if (Config.INTERNET_ONLY) {
            Common.blockAppIfNoIntenet(this);
        }

        // Initialize AdMob Ads
        if (Config.SHOW_ADS && !isPurchased) {
            MobileAds.initialize(getApplicationContext(), getString(R.string.ads_app_id));
            if (SHOW_INTERSTITIAL_AD) {
                AdRequest adRequest = new AdRequest.Builder().build();
                interstitialAds = new InterstitialAd(this);
                interstitialAds.setAdUnitId(getString(R.string.ads_interstitial_id));
                interstitialAds.loadAd(adRequest);
            }
        }

        // Create Fragment for Setting Screen Preference
        Fragment fragment = new SettingsScreen();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        if (savedInstanceState == null) {
            fragmentTransaction.add(R.id.fragmentFrame, fragment, Config.TAG_SETTINGS_SCREEN);
            fragmentTransaction.commit();
        } else {
            fragment = getFragmentManager().findFragmentByTag(Config.TAG_SETTINGS_SCREEN);
        }
    }

    // Show Interstitial Ads
    private void showInterstitialAds() {
        if (Config.SHOW_ADS && !isPurchased) {
            if (interstitialAds.isLoaded()) {
                interstitialAds.show();
            }
        }
    }

    // Setting Screen Preference Fragment
    public static class SettingsScreen extends PreferenceFragment {
        private SharedPref sharedPref;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_screen);
            sharedPref = SharedPref.getPreferences(SplashActivity.context);

            // OnChange Sound/Vibration save the Data on Shared Preference to toggle Sound/Vibration setting accordingly
            Preference.OnPreferenceChangeListener changeListener = new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    sharedPref.setStringData(preference.getKey(), newValue.toString());
                    return true;
                }
            };

            SwitchPreference sound = (SwitchPreference) findPreference(Config.SWITCH_SOUND);
            SwitchPreference vibration = (SwitchPreference) findPreference(Config.SWITCH_VIBRATION);
            SwitchPreference push = (SwitchPreference) findPreference(Config.SWITCH_PUSH);
            Preference reset = (Preference) findPreference(Config.SWITCH_RESET);
            sound.setOnPreferenceChangeListener(changeListener);
            vibration.setOnPreferenceChangeListener(changeListener);
            push.setOnPreferenceChangeListener(changeListener);

            // Reset Game Data
            reset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DatabaseCall.deleteAllData(AppDatabase.getAppDatabase(SplashActivity.context));
                            Toast.makeText(getActivity(), R.string.data_deleted, Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Stuff to do
                        }
                    });

                    builder.setTitle(R.string.confirmation);
                    builder.setMessage(R.string.delete_confirmation);

                    AlertDialog ad = builder.create();
                    ad.show();

                    return true;
                }
            });
        }
    }

    // When back Icon pressed from ActionBar
    @Override
    public void onBackPressed() {
        if (Config.SHOW_ADS && SHOW_INTERSTITIAL_AD) {
            showInterstitialAds();
        }

        super.onBackPressed();
        this.finish();
    }

    // Add back Icon press support
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Calligraphy Library- Inject into Context
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void setLanguage(String language) {
        String languageToLoad  = language; // your language
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());

    }

}
