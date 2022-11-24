package me.kbai.remotetoolkit.ui.ping

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.kbai.remotetoolkit.R
import me.kbai.remotetoolkit.base.BaseFragment
import me.kbai.remotetoolkit.databinding.FragmentPingBinding
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

/**
 * @author sean 2022/11/25
 */
class PingFragment : BaseFragment<FragmentPingBinding>() {
    private var mProcess: Process? = null

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentPingBinding.inflate(inflater)

    override fun initView() = viewBinding.run {
        tvOut.movementMethod = ScrollingMovementMethod.getInstance()
        btnStart.setOnClickListener {
            if (processing()) {
                mProcess?.destroy()
            } else {
                ping(etHost.text.toString())
            }
        }
    }

    private fun ping(host: String) = lifecycleScope.launch {
        viewBinding.tvOut.text = ""
        val process = withContext(Dispatchers.IO) {
            try {
                Runtime.getRuntime().exec("ping -c 20 $host")
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }
        if (process == null) {
            viewBinding.tvOut.setText(R.string.cmd_error)
            return@launch
        }
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

//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        outState.putString("tvOut", viewBinding.tvOut.text.toString())
//    }
//
//    override fun onViewStateRestored(savedInstanceState: Bundle?) {
//        super.onViewStateRestored(savedInstanceState)
//        viewBinding.tvOut.text = savedInstanceState?.getString("tvOut")
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        mProcess?.destroy()
    }
}