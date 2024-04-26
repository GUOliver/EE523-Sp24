package com.example.mybluetooth

import BluetoothFragment
import CameraFragment
import HomeFragment
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.UUID

class MainActivity : AppCompatActivity() {

    // Lazy initialization of BluetoothAdapter
    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    // ArrayAdapter to manage the display of Bluetooth devices
    private lateinit var arrayAdapter: ArrayAdapter<String>
    // Define a request code for handling permission requests
    private val locationPermissionRequestCode = 101
    private val bluetoothScanPermissionRequestCode = 102
    // UUID for Serial Port Profile
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView: BottomNavigationView = findViewById(R.id.navigation)
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.navigation_camera -> {
                    // Handle camera action
                    replaceFragment(CameraFragment())
                    true
                }
                R.id.navigation_bluetooth -> {
                    replaceFragment(BluetoothFragment())
                    // Handle Bluetooth action
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            navView.selectedItemId = R.id.navigation_home // set home as default selected item
        }

        // Setup ListView and ArrayAdapter
        val listView: ListView = findViewById(R.id.listViewScannedDevices)
        arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        listView.adapter = arrayAdapter

        // Set up a button listener to initiate Bluetooth discovery
        findViewById<Button>(R.id.button_Discover).setOnClickListener {
            checkPermissions()
        }

        // Register BroadcastReceiver to listen for Bluetooth discovery results
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Extract the BluetoothDevice from the Intent
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        if (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            arrayAdapter.add("${it.name ?: "Unknown"} | ${it.address}")
                        }
                    }
                }
            }
        }
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN
        )

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, permissions, locationPermissionRequestCode)
        } else if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_SCAN), bluetoothScanPermissionRequestCode)
        } else {
            startDiscovery()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            locationPermissionRequestCode -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkBluetoothScanPermission()
                } else {
                    Toast.makeText(this, "Location permission denied. Cannot discover devices.", Toast.LENGTH_SHORT).show()
                }
            }
            bluetoothScanPermissionRequestCode -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startDiscovery()
                } else {
                    Toast.makeText(this, "Bluetooth scan permission denied. Cannot discover devices.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkBluetoothScanPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_SCAN), bluetoothScanPermissionRequestCode)
        } else {
            startDiscovery()
        }
    }

    private fun startDiscovery() {
        bluetoothAdapter?.startDiscovery()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the broadcast receiver to avoid memory leaks
        unregisterReceiver(receiver)
    }
}
