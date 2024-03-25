package com.ww.simpletv.dialog

import com.ww.simpletv.R
import com.ww.simpletv.TV
import com.ww.simpletv.adapter.ChannelAdapter
import com.ww.simpletv.adapter.GroupAdapter
import com.ww.simpletv.databinding.DialogChannelBinding

/**
 *
 * Copyright (C), 2024 Potato-66, All rights reserved.
 * 创建时间: 2024/2/27
 * @since 1.0
 * @version 1.0
 * @author Potato-66
 */
class ChannelListDialog(private val tvs: Set<TV>?, private val tv: TV?) : BaseDialogFragment<DialogChannelBinding>() {
    var onChoose: ((tv: TV) -> Unit)? = null

    override fun initLayoutResource(): Int = R.layout.dialog_channel

    override fun onResume() {
        super.onResume()
        tvs?.let { tvs ->
            val map = tvs.groupBy { it.group.uppercase() }
            val groups = arrayListOf<String>()
            groups.addAll(map.keys)
            binding.lvGroup.adapter = context?.let { GroupAdapter(it, groups) }
            var index = groups.indexOf(tv?.group?.uppercase())
            if (index == -1) {
                index = 0
            }
            binding.lvGroup.setSelection(index)
            val channelAdapter = context?.let { ChannelAdapter(it) }
            binding.lvChannel.adapter = channelAdapter
            map[groups[index]]?.let {
                channelAdapter?.setChannelList(it)
                binding.lvChannel.setSelection(it.indexOf(tv))
            }
            binding.lvGroup.setOnItemClickListener { _, _, i, _ ->
                map[groups[i]]?.let {
                    channelAdapter?.setChannelList(it)
                    binding.lvChannel.setSelection(0)
                }
            }
            binding.lvChannel.setOnItemClickListener { _, _, i, _ ->
                channelAdapter?.getChannelList()?.let {
                    onChoose?.invoke(it[i])
                }
                dismiss()
            }
        }
    }
}