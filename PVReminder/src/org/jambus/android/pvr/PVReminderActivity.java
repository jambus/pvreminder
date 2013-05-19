package org.jambus.android.pvr;

import java.util.Calendar;
import java.util.Date;

import org.jambus.android.pvr.constant.PVRConstant;
import org.jambus.android.pvr.db.DatabaseHelper;
import org.jambus.android.pvr.util.PVRUtils;

import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PVReminderActivity extends Activity {

	private AlarmManager am = null;
	
	private Calendar nextAlermDate = null;
	private Uri ringURI = null;
	
	private Button completeButton;
	private Button reminderLaterButton;
	private MediaPlayer mobileRing;
	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;
	private ImageView clockImage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pvreminder);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                			    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
			                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                	            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		
		
		dbHelper = new DatabaseHelper(PVReminderActivity.this,PVRConstant.DBNAME);
		db = dbHelper.getWritableDatabase();
		
		updateAlermClock();
		
		TextView title = (TextView) findViewById(R.id.txtTitle);
		Typeface typeFaceBlkchcry = Typeface.createFromAsset(getAssets(),"fonts/BLKCHCRY.TTF");
		if(null != typeFaceBlkchcry){
			title.setTypeface(typeFaceBlkchcry);
		}
		
		completeButton = (Button) findViewById(R.id.completePVBUtton);
		completeButton.setOnClickListener(new CompeleteButtonListener());
		
		reminderLaterButton = (Button) findViewById(R.id.reminderLaterButton);
		reminderLaterButton.setOnClickListener(new ReminderLaterButtonListener());
	
		clockImage = (ImageView) findViewById(R.id.clockImage);
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(PVRConstant.loggerName, "OnResume");
		
		int status = validatePVStatus();
		Log.d(PVRConstant.loggerName, ""+status);
		if(status == PVRConstant.RINGREQUIRE){
			callPhoneRing();
		}
	}
	
	@Override  
    public void onWindowFocusChanged(boolean hasFocus) {  
        super.onWindowFocusChanged(hasFocus);  
        clockImage.setBackgroundResource(R.anim.clock_animation);
        AnimationDrawable animationClock = (AnimationDrawable) clockImage.getBackground();
		if(!animationClock.isRunning()){
			animationClock.start();
		}
    }  

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopAndCloseRing();
		db.close();
	}

	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String result = data.getExtras().getString("result");//得到新Activity 关闭后返回的数据
        Log.d(PVRConstant.loggerName, result);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.pvreminder, menu);
		return true;
	}
	
	private int validatePVStatus(){
		return PVRConstant.RINGREQUIRE;
	}
	
	
	
	private boolean validateTwoDateInSameWeek(Date one, Date two){
		boolean flag = false;
		
		Calendar oneB = Calendar.getInstance();
		Calendar twoB = Calendar.getInstance();
		
		oneB.setFirstDayOfWeek(Calendar.MONDAY);
		oneB.setMinimalDaysInFirstWeek(7);
		oneB.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		oneB.setTime(one);
		
		twoB.setFirstDayOfWeek(Calendar.MONDAY);
		twoB.setMinimalDaysInFirstWeek(7);
		twoB.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		twoB.setTime(two);
		
		Log.d(PVRConstant.loggerName, "date one:"+ oneB.toString());
		Log.d(PVRConstant.loggerName, "date two:"+ twoB.toString());
		
		if(oneB.get(Calendar.YEAR) == twoB.get(Calendar.YEAR)
				&& oneB.get(Calendar.WEEK_OF_YEAR) == twoB.get(Calendar.WEEK_OF_YEAR)){
			flag = true;
		}
		
		return flag;
	}
	
	private Date insertPVTimeSlot(){
		ContentValues values = new ContentValues();
		
		Calendar nowDay = Calendar.getInstance();
		String nowDayStr = PVRConstant.DBFillSlotDateFormat.format(nowDay.getTime());
		
		values.put("fillslot", nowDayStr);
		db.insert(PVRConstant.DBTimeSheetTableName, null, values);
		Log.d(PVRConstant.loggerName, "insert PV　TimeSlot:"+ nowDayStr);
		return nowDay.getTime();
	}
	
	private void updateAlermClock(){
		nextAlermDate = PVRUtils.getNextPVSlot(db);
		
		long triggerAtMillis =  nextAlermDate.getTimeInMillis();
		long interval = PVRConstant.MillisInHour;
		
		Calendar nowDay = Calendar.getInstance();
		Log.d(PVRConstant.loggerName, "Current time:"+ nowDay.getTime().toString());
		
		if(PVRConstant.debug){
			nowDay.add(Calendar.SECOND, 10);
			triggerAtMillis = nowDay.getTimeInMillis();
			interval = 5000;
		}
		
		Intent newIntent = new Intent(PVRConstant.ALERMACTION);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		am.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, interval, pendingIntent);
	}
	
	private void callPhoneRing(){
		
		Log.d(PVRConstant.loggerName, "Play defaut ring");
		
		if(mobileRing == null){
			mobileRing = new MediaPlayer();
		}
		try {
			if(!mobileRing.isPlaying()){
				mobileRing.setDataSource(this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
				mobileRing.setLooping(true);
				mobileRing.prepare();
				mobileRing.start();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			Log.d(PVRConstant.loggerName, ex.getMessage());
			
			Toast.makeText(this, R.string.warningErrorCallRing, Toast.LENGTH_LONG).show();
		}
		
		/*
		Intent intent = new Intent(RingtoneManager.);  
	    // Allow user to pick 'Default'  
	    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);  
	    // Show only ringtones  
	    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,RingtoneManager.TYPE_RINGTONE);  
	    // Don't show 'Silent'  
	    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
	    
	    if (ringURI == null) {  
	    	ringURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);  
	    }  
	  
	    // Put checkmark next to the current ringtone for this contact  
	    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, ringURI);  
	   
	    // Launch! startActivityForResult(intent, REQUEST_CODE_PICK_RINGTONE);  
	    startActivityForResult(intent, PVRConstant.REQUEST_CODE_PICK_RINGTONE);  
	    */
	}
	
	private void stopAndCloseRing(){
		Log.d(PVRConstant.loggerName, "Stop Ring");
		try{
			if(mobileRing!= null){
				mobileRing.stop();
				mobileRing.release();
				mobileRing = null;
			}}finally{
			}
	}
	
	class CompeleteButtonListener implements OnClickListener{

		@Override
		public void onClick(View arg0) {
			
			Calendar nowDay = Calendar.getInstance();
			if(validateTwoDateInSameWeek(nowDay.getTime(),nextAlermDate.getTime()) || PVRUtils.getLastPVSlot(db) == null){
				insertPVTimeSlot();
				updateAlermClock();
			}
			
			Toast.makeText(PVReminderActivity.this, R.string.successCompleteMsg, Toast.LENGTH_LONG).show();
			Log.d(PVRConstant.loggerName, "Complete! Next round is:"+ nextAlermDate.getTime().toString());
			finish();
		}
    	
    }
	
	class ReminderLaterButtonListener implements OnClickListener{

		@Override
		public void onClick(View arg0) {
			Log.d(PVRConstant.loggerName, "Remind later! Next round is:" + nextAlermDate.toString());
			Toast.makeText(PVReminderActivity.this, R.string.successReminderLaterMsg, Toast.LENGTH_LONG).show();
			finish();
		}
    	
    }
}
