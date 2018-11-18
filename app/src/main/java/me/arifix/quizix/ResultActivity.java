package me.arifix.quizix;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import me.arifix.quizix.Utils.Common;
import me.arifix.quizix.Utils.Config;
import me.arifix.quizix.Utils.Utils;
import com.crashlytics.android.Crashlytics;
import com.facebook.share.widget.ShareDialog;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.squareup.picasso.Picasso;

import me.arifix.quizix.Utils.ChartValueFormatter;
import me.arifix.quizix.Utils.CircleTransform;
import me.arifix.quizix.Utils.SharedPref;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ResultActivity extends AppCompatActivity {
    private static final String TAG = "ResultActivity";
    private static final boolean SHOW_INTERSTITIAL_AD = true;

    private boolean isPurchased;
    private int score;

    private ShareDialog shareDialog;
    private InterstitialAd interstitialAds;

    @BindView(R.id.ivUserPhoto)
    ImageView ivUserPhoto;
    @BindView(R.id.tvResultScore)
    TextView tvResultScore;
    @BindView(R.id.chartResultData)
    PieChart chartResultData;
    private String questionsData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Fabric Crashlytics SDK
        Fabric.with(this, new Crashlytics());

        setContentView(R.layout.activity_result);

        // Set ActionBar Title & Add back icon on ActionBar
        setTitle(getString(R.string.result));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Center ActionBar Title
        Utils.centerActionbarTitle(this);

        // Initialize Shared Preference
        SharedPref sharedPref = SharedPref.getPreferences(this);

        // Initialize Logger Library for Logging
        Logger.addLogAdapter(new AndroidLogAdapter());

        // Initialize Butter-knife Library for field and method binding for Android views
        ButterKnife.bind(this);

        // Initialize Facebook SDK
        shareDialog = new ShareDialog(this);

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

        // Get Results from Previous Activity & Display
        int correct = Integer.valueOf(getIntent().getStringExtra(Config.ANSWER_CORRECT));
        int incorrect = Integer.valueOf(getIntent().getStringExtra(Config.ANSWER_INCORRECT));
        int skipped = Integer.valueOf(getIntent().getStringExtra(Config.ANSWER_SKIPPED));
        questionsData = getIntent().getStringExtra(Config.QUESTIONS_DATA);
        score = Integer.valueOf(getIntent().getStringExtra(Config.FIRESTORE_SCORE_COLUMN));

        tvResultScore.setText(String.format(getString(R.string.your_score), score));
        if (!sharedPref.getStringData(Config.USER_PHOTO, "").isEmpty()) {
            Picasso.with(this)
                    .load(sharedPref.getStringData(Config.USER_PHOTO, ""))
                    .error(R.drawable.icon_user)
                    .transform(new CircleTransform())
                    .fit()
                    .centerCrop()
                    .into(ivUserPhoto);
        }

        // Generate & Display Results as Chart
        chartResultData.setRotationEnabled(false);
        chartResultData.getDescription().setEnabled(false);
        chartResultData.setDrawHoleEnabled(false);

        List<PieEntry> entries = new ArrayList<>();
        if (correct > 0) {
            entries.add(new PieEntry(correct, getString(R.string.correct)));
        }
        if (incorrect > 0) {
            entries.add(new PieEntry(incorrect, getString(R.string.incorrect)));
        }
        if (skipped > 0) {
            entries.add(new PieEntry(skipped, getString(R.string.skipped)));
        }

        PieDataSet dataset = new PieDataSet(entries, "");
        dataset.setColors(new int[]{R.color.colorPrimary, R.color.colorRed, R.color.colorBrown}, this);
        PieData piedata = new PieData(dataset);
        piedata.setValueTextSize(18f);
        piedata.setValueFormatter(new ChartValueFormatter());
        chartResultData.setData(piedata);
        chartResultData.invalidate();
    }

    // Show Interstitial Ads
    private void showInterstitialAds() {
        if (Config.SHOW_ADS && !isPurchased) {
            if (interstitialAds.isLoaded()) {
                interstitialAds.show();
            }
        }
    }

    // Open Categories
    @OnClick(R.id.btnCategories)
    void goToCategories() {
        Intent categories = new Intent(ResultActivity.this, CategoriesActivity.class);
        finish();
        startActivity(categories);
    }

    // Share Score
    @OnClick(R.id.btnShare)
    void shareScore() {
        String shareBody = getString(R.string.app_name) + ", " + String.format(getString(R.string.score), score);
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_score)));
    }

    // Restart Game
    @OnClick(R.id.btnRestart)
    void restartGame() {
        Intent restart = new Intent(ResultActivity.this, QuestionActivity.class);
        restart.putExtra(Config.QUESTIONS_DATA, questionsData);
        finish();
        startActivity(restart);
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
            Intent setting = new Intent(ResultActivity.this, SettingActivity.class);
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

    // Add back icon Support
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

}
