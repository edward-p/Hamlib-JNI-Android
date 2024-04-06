package xyz.edward_p.hamlib

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import xyz.edward_p.hamlib.ui.theme.HamlibTheme
import java.util.UUID

class BluetoothActivity : ComponentActivity() {
    val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    override fun onCreate(savedInstanceState: Bundle?) {

        val bluetoothManager = getSystemService(BluetoothManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                var isGranted = false;
                val requestPermissionLauncher = registerForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { result ->
                    isGranted = result
                }
                requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)

                if (isGranted) this.finish()
            }
        }

        val devices = bluetoothManager.adapter.bondedDevices.filter { d ->
            d.uuids!=null && d.uuids.contains(ParcelUuid(SPP_UUID))
        }

        super.onCreate(savedInstanceState)
        setContent {
            HamlibTheme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        val it = devices.iterator()
                        items(
                            count = devices.size,
                        ) { index ->

                            val device = it.next();
                            Column(
                                modifier = Modifier
                                    .border(
                                        width = 2.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .fillMaxWidth()
                                    .padding(20.dp)
                                    .clickable {
                                        val intent = Intent(
                                            this@BluetoothActivity,
                                            SPPService::class.java
                                        )
                                        intent.putExtra("address", device.address)
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            startForegroundService(
                                                intent
                                            )
                                        } else {
                                            startService(
                                                intent
                                            )
                                        }
                                        this@BluetoothActivity.finish()
                                    }
                            ) {

                                Text(
                                    fontSize = 24.sp, text = device.name
                                )
                                Text(
                                    fontSize = 18.sp, text = device.address
                                )

                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }

                    }

                }
            }
        }
    }


}


@Preview(showBackground = true)
@Composable
fun ListPreview() {

    HamlibTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Add 5 items
                items(99) { index ->
                    Column(
                        modifier = Modifier
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            fontSize = 20.sp, text = "Item: $index"
                        )
                        Text(
                            fontSize = 14.sp, text = "Item: $index"
                        )

                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

            }

        }
    }
}