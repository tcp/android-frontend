package com.example.prototype;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.app.Activity;
import android.view.Menu;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayUseLogoEnabled(false);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Display the settings fragment as the main content.
        setTitle("Settings");
        getFragmentManager().beginTransaction().replace(android.R.id.content, new XmlFragment(R.xml.preferences)).commit();
        
        
        // Display the help fragment as the main content.
//        setTitle("Help");
//        getFragmentManager().beginTransaction().replace(android.R.id.content, new XmlFragment(R.xml.help)).commit();
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    public static class XmlFragment extends PreferenceFragment {
        
        private int mXml;
        
        public XmlFragment(int xml) {
            mXml = xml;
        }
        
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(mXml);
        }

    }
    
}
