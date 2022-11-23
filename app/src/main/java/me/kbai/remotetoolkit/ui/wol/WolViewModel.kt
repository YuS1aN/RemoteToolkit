package me.kbai.remotetoolkit.ui.wol

import android.net.MacAddress
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import me.kbai.remotetoolkit.model.FuncResponse
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class WolViewModel : ViewModel() {
    companion object {
        const val CODE_SUCCESS = 0
        const val CODE_MAC_ERROR = 1
        const val CODE_HOST_ERROR = 2
        const val CODE_SEND_ERROR = 3
    }

    fun sendWolMagicPacket(host: String, mac: String, port: Int) = flow<FuncResponse<String?>> {
        val macAddress = try {
            MacAddress.fromString(mac)
        } catch (e: IllegalArgumentException) {
            emit(FuncResponse(CODE_MAC_ERROR, null, e))
            return@flow
        }
        val inetAddress = try {
            withContext(Dispatchers.IO) {
                InetAddress.getByName(host)
            }
        } catch (e: IOException) {
            emit(FuncResponse(CODE_HOST_ERROR, null, e))
            return@flow
        }
        val ff = 0xFF.toUByte().toByte()
        val macByteArray = macAddress.toByteArray()
        val data = ByteArray(102) {
            if (it < 6) ff else macByteArray[it % 6]
        }
        val packet = DatagramPacket(data, data.size, inetAddress, port)
        try {
            withContext(Dispatchers.IO) {
                DatagramSocket().send(packet)
            }
            emit(FuncResponse(CODE_SUCCESS, inetAddress.hostAddress, null))
        } catch (e: IOException) {
            emit(FuncResponse(CODE_SEND_ERROR, null, e))
        }
    }

    private fun String.decodeHex() = chunked(2)
        .map { it.toUByte(16).toByte() }
        .toByteArray()
}