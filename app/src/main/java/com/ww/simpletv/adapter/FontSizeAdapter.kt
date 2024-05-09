package com.ww.simpletv.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.RadioButton
import com.ww.simpletv.Constant
import com.ww.simpletv.R

/**
 *
 * Copyright (C), 2024 Potato-66, All rights reserved.
 * 创建时间: 2024/2/27
 * @since 1.0
 * @version 1.0
 * @author Potato-66
 */
class FontSizeAdapter(private val context: Context, private val groups: List<String>) : BaseAdapter() {
    override fun getCount(): Int = groups.size

    override fun getItem(p0: Int): String = groups[p0]

    override fun getItemId(p0: Int): Long = p0.toLong()

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val holder: GroupHolder
        var view: View? = p1
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_font_size, p2, false)
            holder = GroupHolder(view)
        } else {
            holder = view.tag as GroupHolder
        }
        holder.radioButton.text = groups[p0]
        holder.radioButton.textSize = when (p0) {
            0 -> 15 * Constant.FONT_SIZE_NORMAL
            1 -> 15 * Constant.FONT_SIZE_LARGE
            else -> 15 * Constant.FONT_SIZE_HUGE
        }
        return view!!
    }

    inner class GroupHolder(view: View) {
        val radioButton: RadioButton = view.findViewById(R.id.rb_item)

        init {
            view.tag = this
        }
    }
}