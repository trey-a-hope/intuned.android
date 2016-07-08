package Configuration;

/*
    Common strings such as URLs or Preference Names.
*/

public class AppConfig {
    /**************************
     * AMAZON WEB SERVICES : S3
     *************************/
    public static String S3_IDENTITY_POOL_ID = "us-east-1:ce289779-5e69-4b82-833d-34ff3405e939";
    public static String S3_BUCKET_NAME = "intunedbucket";
    public static String S3_MUSIC_BUCKET_URL = "https://s3.amazonaws.com/intunedbucket/";

    /**************************
     * FIREBASE
     *************************/
    public static String FIREBASE_URL = "https://intuned.firebaseio.com/";
    public static String TABLE_USERS = "Users";

    /**************************
     * TIMER
     *************************/
    public static int SONG_DURATION = 20;               //20 seconds.

    /**************************
     * MESSAGES
     *************************/
    public static String WAITING_MESSAGE = "Please wait...";
}