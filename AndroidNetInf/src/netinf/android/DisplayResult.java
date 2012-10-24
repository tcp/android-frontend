package netinf.android;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class DisplayResult extends Activity {

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub

        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_result);
        Button back = (Button) findViewById(R.id.back_button);
        ImageView result = (ImageView) findViewById(R.id.imageView1);
        
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	finish();                
            }

        });
        
        String path =  this.getIntent().getStringExtra("file_path");
                
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;

        Bitmap mResultImage = BitmapFactory.decodeFile(path, options);
	    result.setImageBitmap(mResultImage);
    }
}
	

