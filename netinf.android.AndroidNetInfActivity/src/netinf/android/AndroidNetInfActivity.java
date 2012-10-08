package netinf.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import netinf.android.access.bluetooth.AndroidBluetoothBOServer;
import netinf.android.access.tcp.TCPServer;
import netinf.android.application.http.SendHttpGetRequestTask;
import netinf.android.application.http.SendHttpPostRequestTask;
import netinf.android.common.security.hashing.HashingSHA256_EncodingBase64URL;
import netinf.android.dialog.MyAlertDialogFragment;
import netinf.android.dialog.MyProgressDialogFragment;
import netinf.android.input.qrscanning.IntentIntegrator;
import netinf.android.input.qrscanning.IntentResult;
import netinf.android.resolution.AndroidLocalResolutionService;
import netinf.android.resolution.local.database.MySQLiteHelper;
import netinf.android.wifi.NetInfWifiDirectManager;
import netinf.node.access.AccessServer;
import netinf.node.api.NetInfNode;
import netinf.node.resolution.ResolutionController;
import netinf.node.resolution.ResolutionService;

import org.apache.commons.io.IOUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.inject.Injector;

public class AndroidNetInfActivity extends Activity{
   
  // Debugging
  private static final String TAG = "AndroidNetInfActivity";
  private static final boolean D = true; // Debugging?
    
  public static final String PROPERTIES = "android_netinfnode.properties";
  
  // Naming variables
  // General
	private String HASH_ALGORITHM;
	private String TARGETHOST;
	private String TARGETPORT;
	
	// Server type
	public static String NCS_SERVER_URL;
	public static String NRS_SERVER_URL;
	public static String QRCODE_SERVER_URL;

	// Properties
	private static final String HASH_ALG_PROPERTY          = "naming.hashAlgorithm";
	private static final String TARGET_HOST_PROPERTY       = "access.http.host";
	private static final String TARGET_PORT_PROPERTY       = "access.http.port";
	private static final String NCS_SERVER_URL_PROPERTY    = "server.ncs.url";
	private static final String NRS_SERVER_URL_PROPERTY    = "server.nrs.url";
	private static final String QRCODE_SERVER_URL_PROPERTY = "server.qrc.url";
		
	//Image picking variables
	private static final int PICK_FROM_CAMERA  = 0;
	private static final int PICK_FROM_FILE    = 1;
	private static final int PICK_FOR_DELETE   = 2;
	
  	//Dialog constants
	public static final String DIALOG_TYPE = "type";
	public static final int ALERT_DIALOG    = 1;
	public static final int PROGRESS_DIALOG = 2;
	
	//Alert dialogs
	public static final String GET_IO_NOT_FOUND           = "get_io_not_found";
	public static final String PUT_IO                     = "put_io";
	public static final String DELETE_IO                  = "delete_io";
	public static final String CACHE_IO                   = "cache_io";
	public static final String MY_SHARED_FOLDER_PROBLEM   = "my_shared_folder_problem";
	public static final String HASH                       = "hash";
	public static final String FILE_PATH                  = "file_path";
	public static final String WIFI_DIRECT_CONNECTION     = "wifi_direct_connection";
	public static final String TRANSFERRING_ERROR         = "transferring_error";
	public static final String TRANSPORT_TECHNOLOGY_ERROR = "transport_technology_error";
		
	//Progress dialogs
	public static final String SEARCHING    = "searching";
	public static final String CONNECTING   = "connecting";
	public static final String DISCOVERING  = "discovering";
	public static final String TRANSFERRING = "transferring";
	public static final String CACHING      = "caching";
	
	//Transport technology 
	public static final int BLUETOOTH       = 1;
	public static final int WIFI            = 2;
	public static final int NCS             = 3;
	
		
	private static final int REQUEST_ENABLE_BT = 3;
    	
	private Uri mImageCaptureUri;
	public int transportTechnology = 0;
	private String cacheContent = "false"; 
	
