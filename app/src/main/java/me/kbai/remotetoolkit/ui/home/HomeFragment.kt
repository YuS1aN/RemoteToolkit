package me.kbai.remotetoolkit.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import me.kbai.remotetoolkit.base.BaseFragment
import me.kbai.remotetoolkit.databinding.FragmentHomeBinding

class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentHomeBinding.inflate(inflater)

    override fun initView() {
        viewBinding.tvHome.text = "HOME"
    }
}