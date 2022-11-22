package me.kbai.remotetoolkit.ui.wol

import android.view.LayoutInflater
import android.view.ViewGroup
import me.kbai.remotetoolkit.base.BaseFragment
import me.kbai.remotetoolkit.databinding.FragmentHomeBinding
import me.kbai.remotetoolkit.databinding.FragmentWolBinding

class WolFragment : BaseFragment<FragmentWolBinding>() {

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentWolBinding.inflate(inflater)

    override fun initView() {

    }
}