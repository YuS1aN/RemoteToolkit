package me.kbai.remotetoolkit.model

/**
 * @author sean 2022/11/24
 */
data class WolRecord(
    val alias: String,
    val host: String,
    val mac: String,
    val port: Int
)
