package cn.xhuww.adb;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class DeviceChooserUtils {

    private static final String ANDROID_TARGET_DEVICES_PROPERTY = "AndroidTargetDevices";

    /**
     * Returns a compatible, already-running device for launch.
     * If no compatible devices are running, returns an empty list.
     * If exactly one compatible device is running, returns it.
     * If multiple compatible devices are running, prompt the user to select devices.
     * If they select any, return them, but if they do not, return null.
     */
    @Nullable
    public static Collection<IDevice> chooseRunningDevice(@NotNull final AndroidFacet facet,
                                                          @Nullable final Predicate<IDevice> deviceFilter) {
        final Collection<IDevice> compatibleDevices = getAllCompatibleDevices(deviceFilter);

        if (compatibleDevices.isEmpty()) {
            return ImmutableList.of();
        }
        else if (compatibleDevices.size() == 1) {
            return compatibleDevices;
        }
        else {
            // Ask the user to pick one (or more) of the matching devices.
            final AtomicReference<IDevice[]> devicesRef = new AtomicReference<IDevice[]>();
            ApplicationManager.getApplication().invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    devicesRef.set(chooseDevicesManually(facet, deviceFilter));
                }
            });
            // Return the selected devices, or null if the user cancelled.
            return devicesRef.get().length > 0 ? ImmutableList.copyOf(devicesRef.get()) : null;
        }
    }

    @NotNull
    private static IDevice[] chooseDevicesManually(@NotNull AndroidFacet facet,
                                                   @Nullable Predicate<IDevice> filter) {
        final Project project = facet.getModule().getProject();
        String value = PropertiesComponent.getInstance(project).getValue(ANDROID_TARGET_DEVICES_PROPERTY);
        String[] selectedSerials = value != null ? deserialize(value) : null;
//        AndroidPlatform platform = AndroidPlatform.getInstance(facet.getModule());
//        if (platform == null) {
////            LOG.error("Android platform not set for module: " + facet.getModule().getName());
//            return DeviceChooser.EMPTY_DEVICE_ARRAY;
//        }
        DeviceChooserDialog chooser =
                new DeviceChooserDialog(facet, false, selectedSerials, filter);
        chooser.show();
        IDevice[] devices = chooser.getSelectedDevices();
        if (chooser.getExitCode() != DialogWrapper.OK_EXIT_CODE || devices.length == 0) {
            return DeviceChooser.EMPTY_DEVICE_ARRAY;
        }
        PropertiesComponent.getInstance(project).setValue(ANDROID_TARGET_DEVICES_PROPERTY, serialize(devices));
        return devices;
    }

    @NotNull
    public static List<IDevice> getAllCompatibleDevices(@Nullable Predicate<IDevice> deviceFilter) {
        final List<IDevice> compatibleDevices = new ArrayList<IDevice>();
        final AndroidDebugBridge bridge = AndroidDebugBridge.getBridge();

        if (bridge != null) {
            IDevice[] devices = bridge.getDevices();

            for (IDevice device : devices) {
                if (deviceFilter == null || deviceFilter.apply(device)) {
                    compatibleDevices.add(device);
                }
            }
        }
        return compatibleDevices;
    }

    @NotNull
    public static String serialize(@NotNull IDevice[] devices) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0, n = devices.length; i < n; i++) {
            builder.append(devices[i].getSerialNumber());
            if (i < n - 1) {
                builder.append(' ');
            }
        }
        return builder.toString();
    }

    @NotNull
    private static String[] deserialize(@NotNull String s) {
        return s.split(" ");
    }

}
