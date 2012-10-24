package netinf.android.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class MyAlertDialogFragment extends DialogFragment {
			
	String mTitle   = "";
	String mMessage = "";
	
	public MyAlertDialogFragment(String title, String message){
		
		if(title!=null)
			mTitle = title;
		if(message!=null)
			mMessage = message;	
	}
	
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreateDialog(savedInstanceState);
		
        return new AlertDialog.Builder(getActivity())
        .setTitle(mTitle)
        .setMessage(mMessage)
        .setPositiveButton("Ok",
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	dialog.dismiss();
                }
            }
        )
        .create();
	}
}
