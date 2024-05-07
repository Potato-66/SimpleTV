package com.ww.simpletv

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.tencent.mmkv.MMKV
import com.ww.simpletv.databinding.ActivityMainBinding
import com.ww.simpletv.dialog.SettingDialog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

/**
 *
 * Copyright (C), 2024 Potato-66, All rights reserved.
 * 创建时间: 2024/3/6
 * @since 1.0
 * @version 1.0
 * @author Potato-66
 */
class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        val binding =
            DataBindingUtil.setContentView(this, R.layout.activity_main) as ActivityMainBinding
        val isInit = MMKV.defaultMMKV().decodeBool(Constant.KEY_INIT, true)
        val handler = CoroutineExceptionHandler { _, throwable ->
            Log.e("ww", "onCreate: exception:${throwable.message}")
            if (isInit) {
                binding.tvMessage.text = getString(R.string.init_channel_list_fail_network_error)
            } else {
                Toast.makeText(this, R.string.update_channel_list_fail_network_error, Toast.LENGTH_LONG).show()
                startActivity(Intent(this@MainActivity, PlayerActivity::class.java))
                finish()
            }
            binding.progress.hide()
        }
        lifecycleScope.launch(handler) {
            if (isInit) {
                binding.tvMessage.setText(R.string.init_channel_list)
            }
            ChannelUtils.parseChannel(this@MainActivity)
            binding.progress.hide()
            if (ChannelUtils.channelSet.isEmpty()) {
                Log.e("ww", "onCreate: 没有频道列表,退出播放")
                binding.tvMessage.text = getString(R.string.load_channel_list_fail)
            } else {
                if (isInit) {
                    MMKV.defaultMMKV().encode(Constant.KEY_INIT, false)
                }
                Log.e("ww", "onCreate: channel size:${ChannelUtils.channelSet.size}")
                binding.tvMessage.visibility = View.GONE
                startActivity(Intent(this@MainActivity, PlayerActivity::class.java))
                finish()
            }
        }
        binding.main.setOnClickListener {
            SettingDialog().show(supportFragmentManager, Constant.DIALOG_TAG_SETTING)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            SettingDialog().show(supportFragmentManager, Constant.DIALOG_TAG_SETTING)
            return true
        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_BUTTON_B) {
            finish()
        }
        return super.onKeyDown(keyCode, event)
    }
}