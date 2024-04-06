package xyz.edward_p.hamlib

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import java.io.IOException
import java.util.UUID


/**
 * This service connects the BluetoothSocket I/O
 * and pseudo terminal master
 */
class SPPService() : Service() {

    val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private lateinit var device: BluetoothDevice
    private lateinit var socket: BluetoothSocket

    private lateinit var sppThread: Thread

    private lateinit var ptmThread: Thread

    override fun onBind(intent: Intent): IBinder? {
        return null;
    }

    private fun startForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            val chan = NotificationChannel(
                "all",
                "channelName",
                NotificationManager.IMPORTANCE_MIN
            )
            chan.description = "all"
            manager.createNotificationChannel(chan)

            val notificationBuilder = NotificationCompat.Builder(this, chan.id)
            val notification = notificationBuilder.setOngoing(true)
                .setContentTitle("SPPService")
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
        Log.d("SPPService", "onCreate")
    }

    private fun connectSPPDevice(intent: Intent?) {

        val address = intent?.extras?.getString("address")
        Log.d("SPPService", "address: ${address}")
        if (address.isNullOrBlank()) {
            return
        }

        val bluetoothManager = getSystemService(BluetoothManager::class.java)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("SPPService", "no permission")
            return
        }

        device =
            bluetoothManager.adapter.bondedDevices.find { d -> d.address.equals(address) } ?: return

        Log.d("SPPService", "Connecting to: ${device.name} ${device.address}")

        try {
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
        } catch (ex: IOException) {
            Log.e("SPPService", "Failed to create RfComm socket: " + ex.toString())
            return
        }

        for (i in 1..5) {
            try {
                socket.connect()
                break
            } catch (ex: IOException) {
                Log.e("SPPService", "Failed to connect. Retrying: " + ex.toString())
                continue

            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        connectSPPDevice(intent)
        if (socket.isConnected) {
            sppThread = Thread {
                val inputStream = socket.inputStream
                val outputStream = Pty.getInstance().getOutputStream()
                inputStream.use {
                    val buf = ByteArray(512)
                    while (!Thread.interrupted()) {
                        val len = inputStream.read(buf)
                        if (len > 0) {
                            outputStream.write(buf, 0, len)
                            outputStream.flush()
                        }
                    }
                }
            }

            ptmThread = Thread {
                val inputStream = Pty.getInstance().getInputStream()
                val outputStream = socket.outputStream
                inputStream.use {
                    val buf = ByteArray(512)
                    while (!Thread.interrupted()) {
                        val len = inputStream.read(buf)
                        if (len > 0) {
                            outputStream.write(buf, 0, len)
                            outputStream.flush()
                        }
                    }
                }
            }

            sppThread.start()
            ptmThread.start()
        }


        return START_STICKY
    }

    override fun onDestroy() {
        sppThread.interrupt()
        ptmThread.interrupt()
    }
}