package Navigation;

import android.app.Activity;
import android.content.Intent;
import com.intuned.app.MainActivity;
import com.intuned.app.profile.ProfileController;
import com.intuned.app.R;
import com.intuned.app.authentication.LoginController;
import com.intuned.app.authentication.SessionManager;

/**
 * Used for navigation within the application. Based on the menuItemId passed in on touched of one of the menu items,
 * the new activity will start.
**/
public class AppNavigator {
    public static void navigate(int menuItemId, Activity activity){
        Class currentClass = activity.getClass();
        SessionManager sessionManager = new SessionManager(activity, activity.getApplicationContext());
        switch(menuItemId) {
            //Home
            case R.id.nav_explore:
                if (MainActivity.class != currentClass) {
                    Intent intent = new Intent(activity, MainActivity.class);
                    activity.startActivity(intent);
                    activity.finish();
                }
                break;
            //Profile
            case R.id.nav_my_profile:
                if (ProfileController.class != currentClass) {
                    Intent intent = new Intent(activity, ProfileController.class);
                    activity.startActivity(intent);
                    activity.finish();
                }
                break;
            //Sign Out
            case R.id.nav_signout:
                sessionManager.logout();
                Intent intent = new Intent(activity, LoginController.class);
                activity.startActivity(intent);
                activity.finish();
                break;
            default:
        }
    }
}