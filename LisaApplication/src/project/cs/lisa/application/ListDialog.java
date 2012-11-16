package project.cs.lisa.application;

import java.util.Set;

import project.cs.lisa.R;
import project.cs.lisa.wifi.WifiHandler;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;

public class ListDialog extends DialogFragment {

    private static final String TAG = "ListDialog";

    private Set<String> mItems;

    String mSelectedItem;

    public ListDialog(Set<String> wifis) {
        mItems = wifis;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final CharSequence[] items = mItems.toArray(new CharSequence[mItems.size()]);
        mSelectedItem = items[0].toString();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_wifi_title)
        .setSingleChoiceItems(items, 0,
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Log.d(TAG, whichButton+"");
                mSelectedItem = items[whichButton].toString();
            }
        })
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Log.d(TAG, whichButton+"");
                onConfirm(mSelectedItem);
            }
        });

        return builder.create();
    }

    public void onConfirm(String item) {
        Log.d(TAG, "onConfirm()");
    }

}