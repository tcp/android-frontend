package project.cs.lisa.application;

import java.util.Set;

import project.cs.lisa.R;
import project.cs.lisa.wifi.WifiHandler;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

public class WifiDialog extends DialogFragment {

    private static final String TAG = "WifiDialog";

    private Set<String> wifis;

    WifiHandler wifiHandler;

    String selectedWifi;

    public WifiDialog(Set<String> wifis, WifiHandler wifiHandler) {
        this.wifis = wifis;
        this.wifiHandler = wifiHandler;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final CharSequence[] networks = wifis.toArray(new CharSequence[wifis.size()]);
        selectedWifi = networks[0].toString();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_wifi_title)
        .setSingleChoiceItems(networks, 0,
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Log.d(TAG, whichButton+"");
                selectedWifi = networks[whichButton].toString();
            }
        })
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Log.d(TAG, whichButton+"");
                wifiHandler.connectToSelectedNetwork(selectedWifi);
            }
        });

        return builder.create();
    }
}