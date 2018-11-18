package me.arifix.quizix.Database;

import me.arifix.quizix.Model.Score;

import java.util.List;

/**
 * Created by Arif Khan on 1/4/2018.
 */

public class DatabaseCall {
    // Insert Score Data
    public static Score addScoreData(AppDatabase db, Score score) {
        db.ScoreDao().insertScore(score);
        return score;
    }

    // Get All Score Data
    public static List<Score> getAllScore(AppDatabase db) {
        return db.ScoreDao().getAllScore();
    }

    // Get Score Data by Category
    public static Score findByCategoryName(AppDatabase db, String categoryName) {
        return db.ScoreDao().findByCategoryName(categoryName);
    }

    // Update Score Data by Category
    public static void updateScoreBycategory(AppDatabase db, int score, String categoryName) {
        db.ScoreDao().updateScoreBycategory(score, categoryName);
    }

    // Delete Score
    public static void deleteAllData(AppDatabase db) {
        db.ScoreDao().deleteEverything();
    }

    // Return Total Score
    public static int totalScore(AppDatabase db){
        return db.ScoreDao().getTotalScore();
    }
}
