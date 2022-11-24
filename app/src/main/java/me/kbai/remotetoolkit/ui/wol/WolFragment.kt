package me.kbai.remotetoolkit.ui.wol

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.kbai.remotetoolkit.R
import me.kbai.remotetoolkit.base.BaseFragment
import me.kbai.remotetoolkit.databinding.FragmentWolBinding
import me.kbai.remotetoolkit.ext.showToast
import me.kbai.remotetoolkit.model.WolRecord

@AndroidEntryPoint
class WolFragment : BaseFragment<FragmentWolBinding>() {
    private val mViewModel by viewModels<WolViewModel>()

    private val mRecordAdapter = WolRecordAdapter()

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentWolBinding.inflate(inflater)

    override fun initView() = viewBinding.run {
        mRecordAdapter.onItemClickListener = { item ->
            etAlias.setText(item.alias)
            etHost.setText(item.host)
            etMac.setText(item.mac)
            etPort.setText(item.port.toString())
        }
        mRecordAdapter.onItemRemoveClickListener = { adapter, index ->
            lifecycleScope.launch {
                mViewModel.deleteRecord(index)
                adapter.notifyItemRemoved(index)
            }
        }
        rvRecord.adapter = mRecordAdapter
        btnSave.setOnClickListener {
            val host = etHost.text.toString()
            val macAddress = etMac.text.toString()
            val portStr = etPort.text.toString()
            val port = if (portStr.isEmpty()) 9 else portStr.toIntOrNull()

            resetError()

            if (!checkParams(host, macAddress, port)) return@setOnClickListener

            it.isEnabled = false
            lifecycleScope.launch {
                val wolRecord = WolRecord(
                    etAlias.text.toString(), host, macAddress, port!!
                )
                mViewModel.saveRecord(wolRecord)
                mRecordAdapter.notifyItemInserted(mRecordAdapter.itemCount - 1)
            }
            it.isEnabled = true
        }
        btnSend.setOnClickListener { btn ->
            val host = etHost.text.toString()
            val macAddress = etMac.text.toString()
            val portStr = etPort.text.toString()
            val port = if (portStr.isEmpty()) 9 else portStr.toIntOrNull()

            resetError()

            if (!checkParams(host, macAddress, port)) return@setOnClickListener

            btn.isEnabled = false

            lifecycleScope.launch {
                mViewModel.sendWolMagicPacket(host, macAddress, port!!)
                    .collect {
                        when (it.code) {
                            WolViewModel.CODE_SUCCESS -> {
                                showToast(getString(R.string.wol_success, it.data))
                            }
                            WolViewModel.CODE_HOST_ERROR -> {
                                tilHost.error(R.string.wol_host_error)
                            }
                            WolViewModel.CODE_MAC_ERROR -> {
                                tilMac.error(R.string.wol_mac_error)
                            }
                            WolViewModel.CODE_SEND_ERROR -> {
                                showToast(R.string.wol_send_error)
                            }
                        }
                        btn.isEnabled = true
                    }
            }
        }
    }

    private fun checkParams(host: String, mac: String, port: Int?): Boolean = viewBinding.run {
        if (host.isBlank()) {
            tilHost.error(R.string.wol_host_error)
            return@run false
        }
        if (mac.isBlank()) {
            tilMac.error(R.string.wol_mac_error)
            return@run false
        }
        if (port == null || port < 0 || port >= 0xFFFF) {
            tilPort.error(R.string.wol_port_error)
            return@run false
        }
        return@run true
    }

    override fun initData() {
        mViewModel.wolRecords.observe(this) {
            mRecordAdapter.records = it
        }
    }

    private fun FragmentWolBinding.resetError() {
        tilMac.isErrorEnabled = false
        tilHost.isErrorEnabled = false
        tilPort.isErrorEnabled = false
    }

    private fun TextInputLayout.error(@StringRes resId: Int) {
        error = getString(resId)
        isErrorEnabled = true
    }
}