	// Bluetooth communication variables
  private BluetoothAdapter mBluetoothAdapter = null;
  private AndroidBluetoothBOServer mBluetoothBOserver;
  public String bluetoothMacAddress;
    
  //Wifi Direct
  WifiP2pManager mManager;
  Channel mChannel;
  IntentFilter mIntentFilter;
  public BroadcastReceiver mReceiver;
  public NetInfWifiDirectManager mNetInfManager;
  public String wifiMacAddress;
    
  //TCP server
  public TCPServer myTcpServer;

	//Reference to the application
	private AndroidNetInfApplication mApplication; 
	
	//Reference to the activity
	private AndroidNetInfActivity mActivity;
    
  //NetInf node variables
	private Injector injector;
	private NetInfNode netInfNode;
	boolean nodeStarted = false;
	private MySQLiteHelper localDBHelper;		
	
	//Handler messages types 
  public static final int MESSAGE_NODE_STATUS        = 1;
  public static final int MESSAGE_GET_REQUEST_RESULT = 2;
    
	//***User Interface variables***
    
  //Dialog used when putting an image to select  from camera or SD 
  AlertDialog putImageDialog;
	
	//Buttons holders
	ImageButton getButton;
	ImageButton putButton;
	ImageButton deleteButton;
	ImageButton ncsButton;
	
	//Checkbox holders
	CheckBox bluetoothCheckbox;
	CheckBox wifiCheckbox;
	CheckBox NCSCheckbox;
	
	//Dialog variables
	String mTitle;
	String mMessage;

  @Override
  public void onCreate(Bundle savedInstanceState) {      	
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main2);
    
    if (D)
    	Log.e(TAG, "+++ ON CREATE +++");
    
    //Getting a reference to the application
    mApplication = (AndroidNetInfApplication) getApplication();

    //Getting a reference to the activity
    mActivity = this;

    //Creating the injector         
    injector = mApplication.getInjector();

    //Reading properties from properties files 
    Properties mAndroidProperties = mApplication.getProperties(PROPERTIES);

    //Initializing rest access variables from properties file               
    TARGETHOST     = mAndroidProperties.getProperty(TARGET_HOST_PROPERTY);
    TARGETPORT     = mAndroidProperties.getProperty(TARGET_PORT_PROPERTY);
    HASH_ALGORITHM = mAndroidProperties.getProperty(HASH_ALG_PROPERTY);

    //Initializing Servers' addresses
    if (D)
    	Log.d(TAG, "Loading servers' addresses from property file");
        
    NCS_SERVER_URL      = mAndroidProperties.getProperty(NCS_SERVER_URL_PROPERTY);      
    NRS_SERVER_URL      = mAndroidProperties.getProperty(NRS_SERVER_URL_PROPERTY);       
    QRCODE_SERVER_URL   = mAndroidProperties.getProperty(QRCODE_SERVER_URL_PROPERTY);

    if (D) {
    	Log.d(TAG, "Target NCS URL = "            + NCS_SERVER_URL);
    	Log.d(TAG, "Target NRS URL = "            + NRS_SERVER_URL);
    	Log.d(TAG, "Target QR_CODE SERVER URL = " + QRCODE_SERVER_URL);
    }

    //Set up wifi direct
    mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
    mChannel = mManager.initialize(this, getMainLooper(), null);
    mNetInfManager = new NetInfWifiDirectManager(mManager,mChannel);
    mReceiver = new NetInfBroadcastReceiver(mManager, mChannel,mNetInfManager);

