package me.kbai.remotetoolkit.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import me.kbai.remotetoolkit.R
import me.kbai.remotetoolkit.base.BaseActivity
import me.kbai.remotetoolkit.databinding.ActivityMainBinding
import me.kbai.remotetoolkit.ext.findNavControllerByManager

/**
 * @author sean 2022/11/22
 */
@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private lateinit var mBinding: ActivityMainBinding
    private val mViewModel by viewModels<MainViewModel>()

    private lateinit var mAppBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        initView()
    }

    private fun initView() {
        setSupportActionBar(mBinding.icBar.toolbar)

        val navController = findNavControllerByManager(R.id.fragment_container)

        mAppBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_wol
            ), mBinding.drawerLayout
        )
        setupActionBarWithNavController(navController, mAppBarConfiguration)
        mBinding.navView.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp() =
        findNavControllerByManager(R.id.fragment_container).navigateUp(mAppBarConfiguration) || super.onSupportNavigateUp()
}