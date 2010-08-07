package de.sinnerschrader.nauticlog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LogEntryActivity extends Activity implements LocationListener {
	
	private LocationManager locationManager;
	private String bestProvider;
	private TextView txtGeo;
	private Location lastLocation;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        bestProvider = locationManager.getBestProvider(criteria, true);
        Log.v(LogEntryActivity.class.getSimpleName(), "LocationManager's provider: " + bestProvider);
        
        final Date currentDate = new Date();
        String currentDateDisplay = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(currentDate);
        TextView txtDate = (TextView) findViewById(R.id.txt_date);
        txtDate.setText(currentDateDisplay);
        
        txtGeo = (TextView) findViewById(R.id.txt_geo);
        Location location = locationManager.getLastKnownLocation(bestProvider);
        onLocationChanged(location);
        
        Button btn_submit = (Button) findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				saveEntry(currentDate);
				
				Toast toast = Toast.makeText(LogEntryActivity.this, "Eintrag gespeichert", Toast.LENGTH_SHORT);
				toast.show();
			}
		});
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	locationManager.requestLocationUpdates(bestProvider, 2000, 2, this);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	locationManager.removeUpdates(this);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.logentrymenu, menu);
    	
    	return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.opt_clear:
    		clearLogEntries();
    		return true;
    		
    	case R.id.opt_export:
    		exportLogEntries();
    		return true;
    	}
    	
    	return super.onOptionsItemSelected(item);
    }
    
    private void clearLogEntries() {
    	NauticLogDatabase db = new NauticLogDatabase(this);
    	try {
	    	SQLiteDatabase dbConn = db.getWritableDatabase();
	    	dbConn.execSQL(LogEntryTable.STMT_CLEAR);
    	} finally {
	    	db.close();
    	}
    	
		Toast toast = Toast.makeText(this, "Alle Einträge wurden gelöscht", Toast.LENGTH_SHORT);
		toast.show();
    }
    
    private void exportLogEntries() {
    	if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
    		Toast toast = Toast.makeText(this, "SD-Karte nicht verfügbar oder nicht beschreibbar: " + Environment.getExternalStorageState(), Toast.LENGTH_SHORT);
    		toast.show();
    		
    		return;
    	}
    	
    	NauticLogDatabase db = new NauticLogDatabase(this);
    	
    	File root = Environment.getExternalStorageDirectory();
    	File nauticRoot = new File(root, "nauticlog");
    	File output = new File(nauticRoot, "nauticlog.xml");
    	try {
    		nauticRoot.mkdirs();
    		
	    	FileWriter writer = new FileWriter(output);
	    	
	    	writer.append("<ENTRIES>");
	    	SQLiteDatabase dbConn = db.getReadableDatabase();
	    	Cursor logEntry = dbConn.rawQuery(LogEntryTable.QUERY_ALL, null);
	    	while (logEntry.moveToNext()) {
	    		writer.append("<ENTRY>");
	    		
	    		writer.append("<DATE>");
	    		long date = logEntry.getLong(0);
	    		writer.append(Long.toString(date));
	    		writer.append("</DATE>");
	    		
	    		writer.append("<LATITUDE>");
	    		double latitude = logEntry.getDouble(1);
	    		writer.append(Double.toString(latitude));
	    		writer.append("</LATITUDE>");
	    		
	    		writer.append("<LONGITUDE>");
	    		double longitude = logEntry.getDouble(2);
	    		writer.append(Double.toString(longitude));
	    		writer.append("</LONGITUDE>");
	    		
	    		writer.append("<NOTE>");
	    		String note = logEntry.getString(3);
	    		writer.append(note);
	    		writer.append("</NOTE>");
	    		
	    		writer.append("</ENTRY>");
	    	}
	    	logEntry.close();
	    	writer.append("</ENTRIES>");
	    	
	    	writer.flush();
	    	writer.close();
	    	
    		Toast toast = Toast.makeText(this, "Alle Einträge wurden exportiert", Toast.LENGTH_SHORT);
    		toast.show();
    	} catch (IOException e) {
    		Toast toast = Toast.makeText(this, "Es ist ein Fehler beim Exportieren aufgetreten", Toast.LENGTH_SHORT);
    		toast.show();
    	} finally {
    		db.close();
    	}
    }
    
    private void saveEntry(Date date) {
    	EditText edtNote = (EditText) findViewById(R.id.edt_note);
    	String note = edtNote.getText().toString();
    	
    	double latitude = 0;
    	double longitude = 0;
    	if (lastLocation != null) {
    		latitude = lastLocation.getLatitude();
    		longitude = lastLocation.getLongitude();
    	}
    	
    	NauticLogDatabase db = new NauticLogDatabase(this);
    	try {
	    	SQLiteDatabase dbConn = db.getWritableDatabase();
	    	dbConn.execSQL(LogEntryTable.STMT_INSERT, new Object[] { date.getTime(), latitude, longitude, note });
    	} finally {
	    	db.close();
    	}
    }

	public void onLocationChanged(Location location) {
		lastLocation = location;
		String text;
        if (location == null) {
        	text = "Geo-Koordinaten: ?";
        } else {
        	text = "Geo-Koordinaten: Lat = " + Location.convert(location.getLatitude(), Location.FORMAT_MINUTES) +
        			", Lon = " + Location.convert(location.getLongitude(), Location.FORMAT_MINUTES);
        }
        
        txtGeo.setText(text);
	}

	public void onProviderDisabled(String provider) {
	}

	public void onProviderEnabled(String provider) {
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
}