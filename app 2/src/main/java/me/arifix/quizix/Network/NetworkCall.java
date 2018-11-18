package me.arifix.quizix.Network;

import me.arifix.quizix.Client.ApiClient;
import me.arifix.quizix.Model.Question;
import me.arifix.quizix.Utils.Config;
import me.arifix.quizix.Model.Category;
import me.arifix.quizix.Model.Tutorial;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Arif Khan on 12/24/2017.
 */

public class NetworkCall implements ApiService {
    private ApiInterface apiInterface = ApiClient.getApiClient(Config.BASE_URL).create(ApiInterface.class);

    // Get All Categories
    @Override
    public void getCategories(String type, final QueryCallback<List<Category>> callback) {
        Call<List<Category>> call = apiInterface.getCategories(type);
        call.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.code() == 200) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(new Exception(response.message()));
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    // Get Subcategories of a Category
    @Override
    public void getSubCategories(int categoryId, String type, final QueryCallback<List<Category>> callback) {
        Call<List<Category>> call = apiInterface.getSubCategories(categoryId, type);
        call.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.code() == 200) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(new Exception(response.message()));
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    // Get Questions of a Category
    @Override
    public void getQuestionsFromCategory(int categoryId, final QueryCallback<List<Question>> callback) {
        Call<List<Question>> call = apiInterface.getQuestionsFromCategory(categoryId);
        call.enqueue(new Callback<List<Question>>() {
            @Override
            public void onResponse(Call<List<Question>> call, Response<List<Question>> response) {
                if (response.code() == 200) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(new Exception(response.message()));
                }
            }

            @Override
            public void onFailure(Call<List<Question>> call, Throwable t) {
                callback.onError(t);
            }
        });
    }

    // Get Tutorial Content
    @Override
    public void getTutorialContent(final QueryCallback<Tutorial> callback) {
        Call<Tutorial> call = apiInterface.getTutorialContent();
        call.enqueue(new Callback<Tutorial>() {
            @Override
            public void onResponse(Call<Tutorial> call, Response<Tutorial> response) {
                if (response.code() == 200) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(new Exception(response.message()));
                }
            }

            @Override
            public void onFailure(Call<Tutorial> call, Throwable t) {
                callback.onError(t);
            }
        });
    }
}
