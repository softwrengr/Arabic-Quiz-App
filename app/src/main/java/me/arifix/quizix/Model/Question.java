package me.arifix.quizix.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Arif Khan on 12/29/2017.
 */

public class Question {
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("category_id")
    @Expose
    private Integer categoryId;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("question_type")
    @Expose
    private String questionType;
    @SerializedName("thumbnail")
    @Expose
    private String thumbnail;
    @SerializedName("number_of_answer")
    @Expose
    private Integer numberOfAnswer;
    @SerializedName("choice_a")
    @Expose
    private String choiceA;
    @SerializedName("choice_b")
    @Expose
    private String choiceB;
    @SerializedName("choice_c")
    @Expose
    private String choiceC;
    @SerializedName("choice_d")
    @Expose
    private String choiceD;
    @SerializedName("choice_e")
    @Expose
    private String choiceE;
    @SerializedName("answer")
    @Expose
    private String answer;
    @SerializedName("explanation")
    @Expose
    private String explanation;
    @SerializedName("status")
    @Expose
    private Integer status;

    public Integer getId() {
        return id;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public String getTitle() {
        return title;
    }

    public String getQuestionType() {
        return questionType;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public Integer getNumberOfAnswer() {
        return numberOfAnswer;
    }

    public String getChoiceA() {
        return choiceA;
    }

    public String getChoiceB() {
        return choiceB;
    }

    public String getChoiceC() {
        return choiceC;
    }

    public String getChoiceD() {
        return choiceD;
    }

    public String getChoiceE() {
        return choiceE;
    }

    public String getAnswer() {
        return answer;
    }

    public String getExplanation() {
        return explanation;
    }

    public Integer getStatus() {
        return status;
    }
}