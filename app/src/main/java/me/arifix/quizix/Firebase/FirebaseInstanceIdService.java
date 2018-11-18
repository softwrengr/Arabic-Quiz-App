package me.arifix.quizix.Firebase;

import com.google.firebase.iid.FirebaseInstanceId;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

/**
 * Created by Arif Khan on 1/3/2018.
 */

public class FirebaseInstanceIdService extends com.google.firebase.iid.FirebaseInstanceIdService {
    private static final String TAG = "FirebaseInstanceIdSer";

    @Override
    public void onTokenRefresh() {
        // Initialize Logger Library
        Logger.addLogAdapter(new AndroidLogAdapter());

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Logger.d(TAG + ": Refreshed token: " + refreshedToken);
    }

    private void sendRegistrationToServer(String token) {
        // You can implement this method to store the token on your server
        // Not required for current project
    }
}