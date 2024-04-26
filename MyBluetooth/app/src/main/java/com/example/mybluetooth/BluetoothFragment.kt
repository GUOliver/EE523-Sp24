import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bluetooth, container, false)

        listView = view.findViewById(R.id.listViewScannedDevices)
        arrayAdapter = ArrayAdapter(context!! , android.R.layout.simple_list_item_1)
        listView.adapter = arrayAdapter

        view.findViewById<Button>(R.id.button_Discover).setOnClickListener {
            if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
            } else {
                discoverDevices()
            }
        }

        return view
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
    }
}
