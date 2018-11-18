package me.arifix.quizix.Client;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Arif Khan on 12/24/2017.
 */

public class ApiClient {
    public static Retrofit retrofit = null;

    public ApiClient() {
        // Blank Constructor
    };

    // Get Instance - Singleton
    public static synchronized Retrofit getApiClient(String baseURL) {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder().baseUrl(baseURL).addConverterFactory(GsonConverterFactory.create()).build();
        }
        return retrofit;
    }
}
