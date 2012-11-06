package netinf.android.dialog;

import netinf.android.DisplayResult;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

public class MyCacheDialogFragment extends DialogFragment {
			
	String mTitle    = "";
	String mMessage  = "";
	String mFilePath = "";
	
	public MyCacheDialogFragment(String title, String message,String filePath){
		
		if(title!=null)
			mTitle = title;
		if(message!=null)
			mMessage = message;
		if(filePath!=null)
			mFilePath = filePath;	
	}
	
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreateDialog(savedInstanceState);
		
        return new AlertDialog.Builder(getActivity())
        .setTitle(mTitle)
        .setMessage(mMessage)
        .setPositiveButton("View file",
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
            		Intent intent = new Intent(getActivity(), DisplayResult.class);
            		intent.putExtra("file_path", mFilePath);
            		
            		getActivity().startActivityForResult(intent, 0);
                }
            }
        )
        .setNegativeButton("Cancel",
        new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	dialog.dismiss();
            }
        }
        )
        .create();
	}
}
