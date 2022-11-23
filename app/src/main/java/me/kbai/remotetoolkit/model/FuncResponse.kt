package me.kbai.remotetoolkit.model

/**
 * @author sean 2022/11/24
 */
data class FuncResponse<T>(
    val code: Int,
    val data: T,
    val e: Throwable?
)