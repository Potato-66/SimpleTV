package com.ww.simpletv

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.tencent.mmkv.MMKV

/**
 *
 * Copyright (C), 2024 Potato-66, All rights reserved.
 * 创建时间: 2024/2/28
 * @since 1.0
 * @version 1.0
 * @author Potato-66
 */
class TVBootReceive : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.e("ww", "onReceive: 开机")
        intent?.run {
            if (action == Intent.ACTION_BOOT_COMPLETED) {
                if (MMKV.defaultMMKV().decodeBool(Constant.KEY_BOOT_STARTUP, false)) {
                    context?.run {
                        startActivity(Intent(this, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                    }
                }
            }
        }
    }
}