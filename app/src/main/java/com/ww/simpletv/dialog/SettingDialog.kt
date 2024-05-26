package com.ww.simpletv.dialog

import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.tencent.mmkv.MMKV
import com.ww.simpletv.AppUtils
import com.ww.simpletv.ChannelUtils
import com.ww.simpletv.Constant
import com.ww.simpletv.FontSizeActivity
import com.ww.simpletv.R
import com.ww.simpletv.databinding.DialogSettingBinding
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 *
 * Copyright (C), 2024 Potato-66, All rights reserved.
 * 创建时间: 2024/2/28
 * @since 1.0
 * @version 1.0
 * @author Potato-66
 */
class SettingDialog : BaseDialogFragment<DialogSettingBinding>() {
    private var fontScale =  MMKV.defaultMMKV().decodeFloat(Constant.KEY_FONT_SIZE, Constant.FONT_SIZE_NORMAL)

    override fun initLayoutResource(): Int = R.layout.dialog_setting

    override fun initBindData() {
        super.initBindData()
        binding.autoBoot = MMKV.defaultMMKV().decodeBool(Constant.KEY_BOOT_STARTUP, false)
        binding.autoUpdate = MMKV.defaultMMKV().decodeBool(Constant.KEY_AUTO_UPDATE, true)
    }

    override fun onResume() {
        super.onResume()
        binding.swAutoBoot.setOnCheckedChangeListener { _, b ->
            MMKV.defaultMMKV().encode(Constant.KEY_BOOT_STARTUP, b)
        }
        binding.swAutoUpdate.setOnCheckedChangeListener { _, b ->
            MMKV.defaultMMKV().encode(Constant.KEY_AUTO_UPDATE, b)
        }
        binding.btnManualUpdate.setOnClickListener {
            context?.let { context ->
                isCancelable = false
                binding.btnManualUpdate.text = getString(R.string.updating)
                binding.btnManualUpdate.isEnabled = false
                lifecycleScope.launch(CoroutineExceptionHandler { _, throwable ->
                    binding.btnManualUpdate.text = getString(R.string.manual_update)
                    binding.btnManualUpdate.isEnabled = true
                    isCancelable = true
                    Toast.makeText(
                        context,
                        "${R.string.update_file}：${throwable.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }) {
                    val b = withContext(Dispatchers.IO) {
                        ChannelUtils.downloadIPTVFile(File(context.filesDir, Constant.FILE_NAME))
                        ChannelUtils.updateChannel(context)
                    }
                    binding.btnManualUpdate.text = getString(R.string.manual_update)
                    binding.btnManualUpdate.isEnabled = true
                    isCancelable = true
                    Toast.makeText(
                        context,
                        if (b) R.string.update_success else R.string.update_file,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        context?.let {
            binding.tvVersion.text = getString(R.string.version_info, AppUtils.getAppVersionName(it))
        }
        binding.btnUpdateVersion.setOnClickListener {
            activity?.let { context ->
                binding.btnUpdateVersion.text = getString(R.string.updating)
                binding.btnUpdateVersion.isEnabled = false
                lifecycleScope.launch {
                    val versionInfo = withContext(Dispatchers.IO) {
                        AppUtils.getVersion()
                    }
                    binding.btnUpdateVersion.text = getString(R.string.update_version)
                    binding.btnUpdateVersion.isEnabled = true
                    versionInfo?.let {
                        if (it.versionCode > AppUtils.getAppVersionCode(context)) {
                            UpdateDialog(it).show(context.supportFragmentManager, Constant.DIALOG_TAG_UPDATE)
                        } else {
                            Toast.makeText(context, getString(R.string.latest_version), Toast.LENGTH_LONG).show()
                        }
                    } ?: let {
                        Toast.makeText(context, getString(R.string.get_version_error_hint), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        val fontScale = MMKV.defaultMMKV().decodeFloat(Constant.KEY_FONT_SIZE, Constant.FONT_SIZE_NORMAL)
        if (this.fontScale != fontScale) {
            this.fontScale = fontScale
            activity?.run {
                AppUtils.recreateActivity(this)
                startActivity(Intent(this,FontSizeActivity::class.java))
            }
        }
        binding.tvFontSize.text =
            when (fontScale) {
                Constant.FONT_SIZE_NORMAL -> getString(R.string.font_size_normal)
                Constant.FONT_SIZE_LARGE -> getString(R.string.font_size_large)
                else -> getString(R.string.font_size_huge)
            }
        binding.rlFontSize.setOnClickListener {
            context?.run {
                startActivity(Intent(this, FontSizeActivity::class.java))
            }
        }
    }
}