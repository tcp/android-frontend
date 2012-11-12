package project.cs.lisa.bluetooth.test;

import java.io.IOException;

import junit.framework.Assert;
import project.cs.lisa.bluetooth.BluetoothServer;
import android.bluetooth.BluetoothAdapter;
import android.test.AndroidTestCase;

/**
 * Tests the Bluetooth Server. Note that a Bluetooth Adapter needs
 * to be existing for the test to work.
 * 
 * @author Kim-Anh Tran
 *
 */
public class BluetoothServerTest extends AndroidTestCase {

	/** The Bluetooth Adapter. */
	BluetoothAdapter mBtAdapter;
	
	/** Bluetooth Server instance to test. */
	BluetoothServer mBtServer;
	
	/** Initializes the Bluetooth Server and the Bluetooth Adapter. */
	public BluetoothServerTest() {
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();	
		mBtAdapter.enable();
		while (!mBtAdapter.isEnabled());
		
		try {
			mBtServer = new BluetoothServer();
		} catch (Exception e) {
			Assert.fail("Creating a Bluetooth Server should not have raised an exception.");
		}
	}
	
	/** 
	 * Starts the Bluetooth Server and tests its reactions
	 * due to different Bluetooth Adapter configurations.
	 */
	public void testRun() {		
		// Creates a Bluetooth Server with a disabled Bluetooth Adapter.
		try {
			mBtAdapter.disable();
			while (mBtAdapter.isEnabled());
			
			mBtServer = new BluetoothServer();
			Assert.fail("Should have raised an IO exception.");
		} catch (IOException e) {
			// Success: Should fail because of an inactive Bluetooth Adapter.
		}
		
		// Creates a Bluetooth Server. This time the Bluetooth Adapter is enabled.
		try {
			mBtAdapter.enable();
			while (!mBtAdapter.isEnabled());
			
			mBtServer = new BluetoothServer();
		} catch (IOException e) {
			Assert.fail("Should not have raised an exception.");
		}
		
		mBtServer.start();
	}
	
	/**
	 * Tries to cancel the Bluetooth Server.
	 */
	public void testCancel() {
		mBtServer.cancel();
		assertFalse(mBtServer.isAlive());
	}

}
