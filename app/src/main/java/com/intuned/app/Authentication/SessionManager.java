package com.intuned.app.authentication;

        import DTO.User;
        import android.app.Activity;
        import android.content.Context;
        import android.content.SharedPreferences;
        import android.content.SharedPreferences.*;
        import com.google.firebase.auth.FirebaseAuth;
        import com.google.gson.Gson;

/**
 * Handles current user session after successful login.
 */
public class SessionManager {
    // Activity calling the service
    private Activity activity;

    // Name of shared preferences.
    private final String SHARED_PREFERENCES_NAME = "VibesStorage";
    private final String USER_INSTANCE = "UserInstance";

    // Shared Preferences
    private SharedPreferences sharedPreferences;

    private Editor editor;

    // Context of activity using service
    private Context context;

    // Shared pref mode
    private int PRIVATE_MODE = 0;

    private static final String KEY_IS_LOGGEDIN = "isLoggedIn";

    // Firebase com.intuned.app.Authentication
    private FirebaseAuth firebaseAuth;

    public SessionManager(Activity activity, Context context){
        this.context = context;
        sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, PRIVATE_MODE);
        editor = sharedPreferences.edit();
        this.activity = activity;
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public void setUserInstance(User user){
        Gson gson = new Gson();
        String json = gson.toJson(user);
        editor.putString(USER_INSTANCE, json);
        // Commit changes.
        editor.commit();
    }

    public User getUserInstance(){
        Gson gson = new Gson();
        String json = sharedPreferences.getString(USER_INSTANCE, "");
        return gson.fromJson(json, User.class);
    }

    public void login(){

        editor.putBoolean(KEY_IS_LOGGEDIN, true);
        // Commit changes.
        editor.commit();
    }

    public void logout(){
//        editor.putBoolean(KEY_IS_LOGGEDIN, false);
//        // Commit changes.
//        editor.commit();
        firebaseAuth.signOut();
    }

    public boolean isLoggedIn(){
//        return sharedPreferences.getBoolean(KEY_IS_LOGGEDIN, false);
        return firebaseAuth.getCurrentUser() != null;
    }
}