package Navigation;

import Services.ModalService;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import com.intuned.app.MainActivity;
import com.intuned.app.ProfileController;
import com.intuned.app.R;

/**
 * Used for navigation within the application. Based on the menuItemId passed in on touched of one of the menu items,
 * the new activity will start.
**/
public class AppNavigator {
    public static void navigate(int menuItemId, Activity activity){
        Class currentClass = activity.getClass();
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
            default:
        }
    }
}