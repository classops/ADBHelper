package cn.xhuww.adb.receiver

import cn.xhuww.adb.ADB_MESSAGE_TITLE
import com.intellij.openapi.ui.Messages

class MessageReceiver : ADBReceiver() {
    override fun done(message: String) {
        Messages.showDialog(
            message,
            ADB_MESSAGE_TITLE,
            arrayOf(Messages.getCancelButton()),
            0,
            Messages.getInformationIcon()
        )
    }
}