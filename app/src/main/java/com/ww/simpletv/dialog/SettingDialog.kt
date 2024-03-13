package com.ww.simpletv.dialog

import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.tencent.mmkv.MMKV
import com.ww.simpletv.ChannelUtils
import com.ww.simpletv.Constant
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
    override fun initLayoutResource(): Int = R.layout.dialog_setting

    override fun initBindData() {
        super.initBindData()
        binding.autoBoot = MMKV.defaultMMKV().decodeBool(Constant.KEY_BOOT_STARTUP, false)
    }

    override fun onResume() {
        super.onResume()
        binding.swAutoBoot.setOnCheckedChangeListener { _, b ->
            MMKV.defaultMMKV().encode(Constant.KEY_BOOT_STARTUP, b)
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
    }
}