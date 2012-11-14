package project.cs.lisa.application;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class WifiInfoMessage extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("To run this app you need a wifi network. " +
                "Do you want to scan for available wifi networks? " +
                "Choose OK to start the discovery. " +
                "Choose EXIT to exit the app.")
                .setTitle("Wifi Information")
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                MainNetInfActivity mA = (MainNetInfActivity) getActivity();
                                mA.doPositiveClickWifiInfoMessage();
                            }
                        })
                 .setNegativeButton("EXIT",
                         new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int whichButton) {
                                 MainNetInfActivity mA = (MainNetInfActivity) getActivity();
                                 mA.doNegativeClickWifiInfoMessage();
                             }
                         });       
        return builder.create();
    }
}