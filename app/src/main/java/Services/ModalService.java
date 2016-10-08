package Services;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import Configuration.AppConfig;

public class ModalService {
    private static ModalService _modalServiceInstance = new ModalService();

    public static ModalService getInstance() {
        return _modalServiceInstance;
    }

    private ModalService() {}

    private AlertDialog.Builder alertDialogBuilder;     //Modal builder.
    private AlertDialog alertDialog;                    //Modal to be displayed.
    private boolean confirmation;
    private ProgressDialog progressDialog;

    public void displayToast(String message, Activity activity){
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }

    public void displayNotification(String title, String message, Activity activity) {
        alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public boolean displayConfirmation(String title, String message, Activity activity) {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message message1) {
                throw new RuntimeException();
            }
        };

        alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                confirmation = true;
                handler.sendMessage(handler.obtainMessage());
            }
        });
        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                confirmation = false;
                handler.sendMessage(handler.obtainMessage());
            }
        });
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        // loop till a runtime exception is triggered.
        try {
            Looper.loop();
        } catch (RuntimeException e2) {
        }

        return confirmation;
    }

    public void displayTest(String message, Activity activity) {
        alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setTitle("Test Modal");
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void toggleProgressDialogOn(Activity activity, String title){
        progressDialog = new ProgressDialog(activity);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(title);
        progressDialog.setMessage(AppConfig.WAIT_MESSAGE);
        progressDialog.show();
    }

    public void toggleProgressDialogOff(){
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }
}