package me.arifix.quizix.Network;

import me.arifix.quizix.Model.Question;
import me.arifix.quizix.Model.Category;
import me.arifix.quizix.Model.Tutorial;

import java.util.List;

/**
 * Created by Arif Khan on 12/24/2017.
 */

public interface ApiService {
    // Get All Categories
    void getCategories(String type, QueryCallback<List<Category>> callback);

    // Get Subcategories of a Category
    void getSubCategories(int categoryId, String type, QueryCallback<List<Category>> callback);

    // Get Questions of a Category
    void getQuestionsFromCategory(int categoryId, QueryCallback<List<Question>> callback);

    // Get Tutorial Content
    void getTutorialContent(QueryCallback<Tutorial> callback);
}
