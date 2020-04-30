package cn.xhuww.adb.view

import cn.xhuww.adb.TopActivityMessageDialog
import com.intellij.openapi.ui.DialogWrapper
import javax.swing.Action
import javax.swing.JComponent

class TopActivityMessageDialogWrapper(
        private val packageName: String,
        private val activityName: String
) : DialogWrapper(true) {
    init {
        init()
    }

    override fun createCenterPanel(): JComponent? {
        return TopActivityMessageDialog().apply {
            textPackageName.text = packageName
            textActivityName.text = activityName
        }.root
    }

    override fun createActions(): Array<Action> {
        val action = cancelAction.apply { putValue(DEFAULT_ACTION, true) }
        return arrayOf(action)
    }
}