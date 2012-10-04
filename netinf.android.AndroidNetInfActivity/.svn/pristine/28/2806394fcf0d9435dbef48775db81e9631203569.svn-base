package netinf.android.resolution.local.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {  
	
    private static final String DATABASE_NAME    = "localresolutionservice";
    private static final int    DATABASE_VERSION = 6;
    public  static final String TABLE_NAME       = "LOCAL_RESOLUTION_TABLE";
 
    //Column names
     
    public  static final String _ID              = "_id";
    public  static final String HASH_ALG         = "HASH_ALG";
    public  static final String HASH_CONTENT     = "HASH_CONTENT";
    
    //Column indexes
    
    public static final int ID_COLUMN_INDEX            = 0;
    public static final int HASH_ALG_COLUMN_INDEX      = 1;
    public static final int HASH_CONTENT_COLUMN_INDEX  = 2;
       
    // Database creation sql statement   
    private static final String DATABASE_CREATE = "create table " + TABLE_NAME  +  
            " (" + _ID           +" integer primary key autoincrement, " + 
                   HASH_ALG      + " text not null," +
                   HASH_CONTENT  + " text not null);"; 
  
    public MySQLiteHelper(Context context) {  
        super(context, DATABASE_NAME, null, DATABASE_VERSION);  
    }  
  
    @Override  
    public void onCreate(SQLiteDatabase database) {  
        database.execSQL(DATABASE_CREATE);  
    }  
  
    @Override  
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {  
		Log.w(MySQLiteHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db); 
    }  
  
} 