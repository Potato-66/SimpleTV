package com.ww.simpletv

import java.io.Serializable

/**
 *
 * Copyright (C), 2024 Potato-66, All rights reserved.
 * 创建时间: 2024/2/27
 * @since 1.0
 * @version 1.0
 * @author Potato-66
 */
data class TV(val id: String = "", val name: String = "", val group: String = "", val logo: String = "", var url: String = "") : Comparable<TV>,
    Serializable {

    override fun compareTo(other: TV): Int {
        return other.group.last() - this.group.last()
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + group.hashCode()
        result = 31 * result + logo.hashCode()
        result = 31 * result + url.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TV

        if (id != other.id) return false
        if (name != other.name) return false
        if (group != other.group) return false
        if (logo != other.logo) return false
        return url == other.url
    }
}