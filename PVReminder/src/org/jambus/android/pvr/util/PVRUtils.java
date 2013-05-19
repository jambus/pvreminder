package org.jambus.android.pvr.util;

import java.util.Calendar;
import java.util.Date;

import org.jambus.android.pvr.PVReminderActivity;
import org.jambus.android.pvr.constant.PVRConstant;
import org.jambus.android.pvr.db.DatabaseHelper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class PVRUtils {
	
	public static Date getLastPVSlot(SQLiteDatabase db){
		Date latestDate = null;
		
		try{
			Cursor cursor = db.query(PVRConstant.DBTimeSheetTableName, new String[]{"id","fillslot"}, null, null, null, null, "fillslot desc");
			if(cursor.moveToNext()){
				String latestDateStr = cursor.getString(cursor.getColumnIndex("fillslot"));
				latestDate = PVRConstant.DBFillSlotDateFormat.parse(latestDateStr);
			}
			cursor.close();
			Log.d(PVRConstant.loggerName, "Last PV time slot is:"+ latestDate);
		}catch(Exception ex){
			ex.printStackTrace();
			Log.e(PVRConstant.loggerName, ex.getMessage());
		}
		return latestDate;
	}
	
	public static Calendar getNextPVSlot(SQLiteDatabase db){
		Calendar nextDate = null;
		
		Date lastDate = PVRUtils.getLastPVSlot(db);
		if(lastDate == null){
			Calendar nowDay = Calendar.getInstance();
			int weekday = nowDay.get(Calendar.DAY_OF_WEEK);
			if(weekday == Calendar.SUNDAY){
				nowDay.add(Calendar.DATE, 5);
			}else if (weekday == Calendar.SATURDAY){
				nowDay.add(Calendar.DATE, 6);
			}else{
				nowDay.add(Calendar.DATE, Calendar.FRIDAY - weekday);
			}
			nowDay.set(Calendar.HOUR_OF_DAY, 12);
			nowDay.set(Calendar.MINUTE, 0);
			nowDay.set(Calendar.SECOND, 0);
			nextDate = nowDay;
		}else{
			Calendar nowDay = Calendar.getInstance();
			nowDay.setTime(lastDate);
			int weekday = nowDay.get(Calendar.DAY_OF_WEEK);
			if(weekday == Calendar.SUNDAY){
				nowDay.add(Calendar.DATE, 5);
			}else{
				nowDay.add(Calendar.DATE, 6 + Calendar.SATURDAY - weekday);
			}
			nowDay.set(Calendar.HOUR_OF_DAY, 12);
			nowDay.set(Calendar.MINUTE, 0);
			nowDay.set(Calendar.SECOND, 0);
			nextDate = nowDay;
		}
		Log.d(PVRConstant.loggerName, "Next PV time slot is:"+ nextDate.getTime().toString());
		
		return nextDate;
	}
}
