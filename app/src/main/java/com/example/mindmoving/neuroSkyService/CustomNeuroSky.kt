package com.example.mindmoving.neuroSkyService

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.Handler
import android.util.Log
import com.neurosky.thinkgear.TGDevice

class CustomNeuroSky(
    private val bluetoothAdapter: BluetoothAdapter,
    private val handler: Handler // Handler para recibir los mensajes del dispositivo (atención, estado, etc.)
) {
    private var tgDevice: TGDevice? = null // Objeto de la SDK de NeuroSky para gestionar la conexión

    // Método para conectar al dispositivo MindWave
    fun connectTo(device: BluetoothDevice) {
        Log.d("MindWave", "🔌 Conectando a ${device.name} (${device.address})")

        tgDevice?.close() // Cierra cualquier conexión previa por seguridad
        tgDevice = TGDevice(bluetoothAdapter, handler) // Crea una nueva instancia del dispositivo

        if (tgDevice?.getState() == TGDevice.STATE_IDLE) {
            tgDevice?.connect(device, false) // Si está inactivo, inicia la conexión
        } else {
            // Si no está inactivo, reinicia la instancia para evitar errores
            Log.w("MindWave", "⚠️ TGDevice no está en estado IDLE, reiniciando...")
            tgDevice?.close()
            tgDevice = TGDevice(bluetoothAdapter, handler)
            tgDevice?.connect(device, false)
        }
    }

    // Método para comenzar la transmisión de datos desde la diadema
    fun start() {
        tgDevice?.start()
    }

    // Método para desconectar el dispositivo y liberar recursos
    fun disconnect() {
        Log.d("MindWave", "🔌 Desconectando MindWave")
        tgDevice?.close()
    }
}