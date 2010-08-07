package de.sinnerschrader.nauticlog;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NauticLogDatabase extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "nauticlog.db";
	private static final int DATABASE_VERSION = 1;
	
	public NauticLogDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(LogEntryTable.SQL_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(LogEntryTable.SQL_DROP);
		onCreate(db);
	}

}
