package com.example.juicekaaa.fireserver.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class LogUtil {
	// The name for
	private static final String GLOBAL_TAG = "POS_LOG.";

	public static final int VERBOSE = Log.VERBOSE;
	public static final int DEBUG = Log.DEBUG;
	public static final int INFO = Log.INFO;
	public static final int WARN = Log.WARN;
	public static final int ERROR = Log.ERROR;
	public static final int ASSERT = Log.ASSERT;
	public static final int PRIORITY_LOWEST = 0;
	public static final int PRIORITY_HIGHEST = ASSERT + 1;
	// TODO 设置此处的Log级别，可屏蔽输出
	public static final int LOGCAT_FILTER_PRIORITY_ADB = PRIORITY_LOWEST;
	public static final int LOGCAT_FILTER_PRIORITY_SAVE_FILE = PRIORITY_HIGHEST;

	public static final boolean IS_ADB_VERBOSE_OPEN = VERBOSE >= LOGCAT_FILTER_PRIORITY_ADB;
	public static final boolean IS_ADB_DEBUG_OPEN = DEBUG >= LOGCAT_FILTER_PRIORITY_ADB;
	public static final boolean IS_ADB_INFO_OPEN = INFO >= LOGCAT_FILTER_PRIORITY_ADB;
	public static final boolean IS_ADB_WARN_OPEN = WARN >= LOGCAT_FILTER_PRIORITY_ADB;
	public static final boolean IS_ADB_ERROR_OPEN = ERROR >= LOGCAT_FILTER_PRIORITY_ADB;
	public static final boolean IS_ADB_ASSERT_OPEN = ASSERT >= LOGCAT_FILTER_PRIORITY_ADB;
	public static final boolean IS_MORE_LOG = false;

	private static final char[] PRIORITY_NAMES = "LLVDIWEA".toCharArray();

	private static final int TRACE_PRIORITY = INFO;
	private static final int TRACE_STACK_POSITION = 4;
	private static final String TRACE_TAG = "TRACE";
	private static final String ERROR_TAG = "FAILED";

	private static final String LOG_FOLDER = "bluetooth_log";
	private static FileOutputStream sLogWriter;
	private static final Object FILE_LOCK = new Object();

	private static final SimpleDateFormat FORMATER = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
	private static final int METHOD_NAME_LENGTH = getTraceLog("", "", "")
			.length();
	private static final int FORMAT_FILE_LOG_LENGTH = formatFileLog(DEBUG, "",
			"").length();

	private LogUtil() {
	}

	public static final void close() {
		closeFileLog();
	}

	private static final void closeFileLog() {
		synchronized (FILE_LOCK) {
			if (sLogWriter == null) {
				return;
			}
			try {
				sLogWriter.close();
				Log.i(GLOBAL_TAG, "log file is closed.");
			} catch (IOException e) {
				Log.e(GLOBAL_TAG, "log file close error.", e);
			} finally {
				sLogWriter = null;
			}
		}
	}

	public static final int println(int priority, String tag, String msg) {
		if (tag == null) {
			tag = "";
		}
		if (msg == null) {
			msg = "";
		}

		int result = 0;
		if (priority >= LOGCAT_FILTER_PRIORITY_ADB) {
			result = Log.println(priority, GLOBAL_TAG + tag, msg);
		}

		if (priority >= LOGCAT_FILTER_PRIORITY_SAVE_FILE) {
			saveToFile(priority, tag, msg);
		}
		return result;
	}

	private static final void saveToFile(int priority, String tag, String msg) {
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return;
		}
		try {
			if (sLogWriter == null) {
				createLogFile();
			}

			String log = formatFileLog(priority, tag, msg);
			byte[] array = log.getBytes();
			sLogWriter.write(array);
			sLogWriter.flush();
		} catch (Throwable t) {
			Log.w(GLOBAL_TAG, "LogUtils.saveToFile error.", t);
			closeFileLog();
		}
	}

	private static final void createLogFile() {
		synchronized (FILE_LOCK) {
			try {
				File sdcard = Environment.getExternalStorageDirectory();
				File fileLog = new File(sdcard, LOG_FOLDER);
				fileLog.mkdirs();
				File file = new File(fileLog, getFileName());
				Log.d(GLOBAL_TAG + "CREAT_LOG_FILE", "log file path: "
						+ getFileName());
				sLogWriter = new FileOutputStream(file, true);
			} catch (IOException e) {
				throw new RuntimeException("create log file error.", e);
			}
		}
	}

	private static String getFileName() {
		StringBuilder fileName = new StringBuilder();
		SimpleDateFormat mFormater = new SimpleDateFormat(
				"yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
		Date date = Calendar.getInstance().getTime();
		fileName.append(mFormater.format(date));
		fileName.append(".txt");
		return fileName.toString();
	}

	private static final String formatFileLog(int priority, String tag,
											  String msg) {
		int length = tag.length() + msg.length() + FORMAT_FILE_LOG_LENGTH;
		StringBuilder builder = new StringBuilder(length);

		builder.append("[");
		builder.append(getTime());
		builder.append("][");
		builder.append(PRIORITY_NAMES[priority]);
		builder.append("][");
		builder.append(tag);
		builder.append("]: ");
		builder.append(msg);
		builder.append("\n");

		return builder.toString();
	}

	private static final String getTime() {
		Calendar calendar = Calendar.getInstance();
		return FORMATER.format(calendar.getTime());
	}

	public static final int v(String tag, String msg) {
		return println(VERBOSE, tag, msg);
	}

	public static final int d(String tag, String msg) {
		return println(DEBUG, tag, msg);
	}

	public static final int i(String tag, String msg) {
		return println(INFO, tag, msg);
	}

	public static final int w(String tag, String msg) {
		return println(WARN, tag, msg);
	}

	public static final int w(Throwable tr) {
		return w(ERROR_TAG, Log.getStackTraceString(tr));
	}

	public static final int w(String tag, String msg, Throwable tr) {
		return w(tag, msg + '\n' + Log.getStackTraceString(tr));
	}

	public static final int e(String tag, String msg) {
		return println(ERROR, tag, msg);
	}

	public static final int e(String tag, String msg, Throwable tr) {
		return e(tag, msg + '\n' + Log.getStackTraceString(tr));
	}

	public static final int e(Throwable e) {
		return e(ERROR_TAG, Log.getStackTraceString(e));
	}

	private static final String getTraceLog() {
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		if (trace == null || (trace.length < TRACE_STACK_POSITION + 1)) {
			return "";
		}

		StackTraceElement ele = trace[TRACE_STACK_POSITION];
		String className = ele.getClassName();
		int dot = className.lastIndexOf('.');
		String simpleClassName = className.substring(dot + 1);
		String methodName = ele.getMethodName();

		Thread thread = Thread.currentThread();
		String threadName = thread.getName() + "(" + thread.getId() + ")";

		String traceLog = getTraceLog(threadName, simpleClassName, methodName);
		return traceLog;
	}

	private static final String getTraceLog(String threadName,
											String simpleClassName, String methodName) {
		StringBuilder sb = new StringBuilder(threadName.length()
				+ simpleClassName.length() + methodName.length()
				+ METHOD_NAME_LENGTH);
		sb.append("[");
		sb.append(threadName);
		sb.append("][");
		sb.append(simpleClassName);
		sb.append(".");
		sb.append(methodName);
		sb.append("]");

		return sb.toString();
	}

	private static final String getAllTraces() {
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		if (trace == null) {
			return "";
		}

		StringBuilder builder = new StringBuilder();
		for (int i = 4; i < trace.length; i++) {
			builder.append("\tat ");
			builder.append(trace[i].toString());
			builder.append("\n");
		}
		return builder.toString();
	}

	// TODO for this style: I/BLUETOOTH_PHONE.TRACE( 1996):
	// [main(1)][OpenDoorActivity.onCreate], output thread name, Class name, function name
	public static final int trace() {
		return println(TRACE_PRIORITY, TRACE_TAG, getTraceLog());
	}

	// TODO for this style: I/BLUETOOTH_PHONE.TRACE( 1996):
	// [main(1)][OpenDoorActivity.onCreate]OpenDoorActivity, get more information
	public static final int trace(String msg) {
		return println(TRACE_PRIORITY, TRACE_TAG, getTraceLog() + msg);
	}

	// TODO output all trace of function stack
	public static final int traces() {
		return println(TRACE_PRIORITY, TRACE_TAG, getAllTraces());
	}
}
