package com.example.mdc;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class PreviousSurvey extends Activity {

	SQLiteDatabase db;

	Cursor cr;

	String query = "SELECT * FROM " + DatabaseHelper.table_schedule + " WHERE "
			+ DatabaseHelper.region + " = \"" + MainPage.region + "\""
			+ " AND (strftime('%s','now') - strftime('%s',"
			+ DatabaseHelper.stopDate + ")) >= 18000" + " AND "
			+ DatabaseHelper.done + " = 0 ORDER BY " + DatabaseHelper.stopDate + " DESC";

	String area_name;
	String start;

	ArrayAdapter<String> mAdapter;

	ListView listView;
	
	Button cancel;

	ArrayList<String> surveys;

	ArrayList<String> scheduleUID;
	
	ArrayList<String> areas;

	ArrayList<String> startDate;
	
	DateFormat format;

	DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.previous_survey);

		listView = (ListView) findViewById(R.id.list);
		cancel = (Button) findViewById(R.id.cancel_previous_survey);
		
		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		format = DateFormat.getDateTimeInstance();
		try {
			surveys = getAllPreviousSurvey();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		mAdapter = new ArrayAdapter<String>(this, R.layout.list_entry,
				surveys);

		listView.setAdapter(mAdapter);

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> listView, View view,
					int position, long id) {
				MainPage.scheduleUID = scheduleUID.get(position);
				MainPage.area_name = areas.get(position);
				MainPage.start = startDate.get(position);
				Intent i = new Intent();
				i.setClass(PreviousSurvey.this, Survey.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);
			}
		});
	}

	ArrayList<String> getAllPreviousSurvey() throws ParseException {
		ArrayList<String> list = new ArrayList<String>();
		scheduleUID = new ArrayList<String>();
		areas = new ArrayList<String>();
		startDate = new ArrayList<String>();
		db = MainPage.myDB.getReadableDatabase();
		cr = db.rawQuery(query, null);
		if (cr.moveToFirst()) {
			while (!cr.isAfterLast()) {
				area_name = cr.getString(cr
						.getColumnIndex(DatabaseHelper.areaName));
				start = cr.getString(cr
						.getColumnIndex(DatabaseHelper.startDate));
				list.add(area_name + "\n" + "Start: "
						+ format.format(df.parse(start)) + "\n");
				scheduleUID.add(cr.getString(cr
						.getColumnIndex(DatabaseHelper.scheduleUID)));
				areas.add(area_name);
				startDate.add(format.format(df.parse(start)));
				cr.moveToNext();
			}
		}
		cr.close();
		db.close();
		return list;
	}

}
