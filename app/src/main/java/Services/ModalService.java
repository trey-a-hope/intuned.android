package Services;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

public class ModalService {
    private static AlertDialog.Builder alertDialogBuilder;     //Modal builder.
    private static AlertDialog alertDialog;                    //Modal to be displayed.
    private static boolean confirmation;

    public static void displayToast(String message, Activity activity){
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }

    public static void displayNotification(String title, String message, Activity activity) {
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

    public static boolean displayConfirmation(String title, String message, Activity activity) {
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

    public static void displayTest(String message, Activity activity) {
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
}