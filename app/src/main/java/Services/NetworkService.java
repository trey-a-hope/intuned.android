package Services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkService {
    private static NetworkService _networkServiceInstance = new NetworkService();

    public static NetworkService getInstance() {
        return _networkServiceInstance;
    }

    private NetworkService() {}

    private ConnectivityManager connectivityManager;
    private NetworkInfo networkInfo;

    // Determine if device is currently connected to the internet.
    public boolean isConnected(Context context){
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    // Determine if connection is wifi.
    public boolean isWifi(Context context){
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo.State mobile = connectivityManager.getNetworkInfo(0).getState();
        return mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING;
    }

    // Determine if connection is mobile.
    public boolean isMobile(Context context){
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo.State mobile = connectivityManager.getNetworkInfo(1).getState();
        return mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING;
    }

}