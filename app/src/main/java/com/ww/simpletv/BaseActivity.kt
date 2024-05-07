package com.ww.simpletv

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.tencent.mmkv.MMKV

open class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context?) {
        AppUtils.setFontScale(newBase, MMKV.defaultMMKV().decodeFloat(Constant.KEY_FONT_SIZE, Constant.FONT_SIZE_NORMAL))
        super.attachBaseContext(newBase)
    }
}