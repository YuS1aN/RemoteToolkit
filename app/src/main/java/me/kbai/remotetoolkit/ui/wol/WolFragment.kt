package me.kbai.remotetoolkit.ui.wol

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.kbai.remotetoolkit.R
import me.kbai.remotetoolkit.base.BaseFragment
import me.kbai.remotetoolkit.databinding.FragmentWolBinding
import me.kbai.remotetoolkit.ext.showToast

class WolFragment : BaseFragment<FragmentWolBinding>() {
    private val mViewModel by viewModels<WolViewModel>()

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentWolBinding.inflate(inflater)

    override fun initView() = viewBinding.run {
        btnSend.setOnClickListener { btn ->
            val host = etIp.text.toString()
            val macAddress = etMac.text.toString()
            val portStr = etPort.text.toString()
            val port = if (portStr.isEmpty()) 9 else portStr.toIntOrNull()

            tilMac.isErrorEnabled = false
            tilIp.isErrorEnabled = false
            tilPort.isErrorEnabled = false

            if (port == null || port < 0 || port >= 0xFFFF) {
                tilPort.error(R.string.wol_port_error)
                return@setOnClickListener
            }

            btn.isEnabled = false

            lifecycleScope.launch {
                mViewModel.sendWolMagicPacket(host, macAddress, port)
                    .collect {
                        when (it.code) {
                            WolViewModel.CODE_SUCCESS -> {
                                showToast(getString(R.string.wol_success, it.data))
                            }
                            WolViewModel.CODE_HOST_ERROR -> {
                                tilIp.error(R.string.wol_host_error)
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

    private fun TextInputLayout.error(@IdRes resId: Int) {
        error = getString(resId)
        isErrorEnabled = true
    }


}