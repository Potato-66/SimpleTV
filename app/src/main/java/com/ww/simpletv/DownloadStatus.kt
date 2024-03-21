package com.ww.simpletv

/**
 *
 * Copyright (C), 2024 Potato-66, All rights reserved.
 * 创建时间: 2024/3/20
 * @since 1.0
 * @version 1.0
 * @author Potato-66
 */
sealed class DownloadStatus {
    data class DownLoading(val progress: Int) : DownloadStatus()
    data class Fail(val code: Int) : DownloadStatus()
    data class Error(val error: Throwable) : DownloadStatus()
    data object Success : DownloadStatus()
}