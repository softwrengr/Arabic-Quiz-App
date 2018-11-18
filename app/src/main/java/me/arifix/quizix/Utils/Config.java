package me.arifix.quizix.Utils;

/**
 * Created by Arif Khan on 12/24/2017.
 */

public class Config {
    public static final String PROJECT_CODENAME = "arifix_quizix"; // Codename of your Project, required for Shared Preference
   // public static final String BASE_URL = "https://www.arifix.me/codecanyon/backend/quizix/"; // Base URL of your Site(for API Call), Trailing Slash is important, don't remove that

    public static final String BASE_URL = "http://sudan1956.com/quiz/api/";

    public static final boolean SHOW_ADS = true; // Show AdMob Ads, value- true/false

    public static final boolean QUESTION_COUNTDOWN = true; // Show CountDown Timer on Question, value- true/false
    public static final boolean MINUS_POINT = true; // Minus Point for per Wrong Answer, value- true/false
    public static final boolean SKIP_QUESTION = true; // Add Skip Question on Question, value- true/false
    public static final boolean FIFTY_FIFTY = true; // Add Fifty Fifty option on Question, value- true/false
    public static final boolean SHOW_EXPLANATION = true; // Show Explanation of Answer, value- true/false
    public static final boolean RANDOM_QUESTION = true; // Show Questions Randomly, value- true/false
    public static final boolean RANDOM_ANSWER = true; // Show Answers Randomly, value- true/false
    public static final boolean SHOW_CORRECT_ANSWER = true; // Show Correct Answer if Wrong, value- true/false

    public static final int QUESTION_COUNTDOWN_TIME = 25; // Countdown Time(In seconds)
    public static final int POINT_PER_CORRECT_ANSWER = 10; // Point per Correct Answer
    public static final int MINUS_POINT_PER_WRONG_ANSWER = 5; // Minus Point per Wrong Answer
    public static final int MAX_FIFTY_FIFTY_CHANCE = 2; // How many times they can take 50/50 Chance
    public static final int MAX_SKIP_QUESTION_CHANCE = 2; // How many times they can Skip Question
    public static final int QUICK_QUIZ_COUNTDOWN_TIME = 25; // Quick Quiz Category Countdown Time(In seconds)

    /* Form V4.0 */
    public static final String DATABASE_NAME = "arifix_quizix"; // Database name used to store Scores
    public static final String DATABASE_TABLE_NAME = "scores"; // Table name used to store Scores
    public static final String FIREBASE_TOPIC = "arifix_quizix"; // Firebase Push Notification Topic Name

    public static final boolean LIFE_LINE = true; // Lifeline, value- true/false
    public static final int MAX_LIFE_LINE = 3; // Max Life Line
    public static final boolean INTERNET_ONLY = false; // Run App only if active Internet Connection
    public static final boolean SPLASH_SCREEN = true; // If true, show Splash Activity on every time & if false, show only for first time.
    public static final int LIFE_PER_WATCH_VIDEO = 1; // How many lifeline user will get by watching per Reward Video Ad

    // Strings - Change those only if applicable
    public static final String CATEGORY_IMAGES_ROOT = "uploads/category/"; // Categories Photo Directory
    public static final String QUESTIONS_IMAGES_ROOT = "uploads/question/"; // Questions Photo Directory

    public static final String API_SUFFIX_ALL = "all";
    public static final String API_SUFFIX_FREE = "free";
    public static final String API_SUFFIX_PREMIUM = "premium";

    public static final String SPLASH_SCREEN_VISITED = "splashScreenVisited";
    public static final String AVAILABLE_LIFE = "availableLife";
    public static final String IS_PURCHASED = "isPurchased";

    public static final String USER_NAME = "userName";
    public static final String USER_EMAIL = "userEmail";
    public static final String USER_PHOTO = "userPhoto";

    public static final String ANSWER_CORRECT = "correctAnswer";
    public static final String ANSWER_INCORRECT = "incorrectAnswer";
    public static final String ANSWER_SKIPPED = "skippedQuestion";

    public static final String TUTORIAL_DATA = "tutorialData";
    public static final String CATEGORIES_DATA = "categoriesData";
    public static final String QUESTIONS_DATA = "questionsData";
    public static final String CATEGORY = "category";
    public static final String CATEGORY_ID = "categoryId";

    public static final String TAG_LEADERS_FRAGMENT = "leaders";
    public static final String TAG_SCORES_FRAGMENT = "scores";
    public static final String TAG_SETTINGS_SCREEN = "settings";

    public static final String SWITCH_SOUND = "switchSound";
    public static final String SWITCH_VIBRATION = "switchVibration";
    public static final String SWITCH_PUSH = "switchPush";
    public static final String SWITCH_RESET = "switchReset";

    public static final String FIRESTORE_COLLECTION_NAME = "leaders";
    public static final String FIRESTORE_NAME_COLUMN = "name";
    public static final String FIRESTORE_PHOTO_COLUMN = "photo";
    public static final String FIRESTORE_SCORE_COLUMN = "score";

    public static final int SHOW_LEADERS_COUNT = 15;
}
