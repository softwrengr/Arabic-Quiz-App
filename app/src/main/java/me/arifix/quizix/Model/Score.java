package me.arifix.quizix.Model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import me.arifix.quizix.Utils.Config;

/**
 * Created by Arif Khan on 1/4/2018.
 */

@Entity(tableName = Config.DATABASE_TABLE_NAME)
public class Score {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "category")
    public String categoryName;

    @ColumnInfo(name = "score")
    public int score;

    @ColumnInfo(name = "thumbnail")
    public String thumbnail;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
}