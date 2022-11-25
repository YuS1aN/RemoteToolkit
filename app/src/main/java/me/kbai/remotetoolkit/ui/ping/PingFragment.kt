package me.kbai.remotetoolkit.ui.ping

import android.content.res.Configuration
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.kbai.remotetoolkit.Constants
import me.kbai.remotetoolkit.R
import me.kbai.remotetoolkit.base.BaseFragment
import me.kbai.remotetoolkit.databinding.FragmentPingBinding
import java.io.*
import javax.inject.Inject

/**
 * @author sean 2022/11/25
 */
@AndroidEntryPoint
class PingFragment : BaseFragment<FragmentPingBinding>() {
    @Inject
    lateinit var gson: Gson

    private var mProcess: Process? = null
    private var mAdapter = PingRecordAdapter()
    private val mRecordFile = File(Constants.filesDir.absolutePath + File.separator + "PingRecords")

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentPingBinding.inflate(inflater)

    override fun initView() = viewBinding.run {
        tvOut.movementMethod = ScrollingMovementMethod.getInstance()

        rvRecord.adapter = mAdapter
        mAdapter.onItemClickListener = {
            etHost.setText(it)
        }
        mAdapter.onItemRemoveClickListener = { adapter, index ->
            adapter.records.removeAt(index)
            adapter.notifyItemRemoved(index)
            lifecycleScope.launch { writeRecordFile() }
        }
        btnSave.setOnClickListener {
            val host = etHost.text.toString()
            if (host.isBlank()) {
                tilHost.error(R.string.wol_host_error)
                return@setOnClickListener
            }
            it.isEnabled = false
            mAdapter.records.add(host)
            mAdapter.notifyItemInserted(mAdapter.records.size - 1)
            lifecycleScope.launch {
                writeRecordFile()
                it.isEnabled = true
            }
        }
        btnStart.setOnClickListener {
            if (processing()) {
                lifecycleScope.launch(Dispatchers.IO) { Runtime.getRuntime().exec("pkill -2 ping") }
            } else {
                ping(etHost.text.toString())
            }
        }
    }

    override fun initData() {
        lifecycleScope.launchWhenCreated { readRecords() }
    }

    private fun ping(host: String) = lifecycleScope.launch {
        // execute command
        viewBinding.tvOut.text = ""
        viewBinding.btnStart.isEnabled = false
        val process = withContext(Dispatchers.IO) {
            try {
                Runtime.getRuntime().exec("ping -c 20 $host")
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }
        viewBinding.btnStart.isEnabled = true
        if (process == null) {
            viewBinding.tvOut.setText(R.string.cmd_error)
            return@launch
        }
        // processing
        mProcess = process
        viewBinding.btnStart.setText(R.string.stop)
        readData(process.inputStream)
        readData(process.errorStream)
        withContext(Dispatchers.IO) {
            try {
                process.waitFor()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        // finished
        mProcess = null
        viewBinding.btnStart.setText(R.string.start)
    }

    private fun readData(inputStream: InputStream) = lifecycleScope.launch(Dispatchers.IO) {
        try {
            val reader = BufferedReader(InputStreamReader(inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                withContext(Dispatchers.Main) {
                    viewBinding.tvOut.run {
                        append("$line\n")
                        val h = lineCount * lineHeight
                        if (h > height) {
                            scrollTo(0, h - height)
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun processing() = mProcess != null

    private suspend fun readRecords() = withContext(Dispatchers.IO) {
        if (!mRecordFile.exists()) {
            return@withContext
        }
        val type = object : TypeToken<List<String>>() {}.type
        try {
            val list: MutableList<String> = gson.fromJson(mRecordFile.readText(), type)
            withContext(Dispatchers.Main) { mAdapter.records = list }
        } catch (e: JsonParseException) {
            e.printStackTrace()
        }
    }

    private suspend fun writeRecordFile() = withContext(Dispatchers.IO) {
        mRecordFile.delete()
        mRecordFile.writeText(gson.toJson(mAdapter.records))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mProcess?.destroy()
    }

    private fun TextInputLayout.error(@StringRes resId: Int) {
        error = getString(resId)
        isErrorEnabled = true
    }
}