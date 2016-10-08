package Navigation;

import Services.ModalService;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import com.intuned.app.home.HomeController;
import com.intuned.app.options.OptionsController;
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
                if (HomeController.class != currentClass) {
                    Intent intent = new Intent(activity, HomeController.class);
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
            //Options
            case R.id.nav_options:
                if(OptionsController.class != currentClass){
                    Intent intent = new Intent(activity, OptionsController.class);
                    activity.startActivity(intent);
                    activity.finish();
                }
                break;
            //Sign Out
            case R.id.nav_signout:
                if(ModalService.getInstance().displayConfirmation("Log Out", "Are you sure?", activity)){
                    sessionManager.logout();
                    Intent intent = new Intent(activity, LoginController.class);
                    activity.startActivity(intent);
                    activity.finish();
                    Log.d("Logging Out: ", "How");
                }
                break;
            default:
                break;
        }
    }
}