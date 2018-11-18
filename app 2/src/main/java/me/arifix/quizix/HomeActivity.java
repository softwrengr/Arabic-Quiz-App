package me.arifix.quizix;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import me.arifix.quizix.Utils.Common;
import me.arifix.quizix.Utils.Config;
import me.arifix.quizix.Utils.Utils;
import com.crashlytics.android.Crashlytics;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.widget.ShareDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import java.security.MessageDigest;
import java.util.Locale;

import me.arifix.quizix.Utils.SharedPref;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class HomeActivity extends AppCompatActivity {
    // Activity Related Settings
    private static final String TAG = "HomeActivity";
    private static final boolean SHOW_BANNER_AD = true;

    private static final int GOOGLE_SIGNIN_CODE = 34;
    private boolean doubleBackToExitPressedOnce = false;

    private ShareDialog shareDialog;
    private FirebaseAuth mAuth;
    private CallbackManager mCallbackManager;
    private GoogleSignInClient mGoogleSignInClient;
    private SharedPref sharedPref;
    private Dialog dialog;

    private ImageView dialogivClose, ivAppIcon;
    private Button dialogbtnLogout;

    @BindView(R.id.adsBanner)
    AdView adsBanner;
    @BindView(R.id.tvUserPanel)
    TextView tvUserPanel;

    @BindView(R.id.tvAppSubtitle)
    TextView tvAppSubtitlef;


    Locale mylocale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        // Initialize Fabric Crashlytics SDK
        Fabric.with(this, new Crashlytics());

        // Conditional Layout assign for Small & Regular Screen
        int screenHeight = getApplicationContext().getResources().getDisplayMetrics().heightPixels;
        int screenWidth = getApplicationContext().getResources().getDisplayMetrics().widthPixels;
        if (screenHeight <= 800 & screenWidth <= 480) {
            setContentView(R.layout.activity_home_small);

        } else {
            setContentView(R.layout.activity_home);
        }



        ivAppIcon = findViewById(R.id.ivAppIcon);
        keyHashes();

        // Set ActionBar Title & Add back icon on ActionBar
        setTitle(getString(R.string.home));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //changing language

        ivAppIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                tvAppSubtitlef.setText(R.string.app_tagline);
            }
        });
        //end

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

        // Initialize Facebook SDK
        shareDialog = new ShareDialog(this);

        // Obtain the FirebaseAnalytics instance.
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Get Instance of Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Shared Preference
        sharedPref = SharedPref.getPreferences(this);


        // Check if Purchased(Premium)
        boolean isPurchased = sharedPref.getBoolData(Config.IS_PURCHASED, false);

        // User Dialog related things
        dialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        dialog.setContentView(R.layout.dialog_user);
        dialogivClose = (ImageView) dialog.findViewById(R.id.dialogivClose);
        dialogbtnLogout = (Button) dialog.findViewById(R.id.dialogbtnLogout);
        SignInButton btnSignInGoogle = (SignInButton) dialog.findViewById(R.id.btnSignInGoogle);

        // Facebook Login
        mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) dialog.findViewById(R.id.btnSignInFacebook);
        // We need Email Address & Public Profile
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Logger.d(TAG + ": Facebook Login:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Logger.d(TAG + ": Facebook Login:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Logger.d(TAG + ": Facebook Login:onError, Error details: " + error);
            }
        });

        // Check if logged out from Facebook
        AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    LoginManager.getInstance().logOut();
                    userSignoutAction();
                }
            }
        };

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Google Login
        btnSignInGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, GOOGLE_SIGNIN_CODE);
            }
        });

        // Block Application if no Internet Connection & Internet-only Mode Activated
        if (Config.INTERNET_ONLY) {
            Common.blockAppIfNoIntenet(this);
        }

        // Show Banner Ads
        if (Config.SHOW_ADS && !isPurchased && SHOW_BANNER_AD) {
            showBannerAd();
        }
    }

    // OnClick Categories open Categories Activity
    @OnClick(R.id.secGame)
    void openCategoriesActivity() {
        Intent categories = new Intent(HomeActivity.this, GameActivity.class);
        startActivity(categories);
    }

    // OnClick Scores open Scores Modal
    @OnClick(R.id.secScore)
    void openScoresActivity() {
        Intent leaderboard = new Intent(HomeActivity.this, LeaderboardActivity.class);
        startActivity(leaderboard);
    }

    // OnClick About open About Activity
    @OnClick(R.id.secAbout)
    void openAboutActivity() {
        new MaterialStyledDialog.Builder(this)
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.about_text))
                .setIcon(R.drawable.icon_app)
                .setScrollable(true, 5)
                .setPositiveText(R.string.rate_us)
                .setNegativeText(R.string.close)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // It will auto dismiss onClick
                    }
                })
                .show();
    }

    // OnClick User open User Activity
    @OnClick(R.id.secUser)
    void openUserDialog() {
        dialog.show();
        dialogivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        });
        dialogbtnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                userSignoutAction();
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

    // Handle Facebook Access Token
    private void handleFacebookAccessToken(AccessToken token) {
        Logger.d(TAG + ": Facebook Login, Token:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in icon_user's information
                            Logger.d("Facebook Login:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            checkSignIn(user);
                        } else {
                            // If sign in fails, display a message to the icon_user.
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthUserCollisionException e) {
                                Toast.makeText(HomeActivity.this, R.string.user_exist, Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Toast.makeText(HomeActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                                Logger.w(TAG + ": Google Login::failure" + e.getMessage());
                            }
                            checkSignIn(null);
                        }
                    }
                });
    }

    // Handle Google Auth with Firebase
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Logger.d("Google Login, ID: " + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Logger.d(TAG + ": Google Login:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(HomeActivity.this, getString(R.string.login_successful), Toast.LENGTH_LONG).show();
                            checkSignIn(user);
                        } else {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthUserCollisionException e) {
                                Toast.makeText(HomeActivity.this, R.string.user_exist, Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Toast.makeText(HomeActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                                Logger.w(TAG + ": Google Login::failure" + e.getMessage());
                            }
                            checkSignIn(null);
                        }
                    }
                });
    }

    // Check Sign-in
    private void checkSignIn(FirebaseUser currentUser) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            dialogbtnLogout.setVisibility(View.VISIBLE);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            if (user.getDisplayName() != null) {
                tvUserPanel.setText(user.getDisplayName());
                sharedPref.setStringData(Config.USER_NAME, user.getDisplayName());
            }
            if (user.getEmail() != null) {
                sharedPref.setStringData(Config.USER_EMAIL, user.getEmail());
            }
            if (user.getPhotoUrl() != null) {
                sharedPref.setStringData(Config.USER_PHOTO, user.getPhotoUrl().toString());
            }

            Logger.d(user.getDisplayName() + ", " + user.getEmail() + ", " + user.getPhotoUrl());
        } else if
                (!sharedPref.getStringData(Config.USER_NAME, "").equals("") && !sharedPref.getStringData(Config.USER_EMAIL, "").equals("")) {
            dialogbtnLogout.setVisibility(View.VISIBLE);
            tvUserPanel.setText(sharedPref.getStringData(Config.USER_NAME, getString(R.string.user_panel)));

            Logger.d(TAG + ": " + sharedPref.getStringData(Config.USER_NAME, "") + ", " + sharedPref.getStringData(Config.USER_EMAIL, "") + ", " + sharedPref.getStringData(Config.USER_PHOTO, ""));
        } else {
            dialogbtnLogout.setVisibility(View.GONE);
            sharedPref.setStringData(Config.USER_NAME, "");
            sharedPref.setStringData(Config.USER_EMAIL, "");
            sharedPref.setStringData(Config.USER_PHOTO, "");
            tvUserPanel.setText(getString(R.string.user_panel));
        }
    }

    // User Signout Action
    private void userSignoutAction() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(HomeActivity.this, getString(R.string.logout_successful), Toast.LENGTH_LONG).show();
        sharedPref.setStringData(Config.USER_NAME, "");
        sharedPref.setStringData(Config.USER_EMAIL, "");
        sharedPref.setStringData(Config.USER_PHOTO, "");
        tvUserPanel.setText(getString(R.string.user_panel));
        dialogbtnLogout.setVisibility(View.GONE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_SIGNIN_CODE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Logger.d(TAG + ": Google Sign-in Failed, Error: " + e);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        checkSignIn(currentUser);
    }

    @Override
    public void onPause() {
        // Pause Banner Ads
        if (adsBanner != null) {
            adsBanner.pause();
        }

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Resume Banner Ads
        if (adsBanner != null) {
            adsBanner.resume();
        }
    }

    @Override
    public void onDestroy() {
        // Destroy Banner Ads
        if (adsBanner != null) {
            adsBanner.destroy();
        }

        super.onDestroy();
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
            Intent setting = new Intent(HomeActivity.this, SettingActivity.class);
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
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.press_back, Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
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

    private void keyHashes() {
        {
            //Get Has Key
            try {
                PackageInfo info = getPackageManager().getPackageInfo("me.arifix.quizix", PackageManager.GET_SIGNATURES);
                for (Signature signature : info.signatures) {
                    MessageDigest md = MessageDigest.getInstance("SHA");
                    md.update(signature.toByteArray());
                    Log.e("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));

                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


}
