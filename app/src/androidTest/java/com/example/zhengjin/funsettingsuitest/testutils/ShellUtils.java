package com.example.zhengjin.funsettingsuitest.testutils;

import android.os.Environment;
import android.os.SystemClock;
import android.support.test.uiautomator.UiDevice;
import android.util.Log;

import com.example.zhengjin.funsettingsuitest.utils.StringUtils;

import junit.framework.Assert;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.example.zhengjin.funsettingsuitest.testutils.TestConstants.CAPTURES_PATH;

/**
 * Created by zhengjin on 2016/5/31.
 *
 * Include the utils for shell ENV.
 */
public final class ShellUtils {

    private final static String TAG = ShellUtils.class.getSimpleName();

    private static final String COMMAND_SU = "su";
    private static final String COMMAND_SH = "sh";
    private static final String COMMAND_EXIT = "exit\n";
    private static final String COMMAND_LINE_END = "\n";

    public static CommandResult execCommand(
            String command, boolean isRoot, boolean isNeedResultMsg) {
        return execCommand(new String[] {command}, isRoot, isNeedResultMsg);
    }

    public static CommandResult execCommand(
            List<String> commands, boolean isRoot, boolean isNeedResultMsg) {
        return execCommand((commands == null ? null : commands.toArray(new String[] {})),
                isRoot, isNeedResultMsg);
    }

    public static CommandResult execCommand(
            String[] commands, boolean isRoot, boolean isNeedResultMsg) {
        int result = -1;
        if (commands == null || commands.length == 0 ) {
            return new CommandResult(result, null, null);
        }

        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;
        DataOutputStream os = null;

        try {
            process = Runtime.getRuntime().exec(isRoot ? COMMAND_SU : COMMAND_SH);
            os = new DataOutputStream(process.getOutputStream());
            for (String command : commands) {
                if (StringUtils.isEmpty(command)) {
                    continue;
                }
                os.write(command.getBytes());
                os.writeBytes(COMMAND_LINE_END);
                os.flush();
            }
            os.writeBytes(COMMAND_EXIT);
            os.flush();

            result = process.waitFor();
            if (isNeedResultMsg) {
                successMsg = new StringBuilder();
                errorMsg = new StringBuilder();
                successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
                errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String tmpStr;
                while ((tmpStr = successResult.readLine()) != null) {
                    successMsg.append(tmpStr);
                }
                while ((tmpStr = errorResult.readLine()) != null) {
                    errorMsg.append(tmpStr);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }

            if (process != null) {
                process.destroy();
            }
        }

        return new CommandResult(result, (successMsg != null ? successMsg.toString() : null),
                (errorMsg != null ? errorMsg.toString() : null));
    }

    public static class CommandResult {
        public int mResult;
        public String mSuccessMsg;
        public String mErrorMsg;

        CommandResult(int results, String successMsg, String errorMsg) {
            mResult = results;
            mSuccessMsg = successMsg;
            mErrorMsg = errorMsg;
        }
    }

    public static void stopProcess(String pkgName) {
        String cmdStopProcess = String.format("am force-stop %s", pkgName);
        ShellUtils.CommandResult result = ShellUtils.execCommand(cmdStopProcess, false, false);
        Assert.assertTrue("Force stop the app process.", (result.mResult == 0));
    }

    public static void stopAndClearPackage(String pkgName) {
        String cmdStopProcess = String.format("pm clear %s", pkgName);
        ShellUtils.CommandResult result = ShellUtils.execCommand(cmdStopProcess, false, false);
        Assert.assertTrue("Clear the app package.", (result.mResult == 0));
    }

    public static void startSpecifiedActivity(String pkgName, String actName) {
        String cmdStart = String.format("am start %s/%s", pkgName, actName);
        ShellUtils.CommandResult result = ShellUtils.execCommand(cmdStart, false, false);
        Assert.assertEquals("Start the specified activity.", 0, result.mResult);
    }

    public static void systemWaitByMillis(long ms) {
        SystemClock.sleep(ms);
    }

    public static String getCurrentDate() {
        SimpleDateFormat formatter =
                new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date curDate = new Date(System.currentTimeMillis());
        return formatter.format(curDate);
    }

    public static String getCurrentDateTime() {
        SimpleDateFormat formatter =
                new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss-SSS", Locale.getDefault());
        Date curTime = new Date(System.currentTimeMillis());
        return formatter.format(curTime);
    }

    public static void takeScreenCapture(UiDevice device) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File testDirPath = new File(CAPTURES_PATH);
            if (!testDirPath.exists()) {
                Assert.assertTrue(String.format(
                        "Error, make directory(%s) for screenshot failed.", CAPTURES_PATH),
                        testDirPath.mkdirs());
            }
        } else {
            Assert.assertTrue("Error, the sdcard is NOT mount.", false);
        }

        final String suffix = ".png";
        String filePath = String.format(
                "%s/capture_%s%s", CAPTURES_PATH, ShellUtils.getCurrentDateTime(), suffix);
        Assert.assertTrue("Take screenshot.", device.takeScreenshot(new File(filePath)));
    }

}
