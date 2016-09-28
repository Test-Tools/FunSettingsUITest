package com.example.zhengjin.funsettingsuitest.testuitasks;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;

import com.example.zhengjin.funsettingsuitest.testuiactions.DeviceActionEnter;
import com.example.zhengjin.funsettingsuitest.testuiactions.DeviceActionHome;
import com.example.zhengjin.funsettingsuitest.testuiactions.DeviceActionMoveRight;
import com.example.zhengjin.funsettingsuitest.testuiactions.DeviceActionMoveUp;
import com.example.zhengjin.funsettingsuitest.testuiactions.UiActionsManager;
import com.example.zhengjin.funsettingsuitest.testutils.TestHelper;
import com.example.zhengjin.funsettingsuitest.utils.StringUtils;

import junit.framework.Assert;

import java.util.List;

import static com.example.zhengjin.funsettingsuitest.testutils.TestConstants.LONG_WAIT;

/**
 * Created by zhengjin on 2016/6/1.
 *
 * Include the UI tasks on Launcher.
 */
public final class TaskLauncher {

//    private final static String TAG = TaskLauncher.class.getSimpleName();
    private static UiActionsManager ACTION = UiActionsManager.getInstance();

    public static BySelector getAllLauncherTabsSelector() {
        return By.res("com.bestv.ott:id/tab_title");
    }

    public static BySelector getLauncherTopBarSelector() {
        return By.res("com.bestv.ott:id/container");
    }

    public static BySelector getQuickAccessBtnSettingsSelector() {
        return By.res("com.bestv.ott:id/setting");
    }

    public static BySelector getQuickAccessBtnNetworkSelector() {
        return By.res("com.bestv.ott:id/network");
    }

    public static void backToLauncher(UiDevice device) {
        // method 1
//        String pkgName = getLauncherPackageName();
//        ACTION.doDeviceActionAndWait(new DeviceActionHome(), WAIT);
//        String pkgName = device.getCurrentPackageName();
//        Assert.assertTrue(message, launcherPackageName.equals(pkgName));

        // method 2
        String launcherPackageName = device.getLauncherPackageName();  //"com.bestv.ott"
        String message = "Error in backToLauncher(), fail to back to the launcher.";
        ACTION.doDeviceActionAndWait(new DeviceActionHome());
        Assert.assertTrue(message, TestHelper.waitForAppOpened(device, launcherPackageName));
    }

    public static String getLauncherPackageName() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        PackageManager pm = InstrumentationRegistry.getContext().getPackageManager();
        ResolveInfo resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);

        String pkgName = resolveInfo.activityInfo.packageName;
        String message =
                "Error in getLauncherPackageName(), the launcher package name is empty or null.";
        Assert.assertFalse(message, StringUtils.isEmpty(pkgName));

        return pkgName;
    }

    public static void navigateToVideoTab(UiDevice device) {
        String message;

        backToLauncher(device);
        ACTION.doDeviceActionAndWait(new DeviceActionMoveUp());

        String textVideoTab = ("视频");
        UiObject2 tabVideo = getSpecifiedTab(device, textVideoTab);
        message = "Error in navigateToVideoTab(), the UI object textVideoTab is NOT found.";
        Assert.assertNotNull(message, tabVideo);
        message = "Error in navigateToVideoTab(), the UI object textVideoTab is NOT focused.";
        Assert.assertTrue(message, tabVideo.isFocused());
    }

    public static void navigateToAppTab(UiDevice device) {
        String message;

        navigateToVideoTab(device);
        ACTION.doRepeatDeviceActionAndWait(new DeviceActionMoveRight(), 2);

        UiObject2 tabApp = getSpecifiedTab(device, "应用");
        message = "Error in navigateToAppTab(), the UI object textAppTab is NOT found.";
        Assert.assertNotNull(message, tabApp);
        message = "Error in navigateToAppTab(), the UI object textAppTab is NOT focused.";
        Assert.assertTrue(message, tabApp.isFocused());
    }

    private static UiObject2 getSpecifiedTab(UiDevice device, String tabName) {
        List<UiObject2> tabs = device.findObjects(getAllLauncherTabsSelector());
        if (tabs.size() == 0) {
            return null;
        }

        for (UiObject2 tab : tabs) {
            if (tabName.equals(tab.getText())) {
                return tab;
            }
        }

        return null;
    }

    public static void openSpecifiedAppFromAppTab(UiDevice device, String appName) {
        focusOnSpecifiedAppFromAppTab(device, appName);
        Assert.assertTrue(ACTION.doDeviceActionAndWait(new DeviceActionEnter(), LONG_WAIT));
    }

    private static void focusOnSpecifiedAppFromAppTab(UiDevice device, String appName) {
        String message;

        navigateToAppTab(device);

        UiObject2 appTest = device.findObject(By.text(appName));
        message = String.format(
                "Error in openSpecifiedAppFromAppTab(), the app %s is NOT found.", appName);
        Assert.assertNotNull(message, appTest);

        UiObject2 appContainer = appTest.getParent();
        message = String.format(
                "Error in openSpecifiedAppFromAppTab(), the app container %s is NOT found.", appName);
        Assert.assertNotNull(message, appContainer);

        ACTION.doClickActionAndWait(appContainer);
    }

    public static void clickOnButtonFromTopQuickAccessBar(UiDevice device, BySelector selector) {
        String message = "Error in clickOnButtonFromTopQuickAccessBar(), " +
                "the settings button from top bar is NOT found.";

        showLauncherTopBar(device);
        UiObject2 quickAccessBtn = device.findObject(selector);
        Assert.assertNotNull(message, quickAccessBtn);

        ACTION.doClickActionAndWait(quickAccessBtn);
        ACTION.doDeviceActionAndWait(new DeviceActionEnter(), LONG_WAIT);
    }

    private static void showLauncherTopBar(UiDevice device) {
        String message;

        backToLauncher(device);
        ACTION.doRepeatDeviceActionAndWait(new DeviceActionMoveUp(), 2);

        UiObject2 bar = device.findObject(getLauncherTopBarSelector());
        message = "Error in showLauncherTopBar(), the top bar on launcher is NOT found.";
        Assert.assertNotNull(message, bar);
        message = "Error in showLauncherTopBar(), the top bar is NOT enabled.";
        Assert.assertTrue(message, bar.isEnabled());
    }

}
