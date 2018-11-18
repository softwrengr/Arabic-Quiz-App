package me.arifix.quizix;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import me.arifix.quizix.Database.AppDatabase;
import me.arifix.quizix.Model.Category;
import me.arifix.quizix.Model.Question;
import me.arifix.quizix.Model.Score;
import me.arifix.quizix.Network.ApiService;
import me.arifix.quizix.Network.NetworkCall;
import me.arifix.quizix.Network.QueryCallback;
import me.arifix.quizix.Utils.Common;
import me.arifix.quizix.Utils.Config;
import me.arifix.quizix.Utils.CustomCountDownTimer;
import me.arifix.quizix.Utils.Utils;
import com.crashlytics.android.Crashlytics;
import com.facebook.share.widget.ShareDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import me.arifix.quizix.Database.DatabaseCall;
import me.arifix.quizix.Utils.SharedPref;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class QuestionActivity extends AppCompatActivity {
    private static final String TAG = "QuestionActivity";
    private static final boolean SHOW_BANNER_AD = true;
    private static final boolean SHOW_INTERSTITIAL_AD = true;

    private boolean isPurchased;

    private ShareDialog shareDialog;
    private SharedPref sharedPref;
    private FirebaseFirestore fsdb;
    private CustomCountDownTimer countDownTimer;
    private InterstitialAd interstitialAds;
    private Category category;
    private List<Question> questionsData;

    private boolean timer = false;
    private boolean minusPoint = false;
    private boolean quickQuiz = false;
    private boolean gameRunning = true;

    private int questionsCount;
    private int score, time, currentQuestionNo;
    private int skipQuestionCount, fiftyFiftyCount, correctAnswerCount, questionsPlayed;
    private int lifes, bonusLife;

    @BindView(R.id.loader)
    AVLoadingIndicatorView loader;
    @BindView(R.id.adsBanner)
    AdView adsBanner;

    // Elements of Question
    @BindView(R.id.ivQuestionPhoto)
    ImageView ivQuestionPhoto;
    @BindView(R.id.tvQuestion)
    TextView tvQuestion;
    @BindView(R.id.tvQuestionCount)
    TextView tvQuestionCount;
    @BindView(R.id.pbTimer)
    ProgressBar pbTimer;
    @BindView(R.id.tvTimerText)
    TextView tvTimerText;
    @BindView(R.id.tvScore)
    TextView tvScore;

    // Questions Answer Indexes
    @BindView(R.id.tvQ1Index)
    TextView tvQ1Index;
    @BindView(R.id.tvQ2Index)
    TextView tvQ2Index;
    @BindView(R.id.tvQ3Index)
    TextView tvQ3Index;
    @BindView(R.id.tvQ4Index)
    TextView tvQ4Index;
    @BindView(R.id.tvQ5Index)
    TextView tvQ5Index;

    // Questions Answers RadioGroup & RadioButton
    private ArrayList<RadioButton> answerRadioButtons;
    private ArrayList<TextView> answerRadioIndexes;
    @BindView(R.id.rgAnswers)
    RadioGroup rgAnswers;
    @BindView(R.id.rbAnswer1)
    RadioButton rbAnswer1;
    @BindView(R.id.rbAnswer2)
    RadioButton rbAnswer2;
    @BindView(R.id.rbAnswer3)
    RadioButton rbAnswer3;
    @BindView(R.id.rbAnswer4)
    RadioButton rbAnswer4;
    @BindView(R.id.rbAnswer5)
    RadioButton rbAnswer5;

    // Game Buttons
    @BindView(R.id.btnFiftyFifty)
    Button btnFiftyFifty;
    @BindView(R.id.btnSkip)
    Button btnSkip;
    @BindView(R.id.btnRestart)
    Button restartGame;

    // Lifeline
    @BindView(R.id.tvLife)
    TextView tvLife;
    @BindView(R.id.secLifeContainer)
    LinearLayout secLifeContainer;

    // Dialog
    private MaterialStyledDialog expDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Fabric Crashlytics SDK
        Fabric.with(this, new Crashlytics());

        // Conditional Layout assign for Small & Regular Screen
        int screenHeight = getApplicationContext().getResources().getDisplayMetrics().heightPixels;
        int screenWidth = getApplicationContext().getResources().getDisplayMetrics().widthPixels;
        if (screenHeight <= 800 & screenWidth <= 480) {
            setContentView(R.layout.activity_question_small);
        } else {
            setContentView(R.layout.activity_question);
        }

        // Add back icon on ActionBar
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

        // Initialize Shared Preference
        sharedPref = SharedPref.getPreferences(this);

        // Initialize Facebook Share Dialog
        shareDialog = new ShareDialog(this);

        // Initialize Firebase Firestore
        fsdb = FirebaseFirestore.getInstance();

        // Check if Purchased(Premium)
        isPurchased = sharedPref.getBoolData(Config.IS_PURCHASED, false);

        // Block Application if no Internet Connection & Internet-only Mode Activated
        if (Config.INTERNET_ONLY) {
            Common.blockAppIfNoIntenet(this);
        }

        // Initialize AdMob Ads
        if (Config.SHOW_ADS && !isPurchased) {
            MobileAds.initialize(getApplicationContext(), getString(R.string.ads_app_id));
            if (SHOW_BANNER_AD) {
                showBannerAd();
            }
            if (SHOW_INTERSTITIAL_AD) {
                AdRequest adRequest = new AdRequest.Builder().build();
                interstitialAds = new InterstitialAd(this);
                interstitialAds.setAdUnitId(getString(R.string.ads_interstitial_id));
                interstitialAds.loadAd(adRequest);
            }
        }

        // Get Available Bonus Life(from watching Reward Video)
        bonusLife = sharedPref.getIntData(Config.AVAILABLE_LIFE, 0);

        // Initialize Default Data
        questionsCount = 0;
        questionsPlayed = 0;

        // Adding Radio Indexs to ArrayList for faster uses
        answerRadioIndexes = new ArrayList<TextView>();
        answerRadioIndexes.add(tvQ1Index);
        answerRadioIndexes.add(tvQ2Index);
        answerRadioIndexes.add(tvQ3Index);
        answerRadioIndexes.add(tvQ4Index);
        answerRadioIndexes.add(tvQ5Index);

        // Adding Radio Buttons to ArrayList for faster uses
        answerRadioButtons = new ArrayList<RadioButton>();
        answerRadioButtons.add(rbAnswer1);
        answerRadioButtons.add(rbAnswer2);
        answerRadioButtons.add(rbAnswer3);
        answerRadioButtons.add(rbAnswer4);
        answerRadioButtons.add(rbAnswer5);

        // Get Data of Category
        String data = getIntent().getStringExtra(Config.QUESTIONS_DATA);
        if (data != null) {
            loader.show();
            category = new Gson().fromJson(data, Category.class);
            if (category.getLimitQuestions() != null) {
                questionsCount = Integer.valueOf(category.getLimitQuestions());
            }
            getDataFromServer();
            rgAnswers.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    checkAnswer(currentQuestionNo);
                }
            });

            setTitle(category.getTitle());

            // Set Default Data
            score = 0;
            currentQuestionNo = 0;
            correctAnswerCount = 0;

            // If Timer On
            if (Config.QUESTION_COUNTDOWN) {
                time = 0;
                timer = true;
                if (Integer.valueOf(category.getQuick()) == 1) {
                    quickQuiz = true;
                    btnSkip.setVisibility(View.GONE);
                    btnFiftyFifty.setVisibility(View.GONE);
                    pbTimer.setMax(Config.QUICK_QUIZ_COUNTDOWN_TIME);
                } else {
                    pbTimer.setMax(Config.QUESTION_COUNTDOWN_TIME);
                }
                pbTimer.setProgress(time);
            }

            // Minus Pointing related config
            if (Config.MINUS_POINT) {
                minusPoint = true;
            }

            // 50-50 related config
            if (!Config.FIFTY_FIFTY) {
                btnFiftyFifty.setVisibility(View.GONE);
            } else {
                fiftyFiftyCount = 0;
            }

            // Skip Question related config
            if (!Config.SKIP_QUESTION) {
                btnSkip.setVisibility(View.GONE);
            } else {
                skipQuestionCount = 0;
            }
        } else {
            Toast.makeText(this, getString(R.string.no_question), Toast.LENGTH_LONG).show();
        }

        // Lifeline
        if (Config.LIFE_LINE & !quickQuiz) {
            lifes = Config.MAX_LIFE_LINE + bonusLife;

            // Generate Heart Icons of Life
            for (int i = 0; i < lifes; i++) {
                ImageView image = new ImageView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(50, 50);
                params.setMargins(10, 10, 0, 0);
                image.setLayoutParams(new android.view.ViewGroup.LayoutParams(50, 50));
                image.setMaxHeight(50);
                image.setMaxWidth(50);
                image.setLayoutParams(params);
                image.setImageDrawable(getResources().getDrawable(R.drawable.icon_hearts));
                secLifeContainer.addView(image);
            }
        } else {
            secLifeContainer.setVisibility(View.GONE);
            tvLife.setVisibility(View.GONE);
        }
    }

    // Get Question Data from Server - API Call
    private void getDataFromServer() {
        ApiService apiService = new NetworkCall();
        apiService.getQuestionsFromCategory(category.getId(), new QueryCallback<List<Question>>() {
            // If API Call Successful
            @Override
            public void onSuccess(List<Question> data) {
                String questions = new Gson().toJson(data);
                sharedPref.setStringData(category.getId().toString(), questions);

                // Shuffle Questions if Random
                if (Config.RANDOM_QUESTION) {
                    Collections.shuffle(data);
                }

                questionsData = data;
                if (questionsCount == 0) {
                    questionsCount = data.size();
                }
                showQuestion(currentQuestionNo);
            }

            // If Unsuccessful
            @Override
            public void onError(Throwable th) {
                String offlineQuestionData = sharedPref.getStringData(category.getId().toString(), "");
                if (!offlineQuestionData.isEmpty()) {
                    Type questionsListType = new TypeToken<ArrayList<Question>>() {
                    }.getType();
                    questionsData = new Gson().fromJson(offlineQuestionData, questionsListType);
                    if (questionsCount == 0) {
                        questionsCount = questionsData.size();
                    }
                    showQuestion(currentQuestionNo);
                } else {
                    btnSkip.setVisibility(View.GONE);
                    btnFiftyFifty.setVisibility(View.GONE);
                    restartGame.setVisibility(View.GONE);
                    rgAnswers.setVisibility(View.GONE);
                    Toast.makeText(QuestionActivity.this, getString(R.string.internet_error), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // Show Banner Ads
    private void showBannerAd() {
        if (!getString(R.string.ads_app_id).isEmpty()) {
            MobileAds.initialize(this, getString(R.string.ads_app_id));
            AdRequest adRequest = new AdRequest.Builder().build();
            adsBanner.loadAd(adRequest);
            adsBanner.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    adsBanner.setVisibility(View.VISIBLE);
                }
            });
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

    // Show Question Data
    private void showQuestion(int questionNo) {
        ivQuestionPhoto.setBackgroundResource(R.drawable.placeholder);
        if (questionNo < questionsCount) {
            questionsPlayed++;

            if (questionsData.get(questionNo).getThumbnail() != null && !questionsData.get(questionNo).getThumbnail().equals("")) {
                if (countDownTimer != null && !quickQuiz) {
                    countDownTimer.cancel();
                }

                Picasso.with(this)
                        .load(Config.BASE_URL + Config.QUESTIONS_IMAGES_ROOT + questionsData.get(questionNo).getThumbnail())
                        .fit()
                        .centerCrop()
                        .error(R.drawable.placeholder)
                        .into(ivQuestionPhoto, new Callback() {
                            @Override
                            public void onSuccess() {
                                if (countDownTimer != null && !quickQuiz) {
                                    countDownTimer.start();
                                }
                            }

                            @Override
                            public void onError() {
                                Toast.makeText(QuestionActivity.this, getString(R.string.internet_error), Toast.LENGTH_LONG).show();
                            }
                        });

            } else if (category.getThumbnail() == null) {
                ivQuestionPhoto.setImageDrawable(getResources().getDrawable(R.drawable.placeholder));
            } else {
                Picasso.with(this)
                        .load(Config.BASE_URL + Config.CATEGORY_IMAGES_ROOT + category.getThumbnail())
                        .fit()
                        .centerCrop()
                        .into(ivQuestionPhoto);
            }

            // Set Image Foreground if Android version is less than Marshmallow
            if (Build.VERSION.SDK_INT <= 22) {
                ivQuestionPhoto.setColorFilter(Color.argb(127, 0, 0, 0));
            }

            tvQuestion.setText(Utils.fromHtml(questionsData.get(questionNo).getTitle().trim()));
            tvQuestion.setMovementMethod(new ScrollingMovementMethod());

            if (Config.RANDOM_ANSWER) {
                List<String> answers = new ArrayList<String>();
                if (questionsData.get(questionNo).getChoiceA() != null && !questionsData.get(questionNo).getChoiceA().equals("")) {
                    answers.add(questionsData.get(questionNo).getChoiceA().trim());
                }
                if (questionsData.get(questionNo).getChoiceB() != null && !questionsData.get(questionNo).getChoiceB().equals("")) {
                    answers.add(questionsData.get(questionNo).getChoiceB().trim());
                }
                if (questionsData.get(questionNo).getChoiceC() != null && !questionsData.get(questionNo).getChoiceC().equals("")) {
                    answers.add(questionsData.get(questionNo).getChoiceC().trim());
                }
                if (questionsData.get(questionNo).getChoiceD() != null && !questionsData.get(questionNo).getChoiceD().equals("")) {
                    answers.add(questionsData.get(questionNo).getChoiceD().trim());
                }
                if (questionsData.get(questionNo).getChoiceE() != null && !questionsData.get(questionNo).getChoiceE().equals("")) {
                    answers.add(questionsData.get(questionNo).getChoiceE().trim());
                }

                // Shuffle Answers
                Collections.shuffle(answers);

                if (questionsData.get(questionNo).getChoiceA() != null && !questionsData.get(questionNo).getChoiceA().equals("")) {
                    answerRadioButtons.get(0).setText(Utils.fromHtml(answers.get(0)));
                }
                if (questionsData.get(questionNo).getChoiceB() != null && !questionsData.get(questionNo).getChoiceB().equals("")) {
                    answerRadioButtons.get(1).setText(Utils.fromHtml(answers.get(1)));
                }

                if (questionsData.get(questionNo).getChoiceC() != null && !questionsData.get(questionNo).getChoiceC().equals("")) {
                    answerRadioButtons.get(2).setText(Utils.fromHtml(answers.get(2)));
                } else {
                    answerRadioIndexes.get(2).setVisibility(View.GONE);
                    answerRadioButtons.get(2).setVisibility(View.GONE);
                }

                if (questionsData.get(questionNo).getChoiceD() != null && !questionsData.get(questionNo).getChoiceD().equals("")) {
                    answerRadioButtons.get(3).setText(Utils.fromHtml(answers.get(3)));
                } else {
                    answerRadioIndexes.get(3).setVisibility(View.GONE);
                    answerRadioButtons.get(3).setVisibility(View.GONE);
                }

                if (questionsData.get(questionNo).getChoiceE() != null && !questionsData.get(questionNo).getChoiceE().equals("")) {
                    answerRadioButtons.get(4).setText(Utils.fromHtml(answers.get(4)));
                } else {
                    answerRadioIndexes.get(4).setVisibility(View.GONE);
                    answerRadioButtons.get(4).setVisibility(View.GONE);
                }
            } else {
                if (questionsData.get(questionNo).getChoiceA() != null && !questionsData.get(questionNo).getChoiceA().equals("")) {
                    rbAnswer1.setText(Utils.fromHtml(questionsData.get(questionNo).getChoiceA().trim()));
                }
                if (questionsData.get(questionNo).getChoiceB() != null && !questionsData.get(questionNo).getChoiceB().equals("")) {
                    rbAnswer2.setText(Utils.fromHtml(questionsData.get(questionNo).getChoiceB().trim()));
                }

                if (questionsData.get(questionNo).getChoiceC() != null && !questionsData.get(questionNo).getChoiceC().equals("")) {
                    rbAnswer3.setText(Utils.fromHtml(questionsData.get(questionNo).getChoiceC().trim()));
                } else {
                    answerRadioButtons.get(2).setVisibility(View.GONE);
                    rbAnswer3.setVisibility(View.GONE);
                }

                if (questionsData.get(questionNo).getChoiceD() != null && !questionsData.get(questionNo).getChoiceD().equals("")) {
                    rbAnswer4.setText(Utils.fromHtml(questionsData.get(questionNo).getChoiceD().trim()));
                } else {
                    answerRadioButtons.get(3).setVisibility(View.GONE);
                    rbAnswer4.setVisibility(View.GONE);
                }

                if (questionsData.get(questionNo).getChoiceE() != null && !questionsData.get(questionNo).getChoiceE().equals("")) {
                    rbAnswer4.setText(Utils.fromHtml(questionsData.get(questionNo).getChoiceE().trim()));
                } else {
                    answerRadioButtons.get(4).setVisibility(View.GONE);
                    rbAnswer5.setVisibility(View.GONE);
                }
            }

            if (loader.isShown()) {
                loader.hide();
            }

            tvScore.setText(String.format(getString(R.string.score), score));
            String count = (questionNo + 1) + "/" + questionsCount;
            tvQuestionCount.setText(count);

            if (timer) {
                if (quickQuiz) {
                    if (currentQuestionNo == 0) {
                        showCountdownTimer(Config.QUICK_QUIZ_COUNTDOWN_TIME);
                    }
                } else {
                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                    }
                    showCountdownTimer(Config.QUESTION_COUNTDOWN_TIME);
                }
            }
        } else if (questionsData.size() == 0) {
            Toast.makeText(this, getString(R.string.no_question), Toast.LENGTH_LONG).show();
            finish();
        } else {
            finishGame();
        }
    }

    // Go to Next Question
    private void nextQuestion() {
        for (int i = 0; i < 5; i++) {
            answerRadioIndexes.get(i).setVisibility(View.VISIBLE);
            answerRadioButtons.get(i).setVisibility(View.VISIBLE);
            answerRadioButtons.get(i).setEnabled(true);
            answerRadioButtons.get(i).setChecked(false);
            answerRadioButtons.get(i).setBackgroundColor(getResources().getColor(R.color.colorWhite));
            answerRadioButtons.get(i).setTextColor(getResources().getColor(R.color.colorPrimary));
            answerRadioButtons.get(i).setPaintFlags(answerRadioButtons.get(i).getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        tvScore.setText(String.format(getString(R.string.score), score));
        btnSkip.setEnabled(true);
        btnFiftyFifty.setEnabled(true);
        time = 0;
        if (!quickQuiz) {
            pbTimer.setProgress(time);
        }
        currentQuestionNo += 1;
        showQuestion(currentQuestionNo);
    }

    // Check the Answer
    private void checkAnswer(int questionNo) {
        if (countDownTimer != null && !quickQuiz) {
            countDownTimer.pause();
        }
        boolean correctAnswer = false;
        switch (rgAnswers.getCheckedRadioButtonId()) {
            case R.id.rbAnswer1:
                if (Utils.fromHtml(rbAnswer1.getText().toString().trim()).toString().equals(Utils.fromHtml(questionsData.get(questionNo).getAnswer().trim()).toString())) {
                    correctAnswer = true;
                    score += Config.POINT_PER_CORRECT_ANSWER;
                } else {
                    if (minusPoint) {
                        score -= Config.MINUS_POINT_PER_WRONG_ANSWER;
                    }
                    rbAnswer1.setBackgroundColor(getResources().getColor(R.color.colorRed));
                    rbAnswer1.setTextColor(getResources().getColor(R.color.colorWhite));
                }
                break;
            case R.id.rbAnswer2:
                if (Utils.fromHtml(rbAnswer2.getText().toString().trim()).toString().equals(Utils.fromHtml(questionsData.get(questionNo).getAnswer().trim()).toString())) {
                    correctAnswer = true;
                    score += Config.POINT_PER_CORRECT_ANSWER;
                } else {
                    if (minusPoint) {
                        score -= Config.MINUS_POINT_PER_WRONG_ANSWER;
                    }
                    rbAnswer2.setBackgroundColor(getResources().getColor(R.color.colorRed));
                    rbAnswer2.setTextColor(getResources().getColor(R.color.colorWhite));
                }
                break;
            case R.id.rbAnswer3:
                if (Utils.fromHtml(rbAnswer3.getText().toString().trim()).toString().equals(Utils.fromHtml(questionsData.get(questionNo).getAnswer().trim()).toString())) {
                    correctAnswer = true;
                    score += Config.POINT_PER_CORRECT_ANSWER;
                } else {
                    if (minusPoint) {
                        score -= Config.MINUS_POINT_PER_WRONG_ANSWER;
                    }
                    rbAnswer3.setBackgroundColor(getResources().getColor(R.color.colorRed));
                    rbAnswer3.setTextColor(getResources().getColor(R.color.colorWhite));
                }
                break;
            case R.id.rbAnswer4:
                if (Utils.fromHtml(rbAnswer4.getText().toString().trim()).toString().equals(Utils.fromHtml(questionsData.get(questionNo).getAnswer().trim()).toString())) {
                    correctAnswer = true;
                    score += Config.POINT_PER_CORRECT_ANSWER;
                } else {
                    if (minusPoint) {
                        score -= Config.MINUS_POINT_PER_WRONG_ANSWER;
                    }
                    rbAnswer4.setBackgroundColor(getResources().getColor(R.color.colorRed));
                    rbAnswer4.setTextColor(getResources().getColor(R.color.colorWhite));
                }
                break;
            case R.id.rbAnswer5:
                if (Utils.fromHtml(rbAnswer5.getText().toString().trim()).toString().equals(Utils.fromHtml(questionsData.get(questionNo).getAnswer().trim()).toString())) {
                    correctAnswer = true;
                    score += Config.POINT_PER_CORRECT_ANSWER;
                } else {
                    if (minusPoint) {
                        score -= Config.MINUS_POINT_PER_WRONG_ANSWER;
                    }
                    rbAnswer5.setBackgroundColor(getResources().getColor(R.color.colorRed));
                    rbAnswer5.setTextColor(getResources().getColor(R.color.colorWhite));
                }
                break;
        }

        if (!quickQuiz) {
            showCorrectAnswer(questionNo);
        }
        if (correctAnswer) {
            correctAnswerCount++;
            if (quickQuiz) {
                nextQuestion();
            } else {
                if (sharedPref.getStringData(Config.SWITCH_SOUND, "true").equals("true")) {
                    // Play Sound
                    MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.yahoo);
                    mp.start();
                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mp.release();
                        }
                    });
                }
                if (Config.SHOW_EXPLANATION) {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showExplanation();
                        }
                    }, 2000);
                } else {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            nextQuestion();
                        }
                    }, 2000);
                }
            }
        } else {
            if (Config.LIFE_LINE & !quickQuiz) {
                removeLife();
            }
            if (quickQuiz) {
                nextQuestion();
            } else {
                // Vibrate
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (sharedPref.getStringData(Config.SWITCH_VIBRATION, "true").equals("true")) {
                    if (vibrator != null) {
                        vibrator.vibrate(500);
                    }
                }

                if (Config.SHOW_EXPLANATION) {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showExplanation();
                        }
                    }, 2000);
                } else {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            nextQuestion();
                        }
                    }, 2000);
                }
            }
        }
    }

    // Show Answer Explanation
    private void showExplanation() {
        for (int i = 0; i < 5; i++) {
            answerRadioButtons.get(i).setEnabled(true);
            answerRadioButtons.get(i).setChecked(false);
        }
        for (int i = 0; i < 5; i++) {
            answerRadioButtons.get(i).setEnabled(true);
            answerRadioButtons.get(i).setChecked(false);
            answerRadioButtons.get(i).setBackgroundColor(getResources().getColor(R.color.colorWhite));
            answerRadioButtons.get(i).setTextColor(getResources().getColor(R.color.colorPrimary));
            answerRadioButtons.get(i).setPaintFlags(answerRadioButtons.get(i).getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
        if (questionsData.get(currentQuestionNo).getExplanation() != null) {
            MaterialStyledDialog.Builder materialDialog = new MaterialStyledDialog.Builder(QuestionActivity.this)
                    .setTitle(getString(R.string.explanation))
                    .setDescription(Utils.fromHtml(questionsData.get(currentQuestionNo).getExplanation().trim()))
                    .setIcon(R.drawable.icon_app)
                    .setNegativeText(getString(R.string.close))
                    .setCancelable(false)
                    .setScrollable(true, 5)
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            nextQuestion();
                        }
                    });
            expDialog = materialDialog.build();
            expDialog.show();
        } else {
            nextQuestion();
        }
    }

    // Reveal the Correct Answer
    private void showCorrectAnswer(int questionNo) {
        for (int i = 0; i < 5; i++) {
            answerRadioButtons.get(i).setChecked(false);
            answerRadioButtons.get(i).setEnabled(false);
            if (Config.SHOW_CORRECT_ANSWER) {
                if (Utils.fromHtml(answerRadioButtons.get(i).getText().toString().trim()).toString().equals(Utils.fromHtml(questionsData.get(questionNo).getAnswer().trim()).toString())) {
                    answerRadioButtons.get(i).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    answerRadioButtons.get(i).setTextColor(getResources().getColor(R.color.colorWhite));
                }
            }
        }
    }

    // Skip Current Question
    @OnClick(R.id.btnSkip)
    void skipQuestion() {
        skipQuestionCount++;
        if (skipQuestionCount <= Config.MAX_SKIP_QUESTION_CHANCE) {
            nextQuestion();
        } else {
            skipQuestionCount--;
            Toast.makeText(this, R.string.skip_limit, Toast.LENGTH_LONG).show();
        }
    }

    // Remove 2 Wrong Answer
    @OnClick(R.id.btnFiftyFifty)
    void fiftyFifty() {
        if (questionsData.get(currentQuestionNo).getNumberOfAnswer() == 2) {
            btnFiftyFifty.setEnabled(false);
            Toast.makeText(this, getString(R.string.fifty_fifty_unavailable), Toast.LENGTH_LONG).show();
        } else if (questionsData.get(currentQuestionNo).getNumberOfAnswer() == 3) {
            fiftyFiftyCount++;
            if (fiftyFiftyCount <= Config.MAX_FIFTY_FIFTY_CHANCE) {
                btnFiftyFifty.setEnabled(false);
                int removedItem = 0;
                for (int i = 0; i < 5; i++) {
                    if (!answerRadioButtons.get(i).getText().toString().trim().equals(questionsData.get(currentQuestionNo).getAnswer().trim()) && removedItem < 1) {
                        answerRadioButtons.get(i).setPaintFlags(answerRadioButtons.get(i).getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        removedItem++;
                    }
                }
            } else {
                Toast.makeText(this, R.string.fifty_fifty_limit, Toast.LENGTH_LONG).show();
            }
        } else if (questionsData.get(currentQuestionNo).getNumberOfAnswer() == 4) {
            fiftyFiftyCount++;
            if (fiftyFiftyCount <= Config.MAX_FIFTY_FIFTY_CHANCE) {
                btnFiftyFifty.setEnabled(false);
                int removedItem = 0;
                for (int i = 0; i < 5; i++) {
                    if (!answerRadioButtons.get(i).getText().toString().trim().equals(questionsData.get(currentQuestionNo).getAnswer().trim()) && removedItem < 2) {
                        answerRadioButtons.get(i).setPaintFlags(answerRadioButtons.get(i).getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        removedItem++;
                    }
                }
            } else {
                Toast.makeText(this, R.string.fifty_fifty_limit, Toast.LENGTH_LONG).show();
            }
        } else {
            fiftyFiftyCount++;
            if (fiftyFiftyCount <= Config.MAX_FIFTY_FIFTY_CHANCE) {
                btnFiftyFifty.setEnabled(false);
                int removedItem = 0;
                for (int i = 0; i < 5; i++) {
                    if (!answerRadioButtons.get(i).getText().toString().trim().equals(questionsData.get(currentQuestionNo).getAnswer().trim()) && removedItem < 3) {
                        answerRadioButtons.get(i).setPaintFlags(answerRadioButtons.get(i).getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        removedItem++;
                    }
                }
            } else {
                Toast.makeText(this, R.string.fifty_fifty_limit, Toast.LENGTH_LONG).show();
            }
        }
    }

    // Restart Game
    @OnClick(R.id.btnRestart)
    void restartGame() {
        score = 0;
        currentQuestionNo = 0;
        fiftyFiftyCount = 0;
        skipQuestionCount = 0;
        correctAnswerCount = 0;
        questionsPlayed = 0;
        for (int i = 0; i < 5; i++) {
            answerRadioButtons.get(i).setEnabled(true);
            answerRadioButtons.get(i).setChecked(false);
        }
        btnFiftyFifty.setEnabled(true);
        btnSkip.setEnabled(true);
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        tvScore.setText(String.format(getString(R.string.score), score));
        pbTimer.setVisibility(View.VISIBLE);
        tvTimerText.setVisibility(View.VISIBLE);
        tvScore.setVisibility(View.VISIBLE);
        tvQuestionCount.setVisibility(View.VISIBLE);
        for (int i = 0; i < 5; i++) {
            answerRadioIndexes.get(i).setVisibility(View.VISIBLE);
            answerRadioButtons.get(i).setVisibility(View.VISIBLE);
        }
        if (Config.RANDOM_QUESTION) {
            Collections.shuffle(questionsData);
        }

        // Generate Heart Icons of Lifes
        if (secLifeContainer.getChildCount() > 0) {
            secLifeContainer.removeAllViews();
        }

        lifes = Config.MAX_LIFE_LINE;
        int newLife = sharedPref.getIntData(Config.AVAILABLE_LIFE, 0) + lifes;
        for (int i = 0; i < newLife; i++) {
            ImageView image = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(50, 50);
            params.setMargins(10, 10, 0, 0);
            image.setLayoutParams(new android.view.ViewGroup.LayoutParams(50, 50));
            image.setMaxHeight(50);
            image.setMaxWidth(50);
            image.setLayoutParams(params);
            image.setImageDrawable(getResources().getDrawable(R.drawable.icon_hearts));
            secLifeContainer.addView(image);
        }

        showQuestion(currentQuestionNo);
    }

    // End the Game & show Result
    private void finishGame() {
        gameRunning = false;
        for (int i = 0; i < 5; i++) {
            answerRadioIndexes.get(i).setVisibility(View.GONE);
            answerRadioButtons.get(i).setEnabled(false);
            answerRadioButtons.get(i).setVisibility(View.GONE);
        }
        btnFiftyFifty.setEnabled(false);
        btnSkip.setEnabled(false);
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        pbTimer.setVisibility(View.GONE);
        tvTimerText.setVisibility(View.GONE);
        tvScore.setVisibility(View.GONE);
        tvQuestionCount.setVisibility(View.GONE);

        tvLife.setVisibility(View.GONE);
        secLifeContainer.setVisibility(View.GONE);

        if (score > 0) {
            Score data = new Score();
            data.setCategoryName(category.getTitle());
            data.setScore(score);
            data.setThumbnail(category.getThumbnail());

            Score existingData = DatabaseCall.findByCategoryName(AppDatabase.getAppDatabase(this), category.getTitle());
            if (existingData == null) {
                Score result = DatabaseCall.addScoreData(AppDatabase.getAppDatabase(this), data);
            } else {
                if (score > existingData.getScore()) {
                    DatabaseCall.updateScoreBycategory(AppDatabase.getAppDatabase(this), score, category.getTitle());
                }
            }

            if (Utils.isNetworkAvailable(getApplicationContext())) {
                if (!sharedPref.getStringData(Config.USER_NAME, "").isEmpty() && !sharedPref.getStringData(Config.USER_EMAIL, "").isEmpty()) {
                    final int totalScore = DatabaseCall.totalScore(AppDatabase.getAppDatabase(getApplicationContext()));

                    final DocumentReference leaderRef = fsdb.collection(Config.FIRESTORE_COLLECTION_NAME).document(sharedPref.getStringData(Config.USER_EMAIL, ""));
                    leaderRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    leaderRef.update(Config.FIRESTORE_NAME_COLUMN, sharedPref.getStringData(Config.USER_NAME, ""));
                                    leaderRef.update(Config.FIRESTORE_PHOTO_COLUMN, sharedPref.getStringData(Config.USER_PHOTO, ""));
                                    leaderRef.update(Config.FIRESTORE_SCORE_COLUMN, String.valueOf(totalScore))
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Logger.d(TAG + ": Score Data Updated Successfully");
                                                }
                                            });
                                } else {
                                    Map<String, String> leader = new HashMap<>();
                                    leader.put("name", sharedPref.getStringData(Config.USER_NAME, ""));
                                    if (!sharedPref.getStringData(Config.USER_PHOTO, "").isEmpty()) {
                                        leader.put("photo", sharedPref.getStringData(Config.USER_PHOTO, ""));
                                    }

                                    leader.put("score", String.valueOf(totalScore));

                                    fsdb.collection(Config.FIRESTORE_COLLECTION_NAME).document(sharedPref.getStringData(Config.USER_EMAIL, "")).set(leader)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Logger.d(TAG + ": Score Data Added Successfully");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Logger.d(TAG + ": Error writing document, " + e);
                                                }
                                            });
                                    Logger.d(leader);
                                }
                            } else {
                                Logger.d(TAG + ": Get failed with " + task.getException());
                            }
                        }
                    });
                }
            }
        }

        String questionsData = getIntent().getStringExtra(Config.QUESTIONS_DATA);
        int incorrectAnswer = questionsPlayed - correctAnswerCount - skipQuestionCount;
        Intent result = new Intent(QuestionActivity.this, ResultActivity.class);
        result.putExtra(Config.ANSWER_CORRECT, String.valueOf(correctAnswerCount));
        result.putExtra(Config.ANSWER_INCORRECT, String.valueOf(incorrectAnswer));
        result.putExtra(Config.ANSWER_SKIPPED, String.valueOf(skipQuestionCount));
        result.putExtra(Config.FIRESTORE_SCORE_COLUMN, String.valueOf(score));
        result.putExtra(Config.QUESTIONS_DATA, questionsData);
        if (expDialog != null) {
            expDialog.dismiss();
        }
        finish();
        startActivity(result);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    // Remove Life
    private void removeLife() {
        lifes--;
        if (lifes == 0) {
            finishGame();
        } else {
            if (secLifeContainer.getChildCount() > 0) {
                secLifeContainer.removeAllViews();
            }
            for (int i = 0; i < lifes; i++) {
                ImageView image = new ImageView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(50, 50);
                params.setMargins(10, 10, 0, 0);
                image.setLayoutParams(new android.view.ViewGroup.LayoutParams(50, 50));
                image.setMaxHeight(50);
                image.setMaxWidth(50);
                image.setLayoutParams(params);
                image.setImageDrawable(getResources().getDrawable(R.drawable.icon_hearts));
                secLifeContainer.addView(image);
            }
        }

        if (bonusLife > lifes) {
            int currentLife = sharedPref.getIntData(Config.AVAILABLE_LIFE, 0);
            if (currentLife > 0) {
                sharedPref.setIntData(Config.AVAILABLE_LIFE, currentLife - 1);
            }
        }
    }

    // Show Countdown Timer
    private void showCountdownTimer(int seconds) {
        if(gameRunning) {
            countDownTimer = new CustomCountDownTimer(seconds * 1000, 500) {
                @Override
                public void onTick(long leftTimeInMilliseconds) {
                    long seconds = leftTimeInMilliseconds / 1000;
                    pbTimer.setProgress((int) seconds);
                    tvTimerText.setText(String.valueOf(seconds % 60));
                }

                @Override
                public void onFinish() {
                    if (quickQuiz) {
                        finishGame();
                    } else {
                        if (Config.LIFE_LINE & !quickQuiz) {
                            removeLife();
                        }
                        nextQuestion();
                    }
                }
            }.start();
        }
        else{
            countDownTimer.cancel();
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
            Intent setting = new Intent(QuestionActivity.this, SettingActivity.class);
            startActivity(setting);
        }

        // OnClick Share icon open Share Dialog
        if (id == R.id.share) {
            Common.showAppShareDialog(this, shareDialog);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (countDownTimer != null) {
            countDownTimer.resume();
        }
        if (adsBanner != null) {
            adsBanner.resume();
        }
    }

    @Override
    protected void onDestroy() {
        if (adsBanner != null) {
            adsBanner.destroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (adsBanner != null) {
            adsBanner.pause();
        }
        super.onPause();
        if (countDownTimer != null) {
            countDownTimer.pause();
        }
    }

    // When back icon pressed from ActionBar
    @Override
    public void onBackPressed() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
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
}