package project.cs.lisa.networksettings;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Class for handling general BT connection
 * TODO: Work in progress for Sprint 3, maybe? 
 * @author tcp
 */

public class LisaBTHandler {
    private final String TAG = "LisaBTHandler";
    private BluetoothAdapter mBluetoothAdapter;
    
    public boolean isEnabled() {
        if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON)
            return true;
        return false;
    }
    
    public boolean discoverBTDevices(Context context) {
        if (!isEnabled()) {
            Log.d(TAG, "BT is disabled!");
            return false;
        }
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
        context.startActivity(discoverableIntent);
        return true;
    }
}
