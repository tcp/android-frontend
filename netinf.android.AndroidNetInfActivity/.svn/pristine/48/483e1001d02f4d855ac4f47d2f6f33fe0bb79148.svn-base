package netinf.android.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;

public class MyProgressDialogFragment extends DialogFragment {
			
	String mMessage = "";
	
	public MyProgressDialogFragment(String message){
		
		if(message!=null)
			mMessage = message;	
	}
		
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreateDialog(savedInstanceState);
		
		ProgressDialog progDialog;
        progDialog = new ProgressDialog(getActivity());
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);	
        progDialog.setMessage(mMessage);
        
        return progDialog;
	}
}
