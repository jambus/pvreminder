package org.jambus.android.pvr;

import java.util.Calendar;
import java.util.Date;

import org.jambus.android.pvr.constant.PVRConstant;
import org.jambus.android.pvr.db.DatabaseHelper;
import org.jambus.android.pvr.util.PVRUtils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class InitialReminderReceiver extends BroadcastReceiver {
	
	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;
	private AlarmManager am = null;

	@Override
	public void onReceive(Context context, Intent intent) {
		
		dbHelper = new DatabaseHelper(context,PVRConstant.DBNAME);
		db = dbHelper.getWritableDatabase();
		
		Calendar nowDay = Calendar.getInstance();
		Log.d(PVRConstant.loggerName, "Reboot compeleted. Current time:"+ nowDay.getTime().toString());
		
		am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Calendar nextAlermDate = PVRUtils.getNextPVSlot(db);
		long triggerAtMillis =  nextAlermDate.getTimeInMillis();
		long interval = PVRConstant.MillisInHour;
		
		if(PVRConstant.debug){
			nowDay.add(Calendar.SECOND, 10);
			triggerAtMillis = nowDay.getTimeInMillis();
			interval = 5000;
		}
		
		Intent newIntent = new Intent(PVRConstant.ALERMACTION);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		am.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, interval, pendingIntent);
	}

}
