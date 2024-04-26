import android.Manifest
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.mybluetooth.R

class BluetoothFragment : Fragment() {

    private var bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private lateinit var listView: ListView
    private lateinit var arrayAdapter: ArrayAdapter<String>
    private var bluetoothGatt: BluetoothGatt? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        arrayAdapter.add("${it.name ?: "Unknown"} | ${it.address}")
                    }
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_bluetooth, container, false)

        listView = view.findViewById(R.id.listViewScannedDevices)
        arrayAdapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_1)
        listView.adapter = arrayAdapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val info = arrayAdapter.getItem(position)
            if (info != null) {
                val address = info.split("|").getOrNull(1)?.trim()  // Safely access the second part, if available
                if (address != null) {
                    val device = bluetoothAdapter?.getRemoteDevice(address)
                    device?.let { connectToDevice(it) }
                } else {
                    Toast.makeText(context, "Device address not available", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Selected item not available", Toast.LENGTH_SHORT).show()
            }
        }

        view.findViewById<Button>(R.id.button_Discover).setOnClickListener {
            if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
            } else {
                discoverDevices()
            }
        }

        return view
    }

    private fun connectToDevice(device: BluetoothDevice) {
        Toast.makeText(context, "Connecting to ${device.name}...", Toast.LENGTH_SHORT).show()
        bluetoothGatt = device.connectGatt(context, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    activity?.runOnUiThread {
                        Toast.makeText(context, "Connected successfully to ${device.name}", Toast.LENGTH_SHORT).show()
                    }
                    gatt.discoverServices()
                }
            }
        })
    }

    private fun discoverDevices() {
        IntentFilter(BluetoothDevice.ACTION_FOUND).also {
            requireActivity().registerReceiver(receiver, it)
        }
        bluetoothAdapter?.startDiscovery()
        Toast.makeText(context, "Discovery started...", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().unregisterReceiver(receiver)
        bluetoothGatt?.close()
    }
}
