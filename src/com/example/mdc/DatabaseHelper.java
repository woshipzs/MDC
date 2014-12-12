package com.example.mdc;

import java.io.File;

import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

	static final String dbName = "/sdcard/MDC/mdc.db";
	static final String table_schedule = "tblSchedule";
	static final String table_surveyheader = "tblSurveyHeader";
	static final String table_party = "tblParties";
	static final String table_interview = "tblInterviews";
	static final String scheduleUID = "ScheduleUID";
	static final String projectUID = "ProjectUID";
	static final String regionID = "RegionID";
	static final String areID = "AREID";
	static final String areaName = "AreaName";
	static final String rngID = "RNGID";
	static final String rangeSurveyType = "RangeSurveyType";
	static final String latitude = "LATITUDE";
	static final String longitude = "LONGITUDE";
	static final String practice = "Practice";
	static final String done = "Done";
	static final String startDate = "startdate";
	static final String stopDate = "stopdate";
	static final String duration = "Duration";
	static final String region = "Region";
	static final String surveydataUID = "SurveyDataUID";
	static final String unitID = "unitID";
	static final String clerk = "clerks";
	static final String weather = "weather_sky";
	static final String temp = "temperature";
	static final String precip = "precipitation";
	static final String comments = "comments";
	static final String fullperiod = "fullperiod";
	static final String period_pct = "period_pct";
	static final String weather_end = "weather_sky_end";
	static final String temp_end = "temperature_end";
	static final String precip_end = "precipitation_end";
	static final String partycount = "Partycount";
	static final String entrydate = "entrydate";
	static final String lastsaved = "lastsaved";
	static final String gps = "RecordGPS";
	static final String mode = "mode";
	static final String uploaded = "uploaded";
	static final String upload_date = "upload_date";
	static final String headerData = "data";
	static final String partyData = "data";
	static final String interviewData = "data";
	

	public static final String DROP_TABLE_SCHEDULE = "DROP TABLE IF EXISTS "
			+ table_schedule;
	public static final String CREATE_TABLE_SCHEDULE = "CREATE TABLE IF NOT EXISTS "
			+ table_schedule
			+ "("
			+ scheduleUID
			+ " TEXT PRIMARY KEY,"
			+ projectUID
			+ " TEXT,"
			+ regionID
			+ " INTEGER,"
			+ region
			+ " TEXT,"
			+ areID
			+ " TEXT,"
			+ areaName
			+ " TEXT,"
			+ rngID
			+ " TEXT,"
			+ rangeSurveyType
			+ " TEXT,"
			+ latitude
			+ " REAL,"
			+ longitude
			+ " REAL,"
			+ startDate
			+ " TEXT,"
			+ stopDate
			+ " TEXT,"
			+ duration + " INTEGER," + practice + " TEXT," + done + " INTEGER)";

	public static final String CREATE_TABLE_SURVEYHEADER = "CREATE TABLE IF NOT EXISTS "
			+ table_surveyheader
			+ "("
			+ headerData
			+ " TEXT,"
			+ uploaded
			+ " INTEGER)";

	public static final String CREATE_TABLE_PARTY = "CREATE TABLE IF NOT EXISTS "
			+ table_party + "(" + partyData + " TEXT," + uploaded + " INTEGER)";

	public static final String CREATE_TABLE_INTERVIEW = "CREATE TABLE IF NOT EXISTS "
			+ table_interview
			+ "("
			+ interviewData
			+ " TEXT,"
			+ uploaded
			+ " INTEGER)";

	public DatabaseHelper(Context context) {
		super(context, DatabaseHelper.dbName, null, 1);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS " + table_schedule);
		db.execSQL(CREATE_TABLE_SCHEDULE);
		db.execSQL("DROP TABLE IF EXISTS " + table_surveyheader);
		db.execSQL(CREATE_TABLE_SURVEYHEADER);
		db.execSQL("DROP TABLE IF EXISTS " + table_party);
		db.execSQL(CREATE_TABLE_PARTY);
		db.execSQL("DROP TABLE IF EXISTS " + table_interview);
		db.execSQL(CREATE_TABLE_INTERVIEW);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		// db.execSQL("DROP TABLE IF EXISTS " + table_schedule);
		db.execSQL(CREATE_TABLE_SCHEDULE);
	}

	public Boolean insert_schedule_into_table(JSONObject schedule) {
		try {

			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(scheduleUID, schedule.getString(scheduleUID));
			values.put(projectUID, schedule.getString(projectUID));
			values.put(regionID, schedule.getInt(regionID));
			values.put(region, schedule.getString(region));
			values.put(areID, schedule.getString(areID));
			values.put(areaName, schedule.getString(areaName));
			values.put(rngID, schedule.getString(rngID));
			values.put(rangeSurveyType, schedule.getString(rangeSurveyType));
			values.put(latitude, (Double) schedule.getDouble(latitude));
			values.put(longitude, (Double) schedule.get(longitude));
			values.put(startDate, schedule.getString(startDate));
			values.put(stopDate, schedule.getString(stopDate));
			values.put(duration, schedule.getInt(duration));
			values.put(practice, schedule.getString(practice));
			values.put(done, 0);
			if (db.insert(table_schedule, null, values) != -1) {
				Log.d("Database", "Insert schedule successfully");
				db.close();
			} else {
				Log.d("Database", "Insert schedule failed");
				db.close();
				return false;
			}
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	public boolean insert_surveyheader_into_table(JSONObject header) {
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(headerData, header.toString());
			values.put(uploaded, 0);
			if (db.insert(table_surveyheader, null, values) != -1) {
				Log.d("Database", "Insert survey header successfully");
				db.close();
			} else {
				Log.d("Database", "Insert survey header failed");
				db.close();
				return false;
			}
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean insert_party_into_table(JSONObject party) {
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(partyData, party.toString());
			values.put(uploaded, 0);
			if (db.insert(table_party, null, values) != -1) {
				Log.d("Database", "Insert party successfully");
				db.close();
			} else {
				Log.d("Database", "Insert party failed");
				db.close();
				return false;
			}
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean insert_interview_into_table(JSONObject interview) {
		try {
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(interviewData, interview.toString());
			values.put(uploaded, 0);
			if (db.insert(table_interview, null, values) != -1) {
				Log.d("Database", "Insert interview successfully");
				db.close();
			} else {
				Log.d("Database", "Insert interview failed");
				db.close();
				return false;
			}
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
}
