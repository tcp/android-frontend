package project.cs.lisa.application.dialogs;

import project.cs.lisa.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

public class ShareDialog extends DialogFragment {
	
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(R.string.dialog_share_msg)
				   .setTitle(R.string.dialog_share_title)
				   .setPositiveButton(R.string.yes, null)
				   .setNegativeButton(R.string.no, null);		
			return builder.create();
		}
}
