package org.jambus.android.pvr.constant;

import java.text.SimpleDateFormat;

public class PVRConstant {
	public static final String ALERMACTION = "android.pvr.alarm.action";
	public static final int MillisInHour = 1000 * 60 * 60;
	public static String loggerName = "pvr";
	public static final int RINGREQUIRE = 1;
	public static final int NOACTIONREQUIRE = 0;
	public static final int REQUEST_CODE_PICK_RINGTONE = 1;
	public static final int DBVersion = 1;
	public static final String DBNAME = "pvrDB";
	public static final String DBTimeSheetTableName = "timesheet";
	public static final SimpleDateFormat DBFillSlotDateFormat =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final boolean debug = false;
}
