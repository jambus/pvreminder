package org.jambus.android.pvr;

import java.util.Date;

import org.jambus.android.pvr.constant.PVRConstant;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class OpenReminderReceiver extends BroadcastReceiver {
	
	private ActivityManager am = null;
	
	public OpenReminderReceiver(){
		Log.d(PVRConstant.loggerName, "Initialize receiver");
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		 Log.d(PVRConstant.loggerName, "Start receiver. Current time is:"+ new Date().toString());
		 
		 if(!isReminderOpened(context)){
			 Intent newIntent = new Intent();
			 newIntent.setClass(context, PVReminderActivity.class);
			 newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			 context.startActivity(newIntent);
		 }else{
			 Log.d(PVRConstant.loggerName, "PlanView Reminder has opened");
		 }
	}
	
	private boolean isReminderOpened(Context context){
		 boolean isOpened = false;
		 am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		 try{
			 ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
			 if(PVReminderActivity.class.getName().equals(cn.getClassName())){
				 isOpened = true;
			 }
		 }catch(Exception ex){
			 ex.printStackTrace();
			 Log.e(PVRConstant.loggerName, ex.getMessage());
		 }
		 return isOpened;
	}

}
