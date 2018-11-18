package me.arifix.quizix.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Arif Khan on 12/23/2017.
 */

public class Category {
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("parent_id")
    @Expose
    private String parent_id;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("thumbnail")
    @Expose
    private String thumbnail;
    @SerializedName("limit_questions")
    @Expose
    private String limitQuestions;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;
    @SerializedName("question_count")
    @Expose
    private String questionCount;
    @SerializedName("children_count")
    @Expose
    private String childrenCount;
    @SerializedName("quick")
    @Expose
    private String quick;
    @SerializedName("position")
    @Expose
    private String position;

    @SerializedName("paid")
    @Expose
    private String paid;

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getParent_id() {
        return parent_id;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getLimitQuestions() {
        return limitQuestions;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getQuestionCount() {
        return questionCount;
    }

    public String getChildrenCount() {
        return childrenCount;
    }

    public String getQuick() {
        return quick;
    }

    public String getPosition() {
        return position;
    }

    public String getPaid() {
        return paid;
    }
}

