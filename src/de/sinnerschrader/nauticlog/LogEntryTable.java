package de.sinnerschrader.nauticlog;

public class LogEntryTable implements LogEntryColumns {

	public static final String TABLE_NAME = "notes";
	
	public static final String SQL_CREATE =
		"CREATE TABLE notes (" +
		"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
		"date INTEGER," +
		"latitude REAL," +
		"longitude REAL," +
		"note TEXT" +
		")";
	
	public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;
	
	public static final String STMT_INSERT =
		"INSERT INTO notes " + 
		"(date, latitude, longitude, note) " +
		"VALUES (?, ?, ?, ?)";
	
	public static final String STMT_CLEAR = 
		"DELETE FROM notes";
	
	public static final String QUERY_ALL =
		"SELECT date, latitude, longitude, note " +
		"FROM notes";
	
}
