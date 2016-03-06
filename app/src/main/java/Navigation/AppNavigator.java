package Navigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import com.intuned.app.MainActivity;
import com.intuned.app.R;

/* Used for navigation within the application. Based on the menuItemId passed in on touched of one of the menu items,
   the new activity will start.
*/
public class AppNavigator {
    private Class viewClass = null;
    private Activity activity;

    public AppNavigator(Activity activity, Context context){
        this.activity = activity;
    }

    public void navigate(int menuItemId){
        Class currentClass = activity.getClass();
        switch(menuItemId) {
            //Home
            case R.id.nav_match:
                viewClass = MainActivity.class;
                if (viewClass != currentClass) {
                    Intent intent = new Intent(activity, viewClass);
                    activity.startActivity(intent);
                    activity.finish();
                }
                break;
            //Profile
            case R.id.nav_my_profile:
                viewClass = MainActivity.class;//TODO: Change to profile activity.
                if (viewClass != currentClass) {
                    Intent intent = new Intent(activity, viewClass);
                    activity.startActivity(intent);
                    activity.finish();
                }
                break;
            default:
                viewClass = MainActivity.class;
        }
    }
}