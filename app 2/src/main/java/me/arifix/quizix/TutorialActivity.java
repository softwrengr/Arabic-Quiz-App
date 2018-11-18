package me.arifix.quizix;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import me.arifix.quizix.Network.ApiService;
import me.arifix.quizix.Network.QueryCallback;
import me.arifix.quizix.Utils.Common;
import me.arifix.quizix.Utils.Config;
import me.arifix.quizix.Utils.NestedWebView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.wang.avi.AVLoadingIndicatorView;

import me.arifix.quizix.Model.Tutorial;
import me.arifix.quizix.Network.NetworkCall;
import me.arifix.quizix.Utils.SharedPref;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class TutorialActivity extends AppCompatActivity {
    private static final String TAG = "TutorialActivity";
    private static final boolean SHOW_INTERSTITIAL_AD = true;

    private boolean isPurchased;

    private SharedPref sharedPref;
    private InterstitialAd interstitialAds;

    @BindView(R.id.loader)
    AVLoadingIndicatorView loader;
    @BindView(R.id.tvContent)
    NestedWebView tvContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize Logger Library for Logging
        Logger.addLogAdapter(new AndroidLogAdapter());

        // Initialize Butter-knife Library for field and method binding for Android views
        ButterKnife.bind(this);

        // Calligraphy Library to add custom Font support, Define default font here
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/OpenSans-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        // Initialize Shared Preference
        sharedPref = SharedPref.getPreferences(this);

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

        // Show Loader while getting Data through API Call
        loader.show();

        // API Call to get Tutorial information from Server
        getDataFromServer();
    }

    // Get Categories Data from Server - API Call
    private void getDataFromServer() {
        ApiService apiService = new NetworkCall();
        apiService.getTutorialContent(new QueryCallback<Tutorial>() {
            @Override
            public void onSuccess(Tutorial data) {
                sharedPref.setStringData(Config.TUTORIAL_DATA, data.getContent());
                String content = "<link rel=\"stylesheet\" href=\"file:///android_asset/style.css\">" + data.getContent();
                tvContent.loadData(content, "text/html; charset=utf-8", "utf-8");

                // Hide Loader
                if (loader.isShown()) {
                    loader.hide();
                }
            }

            @Override
            public void onError(Throwable th) {
                String tutorialData = sharedPref.getStringData(Config.TUTORIAL_DATA, "");
                if (!tutorialData.isEmpty()) {
                    String content = "<link rel=\"stylesheet\" href=\"file:///android_asset/style.css\">" + tutorialData;
                    tvContent.loadData(content, "text/html; charset=utf-8", "utf-8");
                } else {
                    Toast.makeText(TutorialActivity.this, getString(R.string.internet_error), Toast.LENGTH_LONG).show();
                }

                // Hide Loader
                if (loader.isShown()) {
                    loader.hide();
                }
            }
        });
    }

    // Show Interstitial Ads
    private void showInterstitialAds() {
        if (Config.SHOW_ADS && !isPurchased) {
            if (interstitialAds.isLoaded()) {
                interstitialAds.show();
            }
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

    // Calligraphy Library- Inject into Context
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
