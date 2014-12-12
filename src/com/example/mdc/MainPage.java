package com.example.mdc;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainPage extends Activity {

	public TextView clerk_region;
	public TextView clerk_name;
	public static TextView time_tag;
	private static TextView timer;
	private static Button getSchedule;
	private Button upload;
	public Button change_login;
	public Button practiceSurvey;
	public Button previous;
	public Button viewMaps;
	public static CountDownTimer countDownTimer;
	private static Button nextSurvey;
	public static ProgressDialog progressBar;
	public static ProgressDialog uploadBar;

	public final static long interval = 1 * 1000;

	public static long timeDiff;
	public static long timeUntilStart;

	public static Context mContext;

	public static DatabaseHelper myDB;

	public static boolean already_got_schedule = false;
	public static boolean in_a_survey_period = false;

	public static long time_to_end;
	public static String region = null;
	public static String clerkname = null;
	public static String start;
	public static String stop;
	public static String area_name;
	public static String sec;
	public static String min;

	public static String startString;

	public static String scheduleUID;

	public static Boolean practice;

	public static Boolean previousSurvey;

	// date and time variables
	public static DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	public static String headerURL = "http://mdc7test.mdc.mo.gov/webservices/srsproxywebapi/api/SurveyHeader/GetSurveyHeaderData";
	public static String partyURL = "http://mdc7test.mdc.mo.gov/webservices/srsproxywebapi/api/Parties/GetPartyData";
	public static String interviewURL = "http://mdc7test.mdc.mo.gov/webservices/srsproxywebapi/api/Interviews/GetInterviewsData";

	private File path = new File("/sdcard/MDC");

	private File f = new File("/sdcard/MDC/mdc.db");

	JSONArray surveyHeaders;
	JSONArray parties;
	JSONArray interviews;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_page);

		if (!path.exists())
			path.mkdirs();
		if (!f.exists()) {
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		mContext = this;
		practice = false;
		previousSurvey = false;
		// mContext.deleteDatabase(DatabaseHelper.dbName);
		myDB = new DatabaseHelper(mContext);

		df.setTimeZone(TimeZone.getTimeZone("US/Central"));
		if (TimeZone.getDefault().inDaylightTime(new Date()))
			timeDiff = 18000;
		else
			timeDiff = 21600;

		clerk_region = (TextView) findViewById(R.id.clerk_region);
		clerk_name = (TextView) findViewById(R.id.clerk_name);
		timer = (TextView) findViewById(R.id.timer);
		time_tag = (TextView) findViewById(R.id.tv2);
		getSchedule = (Button) findViewById(R.id.getSchedule);
		nextSurvey = (Button) findViewById(R.id.nextSurvey);
		upload = (Button) findViewById(R.id.upload);
		change_login = (Button) findViewById(R.id.change_login);
		practiceSurvey = (Button) findViewById(R.id.practice_survey);
		previous = (Button) findViewById(R.id.previous);
		viewMaps = (Button) findViewById(R.id.viewmap);

		viewMaps.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				Bundle b = new Bundle();
				b.putString("region", region);
				b.putString("name", clerkname);
				intent.putExtras(b);
				intent.setClass(MainPage.this, MapList.class);
				startActivity(intent);

			}

		});

		previous.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				previousSurvey = true;
				Intent i = new Intent();
				i.setClass(MainPage.this, PreviousSurvey.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);

			}
		});

		practiceSurvey.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				practice = true;
				Intent i = new Intent();
				i.setClass(MainPage.this, Survey.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				Bundle b = new Bundle();
				b.putLong("endTimer", 0);
				i.putExtras(b);
				startActivity(i);
			}
		});

		upload.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (checkDataConnectivity()) {
					uploadBar = new ProgressDialog(mContext);
					uploadBar.setMessage("Uploading data...");
					uploadBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					uploadBar.setIndeterminate(true);
					uploadBar.setCancelable(false);
					new HttpAsyncTask().execute();
				} else {
					new AlertDialog.Builder(mContext)
							.setTitle("Warning")
							.setMessage("No network connection")
							.setNegativeButton("OK",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											// User cancelled the dialog
										}
									}).create().show();
				}
			}
		});

		change_login.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		getSchedule.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (checkDataConnectivity()) {
					if (!gotSchedule())
						retrieveSchedule();
					else {
						new AlertDialog.Builder(mContext)
								.setTitle("Warning")
								.setMessage(
										"You have already got your schedule.\n Do you still want to update it?")
								.setPositiveButton("Yes",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												SQLiteDatabase db = myDB
														.getWritableDatabase();
												db.execSQL(DatabaseHelper.DROP_TABLE_SCHEDULE);
												db.execSQL(DatabaseHelper.CREATE_TABLE_SCHEDULE);
												db.close();
												retrieveSchedule();
											}
										})
								.setNegativeButton("No",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												// User cancelled the dialog
											}
										}).create().show();
					}
				} else {
					new AlertDialog.Builder(mContext)
							.setTitle("Warning")
							.setMessage("No network connection")
							.setNegativeButton("OK",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											// User cancelled the dialog
										}
									}).create().show();
				}
			}
		});

		nextSurvey.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (gotSchedule()) {
					practice = false;
					Cursor cursor = null;
					Intent i = new Intent();
					i.setClass(MainPage.this, Survey.class);
					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					if (in_a_survey_period) {
						SQLiteDatabase db = myDB.getWritableDatabase();
						cursor = db
								.rawQuery(
										"SELECT strftime('%s','"
												+ stop
												+ "') - strftime('%s','now') AS timeDiff",
										null);
						if (cursor.moveToFirst()) {
							time_to_end = Long.parseLong(cursor
									.getString(cursor
											.getColumnIndex("timeDiff")))
									+ timeDiff;
						}
						cursor.close();
						db.close();
						Bundle b = new Bundle();
						b.putLong("endTimer", time_to_end);
						i.putExtras(b);
					}
					startActivity(i);
				} else {
					new AlertDialog.Builder(mContext)
							.setTitle("Warning")
							.setMessage("Please download your schedule first.")
							.setNegativeButton("OK",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											// User cancelled the dialog
										}
									}).create().show();
				}
			}
		});

		Bundle b = getIntent().getExtras();
		region = b.getString("region");
		clerk_region.setText(region);
		clerkname = b.getString("name");
		clerk_name.setText(clerkname);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		updateInterface();
		super.onStart();
	}

	public static class MyCountDownTimer extends CountDownTimer {
		public MyCountDownTimer(long startTime, long interval) {
			super(startTime, interval);
		}

		@Override
		public void onTick(long millisUntilFinished) {
			// TODO Auto-generated method stub
			timeUntilStart -= 1;
			int total_hours = (int) (millisUntilFinished / (1000 * 3600));
			int days = (int) total_hours / 24;
			int hours = total_hours - 24 * days;
			int minutes = (int) (millisUntilFinished - total_hours * 3600 * 1000)
					/ (60 * 1000);
			int seconds = (int) (millisUntilFinished - total_hours * 3600
					* 1000 - minutes * 60 * 1000) / 1000;
			if (seconds < 10)
				sec = "0" + seconds;
			else
				sec = Integer.toString(seconds);
			if (minutes < 10)
				min = "0" + minutes;
			else
				min = Integer.toString(minutes);
			if (days > 0)
				timer.setText(days + "day(s)");
			else
				timer.setText(hours + ":" + min + ":" + sec);
		}

		@Override
		public void onFinish() {
			// TODO Auto-generated method stub
			timer.setText("Survey Time!!!");
			timer.setTextColor(Color.RED);
		}
	}

	public void retrieveSchedule() {
		if (checkDataConnectivity()) {
			progressBar = new ProgressDialog(mContext);
			progressBar.setMessage("Downloading Schedule...");
			progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressBar.setIndeterminate(true);
			progressBar.setCancelable(false);
			new GetSchedule().execute();
		} else {
			new AlertDialog.Builder(mContext)
					.setTitle("Warning")
					.setMessage("No network connection")
					.setNegativeButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// User cancelled the dialog
								}
							}).create().show();
		}
	}

	public boolean checkDataConnectivity() {
		ConnectivityManager connectivity = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static void updateInterface() {
		if (!gotSchedule()) {
			getSchedule.setText("Click To Load Schedule");
			getSchedule.setTextColor(Color.RED);
		} else {
			getSchedule.setText("Schedule Loaded");
			getSchedule.setTextColor(Color.GRAY);

			SQLiteDatabase db = myDB.getReadableDatabase();

			String query = "SELECT * FROM " + DatabaseHelper.table_schedule
					+ " WHERE " + DatabaseHelper.region + " = \"" + region
					+ "\"" + " AND (strftime('%s'," + DatabaseHelper.stopDate
					+ ") - strftime('%s','now')) + " + timeDiff
					+ " >= 0 AND (strftime('%s','now') - strftime('%s',"
					+ DatabaseHelper.startDate + ")) >= " + timeDiff + " AND "
					+ DatabaseHelper.done + " = 0";
			Cursor cursor = db.rawQuery(query, null);
			if (cursor.getCount() == 0) {
				query = "SELECT * FROM " + DatabaseHelper.table_schedule
						+ " WHERE " + DatabaseHelper.region + " = \"" + region
						+ "\"" + " AND (strftime('%s',"
						+ DatabaseHelper.startDate
						+ ") - strftime('%s','now')) + " + timeDiff
						+ " >= 0 AND " + DatabaseHelper.done + " = 0 "
						+ " ORDER BY " + DatabaseHelper.startDate
						+ " ASC LIMIT 1";
				cursor = db.rawQuery(query, null);
				in_a_survey_period = false;
			} else
				in_a_survey_period = true;
			if (cursor.moveToFirst()) {
				area_name = cursor.getString(cursor
						.getColumnIndex(DatabaseHelper.areaName));
				scheduleUID = cursor.getString(cursor
						.getColumnIndex(DatabaseHelper.scheduleUID));
				start = cursor.getString(cursor
						.getColumnIndex(DatabaseHelper.startDate));
				stop = cursor.getString(cursor
						.getColumnIndex(DatabaseHelper.stopDate));
				DateFormat format = DateFormat.getDateTimeInstance();
				try {
					startString = format.format(df.parse(start));
					nextSurvey.setText(area_name + "\n" + "Start: "
							+ format.format(df.parse(start)) + "\n" + "Stop: "
							+ format.format(df.parse(stop)));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				nextSurvey.setTextSize(30);
				cursor.close();
				if (!in_a_survey_period) {
					time_tag.setText("Time until start:");
					cursor = db.rawQuery("SELECT strftime('%s','" + start
							+ "') - strftime('%s','now') AS timeDiff", null);
					Log.d("database",
							"query returned : "
									+ Integer.toString(cursor.getCount()));
					if (cursor.moveToFirst()) {
						long time = Long.parseLong(cursor.getString(cursor
								.getColumnIndex("timeDiff")));
						if (countDownTimer != null)
							countDownTimer.cancel();
						timeUntilStart = time + timeDiff;
						countDownTimer = new MyCountDownTimer(
								(timeUntilStart) * 1000, interval);
						countDownTimer.start();
					}
					cursor.close();
				} else {
					timer.setText("Survey Time Now!!!");
					timer.setTextColor(Color.RED);
				}
			} else
				cursor.close();
			db.close();
		}
	}

	protected static Boolean gotSchedule() {

		SQLiteDatabase db = myDB.getReadableDatabase();
		Cursor cr = db.rawQuery("SELECT * FROM "
				+ DatabaseHelper.table_schedule + " WHERE "
				+ DatabaseHelper.region + " = \"" + region + "\"", null);
		if (cr != null) {
			if (cr.getCount() > 0) {
				cr.close();
				db.close();
				return true;
			}
			cr.close();
			db.close();
		}
		return false;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		practice = false;
		previousSurvey = false;
		updateInterface();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		if (countDownTimer != null)
			countDownTimer.cancel();
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (countDownTimer != null)
			countDownTimer.cancel();
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(mContext)
				.setTitle("Warning")
				.setMessage("Are you sure to go back?")
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								finish();
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// User cancelled the dialog
					}
				}).create().show();
	}

	public void uploadData() throws JSONException, ClientProtocolException,
			IOException {

		if (checkDataConnectivity()) {

			surveyHeaders = new JSONArray();
			parties = new JSONArray();
			interviews = new JSONArray();

			JSONObject object;

			SQLiteDatabase db = myDB.getReadableDatabase();
			Cursor cr = db.rawQuery("SELECT * FROM "
					+ DatabaseHelper.table_surveyheader + " WHERE "
					+ DatabaseHelper.uploaded + " = 0", null);
			if (cr.moveToFirst()) {
				do {
					object = new JSONObject(cr.getString(cr
							.getColumnIndex(DatabaseHelper.headerData)));
					object.put("upload_date", Utilities.generateDateTag());
					Log.d("upload header", object.toString());
					surveyHeaders.put(object);
				} while (cr.moveToNext());
				cr.close();
			}
			cr = db.rawQuery("SELECT * FROM " + DatabaseHelper.table_party
					+ " WHERE " + DatabaseHelper.uploaded + " = 0", null);
			if (cr.moveToFirst()) {
				do {
					object = new JSONObject(cr.getString(cr
							.getColumnIndex(DatabaseHelper.partyData)));
					object.put("upload_date", Utilities.generateDateTag());
					Log.d("upload party", object.toString());
					parties.put(object);
				} while (cr.moveToNext());
				cr.close();
			}
			cr = db.rawQuery("SELECT * FROM " + DatabaseHelper.table_interview
					+ " WHERE " + DatabaseHelper.uploaded + " = 0", null);
			if (cr.moveToFirst()) {
				do {
					object = new JSONObject(cr.getString(cr
							.getColumnIndex(DatabaseHelper.interviewData)));
					object.put("upload_date", Utilities.generateDateTag());
					Log.d("upload interview", object.toString());
					interviews.put(object);
				} while (cr.moveToNext());
				cr.close();
			}
			db.close();

			JSONObject surveyHeaderRecs = new JSONObject();
			surveyHeaderRecs.accumulate("surveyHeaderRecs", surveyHeaders);
			JSONObject partiesRecs = new JSONObject();
			partiesRecs.accumulate("partiesRecs", parties);
			JSONObject interviewsRecs = new JSONObject();
			interviewsRecs.accumulate("interviewsRecs", interviews);

			HttpPost httpPost = new HttpPost(headerURL);
			StringEntity se = new StringEntity(surveyHeaderRecs.toString(),
					HTTP.UTF_8);
			httpPost.setEntity(se);
			httpPost.setHeader("Content-type", "application/json;charset=utf-8");
			HttpResponse res = new DefaultHttpClient().execute(httpPost);
			uploadBar.incrementProgressBy(15);

			httpPost = new HttpPost(partyURL);
			se = new StringEntity(partiesRecs.toString(), HTTP.UTF_8);
			httpPost.setEntity(se);
			httpPost.setHeader("Content-type", "application/json;charset=utf-8");
			res = new DefaultHttpClient().execute(httpPost);
			uploadBar.incrementProgressBy(15);

			httpPost = new HttpPost(interviewURL);
			se = new StringEntity(interviewsRecs.toString(), HTTP.UTF_8);
			httpPost.setEntity(se);
			httpPost.setHeader("Content-type", "application/json;charset=utf-8");
			res = new DefaultHttpClient().execute(httpPost);
			uploadBar.incrementProgressBy(20);

			httpPost = new HttpPost(
					"http://dslsrv8.cs.missouri.edu/~hw85f/MDCSurvey/DataProcesser.php");
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("surveyheader", surveyHeaders
					.toString()));
			params.add(new BasicNameValuePair("party", parties.toString()));
			params.add(new BasicNameValuePair("interview", interviews
					.toString()));
			try {

				httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
				res = new DefaultHttpClient().execute(httpPost);
				uploadBar.incrementProgressBy(50);
				if (res.getStatusLine().getStatusCode() == 200) {
					db = myDB.getWritableDatabase();
					try {
						db.execSQL("UPDATE "
								+ DatabaseHelper.table_surveyheader
								+ " SET uploaded = 1 WHERE uploaded = 0");
						db.execSQL("UPDATE " + DatabaseHelper.table_party
								+ " SET uploaded = 1 WHERE uploaded = 0");
						db.execSQL("UPDATE " + DatabaseHelper.table_interview
								+ " SET uploaded = 1 WHERE uploaded = 0");
					} catch (SQLException e) {
						Log.d("update", "failed to update tables");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (!cr.isClosed())
				cr.close();
			db.close();
		} else {
			new AlertDialog.Builder(mContext)
					.setTitle("Warning")
					.setMessage("No network connection")
					.setNegativeButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// User cancelled the dialog
								}
							}).create().show();
		}

		// The following is uploading data using Joel's server

		// uploadSurveyHeaders();
		// uploadParties();
		// uploadInterviews();
		/*
		 * JSONObject partiesRecs = new JSONObject();
		 * partiesRecs.accumulate("partiesRecs", new JSONArray()); JSONObject
		 * interviewsRecs = new JSONObject();
		 * interviewsRecs.accumulate("interviewsRecs", new JSONArray());
		 * JSONObject surveyHeaderRecs = new JSONObject();
		 * surveyHeaderRecs.accumulate("surveyHeaderRecs", new JSONArray());
		 * 
		 * StringEntity se; HttpPost httpPost; HttpResponse response;
		 * 
		 * JSONObject object;
		 * 
		 * SQLiteDatabase db = myDB.getReadableDatabase(); Cursor cr =
		 * db.rawQuery("SELECT * FROM " + DatabaseHelper.table_surveyheader +
		 * " WHERE " + DatabaseHelper.uploaded + " = 0", null);
		 * if(cr.moveToFirst()){ do{ object = new
		 * JSONObject(cr.getString(cr.getColumnIndex
		 * (DatabaseHelper.headerData))); object.put("upload_date",
		 * Utilities.generateDateTag());
		 * surveyHeaderRecs.accumulate("surveyHeaderRecs", object);
		 * }while(cr.moveToNext()); httpPost = new HttpPost(headerURL); se = new
		 * StringEntity(surveyHeaderRecs.toString(), HTTP.UTF_8);
		 * httpPost.setEntity(se); httpPost.setHeader("Content-type",
		 * "application/json;charset=utf-8"); response = new
		 * DefaultHttpClient().execute(httpPost);
		 * if(response.getStatusLine().getStatusCode() == 200){ ContentValues
		 * values = new ContentValues(); values.put("uploaded", 1);
		 * if(db.update(DatabaseHelper.table_surveyheader, values,
		 * "uploaded = 0", null) <= 0) Log.d("update",
		 * "failed to update table_surveyheader"); else Log.d("update",
		 * "successfully updated table_surveyheader"); } cr.close(); } cr =
		 * db.rawQuery("SELECT * FROM " + DatabaseHelper.table_party + " WHERE "
		 * + DatabaseHelper.uploaded + " = 0", null); if(cr.moveToFirst()){ do{
		 * object = new
		 * JSONObject(cr.getString(cr.getColumnIndex(DatabaseHelper.
		 * headerData))); object.put("upload_date",
		 * Utilities.generateDateTag());
		 * surveyHeaderRecs.accumulate("surveyHeaderRecs", object);
		 * }while(cr.moveToNext()); httpPost = new HttpPost(headerURL); se = new
		 * StringEntity(surveyHeaderRecs.toString(), HTTP.UTF_8);
		 * httpPost.setEntity(se); httpPost.setHeader("Content-type",
		 * "application/json;charset=utf-8"); response = new
		 * DefaultHttpClient().execute(httpPost);
		 * if(response.getStatusLine().getStatusCode() == 200){ ContentValues
		 * values = new ContentValues(); values.put("uploaded", 1);
		 * if(db.update(DatabaseHelper.table_surveyheader, values,
		 * "uploaded = 0", null) <= 0) Log.d("update",
		 * "failed to update table_surveyheader"); else Log.d("update",
		 * "successfully updated table_surveyheader"); } cr.close(); } /*
		 * JSONObject partiesRecs = new JSONObject(); JSONArray jArray = new
		 * JSONArray();
		 * 
		 * JSONObject fake = new JSONObject(); fake.put("PartyUID",
		 * "777BBAEA-F9DF-47A8-A2CD-F91AB9D1DBD9"); fake.put("SurveyDataUID",
		 * "C05FCF89-10B5-42DA-8E4C-8786870BEABA"); fake.put("partytype",
		 * "car"); fake.put("partysize", 3); fake.put("comments",
		 * "Test comments1"); fake.put("interviewstatus", "complete");
		 * fake.put("entrydate", "2014-02-24T00:00:00"); fake.put("lastsave",
		 * "2014-02-24T00:00:00"); fake.put("RecordGPS", "36.9301593.01803");
		 * //fake.put("uploaded", 1); fake.put("upload_date",
		 * "2014-03-24T08:30:38"); fake.put("whatever", "fool");
		 * partiesRecs.accumulate("partiesRecs", jArray);
		 * partiesRecs.accumulate("partiesRecs", fake);
		 * 
		 * JsonPartiesRec p = new JsonPartiesRec(); p.PartyUID =
		 * "987BBAEA-F9DF-47A8-A2CD-F91AB9D1DBD9"; p.SurveyDataUID =
		 * "C05FCF89-10B5-42DA-8E4C-8786870BEABA"; p.partytype = "car";
		 * p.partysize = 30; p.comments = "Test comments1"; p.interviewstatus =
		 * "complete"; p.entrydate = "2014-02-24T00:00:00"; p.lastsave =
		 * "2014-02-24T00:00:00"; p.RecordGPS = "36.9301593.01803"; p.uploaded =
		 * 1; p.upload_date = "2014-03-24T08:30:38";
		 * 
		 * JsonPartiesDTO j = new JsonPartiesDTO(); j.partiesRecs.add(p); Gson
		 * gson = new Gson(); String json = gson.toJson(j); Log.d("upload",
		 * json);
		 * 
		 * String data = partiesRecs.toString();
		 * 
		 * HttpClient httpClient = new DefaultHttpClient(); StringEntity se =
		 * new StringEntity(data, HTTP.UTF_8); HttpPost httpPost = new HttpPost(
		 * "http://mdc7test.mdc.mo.gov/webservices/srsproxywebapi/api/Parties/GetPartyData"
		 * ); httpPost.setEntity(se);
		 * 
		 * httpPost.setHeader("Content-type", "application/json;charset=utf-8");
		 * HttpResponse httpResponse = httpClient.execute(httpPost);
		 * 
		 * InputStream inputStream = httpResponse.getEntity().getContent();
		 * String result; if (inputStream != null) result = "It works"; else
		 * result = "Did not work!"; Log.d("Server reply",
		 * inputStream.toString());
		 * 
		 * if (httpResponse.getStatusLine().getStatusCode() != 200) {
		 * Log.d("Server reply", Integer.toString(httpResponse.getStatusLine()
		 * .getStatusCode())); }
		 * 
		 * /* URL url = new URL(
		 * "http://mdc7test.mdc.mo.gov/webservices/srsproxywebapi/api/Parties/GetPartyData"
		 * ); HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		 * conn.setDoOutput(true); conn.setRequestMethod("POST");
		 * conn.setRequestProperty("Content-Type", "application/json");
		 * conn.connect(); OutputStream os = conn.getOutputStream();
		 * os.write(json.getBytes()); os.flush(); int code =
		 * conn.getResponseCode(); if (code==200) { Log.d("Server reply", "OK");
		 * } conn.disconnect();
		 */

	}

	private class HttpAsyncTask extends AsyncTask<Void, Void, String> {
		@Override
		protected String doInBackground(Void... voids) {
			try {
				uploadData();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return "OK";
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String results) {
			Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG)
					.show();
			if (uploadBar.isShowing()) {
				uploadBar.dismiss();
			}
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			uploadBar.setMax(100);
			uploadBar.show();
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
		}
	}

	protected void uploadSurveyHeaders() throws JSONException,
			ClientProtocolException, IOException {
		JSONObject surveyHeaderRecs = new JSONObject();
		surveyHeaderRecs.accumulate("surveyHeaderRecs", new JSONArray());

		StringEntity se;
		HttpPost httpPost;
		HttpResponse response;

		JSONObject object;

		SQLiteDatabase db = myDB.getReadableDatabase();
		Cursor cr = db.rawQuery("SELECT * FROM "
				+ DatabaseHelper.table_surveyheader + " WHERE "
				+ DatabaseHelper.uploaded + " = 0", null);
		if (cr.moveToFirst()) {
			do {
				object = new JSONObject(cr.getString(cr
						.getColumnIndex(DatabaseHelper.headerData)));
				object.put("upload_date", Utilities.generateDateTag());
				Log.d("upload header", object.toString());
				surveyHeaderRecs.accumulate("surveyHeaderRecs", object);
			} while (cr.moveToNext());
			db.close();
			httpPost = new HttpPost(headerURL);
			se = new StringEntity(surveyHeaderRecs.toString(), HTTP.UTF_8);
			httpPost.setEntity(se);
			httpPost.setHeader("Content-type", "application/json;charset=utf-8");
			response = new DefaultHttpClient().execute(httpPost);
			if (response.getStatusLine().getStatusCode() == 200) {
				db = myDB.getWritableDatabase();
				try {
					db.execSQL("UPDATE " + DatabaseHelper.table_surveyheader
							+ " SET uploaded = 1 WHERE uploaded = 0");
				} catch (SQLException e) {
					Log.d("update", "failed to update table_surveyheader");
				}
			}
			cr.close();
			db.close();
		}
	}

	protected void uploadParties() throws JSONException,
			ClientProtocolException, IOException {
		JSONObject partiesRecs = new JSONObject();
		partiesRecs.accumulate("partiesRecs", new JSONArray());

		StringEntity se;
		HttpPost httpPost;
		HttpResponse response;

		JSONObject object;

		SQLiteDatabase db = myDB.getReadableDatabase();
		Cursor cr = db.rawQuery("SELECT * FROM " + DatabaseHelper.table_party
				+ " WHERE " + DatabaseHelper.uploaded + " = 0", null);
		if (cr.moveToFirst()) {
			do {
				object = new JSONObject(cr.getString(cr
						.getColumnIndex(DatabaseHelper.partyData)));
				object.put("upload_date", Utilities.generateDateTag());
				Log.d("upload party", object.toString());
				partiesRecs.accumulate("partiesRecs", object);
			} while (cr.moveToNext());
			db.close();
			httpPost = new HttpPost(partyURL);
			se = new StringEntity(partiesRecs.toString(), HTTP.UTF_8);
			httpPost.setEntity(se);
			httpPost.setHeader("Content-type", "application/json;charset=utf-8");
			response = new DefaultHttpClient().execute(httpPost);
			if (response.getStatusLine().getStatusCode() == 200) {
				db = myDB.getWritableDatabase();
				try {
					db.execSQL("UPDATE " + DatabaseHelper.table_party
							+ " SET uploaded = 1 WHERE uploaded = 0");
				} catch (SQLException e) {
					Log.d("update", "failed to update table_party");
				}

			}
			cr.close();
			db.close();
		}
	}

	protected void uploadInterviews() throws JSONException,
			ClientProtocolException, IOException {
		JSONObject interviewsRecs = new JSONObject();
		interviewsRecs.accumulate("interviewsRecs", new JSONArray());

		StringEntity se;
		HttpPost httpPost;
		HttpResponse response;

		JSONObject object;

		SQLiteDatabase db = myDB.getReadableDatabase();
		Cursor cr = db.rawQuery("SELECT * FROM "
				+ DatabaseHelper.table_interview + " WHERE "
				+ DatabaseHelper.uploaded + " = 0", null);
		if (cr.moveToFirst()) {
			do {
				object = new JSONObject(cr.getString(cr
						.getColumnIndex(DatabaseHelper.interviewData)));
				object.put("upload_date", Utilities.generateDateTag());
				Log.d("upload interview", object.toString());
				interviewsRecs.accumulate("interviewsRecs", object);
			} while (cr.moveToNext());
			db.close();
			httpPost = new HttpPost(interviewURL);
			se = new StringEntity(interviewsRecs.toString(), HTTP.UTF_8);
			Log.d("upload interview", interviewsRecs.toString());
			httpPost.setEntity(se);
			httpPost.setHeader("Content-type", "application/json;charset=utf-8");
			response = new DefaultHttpClient().execute(httpPost);
			if (response.getStatusLine().getStatusCode() == 200) {
				db = myDB.getWritableDatabase();
				try {
					db.execSQL("UPDATE " + DatabaseHelper.table_interview
							+ " SET uploaded = 1 WHERE uploaded = 0");
				} catch (SQLException e) {
					Log.d("update", "failed to update table_interview");
				}
			}
			cr.close();
			db.close();
		}
	}
}
