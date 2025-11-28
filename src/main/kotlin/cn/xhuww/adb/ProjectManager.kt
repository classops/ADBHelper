package cn.xhuww.adb

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import com.android.tools.idea.gradle.project.model.AndroidModuleModel
import com.android.tools.idea.run.DeviceCount
import com.android.tools.idea.run.DeviceSelectionUtils
import com.android.tools.idea.run.TargetDeviceFilter.UsbDeviceFilter
import com.android.tools.idea.util.androidFacet
import com.intellij.facet.ProjectFacetManager
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import org.jetbrains.android.facet.AndroidFacet
import org.jetbrains.android.sdk.AndroidSdkUtils

class ProjectManager(private val project: Project) {

    private val logger = thisLogger()

    private val androidBridge: AndroidDebugBridge? by lazy {
        AndroidSdkUtils.getDebugBridge(project)
    }

    private val connectedDevices: List<IDevice> by lazy {
        androidBridge?.devices?.asList() ?: emptyList()
    }

    private fun getAndroidFacet(): AndroidFacet? {
        val facets = ProjectFacetManager.getInstance(project).getFacets(AndroidFacet.ID)

        return getFacet(facets)

//        return try {
//            // 检查项目是否已关闭
//            if (project.isDisposed) {
//                return null
//            }
//
//            ModuleManager.getInstance(project)
//                .modules
//                .filter { !it.isDisposed } // 过滤已释放的模块
//                .mapNotNull { module ->
//                    try {
//                        AndroidFacet.getInstance(module)
//                    } catch (e: IllegalStateException) {
//                        e.printStackTrace()
//                        // Facet 不可用，跳过
//                        null
//                    }
//                }
//                .firstOrNull { facet ->
//                    try {
//                        // 安全地访问 configuration，如果不可用则返回 false
//                        facet.configuration.isAppProject
//                    } catch (e: IllegalStateException) {
//                        e.printStackTrace()
//                        // Facet configuration 不可用，跳过这个 Facet
//                        false
//                    }
//                }
//        } catch (e: Exception) {
//            // 捕获所有异常，避免崩溃
//            null
//        }
    }

    private fun getFacet(facets: List<AndroidFacet>): AndroidFacet? {
        val appFacets = facets
            .mapNotNull { it.module.androidFacet }

        logger.info("facets: ${appFacets}")

        return  appFacets[0]
//
//        return if (appFacets.size > 1) {
//            ModuleChooserDialogHelper.showDialogForFacets(project, appFacets)
//        } else {
//            appFacets[0]
//        }
    }

    private fun getConnectedDevice(): IDevice? {
        return if (connectedDevices.size > 1) {
            getAndroidFacet()?.let {
                DeviceSelectionUtils.chooseRunningDevice(it, UsbDeviceFilter(), DeviceCount.SINGLE)?.firstOrNull()
            }
        } else {
            connectedDevices.firstOrNull()
        }
    }

    fun getProjectRunData(): ProjectRunData? {
        val facet = getAndroidFacet()
        val device = getConnectedDevice()
        if (facet == null) {
            showErrorDialog("Project was not compiled successfully")
            return null
        }
        if (device == null) {
            showErrorDialog("No device connected")
            return null
        }
        return ProjectRunData(project, facet, device)
    }
}

data class ProjectRunData(
    val project: Project,
    val facet: AndroidFacet,
    val device: IDevice
) {
    var packageName: String? = null
        get() = AndroidModuleModel.get(facet)?.applicationId
        private set
}
