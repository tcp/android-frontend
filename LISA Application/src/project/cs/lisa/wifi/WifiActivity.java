package project.cs.lisa.wifi;

import project.cs.lisa.R;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

public class WifiActivity extends Activity {

	private TextView mTextView;
	private WifiHandler wifi;
	
  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_wifi);
      wifi = new WifiHandler(this);
      displayCheckWifi();
  }
  
  /**
   * Function to handle the Toggle Button that allows the user to
   * set the WIFI state on or off. Have in mind that this changes
   * the whole state, meaning that switching off stops the WiFi
   * service for the whole phone. 
   * @param view
   */
  
  public void onToggleClicked(View view) {
  	Log.d("WiFi Activity", "onClick()");
  	
  	boolean toggleOn = ((ToggleButton) view).isChecked();
  	Log.d("WiFi Activity", "Toggle Response: " + toggleOn);

  	if (toggleOn) {
  		if (!wifi.disableWifi()) {
  			Log.d("WiFi Activity", "Failed to disable WiFi");
  		}
  		else {
  			Log.d("WiFi Activity", "WiFi disabled");
  		}
  	}
  	else {
  		if (!wifi.enableWifi()) {
  			Log.d("WiFi Activity", "Failed to enable WiFi");
  		}
  		else {
  			Log.d("WiFi Activity", "WiFi enabled");
  		}
  	}
  }
  
  public void displayCheckWifi() {
  	if (wifi.wifiInfo != null) {
  		mTextView = (TextView) findViewById(R.id.textView2);
  		mTextView.setText(wifi.wifiInfo.getSSID());
  	}
  	else
  		mTextView.setText("Not connected");
  }

}
