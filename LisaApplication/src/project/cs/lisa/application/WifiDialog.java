package project.cs.lisa.application;

import java.util.List;

import project.cs.lisa.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;

public class WifiDialog extends DialogFragment {

    private static final String TAG = "WifiDialog";

    private List<String> wifis;
    
    public WifiDialog(List<String> wifis) {
        this.wifis = wifis;
        Log.d(TAG, wifis.toString());
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final CharSequence[] networks = wifis.toArray(new CharSequence[wifis.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_wifi_title)
        .setSingleChoiceItems(networks, 0, null)
        .setPositiveButton(R.string.ok, null)
        .setNegativeButton(R.string.cancel, null);		
        return builder.create();
    }
}
