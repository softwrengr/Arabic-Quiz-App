package me.arifix.quizix;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import me.arifix.quizix.Utils.Common;
import me.arifix.quizix.Utils.Config;
import me.arifix.quizix.Utils.SharedPref;
import me.arifix.quizix.Utils.Utils;
import com.crashlytics.android.Crashlytics;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class GameActivity extends AppCompatActivity implements RewardedVideoAdListener, BillingProcessor.IBillingHandler {
    // Activity Related Settings
    private static final String TAG = "GameActivity";
    private static final boolean SHOW_INTERSTITIAL_AD = true;

    private boolean isPurchased;

    private SharedPref sharedPref;
    private RewardedVideoAd adsReward;
    private ShareDialog shareDialog;
    private BillingProcessor bp;
    private InterstitialAd interstitialAds;

    @BindView(R.id.ivCategory)
    ImageView ivCategory;
    @BindView(R.id.ivTutorial)
    ImageView ivTutorial;
    @BindView(R.id.ivBuy)
    ImageView ivBuy;
    @BindView(R.id.ivLife)
    ImageView ivLife;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Fabric Crashlytics SDK
        Fabric.with(this, new Crashlytics());

        setContentView(R.layout.activity_game);

        // Set ActionBar Title & Add back icon on ActionBar
        setTitle(getString(R.string.game));
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

        // Set Image Foreground if Android version is less than Marshmallow
        if (Build.VERSION.SDK_INT <= 22) {
            ivCategory.setColorFilter(Color.argb(127, 0, 0, 0));
            ivTutorial.setColorFilter(Color.argb(127, 0, 0, 0));
            ivBuy.setColorFilter(Color.argb(127, 0, 0, 0));
            ivLife.setColorFilter(Color.argb(127, 0, 0, 0));
        }

        // Initialize Shared Preference
        sharedPref = SharedPref.getPreferences(this);

        // Initialize Facebook Share Dialog
        shareDialog = new ShareDialog(this);

        // Initialize Google Play Billing Processor
        bp = new BillingProcessor(this, getString(R.string.google_play_key), this);

        // Check if Purchased(Premium)
        isPurchased = sharedPref.getBoolData(Config.IS_PURCHASED, false);

        // Block Application if no Internet Connection & Internet-only Mode Activated
        if (Config.INTERNET_ONLY) {
            Common.blockAppIfNoIntenet(this);
        }

        // Initialize AdMob Ads
        if (Config.SHOW_ADS && !isPurchased) {
            if (SHOW_INTERSTITIAL_AD) {
                AdRequest adRequest = new AdRequest.Builder().build();
                interstitialAds = new InterstitialAd(this);
                interstitialAds.setAdUnitId(getString(R.string.ads_interstitial_id));
                interstitialAds.loadAd(adRequest);
            }
        }

        // Initialize Reward Ads
        if (!getString(R.string.ads_app_id).isEmpty()) {
            MobileAds.initialize(getApplicationContext(), getString(R.string.ads_app_id));
            adsReward = MobileAds.getRewardedVideoAdInstance(this);
            adsReward.setRewardedVideoAdListener(this);
        }
    }

    // Open Categories Activity
    @OnClick(R.id.secCategory)
    void goToCategories() {
        Intent categories = new Intent(GameActivity.this, CategoriesActivity.class);
        startActivity(categories);
    }

    // Open Tutorial Activity
    @OnClick(R.id.secTutorial)
    void goToTutorial() {
        Intent tutorial = new Intent(GameActivity.this, TutorialActivity.class);
        startActivity(tutorial);
    }

    // Buy Premium Version/IAP(In App Purchase)
    @OnClick(R.id.secBuy)
    void buyPremium() {
        bp.purchase(this, getString(R.string.play_iap_id));
    }

    // Add Extra Life by Watching Reward Video
    @OnClick(R.id.secLife)
    void getLife() {
        Toast.makeText(this, "video will be load in while please wait", Toast.LENGTH_SHORT).show();
        if (!getString(R.string.ads_reward_id).isEmpty()) {
            if (!adsReward.isLoaded()) {
                adsReward.loadAd(getString(R.string.ads_reward_id), new AdRequest.Builder().build());
                adsReward.show();
            } else {
                adsReward.show();
            }
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

    @Override
    public void onPause() {
        if (adsReward != null) {
            adsReward.pause(this);
        }

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adsReward != null) {
            adsReward.resume(this);
        }
    }

    @Override
    public void onDestroy() {
        if (adsReward != null) {
            adsReward.destroy(this);
        }

        if (bp != null) {
            bp.release();
        }

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // OnClick Setting icon open SettingActivity
        if (id == R.id.settings) {
            Intent setting = new Intent(GameActivity.this, SettingActivity.class);
            startActivity(setting);
        }

        // OnClick Share icon open Share Dialog
        if (id == R.id.share) {
            Common.showAppShareDialog(this, shareDialog);
        }

        return super.onOptionsItemSelected(item);
    }

    // When back icon pressed from ActionBar
    @Override
    public void onBackPressed() {
        if (Config.SHOW_ADS && SHOW_INTERSTITIAL_AD) {
            showInterstitialAds();
        }

        super.onBackPressed();
        this.finish();
    }

    // Add back icon press Support
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

    // Reward Video Ad related Listeners
    @Override
    public void onRewardedVideoAdLoaded() {
        Logger.d(TAG + ": onRewardedVideoAdLoaded");
    }

    @Override
    public void onRewardedVideoAdOpened() {
        Logger.d(TAG + ": onRewardedVideoAdOpened");
    }

    @Override
    public void onRewardedVideoStarted() {
        Logger.d(TAG + ": onRewardedVideoStarted");
    }

    @Override
    public void onRewardedVideoAdClosed() {
        Logger.d(TAG + ": onRewardedVideoAdClosed");
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
        Logger.d(TAG + ": onRewarded");
        int previousLife = sharedPref.getIntData(Config.AVAILABLE_LIFE, 0);
        sharedPref.setIntData(Config.AVAILABLE_LIFE, previousLife + Config.LIFE_PER_WATCH_VIDEO);
        Toast.makeText(this, R.string.life_added, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
        Logger.d(TAG + ": onRewardedVideoAdLeftApplication");
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {
        Logger.d(TAG + ": onRewardedVideoAdFailedToLoad");
    }

    @Override
    public void onRewardedVideoCompleted() {
        Logger.d(TAG + ": onRewardedVideoCompleted");
    }

    // In App Purchase related Listeners
    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
        Logger.d(TAG + ": onProductPurchased");
        sharedPref.setBoolData(Config.IS_PURCHASED, true);
        Toast.makeText(this, R.string.purchase_successful, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPurchaseHistoryRestored() {
        Logger.d(TAG + ": onPurchaseHistoryRestored");
    }

    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {
        Logger.d(TAG + ": onBillingError");
    }

    @Override
    public void onBillingInitialized() {
        Logger.d(TAG + ": onBillingInitialized");
    }
}
