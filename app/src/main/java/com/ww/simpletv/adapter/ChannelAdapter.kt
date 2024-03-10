package com.ww.simpletv.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ww.simpletv.R
import com.ww.simpletv.TV

/**
 *
 * Copyright (C), 2024 Potato-66, All rights reserved.
 * 创建时间: 2024/2/27
 * @since 1.0
 * @version 1.0
 * @author Potato-66
 */
class ChannelAdapter(private val context: Context) : BaseAdapter() {
    private val tvs = arrayListOf<TV>()

    fun setChannelList(tvs: List<TV>) {
        if (tvs.isEmpty()) {
            return
        }
        this.tvs.clear()
        this.tvs.addAll(tvs)
        notifyDataSetChanged()
    }

    fun getChannelList(): List<TV> = tvs

    override fun getCount(): Int = tvs.size

    override fun getItem(p0: Int): TV = tvs[p0]

    override fun getItemId(p0: Int): Long = p0.toLong()

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val holder: GroupHolder
        var view: View? = p1
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_channel, p2, false)
            holder = GroupHolder(view)
        } else {
            holder = view.tag as GroupHolder
        }
        holder.textView.text = tvs[p0].name
        holder.tvNumber.text = "${p0 +1}."
        Glide.with(context).load(tvs[p0].logo).apply(RequestOptions().placeholder(R.drawable.place_icon))
            .into(holder.icon)
        return view!!
    }

    inner class GroupHolder(view: View) {
        val icon: ImageView = view.findViewById(R.id.iv_icon)
        val textView: TextView = view.findViewById(R.id.tv_channel)
        val tvNumber: TextView = view.findViewById(R.id.tv_num)

        init {
            view.tag = this
        }
    }
}