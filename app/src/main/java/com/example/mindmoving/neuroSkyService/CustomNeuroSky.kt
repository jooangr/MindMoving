package com.example.mindmoving.neuroSkyService

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.Handler
import android.util.Log
import com.neurosky.thinkgear.TGDevice

class CustomNeuroSky(
    private val bluetoothAdapter: BluetoothAdapter,
    private val handler: Handler
) {
    private var tgDevice: TGDevice? = null

    fun connectTo(device: BluetoothDevice) {
        Log.d("MindWave", "🔌 Conectando a ${device.name} (${device.address})")

        tgDevice?.close() // Limpieza previa
        tgDevice = TGDevice(bluetoothAdapter, handler)

        if (tgDevice?.getState() == TGDevice.STATE_IDLE) {
            tgDevice?.connect(device, false)
        } else {
            Log.w("MindWave", "⚠️ TGDevice no está en estado IDLE, reiniciando...")
            tgDevice?.close()
            tgDevice = TGDevice(bluetoothAdapter, handler)
            tgDevice?.connect(device, false)
        }
    }

    fun start() {
        tgDevice?.start()
    }

    fun disconnect() {
        Log.d("MindWave", "🔌 Desconectando MindWave")
        tgDevice?.close()
    }
}
