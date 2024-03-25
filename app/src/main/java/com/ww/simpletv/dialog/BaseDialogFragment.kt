package com.ww.simpletv.dialog

import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import com.ww.simpletv.Constant

/**
 *
 * Copyright (C), 2024 Potato-66, All rights reserved.
 * 创建时间: 2024/2/27
 * @since 1.0
 * @version 1.0
 * @author Potato-66
 */
abstract class BaseDialogFragment<T : ViewDataBinding> : DialogFragment() {
    lateinit var binding: T

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, initLayoutResource(), container, false)
        initBindData()
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        isCancelable = true
        dialog?.let {
            val window = it.window
            val params = window?.attributes
            params?.run {
                if (tag == Constant.DIALOG_TAG_UPDATE) {
                    width = (resources.displayMetrics.widthPixels * 0.5).toInt()
                    height = WindowManager.LayoutParams.WRAP_CONTENT
                    gravity = Gravity.CENTER
                } else {
                    width = (resources.displayMetrics.widthPixels * 0.4).toInt()
                    height = resources.displayMetrics.heightPixels
                    gravity = Gravity.START
                }
                dimAmount = 0f
                alpha = 0.9f
                window.attributes = params
            }
            it.setOnKeyListener { _, _, keyEvent ->
                if (keyEvent.keyCode == KeyEvent.KEYCODE_BACK && tag != Constant.DIALOG_TAG_UPDATE) {
                    dismiss()
                    true
                } else {
                    false
                }
            }
        }
    }

    abstract fun initLayoutResource(): Int

    open fun initBindData() {}

    override fun onDestroy() {
        super.onDestroy()
        binding.unbind()
    }
}