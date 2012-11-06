package com.example.prototype;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ProgressBar;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Set progress bar
        ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    public final void goButtonClicked(final View v) {
        EditText editText = (EditText) findViewById(R.id.url);
        String url = editText.getText().toString();

        WebView webView = (WebView) findViewById(R.id.webView);
        webView.loadUrl(url);
    }
}
