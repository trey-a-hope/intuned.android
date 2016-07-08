package Network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionManager {
    static ConnectivityManager cm;
    static NetworkInfo activeNetwork;

    // Determine if device is currently connected to the internet.
    public static boolean isConnected(Context context){
        cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    // Determine if connection is wifi.
    public static boolean isWifi(Context context){
        cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo.State mobile = cm.getNetworkInfo(0).getState();
        return mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING;
    }

    // Determine if connection is mobile.
    public static boolean isMobile(Context context){
        cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo.State mobile = cm.getNetworkInfo(1).getState();
        return mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING;
    }

}