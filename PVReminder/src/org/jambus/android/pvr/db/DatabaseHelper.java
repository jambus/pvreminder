package org.jambus.android.pvr.db;

import org.jambus.android.pvr.constant.PVRConstant;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
	
	public DatabaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}
	
	public DatabaseHelper(Context context, String name) {
		super(context, name, null, PVRConstant.DBVersion);
	}
	
	public DatabaseHelper(Context context, String name, CursorFactory factory) {
		super(context, name, factory, PVRConstant.DBVersion);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(PVRConstant.loggerName, "Create Database");
		db.execSQL("create table "+ PVRConstant.DBTimeSheetTableName +" (id integer primary key AUTOINCREMENT, fillslot varchar(255))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(PVRConstant.loggerName, "Upgrade Database");
	}

}
