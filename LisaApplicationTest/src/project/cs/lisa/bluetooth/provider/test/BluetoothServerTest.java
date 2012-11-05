package project.cs.lisa.bluetooth.provider.test;

import project.cs.lisa.bluetooth.BluetoothServer;
import android.test.AndroidTestCase;

/**
 * Tests the Bluetooth Server functionality.
 * 
 * @author Kim-Anh Tran
 *
 */
public class BluetoothServerTest extends AndroidTestCase {
	
	/** The Bluetooth Server instance to test. */
	private BluetoothServer mBtServer;
	
	/** Sets up a new Bluetooth Server. */
	public BluetoothServerTest() {
		super();
		
		mBtServer = new BluetoothServer();
	}
	
	/** Tries to cancel the Bluetooth Server and checks the results. */
	public void testCancel() {
		mBtServer.cancel();
		
		assertFalse(mBtServer.isAlive());
	}
}
