package com.example.mindmoving

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.Handler
import android.os.Message
import android.util.Log
import com.neurosky.thinkgear.TGDevice

class CustomNeuroSky(
    private val bluetoothAdapter: BluetoothAdapter,
    private val handler: Handler
) {
    private var tgDevice: TGDevice? = null

    fun connectTo(device: BluetoothDevice) {
        Log.d("MindWave", "Conectando al dispositivo: ${device.name} - ${device.address}")

        tgDevice = TGDevice(bluetoothAdapter, handler)

        // Esto evita conflictos por conexiones previas
        if (tgDevice?.getState() != TGDevice.STATE_IDLE) {
            Log.d("MindWave", "TGDevice no estaba IDLE, reiniciando...")
            tgDevice?.close()
        }

        tgDevice?.connect(device, false) // 'false' para rawEnabled
    }

    fun disconnect() {
        Log.d("MindWave", "Desconectando MindWave")
        tgDevice?.close()
    }
}
