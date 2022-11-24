package me.kbai.remotetoolkit.ui.wol

import android.net.MacAddress
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import me.kbai.remotetoolkit.Constants
import me.kbai.remotetoolkit.model.FuncResponse
import me.kbai.remotetoolkit.model.WolRecord
import java.io.File
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import javax.inject.Inject

@HiltViewModel
class WolViewModel @Inject constructor(
    private val gson: Gson
) : ViewModel() {
    companion object {
        const val CODE_SUCCESS = 0
        const val CODE_MAC_ERROR = 1
        const val CODE_HOST_ERROR = 2
        const val CODE_SEND_ERROR = 3
    }

    private val mRecordFile = File(Constants.filesDir.absolutePath + File.separator + "WolRecords")
    private val _wolRecords = MutableLiveData<MutableList<WolRecord>>()
    val wolRecords: LiveData<MutableList<WolRecord>> = _wolRecords

    init {
        _wolRecords.value = ArrayList()
        readRecords()
    }

    private fun readRecords() {
        if (!mRecordFile.exists()) {
            return
        }
        val type = object : TypeToken<List<WolRecord>>() {}.type
        try {
            val map: MutableList<WolRecord> = gson.fromJson(mRecordFile.readText(), type)
            _wolRecords.value!!.run {
                clear()
                addAll(map)
            }
        } catch (e: JsonParseException) {
            e.printStackTrace()
        }
    }

    private suspend fun writeRecordFile() = withContext(Dispatchers.IO) {
        mRecordFile.delete()
        mRecordFile.writeText(gson.toJson(_wolRecords.value))
    }

    suspend fun saveRecord(wolRecord: WolRecord) {
        _wolRecords.value!!.add(wolRecord)
        writeRecordFile()
    }

    suspend fun deleteRecord(index: Int) {
        val list = _wolRecords.value!!
        list.removeAt(index)
        writeRecordFile()
    }

    fun sendWolMagicPacket(host: String, mac: String, port: Int) = flow<FuncResponse<String?>> {
        val inetAddress = try {
            withContext(Dispatchers.IO) {
                InetAddress.getByName(host)
            }
        } catch (e: IOException) {
            emit(FuncResponse(CODE_HOST_ERROR, null, e))
            return@flow
        }
        val macByteArray = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                MacAddress.fromString(mac).toByteArray()
            } else {
                val pattern = "[a-z]{2}:[a-z]{2}:[a-z]{2}:[a-z]{2}:[a-z]{2}:[a-z]{2}".toPattern()
                val macLower = mac.lowercase()
                if (pattern.matcher(macLower).find()) {
                    macLower.replace(":", "").decodeHex()
                } else {
                    throw IllegalArgumentException()
                }
            }
        } catch (e: IllegalArgumentException) {
            emit(FuncResponse(CODE_MAC_ERROR, null, e))
            return@flow
        }
        val ff = 0xFF.toUByte().toByte()
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