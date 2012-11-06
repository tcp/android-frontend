package project.cs.lisa.bluetooth.test;

import project.cs.lisa.bluetooth.BluetoothServer;
import android.test.AndroidTestCase;

/**
 * Tests the Bluetooth Server.
 * 
 * @author Kim-Anh Tran
 *
 */
public class BluetoothServerTest extends AndroidTestCase {

	/** Bluetooth Server instance to test. */
	BluetoothServer mBtServer;
	
	/** Initializes a Bluetooth Server to test. */
	public BluetoothServerTest() {
		super();
		
		mBtServer = new BluetoothServer();
	}
	
	/**
	 * Tries to cancel the Bluetooth Server.
	 */
	public void testCancel() {
		mBtServer.cancel();
		
		assertFalse(mBtServer.isAlive());
	}
}
