package project.cs.lisa.application;

import project.cs.lisa.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

public class WifiDialog extends DialogFragment {
	
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			
			final CharSequence[] networks = {"UpUnet-S","My Phone"};
			
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.dialog_wifi_title)
				   .setSingleChoiceItems(networks, 0, null)
				   .setPositiveButton(R.string.ok, null)
				   .setNegativeButton(R.string.cancel, null);		
			return builder.create();
		}
}