    mIntentFilter = new IntentFilter();
    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);       

    WifiManager wm = (WifiManager) mActivity.getSystemService(Context.WIFI_SERVICE);
    wifiMacAddress = wm.getConnectionInfo().getMacAddress();
    
    if (wifiMacAddress != null)
    	wifiMacAddress = calculateWifiDirectMac(wifiMacAddress);
      
    // Get local Bluetooth adapter
    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    //bluetoothMacAddress = mBluetoothAdapter.getAddress();

    mIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);

    // If the adapter is null, then Bluetooth is not supported
    if (mBluetoothAdapter == null) {
    	Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
    	finish();
    	return;
    }

    if (!mBluetoothAdapter.isEnabled()) {
    	Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    	startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
    	// Otherwise, start the NetInfNode
    }
    else{
    	//Starting the NetInfNode
    	new NodeStarter(this).start();
    }

    //Initializing UI variables
    initializeUI();                           	      
  }
   
  @Override
  public void onStart() {
  	super.onStart();
  	if (D)
  		Log.e(TAG, "++ ON START ++");
  }

  @Override
  protected void onResume() {
  	super.onResume();
  	
  	if (D)
  		Log.e(TAG, "+ ON RESUME +");
  	
  	registerReceiver(mReceiver, mIntentFilter);
  }

  /* unregister the broadcast receiver */
  @Override
  protected void onPause() {
  	super.onPause();
  	
  	if (D)
  		Log.e(TAG, "- ON PAUSE -");
  	
  	unregisterReceiver(mReceiver);
  }

  @Override
  public void onStop() {
  	super.onStop();
  	
  	if (D)
  	  Log.e(TAG, "-- ON STOP --");
  }

  @Override
  public void onDestroy() {
  	super.onDestroy();
  	
  	if (localDBHelper != null)
  		localDBHelper.close();
  	
  	if (D)
  		Log.e(TAG, "--- ON DESTROY ---");
  }

  private void initializeUI() {
  	//Setting up the button holders      
  	getButton         = (ImageButton) findViewById(R.id.get);
  	putButton         = (ImageButton) findViewById(R.id.put);
  	deleteButton      = (ImageButton) findViewById(R.id.delete);
  	ncsButton         = (ImageButton) findViewById(R.id.ncs);

  	//Setting up checkbox holders
  	bluetoothCheckbox = (CheckBox) findViewById(R.id.checkBox1);
  	wifiCheckbox      = (CheckBox) findViewById(R.id.checkBox2);
  	NCSCheckbox       = (CheckBox) findViewById(R.id.checkBox3);

  	//Selecting Bluetooth as the default transport technology
  	bluetoothCheckbox.setChecked(true);
  	bluetoothCheckbox.setTextColor(Color.BLUE);

  	wifiCheckbox.setChecked(false);
  	wifiCheckbox.setTextColor(Color.BLACK);

  	wifiCheckbox.setChecked(false);
  	wifiCheckbox.setTextColor(Color.BLACK);

  	NCSCheckbox.setChecked(false);
  	NCSCheckbox.setTextColor(Color.BLACK);

  	transportTechnology = BLUETOOTH;

  	//Listeners

  	//Buttons

  	//Get button listener
  	getButton.setOnClickListener(new OnClickListener() {
  		public void onClick(View v) {        	
  			if (!nodeStarted) {		
					if (transportTechnology == AndroidNetInfActivity.WIFI) {
						if (mNetInfManager.isWifiP2pEnabled)
							IntentIntegrator.initiateScan(mActivity);
						else {
							mTitle   = "Transport technology problem";
							mMessage = "The transport technology selected is not turned " +
												 "on or the device does not support this technology";
								
							DialogFragment newFragment = new MyAlertDialogFragment(mTitle,mMessage);
							newFragment.show(mActivity.getFragmentManager(), AndroidNetInfActivity.TRANSPORT_TECHNOLOGY_ERROR); 				
						}
					}
					else	        			
						IntentIntegrator.initiateScan(mActivity);					 
  			}
  		}
  	});

  	//Put button listener
  	putButton.setOnClickListener(new OnClickListener() {
  		public void onClick(View v) {        	
  			if (nodeStarted) {       		
  				putImageDialog.show();
  			}
  		}
  	});

  	//Delete button listener
  	deleteButton.setOnClickListener(new OnClickListener() {
  		public void onClick(View v) {        	
  			if (nodeStarted) {	        		 	    		 
  				Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
  				photoPickerIntent.setType("image/*");	        		    
  				startActivityForResult(photoPickerIntent, PICK_FOR_DELETE); 	                    	        		 
  			}
  		}
  	});   

  	//Description button listener
  	ncsButton.setOnClickListener(new OnClickListener() {
  		public void onClick(View v) {        	
  			cacheContent = "true";
  			putImageDialog.show();
  		}
  	});

  	//Checkmarks

  	//Bluetooth checkmark listener
  	bluetoothCheckbox.setOnClickListener(new OnClickListener() {
  		public void onClick(View v) {        	
  			wifiCheckbox.setChecked(false);
  			NCSCheckbox.setChecked(false);
  			bluetoothCheckbox.setTextColor(Color.BLUE);
  			wifiCheckbox.setTextColor(Color.BLACK);
  			NCSCheckbox.setTextColor(Color.BLACK);	  
  			transportTechnology = BLUETOOTH;
  		}
  	});

  	//Wifi checkmark listener
  	wifiCheckbox.setOnClickListener(new OnClickListener() {
  		public void onClick(View v) {
  			bluetoothCheckbox.setChecked(false);
  			NCSCheckbox.setChecked(false);
  			wifiCheckbox.setTextColor(Color.BLUE);
  			bluetoothCheckbox.setTextColor(Color.BLACK);
  			NCSCheckbox.setTextColor(Color.BLACK);
  			transportTechnology = WIFI;
  		}
  	});

  	//Mobile network checkmark listener
  	NCSCheckbox.setOnClickListener(new OnClickListener() {
  		public void onClick(View v) {
  			wifiCheckbox.setChecked(false);
  			bluetoothCheckbox.setChecked(false);
  			NCSCheckbox.setTextColor(Color.BLUE);
  			bluetoothCheckbox.setTextColor(Color.BLACK);
  			wifiCheckbox.setTextColor(Color.BLACK);
  			transportTechnology = NCS;   	 
  		}
  	});

  	putImageDialog = createPutImageDialog(); 	
  }

  private AlertDialog createPutImageDialog() {

  	final String [] items           = new String [] {"From Camera", "From SD Card"};
  	ArrayAdapter<String> adapter1   = new ArrayAdapter<String> (this, android.R.layout.select_dialog_item,items);
  	AlertDialog.Builder builder     = new AlertDialog.Builder(this);

  	builder.setTitle("Select Image");
  	builder.setAdapter( adapter1, new DialogInterface.OnClickListener() {
  		public void onClick( DialogInterface dialog, int item ) {

  			//First the application checks if the MySharedFiles folder is created.
  			if (createSharedFolder()) { 
  				switch (item) {
  					case PICK_FROM_CAMERA: {
  						String fileName = "netinf"+ System.currentTimeMillis() + ".jpg";	                      	    
  						Intent intent    = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); 
  						File file        = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),fileName);

  						mImageCaptureUri = Uri.fromFile(file);

  						try {
  							intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
  							startActivityForResult(intent, PICK_FROM_CAMERA);
  						}
  						catch (Exception e) {
  							e.printStackTrace();
  						}

  						dialog.cancel();
  						break;
  					}

  					case PICK_FROM_FILE:{
  						Intent intent = new Intent();

  						intent.setType("image/*");
  						intent.setAction(Intent.ACTION_GET_CONTENT);

  						startActivityForResult(Intent.createChooser(intent, "Complete action using"), PICK_FROM_FILE);	            		
  						break;
  					}
  				}           	            		            		                        		
  			}
  			else {
  				DialogFragment newFragment = new MyAlertDialogFragment("Error","Problem while creating the MySharedFiles folder");
  				newFragment.show(mActivity.getFragmentManager(), AndroidNetInfActivity.MY_SHARED_FOLDER_PROBLEM);        		
  				Log.d(TAG, "Problem while creating the MySharedFiles folder");          	           		
  			}
  		}
  	});
  	
  	return builder.create();
  }

  private boolean createSharedFolder() {
  	File folder = new File(Environment.getExternalStorageDirectory() + "/MySharedFiles");
  	boolean success = false;
  	
  	if (!folder.exists()) {
  		success = folder.mkdir();
  		return success;
  	}
  	else { 
  		//Shared folder exists already
  		return success=true;
  	}
  }   

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
  	String path = "";
  	File myFile = null;

  	switch (requestCode) {
  		case PICK_FROM_FILE:
  			if (resultCode != RESULT_OK)
  				return;

  			mImageCaptureUri = data.getData();
  			path = getRealPathFromURI(mImageCaptureUri); //from Gallery

  			if (path == null)
  				path = mImageCaptureUri.getPath(); //from File Manager

  			myFile = new File(path);
  			
  			if (myFile.exists()) {
  				try {
  					byte[] myFileBytes = IOUtils.toByteArray(new FileInputStream(myFile));
  					createHTTPRequestFromByteArray("PUT",myFileBytes,path);
  				}
  				catch (FileNotFoundException e) {
  					// TODO Auto-generated catch block
  					e.printStackTrace();
  				}
  				catch (IOException e) {
  					// TODO Auto-generated catch block
  					e.printStackTrace();
  				}
  			}
  			
  			break;

  		case PICK_FROM_CAMERA:
  			if (resultCode != RESULT_OK)
  				return;

  			path    = mImageCaptureUri.getPath();

  			//without this the Gallery will not see the picture
  			String[] paths = { path };
  			String[] mediaType = {"image/jpeg"};
  			MediaScannerConnection.scanFile(this, paths, mediaType, null);

  			myFile = new File(path);
  			if (myFile.exists()) {
  				try {
  					byte[] myFileBytes = IOUtils.toByteArray(new FileInputStream(myFile));
  					createHTTPRequestFromByteArray("PUT",myFileBytes,path);
  				}
  				catch (FileNotFoundException e) {
  					// TODO Auto-generated catch block
  					e.printStackTrace();
  				}
  				catch (IOException e) {
  					// TODO Auto-generated catch block
  					e.printStackTrace();
  				}	
  			}

  			break;

  		case PICK_FOR_DELETE:
  			if (resultCode != RESULT_OK)
  				return;

  			mImageCaptureUri = data.getData();
  			path = getRealPathFromURI(mImageCaptureUri); //from Gallery

  			if (path == null)
  				path = mImageCaptureUri.getPath(); //from File Manager

  			myFile = new File(path);
  			
  			if (myFile.exists()) {
  				try {
  					byte[] myFileBytes = IOUtils.toByteArray(new FileInputStream(myFile));
  					createHTTPRequestFromByteArray("DELETE",myFileBytes,path);
  				}
  				catch (FileNotFoundException e) {
  					// TODO Auto-generated catch block
  					e.printStackTrace();
  				}
  				catch (IOException e) {
  					// TODO Auto-generated catch block
  					e.printStackTrace();
  				}				
  			} 
  			
  			break;

  		case IntentIntegrator.REQUEST_CODE:
  			if (resultCode != RESULT_OK)
  				return;
  			
  			if (resultCode != RESULT_CANCELED) { 
  				IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data); 
  			
  				if (scanResult != null) { 
  					String hash = scanResult.getContents();	
  					SendHttpGetRequestTask task = new SendHttpGetRequestTask(mActivity,HASH_ALGORITHM,TARGETHOST,TARGETPORT,transportTechnology,"Resolving the IO...");
  					task.execute(new String[] { "GET", hash, null });                	
  				}
  			}
  			
  			break;

  		case REQUEST_ENABLE_BT:
  			// When the request to enable Bluetooth returns
  			if (resultCode == Activity.RESULT_OK) {
  				// Bluetooth is now enabled, so start the NetInfNode
  				new NodeStarter(this).start();
  				break;
  			}
  			else {
  				// User did not enable Bluetooth or an error occured
  				Log.d(TAG, "BT not enabled");
  				Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
  				finish();
  			}
  			
  			break;
  	}	
  }


  public void createHTTPRequestFromByteArray(String requestType, byte[] fileArray, String filePath) {
  	HashingSHA256_EncodingBase64URL encondingContent = new HashingSHA256_EncodingBase64URL(fileArray);
  	String hash = encondingContent.encodeResult();
  	String message = null;

  	if (requestType.equals("PUT")) {
  		copyFileToMySharedFilesFolder(filePath,hash);
  		message = "Putting the IO...";
  	}

  	if (requestType.equals("DELETE")) {
  		deleteFileFromMySharedFilesFolder(filePath);
  		message = "Deleting the IO...";
  	}

  	if (requestType.equals("CACHE")) {
  		message = "Caching the IO...";
  	}

  	if (cacheContent.equals("false")) {
  		SendHttpGetRequestTask task = new SendHttpGetRequestTask(mActivity,HASH_ALGORITHM,TARGETHOST,TARGETPORT,transportTechnology,message);
  		task.execute(new String[] { requestType, hash, cacheContent });
  	}
  	
  	else{
  		DialogFragment newFragment = new MyProgressDialogFragment("Caching content in NCS...");
  		newFragment.show(mActivity.getFragmentManager(), AndroidNetInfActivity.TRANSFERRING);

  		if (D) 
  			Log.d(TAG, "The content will be cached in the NCS");
  		
  		new SendHttpPostRequestTask (mActivity, TARGETHOST, TARGETPORT,AndroidNetInfActivity.NCS,HASH_ALGORITHM,hash).execute();	
  	}

  	cacheContent = "false";

  }

  public String getRealPathFromURI(Uri contentUri) {
  	String [] proj = {MediaStore.Images.Media.DATA};
  	Cursor cursor  = managedQuery( contentUri, proj, null, null,null);

  	if (cursor == null) 
  		return null;

  	int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

  	cursor.moveToFirst();

  	return cursor.getString(column_index);
  }

  private String copyFileToMySharedFilesFolder(String path, String hash) {

  	InputStream in 	 = null;
  	OutputStream out = null;
  	String newPath   = "";

  	try {
  		in = new FileInputStream(path);
  		newPath = Environment.getExternalStorageDirectory() + "/MySharedFiles/" + hash;
  		out = new FileOutputStream(newPath);

  		// Copy the bits from instream to outstream
  		byte[] buf = new byte[1024];
  		int len;

  		while ((len = in.read(buf)) > 0) {
  			out.write(buf, 0, len);
  		}
  		
  		in.close();
  		out.close();

  		//without this the Gallery will not see the picture
  		String[] paths = { newPath };
  		String[] mediaType = {"image/jpeg"};
  		MediaScannerConnection.scanFile(this, paths, mediaType, null);

  	}
  	catch (FileNotFoundException e) {
  		// TODO Auto-generated catch block
  		e.printStackTrace();
  	}
  	catch (IOException e) {
  		// TODO Auto-generated catch block
  		e.printStackTrace();
  	}

  	return newPath;	
  }

  private boolean deleteFileFromMySharedFilesFolder(String path) {
  	boolean result = false;

  	File fileToDelete = new File(path);
  	result = fileToDelete.delete();

  	//without this the Gallery will not see that the picture has been deleted		
  	sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse
  						("file://" + Environment.getExternalStorageDirectory())));

  	return result;
  }

  private final Handler mHandler = new Handler() {
  	@Override
  	public void handleMessage(Message msg) {
  		switch (msg.what) {
  			case MESSAGE_NODE_STATUS:
  				nodeStarted = true;
  				break;
  		}
  	}
  };

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
  	MenuInflater inflater = getMenuInflater();
  	inflater.inflate(R.menu.option_menu, menu);
  	return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
  	switch (item.getItemId()) {
  		case R.id.discoverable:
  			// Ensure this device is discoverable by others
  			ensureDiscoverable();
  			return true;
  	}
  	return false;
  }

  private void ensureDiscoverable() {
  	if (D)
  		Log.d(TAG, "ensure discoverable");
  	
  	if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
  		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
  		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
  		startActivity(discoverableIntent);
  	}
  }

  private String calculateWifiDirectMac(String wifiMac) {
  	String wifiDirectMac=null;

  	Log.d(TAG, "Calculating the WiFi Direct Mac from Wifi Mac");
  	Log.d(TAG, "wifiMac = " + wifiMac);

  	String first  = wifiMac.substring(0, 1);
  	String second = wifiMac.substring(1, 2);
  	String rest   = wifiMac.substring(2);
  	
  	int value = Integer.parseInt(second, 16);
  	
  	// «»«»«» THIS MAGIC HERE «»«»«»
  	value++;
  	value++;
  	// «»«»«» THIS MAGIC HERE «»«»«»

  	second = Integer.toHexString(value);

  	wifiDirectMac = first + second + rest;
  	Log.d(TAG,"wifiDirectMac = " + wifiDirectMac );

  	return wifiDirectMac.toLowerCase();
  }

  class NodeStarter extends Thread {
  	private boolean running;
  	Context mContext;

  	public NodeStarter(Context context) {
  		running = true; 
  		this.setName("NodeStarter");
  		mContext=context;
  	}

  	@Override
  	public void run() {
  		netInfNode = injector.getInstance(NetInfNode.class);

  		startResolution();
  		startAPIAccess();
  		startN2NAccess();

  		Message msg = mHandler.obtainMessage(AndroidNetInfActivity.MESSAGE_NODE_STATUS);
  		mHandler.sendMessage(msg);
  	}

  	private void startResolution() {

  		ResolutionController resolutionController = netInfNode.getResolutionController();

  		if (resolutionController != null) {
  			// Plug in Resolution Services
  			ResolutionService[] resolutionServices = injector.getInstance(ResolutionService[].class);

  			if (resolutionServices.length == 0) {
  				if (D) 
  					Log.d(TAG, " NodeStarter - I have no active resolution services");
  			}

  			for (ResolutionService resolutionService : resolutionServices) {
  				//Open SQLite database if Local      	         
  				if (resolutionService.describe().contains("Local Resolution Service")) {
  					//The Resolution service is local, therefore we must open the SQLite database
  					try{
  						localDBHelper = ((AndroidLocalResolutionService)resolutionService).openDatabase(mContext);     	        	 
  					}
  					catch(SQLiteException ex) {
  						if (D)
  							Log.e(TAG, " NodeStarter - Error opening the SQLITE database - aborting");

  						finish();
  					}    	        	 
  				} 
  				if (resolutionService.describe().contains("Remote Name Resolution Server")) {
  					bluetoothMacAddress = mBluetoothAdapter.getAddress();
  					//
  					//if (bluetoothMacAddress != null) {
  					//  ((AndroidRemoteNameResolutionService)resolutionService).initializeBluetoothMac(bluetoothMacAddress);
  					//}
  					//
  					//if (wifiMacAddress != null) {	      	        		  
  					//  ((AndroidRemoteNameResolutionService)resolutionService).initializeWifiMac(wifiMacAddress);
  					//}
  				}

  				resolutionController.addResolutionService(resolutionService);
  				
  				if (D)
  					Log.d(TAG, " NodeStarter - Added resolution service '" + resolutionService.getClass().getCanonicalName() + "'");
  			}
  		}     	       	      
  	}

  	private void startAPIAccess() {
  		AccessServer accessServer = injector.getInstance(AccessServer.class);
  		accessServer.start();
  	}

  	private void startN2NAccess() {
  		mBluetoothBOserver = injector.getInstance(AndroidBluetoothBOServer.class);
  		mBluetoothBOserver.start();
  		//myTcpServer = injector.getInstance(TCPServer.class);
  		//try {
  		//  myTcpServer.start();
  		//}
  		//catch (NetInfCheckedException e) {
  		//// TODO Auto-generated catch block
  		//e.printStackTrace();
  		//}
  	}
  	
  	@Override
  	public void interrupt() {
  		running = false;
  		super.interrupt();
  	}

  	public boolean isRunning() {
  		return running;
  	}

  	public void cancel() {

  	}
  }    
}