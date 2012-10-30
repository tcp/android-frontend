package com.example.prototype;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;

public class PopUpActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showStartDialog(new WifiDialog());
        showStartDialog(new ShareDialog());
    }

    private void showStartDialog(DialogFragment dialog) {
    	android.app.FragmentManager fm = getFragmentManager();
		dialog.show(fm, "");
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}
