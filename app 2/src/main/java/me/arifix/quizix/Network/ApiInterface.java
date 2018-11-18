package me.arifix.quizix.Network;

import me.arifix.quizix.Model.Category;
import me.arifix.quizix.Model.Question;
import me.arifix.quizix.Model.Tutorial;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by Arif Khan on 12/24/2017.
 */

public interface ApiInterface {
//    // Get All Categories
//    @GET("api/categories/{type}")
//    Call<List<Category>> getCategories(@Path("type") String type);
//
//    // Get Subcategories of a Category
//    @GET("api/categories/{id}/{type}")
//    Call<List<Category>> getSubCategories(@Path("id") int categoryId, @Path("type") String type);
//
//    // Get Questions of a Category
//    @GET("api/category/{id}/questions")
//    Call<List<Question>> getQuestionsFromCategory(@Path("id") int categoryId);
//
//    // Get Tutorial Content
//    @GET("api/tutorial")
//    Call<Tutorial> getTutorialContent();



    @GET("categories/{type}")
    Call<List<Category>> getCategories(@Path("type") String type);

    // Get Subcategories of a Category
    @GET("categories/{id}/{type}")
    Call<List<Category>> getSubCategories(@Path("id") int categoryId, @Path("type") String type);

    // Get Questions of a Category
    @GET("category/{id}/questions")
    Call<List<Question>> getQuestionsFromCategory(@Path("id") int categoryId);

    // Get Tutorial Content
    @GET("tutorial")
    Call<Tutorial> getTutorialContent();
}
