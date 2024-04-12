package xyz.edward_p.hamlib.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import xyz.edward_p.hamlib.HamlibJNI
import java.io.IOException
import java.util.UUID


/**
 * This service connects the BluetoothSocket I/O
 * and pseudo terminal master
 */
class HamlibSPPService() : Service() {

    val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    override fun onBind(intent: Intent): IBinder? {
        return null;
    }

    private fun startForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            val chan = NotificationChannel(
                "all",
                "hamlib",
                NotificationManager.IMPORTANCE_MIN
            )
            chan.description = "all"
            manager.createNotificationChannel(chan)

            val notificationBuilder = NotificationCompat.Builder(this, chan.id)
            val notification = notificationBuilder.setOngoing(true)
                .setContentTitle("HamlibSPPService")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build()

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(1, notification)
            } else {
                startForeground(
                    1, notification,
                    FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                )
            }
        }
    }

    override fun onCreate() {
        startForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        val bluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter = bluetoothManager.adapter

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return START_NOT_STICKY
        }

        bluetoothAdapter.cancelDiscovery()

        val address = intent?.extras?.getString("address")
        Log.d("HamlibSPPService", "address: ${address}")
        if (address.isNullOrBlank()) {
            return START_NOT_STICKY
        }


        val device = bluetoothAdapter.bondedDevices.find { d -> d.address.equals(address) }
            ?: return START_NOT_STICKY

        Log.d("HamlibSPPService", "Connecting to: ${device.name} ${device.address}")

        val socket = try {
            device.createRfcommSocketToServiceRecord(SPP_UUID)
        } catch (ex: IOException) {
            Log.e("HamlibSPPService", "Failed to create RfComm socket: " + ex.toString())
            return START_NOT_STICKY
        }

        for (i in 1..5) {
            try {
                socket.connect()
                break
            } catch (ex: IOException) {
                Log.e("HamlibSPPService", "Failed to connect. Retrying: " + i, ex)
                continue
            }
        }


        if (socket.isConnected) {;

            Thread {
                val inputStream = HamlibJNI.instance.getInputStream()
                val outputStream = socket.outputStream
                try {
                    val buf = ByteArray(512)
                    while (!Thread.interrupted()) {
                        val len = inputStream.read(buf)
                        if (len == -1) return@Thread;
                        outputStream.write(buf, 0, len)
                        outputStream.flush()
                    }
                } catch (ex: Exception) {
                    // just exit
                    Log.e("ptmThread", "ioexception", ex);
                } finally {
                    outputStream.close()
                    inputStream.close()
                }
            }.start()

            Thread {
                val inputStream = socket.inputStream
                val outputStream = HamlibJNI.instance.getOutputStream()
                try {
                    val buf = ByteArray(512)
                    while (!Thread.interrupted()) {
                        val len = inputStream.read(buf)
                        if (len == -1) return@Thread;

                        outputStream.write(buf, 0, len)
                        outputStream.flush()
                    }

                } catch (ex: IOException) {
                    // Just exit
                    Log.e("sppThread", "ioexception", ex);
                } finally {
                    outputStream.close()
                    inputStream.close()
                    socket.close()
                }
            }.start()

        }

        return START_NOT_STICKY
    }

}