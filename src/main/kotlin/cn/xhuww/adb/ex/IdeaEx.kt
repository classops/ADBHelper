package cn.xhuww.adb.ex

import com.intellij.openapi.application.ApplicationManager

fun runOnEDT(runnable: Runnable) {
    ApplicationManager.getApplication().invokeLater(runnable)
}