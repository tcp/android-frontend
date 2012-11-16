package project.cs.lisa.application.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

public class OkButtonDialog extends DialogFragment {

    OnClickListener mOkListener;
    String mMessage;
    String mTitle;
    
    public OkButtonDialog(String title, String message, OnClickListener okListener) {
        mOkListener = okListener;
        mMessage = message;
        mTitle = title;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(mMessage)
                .setTitle(mTitle)
                .setPositiveButton("OK", mOkListener);       
        return builder.create();
    }
}