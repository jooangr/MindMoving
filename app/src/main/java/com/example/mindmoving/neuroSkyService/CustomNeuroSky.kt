package com.example.mindmoving.neuroSkyService

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.Handler
import android.util.Log
import com.neurosky.thinkgear.TGDevice

/**
 * Clase que encapsula la lógica de conexión y control del dispositivo NeuroSky MindWave.
 * Se encarga de:
 * - Conectar al dispositivo por Bluetooth.
 * - Iniciar la transmisión de datos.
 * - Desconectar y liberar recursos correctamente.
 */
class CustomNeuroSky(
    private val bluetoothAdapter: BluetoothAdapter, // Adaptador Bluetooth del dispositivo Android
    private val handler: Handler                    // Handler para recibir los mensajes de la diadema (atención, meditación, etc.)
) {
    private var tgDevice: TGDevice? = null // Objeto de la SDK de NeuroSky para gestionar la conexión

    /**
     * Conecta al dispositivo MindWave especificado.
     * Si hay una conexión previa, se cierra primero. Luego se crea una nueva instancia de TGDevice.
     */
    fun connectTo(device: BluetoothDevice) {
        Log.d("MindWave", "🔌 Conectando a ${device.name} (${device.address})")

        // Cierra cualquier conexión previa por seguridad
        tgDevice?.close()

        // Crea una nueva instancia del dispositivo con el adaptador y el handler
        tgDevice = TGDevice(bluetoothAdapter, handler)

        // Verifica que la instancia no sea nula
        tgDevice?.let { deviceInstance ->
            when (deviceInstance.state) {
                TGDevice.STATE_IDLE -> {
                    // Si está en estado IDLE, se puede conectar directamente
                    deviceInstance.connect(device, false)
                }
                else -> {
                    // Si no está IDLE, se reinicia la conexión
                    Log.w("MindWave", "⚠️ TGDevice no está en estado IDLE. Reiniciando...")
                    deviceInstance.close()
                    tgDevice = TGDevice(bluetoothAdapter, handler)
                    tgDevice?.connect(device, false)
                }
            }
        } ?: Log.e("MindWave", "❌ No se pudo crear la instancia de TGDevice")
    }

    /**
     * Inicia la transmisión de datos desde la diadema.
     * Solo se activa si el dispositivo ya está conectado.
     */
    fun start() {
        tgDevice?.let {
            if (it.getState() == TGDevice.STATE_CONNECTED) {
                it.start()
            } else {
                Log.w("MindWave", "⚠️ TGDevice no está conectado. No se puede iniciar transmisión.")
            }
        }
    }

    /**
     * Desconecta el dispositivo y libera los recursos.
     * También borra la instancia para evitar fugas de memoria.
     */
    fun disconnect() {
        Log.d("MindWave", "🔌 Desconectando MindWave")
        tgDevice?.close()
        tgDevice = null
    }
}
