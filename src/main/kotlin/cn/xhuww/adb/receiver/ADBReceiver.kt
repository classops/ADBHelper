package cn.xhuww.adb.receiver

import cn.xhuww.adb.ex.runOnEDT
import com.android.ddmlib.IShellOutputReceiver
import java.nio.charset.StandardCharsets

open class ADBReceiver : IShellOutputReceiver {
    private val stringBuffer = StringBuilder()

    override fun addOutput(data: ByteArray, offset: Int, length: Int) {
        val s = String(data, offset, length, StandardCharsets.UTF_8)
        stringBuffer.append(s)
    }

    override fun flush() {
        // 这里IO线程，done 放到 EDT 线程执行
        val msg = stringBuffer.toString()
        runOnEDT {
            done(msg)
        }
    }

    override fun isCancelled(): Boolean = false

    open fun done(message: String) {}
}
