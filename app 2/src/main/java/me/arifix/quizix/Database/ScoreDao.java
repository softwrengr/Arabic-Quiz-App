package me.arifix.quizix.Database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import me.arifix.quizix.Model.Score;
import me.arifix.quizix.Utils.Config;

import java.util.List;

/**
 * Created by Arif Khan on 1/4/2018.
 */

@Dao
public interface ScoreDao {
    // Get All Score Data
    @Query("SELECT * FROM " + Config.DATABASE_TABLE_NAME)
    List<Score> getAllScore();

    // Insert Score Data
    @Insert
    void insertScore(Score... scores);

    // Get Score Data by Category
    @Query("SELECT * FROM " + Config.DATABASE_TABLE_NAME + " where category = :categoryName LIMIT 1")
    Score findByCategoryName(String categoryName);

    // Update Score Data by Category
    @Query("UPDATE " + Config.DATABASE_TABLE_NAME + " SET score = :score WHERE category = :categoryName")
    void updateScoreBycategory(int score, String categoryName);

    // Delete Score Data
    @Query("DELETE FROM " + Config.DATABASE_TABLE_NAME)
    void deleteEverything();

    // Get Sum of Scores
    @Query("SELECT SUM(score) AS \"total_score\" FROM " + Config.DATABASE_TABLE_NAME)
    int getTotalScore();
}
