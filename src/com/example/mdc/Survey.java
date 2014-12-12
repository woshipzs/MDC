package com.example.mdc;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class Survey extends Activity {

	Button vehicleAdd;
	Button vehicleSub;
	Button cancel;
	Button next;
	Button survey_done;
	Button new_party;
	Button party_decline;
	Button new_interview;
	Button next_interview;
	Button done_interviews;
	Button newParty_cancel;
	Button newInterview_cancel;
	Button next_interview_page;
	Button interview_refused;
	Button prev_page;
	Button back;
	Button end_header_done;

	Spinner how_often;
	ArrayAdapter<String> how_often_adapter;
	public static String[] how_often_choices;

	RadioGroup vehicle_type;
	RadioButton vt;
	RadioGroup gender;
	RadioButton b_gender;
	RadioGroup age;
	RadioButton b_age;
	RadioGroup hunter;
	RadioGroup kept_away;

	EditText vehicleTotal;
	EditText num_in_party;
	EditText zipcode;
	EditText spent_hrs;
	EditText spent_mins;
	EditText visits;
	EditText comments;
	EditText survey_comments;
	EditText time_percentage;

	static TextView endTimer;
	TextView ct_interviewee;
	TextView tv_party;
	TextView tv_partyType;
	TextView surveyTag;
	TextView tv_time_spent;
	TextView tv_zipcode;
	TextView tv_visits;

	Spinner weather;
	Spinner temp;
	Spinner precip;

	CheckBox before;
	CheckBox firsttime;
	CheckBox driver;

	CheckBox rifle;
	CheckBox shotgun;
	CheckBox sighting;
	CheckBox pattern;
	CheckBox handgun;
	CheckBox msr;
	CheckBox muzzleloader;
	CheckBox self_defense;
	CheckBox archery;
	CheckBox other;
	CheckBox noshoot;
	CheckBox observing;

	CheckBox litter;
	CheckBox vandalism;
	CheckBox downrange;
	CheckBox drunk;
	CheckBox auto_fire;
	CheckBox move_downrange;
	CheckBox unap_tar;
	CheckBox ex_tar;
	CheckBox other_compliance;
	CheckBox fullPeriod;
	CheckBox notify_manager;

	ArrayAdapter<String> weather_adapter;
	ArrayAdapter<String> temp_adapter;
	ArrayAdapter<String> precip_adapter;

	public static String[] weather_choices;
	public static String[] temp_choices;
	public static String[] precip_choices;

	Context mContext;

	static CountDownTimer cdt = null;

	Long endtime;

	int interviewee_counter;
	int num_of_people_in_party;
	double time_spent;
	int time_min;

	// BluetoothAdapter mBluetoothAdapter =
	// BluetoothAdapter.getDefaultAdapter();

	// data storage variables
	JSONObject tblSurveyHeaders = new JSONObject();
	JSONObject surveyHeader = new JSONObject();
	JSONObject party = new JSONObject();
	JSONObject interview = new JSONObject();
	JSONObject tblParties = new JSONObject();
	JSONObject tblInterviews = new JSONObject();

	public static String surveyDataUID;
	public static String partyUID;
	public static String interviewUID;
	public static String wea;
	public static String tem;
	public static String pre;
	public static String spent_min;
	public static String spent_hour;

	public String partyType;
	public String s_gender;
	public String s_age;
	public String re_gender;
	public String re_age;
	public String comp;
	public String scheduleUID;
	public static String sec;
	public static String min;

	public static boolean b1;
	public static boolean b2;
	public static boolean is_driver;

	private LocationManager mLocationManager;
	private Location mLocation;

	SQLiteDatabase db;
	Cursor cr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.header_main);

		mContext = this;
		interviewee_counter = 0;
		b1 = false;
		b2 = false;
		surveyDataUID = Utilities.generateUID();
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mLocation = mLocationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				300 * 1000, 30, listener);
		surveyTag = (TextView) findViewById(R.id.survey_tag);
		if (MainPage.practice) {
			surveyTag.setText("Practice!");
			surveyTag.setTextColor(Color.RED);
			surveyTag.setTextSize(40);
		} else
			surveyTag.setText(MainPage.region + "\n" + MainPage.area_name
					+ "\n" + MainPage.startString);

		try {
			tblSurveyHeaders.accumulate("surveyHeaderRecs", new JSONArray());
			tblParties.accumulate("partiesRecs", new JSONArray());
			tblInterviews.accumulate("interviewsRecs", new JSONArray());
			if (MainPage.practice) {
				scheduleUID = Utilities.generateUID();
				surveyHeader.put("ScheduleUID", scheduleUID);
			} else {
				scheduleUID = MainPage.scheduleUID;
				surveyHeader.put("ScheduleUID", scheduleUID);
			}
			surveyHeader.put("SurveyDataUID", surveyDataUID);
			db = MainPage.myDB.getReadableDatabase();
			cr = db.rawQuery("SELECT * FROM "
					+ DatabaseHelper.table_surveyheader, null);
			if (!cr.moveToFirst())
				surveyHeader.put("unitID", Utilities.generateUID());
			else {
				cr.moveToFirst();
				JSONObject o = new JSONObject(cr.getString(cr
						.getColumnIndex("data")));
				surveyHeader.put("unitID", o.getString("unitID"));
			}
			cr.close();
			db.close();
			surveyHeader.put("clerks", MainPage.clerkname);
			// surveyHeader.put("gps",Double.toString(mLocation.getLatitude())
			// + ","+ Double.toString(mLocation.getLongitude()));
			surveyHeader.put("entrydate", Utilities.generateDateTag());
			surveyHeader.put("uploaded", 0);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			goToHeaderPage();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	LocationListener listener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			if (location != null) {
				mLocation = location;
			}
		}

		@Override
		public void onProviderDisabled(String arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// TODO Auto-generated method stub

		}

	};

	public static class MyCountDownTimer2 extends CountDownTimer {
		public MyCountDownTimer2(long startTime, long interval) {
			super(startTime, interval);
		}

		@Override
		public void onTick(long millisUntilFinished) {
			// TODO Auto-generated method stub
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
				endTimer.setText("Time left:\n" + days + "d" + hours + ":"
						+ min + ":" + sec);
			else
				endTimer.setText("Time left:\n" + hours + ":" + min + ":" + sec);
		}

		@Override
		public void onFinish() {
			// TODO Auto-generated method stub
			endTimer.setText("Survey Is Over!!!");
			endTimer.setTextColor(Color.RED);
		}

	}

	public static class MyCountDownTimer3 extends CountDownTimer {
		public MyCountDownTimer3(long startTime, long interval) {
			super(startTime, interval);
		}

		@Override
		public void onTick(long millisUntilFinished) {
			// TODO Auto-generated method stub
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
				endTimer.setText("Time until start:\n" + days + "d" + hours
						+ ":" + min + ":" + sec);
			else
				endTimer.setText("Time until start:\n" + hours + ":" + min
						+ ":" + sec);
		}

		@Override
		public void onFinish() {
			// TODO Auto-generated method stub
			if (cdt != null)
				cdt.cancel();
			cdt = new MyCountDownTimer2(14400 * 1000, MainPage.interval);
			cdt.start();
		}
	}

	protected void goToSurveyMainPage() {
		RelativeLayout r1 = (RelativeLayout) findViewById(R.id.layout1);
		r1.removeAllViews();
		r1.addView(View.inflate(mContext, R.layout.survey_main, null));
		survey_done = (Button) findViewById(R.id.survey_done);
		new_party = (Button) findViewById(R.id.new_party);
		back = (Button) findViewById(R.id.back);

		survey_done.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(mContext)
						.setTitle("Warning")
						.setMessage("Are you sure to end this survey?")
						.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										goToEndSurveyPage();
										loadEndHeaderData();
									}
								})
						.setNegativeButton("No",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// User cancelled the dialog
									}
								}).create().show();
			}
		});

		new_party.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				goToNewPartyPage();
			}
		});

		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				RelativeLayout r1 = (RelativeLayout) findViewById(R.id.layout1);
				r1.removeAllViews();
				r1.addView(View.inflate(mContext, R.layout.header, null));
				try {
					goToHeaderPage();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

	}

	protected void goToHeaderPage() throws JSONException {

		vehicleAdd = (Button) findViewById(R.id.vehicle_add);
		vehicleSub = (Button) findViewById(R.id.vehicle_sub);

		cancel = (Button) findViewById(R.id.cancel);
		next = (Button) findViewById(R.id.next);

		vehicle_type = (RadioGroup) findViewById(R.id.vehicle_type);

		vehicleTotal = (EditText) findViewById(R.id.vehicle_total);
		endTimer = (TextView) findViewById(R.id.endtimer);

		weather = (Spinner) findViewById(R.id.weather);
		temp = (Spinner) findViewById(R.id.temp);
		precip = (Spinner) findViewById(R.id.precipitation);

		weather_choices = getResources().getStringArray(R.array.weather);
		temp_choices = getResources().getStringArray(R.array.temp);
		precip_choices = getResources().getStringArray(R.array.precip);

		weather_adapter = new ArrayAdapter<String>(this, R.layout.spinner_item,
				weather_choices) {
			@Override
			public int getCount() {
				int count = super.getCount();
				return count > 0 ? count - 1 : count;
			}
		};
		temp_adapter = new ArrayAdapter<String>(this, R.layout.spinner_item,
				temp_choices) {
			@Override
			public int getCount() {
				int count = super.getCount();
				return count > 0 ? count - 1 : count;
			}
		};
		precip_adapter = new ArrayAdapter<String>(this, R.layout.spinner_item,
				precip_choices) {
			@Override
			public int getCount() {
				int count = super.getCount();
				return count > 0 ? count - 1 : count;
			}
		};

		weather_adapter.setDropDownViewResource(R.layout.spinner_item);
		temp_adapter.setDropDownViewResource(R.layout.spinner_item);
		precip_adapter.setDropDownViewResource(R.layout.spinner_item);

		weather.setAdapter(weather_adapter);
		temp.setAdapter(temp_adapter);
		precip.setAdapter(precip_adapter);

		if (surveyHeader.isNull("weather_sky"))
			weather.setSelection(weather_adapter.getCount());
		else
			weather.setSelection(weather_adapter
					.getPosition((String) surveyHeader.get("weather_sky")));
		if (surveyHeader.isNull("temperature"))
			temp.setSelection(temp_adapter.getCount());
		else
			temp.setSelection(temp_adapter.getPosition((String) surveyHeader
					.get("temperature")));
		if (surveyHeader.isNull("precipitation"))
			precip.setSelection(precip_adapter.getCount());
		else
			precip.setSelection(precip_adapter
					.getPosition((String) surveyHeader.get("precipitation")));

		weather.setPrompt("Select one below");
		temp.setPrompt("Select one below");
		precip.setPrompt("Select one below");

		weather.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				wea = parent.getItemAtPosition(position).toString();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});

		temp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				tem = parent.getItemAtPosition(position).toString();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});

		precip.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				pre = parent.getItemAtPosition(position).toString();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});

		vehicleAdd.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (TextUtils.isEmpty(vehicleTotal.getText().toString())) {
					vehicleTotal.setText(String.valueOf(1));
				} else {
					vehicleTotal.setText(String.valueOf(Integer
							.parseInt(vehicleTotal.getText().toString()) + 1));
				}
			}
		});
		vehicleSub.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(mContext)
						.setTitle("Warning")
						.setMessage("Are you sure to subtract?")
						.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										if (TextUtils.isEmpty(vehicleTotal
												.getText().toString())) {
											vehicleTotal.setText(String
													.valueOf(0));
										} else if (Integer
												.parseInt(vehicleTotal
														.getText().toString()) > 0) {
											vehicleTotal.setText(String.valueOf(Integer
													.parseInt(vehicleTotal
															.getText()
															.toString()) - 1));
										} else {

										}
									}
								})
						.setNegativeButton("No",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// User cancelled the dialog
									}
								}).create().show();
			}
		});

		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(mContext)
						.setTitle("Warning")
						.setMessage("Are you sure?")
						.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// User cancelled the dialog
										finish();
									}
								})
						.setNegativeButton("No",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// User cancelled the dialog
									}
								}).create().show();
			}
		});

		next.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				nextButtonOnClick();
			}
		});

		if (MainPage.in_a_survey_period) {
			Bundle b = getIntent().getExtras();
			endtime = b.getLong("endTimer");
			if (endTimer.getText().length() == 0) {
				cdt = new MyCountDownTimer2(endtime * 1000, MainPage.interval);
				cdt.start();
			}
		} else {
			if (endTimer.getText().length() == 0) {
				cdt = new MyCountDownTimer3(MainPage.timeUntilStart * 1000,
						MainPage.interval);
				cdt.start();
			}
		}

	}

	protected void goToEndSurveyPage() {

		RelativeLayout r1 = (RelativeLayout) findViewById(R.id.layout1);
		r1.removeAllViews();
		r1.addView(View.inflate(mContext, R.layout.end_survey_header, null));

		cancel = (Button) findViewById(R.id.cancel);
		end_header_done = (Button) findViewById(R.id.done);

		time_percentage = (EditText) findViewById(R.id.percentage);

		weather = (Spinner) findViewById(R.id.weather);
		temp = (Spinner) findViewById(R.id.temp);
		precip = (Spinner) findViewById(R.id.precipitation);
		survey_comments = (EditText) findViewById(R.id.comments);
		fullPeriod = (CheckBox) findViewById(R.id.full_period);

		weather_choices = getResources().getStringArray(R.array.weather);
		temp_choices = getResources().getStringArray(R.array.temp);
		precip_choices = getResources().getStringArray(R.array.precip);

		weather_adapter = new ArrayAdapter<String>(this, R.layout.spinner_item,
				weather_choices) {
			@Override
			public int getCount() {
				int count = super.getCount();
				return count > 0 ? count - 1 : count;
			}
		};
		temp_adapter = new ArrayAdapter<String>(this, R.layout.spinner_item,
				temp_choices) {
			@Override
			public int getCount() {
				int count = super.getCount();
				return count > 0 ? count - 1 : count;
			}
		};
		precip_adapter = new ArrayAdapter<String>(this, R.layout.spinner_item,
				precip_choices) {
			@Override
			public int getCount() {
				int count = super.getCount();
				return count > 0 ? count - 1 : count;
			}
		};

		weather_adapter.setDropDownViewResource(R.layout.spinner_item);
		temp_adapter.setDropDownViewResource(R.layout.spinner_item);
		precip_adapter.setDropDownViewResource(R.layout.spinner_item);

		weather.setAdapter(weather_adapter);
		temp.setAdapter(temp_adapter);
		precip.setAdapter(precip_adapter);

		weather.setSelection(weather_adapter.getCount());
		temp.setSelection(temp_adapter.getCount());
		precip.setSelection(precip_adapter.getCount());

		weather.setPrompt("Select one below");
		temp.setPrompt("Select one below");
		precip.setPrompt("Select one below");

		weather.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				wea = parent.getItemAtPosition(position).toString();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});

		temp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				tem = parent.getItemAtPosition(position).toString();

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});

		precip.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				pre = parent.getItemAtPosition(position).toString();

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});

		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(mContext)
						.setTitle("Warning")
						.setMessage("Are you sure?")
						.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// User cancelled the dialog		
											goToSurveyMainPage();
									}
								})
						.setNegativeButton("No",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// User cancelled the dialog
									}
								}).create().show();

			}
		});

		end_header_done.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (checkSurveyHeaderInput()) {
					if (fullPeriod.isChecked()
							|| !time_percentage.getText().toString().equals("")) {
						saveEndHeaderData();
						SQLiteDatabase db = MainPage.myDB.getWritableDatabase();
						if (!MainPage.practice) {
							try {
								db.execSQL("UPDATE "
										+ DatabaseHelper.table_schedule
										+ " SET done = 1 WHERE ScheduleUID = \""
										+ scheduleUID + "\"");
							} catch (SQLException e) {
								Log.d("update",
										"failed to update table_schedule");
							}
						}
						db.close();
						finish();
					} else {
						new AlertDialog.Builder(mContext)
								.setTitle("Warning")
								.setMessage(
										"Did you do the full period?")
								.setNegativeButton("OK",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												// User cancelled the dialog
											}
										}).create().show();
					}
				}
			}
		});

	}

	protected void goToNewPartyPage() {
		RelativeLayout r1 = (RelativeLayout) findViewById(R.id.layout1);
		r1.removeAllViews();
		r1.addView(View.inflate(mContext, R.layout.new_party, null));

		new_interview = (Button) findViewById(R.id.new_interview);
		party_decline = (Button) findViewById(R.id.party_decline);
		newParty_cancel = (Button) findViewById(R.id.newparty_cancel);
		num_in_party = (EditText) findViewById(R.id.num_in_party);
		tv_party = (TextView) findViewById(R.id.textView1);
		tv_partyType = (TextView) findViewById(R.id.textView3);
		vehicle_type = (RadioGroup) findViewById(R.id.vehicle_type);

		is_driver = false;

		vehicle_type.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
				// TODO Auto-generated method stub
				partyType = ((RadioButton) findViewById(vehicle_type
						.getCheckedRadioButtonId())).getText().toString();
				if (partyType.equals("bus/van")) {
					new_interview.setText("Done");
				} else {
					new_interview.setText("First Person");
				}
			}

		});

		partyUID = Utilities.generateUID();

		party_decline.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (checkPartyHeaderInput()) {

					num_of_people_in_party = Integer.parseInt(num_in_party
							.getText().toString());
					try {
						party.put("PartyUID", partyUID);
						party.put("SurveyDataUID", surveyDataUID);
						party.put("partytype", partyType);
						party.put("partysize", num_of_people_in_party);
						party.put("interviewstatus", "Declined");
						party.put("entrydate", Utilities.generateDateTag());
						party.put("lastsave", Utilities.generateDateTag());
						if (mLocation != null)
							party.put(
									"RecordGPS",
									Double.toString(mLocation.getLatitude())
											+ ","
											+ Double.toString(mLocation
													.getLongitude()));
						party.put("uploaded", 0);
						if (MainPage.myDB.insert_party_into_table(party))
							Log.d("Database",
									"party info inserted successfully");
						else
							Log.d("Database",
									"party info inserted successfully");
						clearPartyData();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					goToSurveyMainPage();
				}
			}
		});

		newParty_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(mContext)
						.setTitle("Warning")
						.setMessage("Are you sure?")
						.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// User cancelled the dialog
										goToSurveyMainPage();
									}
								})
						.setNegativeButton("No",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// User cancelled the dialog
									}
								}).create().show();
			}
		});

		new_interview.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (checkPartyHeaderInput()) {

					num_of_people_in_party = Integer.parseInt(num_in_party
							.getText().toString());
					if (partyType.equals("bus/van")) {
						saveNewPartyData();
						try {
							party.put("interviewstatus", "done");
							party.put("lastsave", Utilities.generateDateTag());
							if (mLocation != null)
								party.put(
										"RecordGPS",
										Double.toString(mLocation.getLatitude())
												+ ","
												+ Double.toString(mLocation
														.getLongitude()));
						} catch (JSONException e) {
							// TODO Auto-generated catch
							// block
							e.printStackTrace();
						}
						if (MainPage.myDB.insert_party_into_table(party))
							Log.d("Database",
									"party info inserted successfully");
						else
							Log.d("Database",
									"party info inserted successfully");
						clearPartyData();
						goToSurveyMainPage();
					} else if (num_of_people_in_party > 8
							&& num_of_people_in_party <= 50) {
						new AlertDialog.Builder(mContext)
								.setTitle("Warning")
								.setMessage(
										"Are you sure there are more than 8 people?")
								.setPositiveButton("Yes",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												saveNewPartyData();
												goToNewInterviewPage1();
												loadPage1Data();
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

					} else if (num_of_people_in_party > 50) {
						new AlertDialog.Builder(mContext)
								.setTitle("Warning")
								.setMessage(
										"Are you sure there are more than 50 people?")
								.setPositiveButton("Yes",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												saveNewPartyData();
												goToNewInterviewPage1();
												loadPage1Data();
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
					} else {
						saveNewPartyData();
						goToNewInterviewPage1();
						loadPage1Data();
					}
				}
			}
		});
	}

	protected void goToNewInterviewPage1() {
		// TODO Auto-generated method stub
		RelativeLayout r1 = (RelativeLayout) findViewById(R.id.layout1);
		r1.removeAllViews();
		r1.addView(View.inflate(mContext, R.layout.new_interview_page1, null));

		newInterview_cancel = (Button) findViewById(R.id.interview_cancel);
		next_interview_page = (Button) findViewById(R.id.next_page);
		interview_refused = (Button) findViewById(R.id.refuse);
		ct_interviewee = (TextView) findViewById(R.id.interviewee_counter);
		zipcode = (EditText) findViewById(R.id.zip);
		spent_hrs = (EditText) findViewById(R.id.spent_hour);
		spent_mins = (EditText) findViewById(R.id.spent_min);
		visits = (EditText) findViewById(R.id.visit_per_year);
		gender = (RadioGroup) findViewById(R.id.gender);
		age = (RadioGroup) findViewById(R.id.age);
		before = (CheckBox) findViewById(R.id.interviewed_before);
		firsttime = (CheckBox) findViewById(R.id.first_time_visit);
		driver = (CheckBox) findViewById(R.id.driver);
		tv_time_spent = (TextView) findViewById(R.id.textView2);
		tv_zipcode = (TextView) findViewById(R.id.textView1);
		tv_visits = (TextView) findViewById(R.id.textView5);

		s_gender = "";
		s_age = "";

		ct_interviewee.setText("Interview "
				+ String.valueOf(interviewee_counter) + " of "
				+ String.valueOf(num_of_people_in_party) + " in party");

		gender.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
				// TODO Auto-generated method stub
				s_gender = ((RadioButton) findViewById(gender
						.getCheckedRadioButtonId())).getText().toString();
			}

		});

		age.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
				// TODO Auto-generated method stub
				s_age = ((RadioButton) findViewById(age
						.getCheckedRadioButtonId())).getText().toString();
			}

		});

		newInterview_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(mContext)
						.setTitle("Warning")
						.setMessage("Are you sure?")
						.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										interviewee_counter = 0;
										goToNewPartyPage();
									}
								})
						.setNegativeButton("No",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// User cancelled the dialog
									}
								}).create().show();
			}
		});

		next_interview_page.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				spent_hour = spent_hrs.getText().toString();
				spent_min = spent_mins.getText().toString();
				if (page1InputCheck()) {
					savePage1Data();
					if (partyType.equals("motorcycle") && s_age.equals("Child")) {
						new AlertDialog.Builder(mContext)
								.setTitle("Warning")
								.setMessage(
										"Are you sure? Vehicle Type: motorcycle. Age: child.")
								.setPositiveButton("Yes",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												b1 = true;
												if (b2) {
													goToNewInterviewPage2();
													loadPage2Data();
												}
											}
										})
								.setNegativeButton("No",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												// User cancelled the dialog
												b1 = false;
											}
										}).create().show();
					} else {
						b1 = true;
					}

					if (!spent_hour.equals("")) {
						if (Integer.parseInt(spent_hour) >= 12) {
							new AlertDialog.Builder(mContext)
									.setTitle("Warning")
									.setMessage(
											"Are you sure they have spent more than 12 hours at the range?")
									.setPositiveButton(
											"Yes",
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int id) {
													b2 = true;
													if (b1) {
														goToNewInterviewPage2();
														loadPage2Data();
													}
												}
											})
									.setNegativeButton(
											"No",
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int id) {
													// User cancelled the dialog
													b2 = false;
												}
											}).create().show();
						} else {
							b2 = true;
						}
					} else {
						b2 = true;
					}
					if (b1 && b2) {
						goToNewInterviewPage2();
						loadPage2Data();
					}
				}
			}
		});

		interview_refused.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				savePage1Data();
				try {
					interview.put("interviewstatus", "Refused");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				refuseDialogGenerator();
			}
		});

	}

	protected void goToNewInterviewPage2() {
		// TODO Auto-generated method stub
		RelativeLayout r1 = (RelativeLayout) findViewById(R.id.layout1);
		r1.removeAllViews();
		r1.addView(View.inflate(mContext, R.layout.new_interview_page2, null));

		interview_refused = (Button) findViewById(R.id.refuse);
		next_interview_page = (Button) findViewById(R.id.next_page);
		prev_page = (Button) findViewById(R.id.prev_page);
		ct_interviewee = (TextView) findViewById(R.id.interviewee_counter);
		hunter = (RadioGroup) findViewById(R.id.hunter);
		rifle = (CheckBox) findViewById(R.id.rifle);
		shotgun = (CheckBox) findViewById(R.id.shotgun);
		sighting = (CheckBox) findViewById(R.id.sighting);
		pattern = (CheckBox) findViewById(R.id.pattern);
		handgun = (CheckBox) findViewById(R.id.handgun);
		msr = (CheckBox) findViewById(R.id.msr);
		muzzleloader = (CheckBox) findViewById(R.id.muzzleloader);
		self_defense = (CheckBox) findViewById(R.id.self_defense);
		archery = (CheckBox) findViewById(R.id.archery);
		other = (CheckBox) findViewById(R.id.other);
		noshoot = (CheckBox) findViewById(R.id.noshoot);
		observing = (CheckBox) findViewById(R.id.observing);

		ct_interviewee.setText("Interview "
				+ String.valueOf(interviewee_counter) + " of "
				+ String.valueOf(num_of_people_in_party) + " in party");

		interview_refused.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				savePage2Data();
				try {
					interview.put("interviewstatus", "Refused");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				refuseDialogGenerator();
			}
		});

		next_interview_page.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				savePage2Data();
				goToNewInterviewPage3();
				loadPage3Data();
			}
		});

		prev_page.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				savePage2Data();
				goToNewInterviewPage1();
				loadPage1Data();
				b1 = false;
				b2 = false;
			}
		});
	}

	protected void goToNewInterviewPage3() {
		// TODO Auto-generated method stub
		RelativeLayout r1 = (RelativeLayout) findViewById(R.id.layout1);
		r1.removeAllViews();
		r1.addView(View.inflate(mContext, R.layout.new_interview_page3, null));

		interview_refused = (Button) findViewById(R.id.refuse);
		next_interview_page = (Button) findViewById(R.id.next_page);
		prev_page = (Button) findViewById(R.id.prev_page);
		how_often = (Spinner) findViewById(R.id.how_often);
		kept_away = (RadioGroup) findViewById(R.id.kept_away);
		vandalism = (CheckBox) findViewById(R.id.vandalism);
		downrange = (CheckBox) findViewById(R.id.downrange);
		drunk = (CheckBox) findViewById(R.id.drunk);
		auto_fire = (CheckBox) findViewById(R.id.auto_fire);
		move_downrange = (CheckBox) findViewById(R.id.move_downrange);
		unap_tar = (CheckBox) findViewById(R.id.unap_tar);
		ex_tar = (CheckBox) findViewById(R.id.ex_tar);
		other_compliance = (CheckBox) findViewById(R.id.other);
		litter = (CheckBox) findViewById(R.id.litter);

		how_often_choices = getResources().getStringArray(R.array.how_often);

		how_often_adapter = new ArrayAdapter<String>(this,
				R.layout.spinner_item, how_often_choices) {
			@Override
			public int getCount() {
				int count = super.getCount();
				return count > 0 ? count - 1 : count;
			}
		};

		how_often_adapter.setDropDownViewResource(R.layout.spinner_item);
		how_often.setAdapter(how_often_adapter);
		how_often.setSelection(how_often_adapter.getCount());
		how_often.setPrompt("Select one below");
		how_often
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						// TODO Auto-generated method stub
						comp = parent.getItemAtPosition(position).toString();
						if (comp.equals("Never")) {
							vandalism.setEnabled(false);
							vandalism.setChecked(false);
							downrange.setEnabled(false);
							downrange.setChecked(false);
							drunk.setEnabled(false);
							drunk.setChecked(false);
							auto_fire.setEnabled(false);
							auto_fire.setChecked(false);
							move_downrange.setEnabled(false);
							move_downrange.setChecked(false);
							unap_tar.setEnabled(false);
							unap_tar.setChecked(false);
							ex_tar.setEnabled(false);
							ex_tar.setChecked(false);
							other_compliance.setEnabled(false);
							other_compliance.setChecked(false);
							litter.setEnabled(false);
							litter.setChecked(false);
							kept_away.getChildAt(0).setEnabled(false);
							kept_away.getChildAt(1).setEnabled(false);
							kept_away.clearCheck();
						} else {
							vandalism.setEnabled(true);
							downrange.setEnabled(true);
							drunk.setEnabled(true);
							auto_fire.setEnabled(true);
							move_downrange.setEnabled(true);
							unap_tar.setEnabled(true);
							ex_tar.setEnabled(true);
							other_compliance.setEnabled(true);
							litter.setEnabled(true);
							kept_away.getChildAt(0).setEnabled(true);
							kept_away.getChildAt(1).setEnabled(true);
						}

					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						// TODO Auto-generated method stub

					}
				});
		ct_interviewee = (TextView) findViewById(R.id.interviewee_counter);

		ct_interviewee.setText("Interview "
				+ String.valueOf(interviewee_counter) + " of "
				+ String.valueOf(num_of_people_in_party) + " in party");

		interview_refused.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				savePage3Data();
				try {
					interview.put("interviewstatus", "Refused");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				refuseDialogGenerator();
			}
		});

		next_interview_page.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				savePage3Data();
				goToNewInterviewPage4();
				loadPage4Data();
			}
		});

		prev_page.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				savePage3Data();
				goToNewInterviewPage2();
				loadPage2Data();
			}
		});
	}

	protected void goToNewInterviewPage4() {
		// TODO Auto-generated method stub
		RelativeLayout r1 = (RelativeLayout) findViewById(R.id.layout1);
		r1.removeAllViews();
		r1.addView(View.inflate(mContext, R.layout.new_interview_page4, null));

		next_interview = (Button) findViewById(R.id.next_interview);
		done_interviews = (Button) findViewById(R.id.done);
		prev_page = (Button) findViewById(R.id.prev_page);
		ct_interviewee = (TextView) findViewById(R.id.interviewee_counter);
		comments = (EditText) findViewById(R.id.comments);
		notify_manager = (CheckBox) findViewById(R.id.emergency);

		ct_interviewee.setText("Interview "
				+ String.valueOf(interviewee_counter) + " of "
				+ String.valueOf(num_of_people_in_party) + " in party");

		if (interviewee_counter == num_of_people_in_party) {
			next_interview.setEnabled(false);
		} else {
			next_interview.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					interviewee_counter++;
					savePage4Data();
					interviewTag();
					try {
						interview.put("interviewstatus", "done");
						if (interview.getInt("driver") == 1)
							is_driver = true;
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (MainPage.myDB.insert_interview_into_table(interview))
						Log.d("Database",
								"interview info inserted successfully");
					else
						Log.d("Database",
								"interview info inserted successfully");
					clearInterviewData();
					goToNewInterviewPage1();
					loadPage1Data();
				}
			});
		}

		done_interviews.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (interviewee_counter < num_of_people_in_party) {
					new AlertDialog.Builder(mContext)
							.setTitle("Warning")
							.setMessage(
									"Are you sure you're done with this party?")
							.setPositiveButton("Yes",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											interviewee_counter = 0;
											savePage4Data();
											interviewTag();
											try {
												interview.put(
														"interviewstatus",
														"done");
											} catch (JSONException e) {
												// TODO Auto-generated catch
												// block
												e.printStackTrace();
											}
											if (MainPage.myDB
													.insert_interview_into_table(interview))
												Log.d("Database",
														"interview info inserted successfully");
											else
												Log.d("Database",
														"interview info inserted successfully");
											clearInterviewData();
											try {
												interview.put("zipcode", null);
												interview
														.put("timespent", null);
												party.put("interviewstatus",
														"done");
												party.put(
														"lastsave",
														Utilities
																.generateDateTag());
												if (mLocation != null)
													party.put(
															"RecordGPS",
															Double.toString(mLocation
																	.getLatitude())
																	+ ","
																	+ Double.toString(mLocation
																			.getLongitude()));
											} catch (JSONException e) {
												// TODO Auto-generated catch
												// block
												e.printStackTrace();
											}
											if (MainPage.myDB
													.insert_party_into_table(party))
												Log.d("Database",
														"party info inserted successfully");
											else
												Log.d("Database",
														"party info inserted successfully");
											clearPartyData();
											goToSurveyMainPage();
										}
									})
							.setNegativeButton("No",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											// User cancelled the dialog
										}
									}).create().show();
				} else {
					interviewee_counter = 0;
					savePage4Data();
					interviewTag();
					try {
						interview.put("interviewstatus", "done");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (MainPage.myDB.insert_interview_into_table(interview))
						Log.d("Database",
								"interview info inserted successfully");
					else
						Log.d("Database",
								"interview info inserted successfully");
					clearInterviewData();
					try {
						interview.put("zipcode", null);
						interview.put("timespent", null);
						party.put("interviewstatus", "done");
						party.put("lastsave", Utilities.generateDateTag());
						if (mLocation != null)
							party.put(
									"RecordGPS",
									Double.toString(mLocation.getLatitude())
											+ ","
											+ Double.toString(mLocation
													.getLongitude()));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (MainPage.myDB.insert_party_into_table(party))
						Log.d("Database", "party info inserted successfully");
					else
						Log.d("Database", "party info inserted successfully");
					clearPartyData();
					goToSurveyMainPage();
				}
			}
		});

		prev_page.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				savePage4Data();
				goToNewInterviewPage3();
				loadPage3Data();
			}
		});
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (cdt != null)
			cdt.cancel();
		mLocationManager.removeUpdates(listener);
		super.onDestroy();
	}

	public void refuseDialogGenerator() {
		new AlertDialog.Builder(mContext)
				.setTitle("Warning")
				.setMessage("Are you sure?")
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								if (interviewee_counter < num_of_people_in_party) {
									interviewee_counter++;
									interviewTag();
									if (MainPage.myDB
											.insert_interview_into_table(interview))
										Log.d("Database",
												"interview info inserted successfully");
									else
										Log.d("Database",
												"interview info inserted successfully");
									clearInterviewData();
									goToNewInterviewPage1();
								} else {
									interviewee_counter = 0;
									if (MainPage.myDB
											.insert_interview_into_table(interview))
										Log.d("Database",
												"interview info inserted successfully");
									else
										Log.d("Database",
												"interview info inserted successfully");
									clearInterviewData();
									try {
										interview.put("zipcode", null);
										interview.put("timespent", null);
										party.put("interviewstatus", "done");
										party.put("lastsave",
												Utilities.generateDateTag());
										if (mLocation != null)
											party.put(
													"RecordGPS",
													Double.toString(mLocation
															.getLatitude())
															+ ","
															+ Double.toString(mLocation
																	.getLongitude()));
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									if (MainPage.myDB
											.insert_party_into_table(party))
										Log.d("Database",
												"party info inserted successfully");
									else
										Log.d("Database",
												"party info inserted successfully");
									clearPartyData();
									goToSurveyMainPage();
								}
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// User cancelled the dialog
					}
				}).create().show();
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

	public boolean checkSurveyHeaderInput() {
		if (weather.getSelectedItem().toString().equals("Select One")
				|| temp.getSelectedItem().toString().equals("Select One")
				|| precip.getSelectedItem().toString().equals("Select One")) {
			new AlertDialog.Builder(mContext)
					.setTitle("Warning")
					.setMessage(
							"One or more fields missing.")
					.setNegativeButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// User cancelled the dialog
								}
							}).create().show();
			return false;
		}
		if (weather.getSelectedItem().toString().equals("Sunny")
				&& !precip.getSelectedItem().toString().equals("None")) {
			new AlertDialog.Builder(mContext)
					.setTitle("Warning")
					.setMessage(
							"Weather error: Weather: sunny; Precipitation: not None.")
					.setNegativeButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// User cancelled the dialog
								}
							}).create().show();
			return false;
		}
		return true;
	}

	public void nextButtonOnClick() {
		if (checkSurveyHeaderInput()) {
			try {
				surveyHeader.put("weather_sky", wea);
				surveyHeader.put("temperature", tem);
				surveyHeader.put("precipitation", pre);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			goToSurveyMainPage();
		}
	}

	public boolean checkPartyHeaderInput() {
		if (vehicle_type.getCheckedRadioButtonId() == -1) {
			new AlertDialog.Builder(mContext)
					.setTitle("Warning")
					.setMessage("Please select a party type.")
					.setNegativeButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// User cancelled the dialog
									tv_partyType.setTextColor(Color.RED);
								}
							}).create().show();
			return false;
		} else if (num_in_party.getText().toString().equals("")) {
			new AlertDialog.Builder(mContext)
					.setTitle("Warning")
					.setMessage(
							"Please enter the number of people in the party.")
					.setNegativeButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// User cancelled the dialog
									tv_partyType.setTextColor(Color.BLACK);
									tv_party.setTextColor(Color.RED);
								}
							}).create().show();
			return false;
		} else if (Integer.parseInt(num_in_party.getText().toString()) <= 0) {
			new AlertDialog.Builder(mContext)
					.setTitle("Warning")
					.setMessage(
							"Please enter a positive number of people.")
					.setNegativeButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// User cancelled the dialog
									tv_party.setTextColor(Color.RED);
								}
							}).create().show();
			return false;
		}
		return true;

	}

	protected void savePage1Data() {

		interviewUID = Utilities.generateUID();

		try {
			interview.put("InterviewUID", interviewUID);
			interview.put("PartyUID", partyUID);
			interview.put("entrydate", Utilities.generateDateTag());
			if (!zipcode.getText().toString().equals(""))
				interview.put("zipcode", zipcode.getText().toString());
			if (spent_hour.equals("") && !spent_min.equals(""))
				interview
						.put("timespent", Double.parseDouble(spent_min) / 60.0);
			else if (!spent_hour.equals("") && spent_min.equals(""))
				interview.put("timespent", Double.parseDouble(spent_hour));
			else if (!spent_hour.equals("") && !spent_min.equals(""))
				interview.put("timespent", Double.parseDouble(spent_hour)
						+ Double.parseDouble(spent_min) / 60.0);
			if (!visits.getText().toString().equals(""))
				interview.put("visits",
						Integer.parseInt(visits.getText().toString()));
			if (gender.getCheckedRadioButtonId() != -1)
				interview.put("gender", s_gender);
			if (age.getCheckedRadioButtonId() != -1)
				interview.put("adult", s_age);
			interview.put("interviewedBefore", before.isChecked() ? 1 : 0);
			interview.put("firsttime", firsttime.isChecked() ? 1 : 0);
			interview.put("driver", driver.isChecked() ? 1 : 0);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void loadPage1Data() {
		try {
			if (!interview.isNull("zipcode"))
				zipcode.setText(interview.getString("zipcode"));
			if (!interview.isNull("timespent")) {
				time_spent = interview.getDouble("timespent");
				if (time_spent >= 1) {
					spent_hrs.setText(Long.toString((long) time_spent));
					time_min = (int) ((time_spent - (long) time_spent) * 60);
					spent_mins.setText(Integer.toString(time_min));
				} else {
					time_min = (int) (time_spent * 60);
					spent_mins.setText(Integer.toString(time_min));
				}
			}
			if (!interview.isNull("visits"))
				visits.setText(Integer.toString(interview.getInt("visits")));
			if (!interview.isNull("gender")) {
				re_gender = interview.getString("gender");
				if (re_gender.equals("Male"))
					gender.check(R.id.male);
				else if (re_gender.equals("Female"))
					gender.check(R.id.female);
			}
			if (!interview.isNull("adult")) {
				re_age = interview.getString("adult");
				if (re_age.equals("Adult"))
					age.check(R.id.adult);
				else if (re_age.equals("Child"))
					age.check(R.id.child);
			}
			if (!interview.isNull("interviewedBefore")) {
				if (interview.getInt("interviewedBefore") == 1) {
					before.toggle();
					firsttime.setEnabled(false);
				}
			}
			if (!interview.isNull("firsttime")) {
				if (interview.getInt("firsttime") == 1) {
					firsttime.toggle();
					before.setEnabled(false);
				}
			}
			if (!interview.isNull("driver")) {
				if (interview.getInt("driver") == 1)
					driver.toggle();
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void loadPage2Data() {
		try {
			if (!interview.isNull("hunter")) {
				if (interview.getInt("hunter") == 1)
					hunter.check(R.id.yes);
				else if (interview.getInt("hunter") == 0)
					hunter.check(R.id.no);
			}
			if (!interview.isNull("shootingtype")) {
				if (!interview.getString("shootingtype").equals("")) {
					String[] types = interview.getString("shootingtype").split(
							",");
					for (int i = 0; i < types.length; i++) {
						if (types[i].equals("rifle"))
							rifle.toggle();
						if (types[i].equals("shotgun"))
							shotgun.toggle();
						if (types[i].equals("sighting"))
							sighting.toggle();
						if (types[i].equals("pattern"))
							pattern.toggle();
						if (types[i].equals("handgun"))
							handgun.toggle();
						if (types[i].equals("msr"))
							msr.toggle();
						if (types[i].equals("muzzleloader"))
							muzzleloader.toggle();
						if (types[i].equals("self_defense"))
							self_defense.toggle();
						if (types[i].equals("archery"))
							archery.toggle();
						if (types[i].equals("other"))
							other.toggle();
						if (types[i].equals("noshoot"))
							noshoot.toggle();
						if (types[i].equals("observing"))
							observing.toggle();
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void savePage2Data() {
		try {
			if (hunter.getCheckedRadioButtonId() == R.id.yes)
				interview.put("hunter", 1);
			else if (hunter.getCheckedRadioButtonId() == R.id.no)
				interview.put("hunter", 0);
			StringBuffer shoot_type = new StringBuffer("");
			if (rifle.isChecked())
				shoot_type.append("rifle,");
			if (shotgun.isChecked())
				shoot_type.append("shotgun,");
			if (sighting.isChecked())
				shoot_type.append("sighting,");
			if (pattern.isChecked())
				shoot_type.append("pattern,");
			if (handgun.isChecked())
				shoot_type.append("handgun,");
			if (msr.isChecked())
				shoot_type.append("msr,");
			if (muzzleloader.isChecked())
				shoot_type.append("muzzleloader,");
			if (self_defense.isChecked())
				shoot_type.append("self_defense,");
			if (archery.isChecked())
				shoot_type.append("archery,");
			if (other.isChecked())
				shoot_type.append("other,");
			if (noshoot.isChecked())
				shoot_type.append("noshoot,");
			if (observing.isChecked())
				shoot_type.append("observing,");
			interview.put("shootingtype", shoot_type.toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void loadPage3Data() {

		try {
			if (interview.isNull("compliance"))
				how_often.setSelection(how_often_adapter.getCount());
			else
				how_often.setSelection(how_often_adapter
						.getPosition((String) interview.get("compliance")));
			if (!interview.isNull("compliance_keptaway")) {
				if (interview.getInt("compliance_keptaway") == 1)
					kept_away.check(R.id.yes);
				else if (interview.getInt("compliance_keptaway") == 0)
					kept_away.check(R.id.no);
			}
			if (!interview.isNull("compliance_type")) {
				if (!interview.getString("compliance_type").equals("")) {
					String[] types = interview.getString("compliance_type")
							.split(",");
					for (int i = 0; i < types.length; i++) {
						if (types[i].equals("litter"))
							litter.toggle();
						if (types[i].equals("vandalism"))
							vandalism.toggle();
						if (types[i].equals("downrange"))
							downrange.toggle();
						if (types[i].equals("drunk"))
							drunk.toggle();
						if (types[i].equals("auto_fire"))
							auto_fire.toggle();
						if (types[i].equals("move_downrange"))
							move_downrange.toggle();
						if (types[i].equals("unap_tar"))
							unap_tar.toggle();
						if (types[i].equals("ex_tar"))
							ex_tar.toggle();
						if (types[i].equals("other"))
							other_compliance.toggle();
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	protected void savePage3Data() {
		try {
			if (how_often.getSelectedItemPosition() != how_often.getCount())
				interview.put("compliance", comp);
			StringBuffer compliance_type = new StringBuffer("");
			if (litter.isChecked())
				compliance_type.append("litter,");
			if (vandalism.isChecked())
				compliance_type.append("vandalism,");
			if (downrange.isChecked())
				compliance_type.append("downrange,");
			if (drunk.isChecked())
				compliance_type.append("drunk,");
			if (auto_fire.isChecked())
				compliance_type.append("auto_fire,");
			if (move_downrange.isChecked())
				compliance_type.append("move_downrange,");
			if (unap_tar.isChecked())
				compliance_type.append("unap_tar,");
			if (ex_tar.isChecked())
				compliance_type.append("ex_tar,");
			if (other_compliance.isChecked())
				compliance_type.append("other,");
			interview.put("compliance_type", compliance_type.toString());
			if (kept_away.getCheckedRadioButtonId() == R.id.yes)
				interview.put("compliance_keptaway", 1);
			else if (kept_away.getCheckedRadioButtonId() == R.id.no)
				interview.put("compliance_keptaway", 0);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	protected void loadPage4Data() {
		try {
			if (!interview.isNull("comments")) {
				comments.setText(interview.getString("comments"));
			}
			if (!interview.isNull("alert")) {
				if (interview.getInt("alert") == 1)
					notify_manager.setChecked(true);
				else if (interview.getInt("alert") == 0)
					notify_manager.setChecked(false);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void savePage4Data() {

		try {
			interview.put("comments", comments.getText().toString());
			interview.put("alert", notify_manager.isChecked() ? 1 : 0);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void loadEndHeaderData() {
		try {
			if (surveyHeader.isNull("weather_sky_end"))
				weather.setSelection(weather_adapter.getCount());
			else
				weather.setSelection(weather_adapter
						.getPosition((String) surveyHeader.get("weather_sky_end")));
			if (surveyHeader.isNull("temperature_end"))
				temp.setSelection(temp_adapter.getCount());
			else
				temp.setSelection(temp_adapter
						.getPosition((String) surveyHeader
								.get("temperature_end")));
			if (surveyHeader.isNull("precipitation_end"))
				precip.setSelection(precip_adapter.getCount());
			else
				precip.setSelection(precip_adapter
						.getPosition((String) surveyHeader
								.get("precipitation_end")));
			if (!surveyHeader.isNull("comments"))
				survey_comments.setText(surveyHeader.getString("comments"));
			if (!surveyHeader.isNull("period_pct"))
				time_percentage
						.setText(surveyHeader.getString("period_pct"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void saveEndHeaderData() {

		if (checkSurveyHeaderInput()) {
			try {
				surveyHeader.put("weather_sky_end", wea);
				surveyHeader.put("temperature_end", tem);
				surveyHeader.put("precipitation_end", pre);
				surveyHeader.put("comments", survey_comments.getText()
						.toString());
				if (fullPeriod.isChecked())
					surveyHeader.put("fullperiod", 1);
				else {
					surveyHeader.put("fullperiod", 0);
					if (!time_percentage.getText().toString().equals(""))
						surveyHeader
								.put("period_pct", Integer
										.parseInt(time_percentage.getText()
												.toString()));
				}
				surveyHeader.put("Partycount",
						Integer.parseInt(vehicleTotal.getText().toString()));
				surveyHeader.put("lastsave", Utilities.generateDateTag());
				if (MainPage.practice)
					surveyHeader.put("mode", "practice");
				else
					surveyHeader.put("mode", "done");
				if (mLocation != null)
					surveyHeader
							.put("RecordGPS",
									Double.toString(mLocation.getLatitude())
											+ ","
											+ Double.toString(mLocation
													.getLongitude()));
				if (MainPage.myDB.insert_surveyheader_into_table(surveyHeader))
					Log.d("Database", "header info inserted successfully");
				else
					Log.d("Database", "header info insertion failed");

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void onCheckboxClicked(View view) {

		switch (view.getId()) {
		case R.id.first_time_visit:
			if (((CheckBox) view).isChecked()) {
				before.setEnabled(false);
				if (Integer.parseInt(visits.getText().toString()) > 1) {
					new AlertDialog.Builder(mContext)
							.setTitle("Warning")
							.setMessage(
									"Are you sure this is the first time visit?")
							.setPositiveButton("Yes",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											// User cancelled the dialog
											visits.setText("1");
										}
									})
							.setNegativeButton("No",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											// User cancelled the dialog
											firsttime.setChecked(false);
										}
									}).create().show();
				}
			} else {
				before.setEnabled(true);
			}
			break;
		case R.id.interviewed_before:
			if (((CheckBox) view).isChecked()) {
				firsttime.setEnabled(false);
			} else {
				firsttime.setEnabled(true);
			}
			break;
		case R.id.full_period:
			if (((CheckBox) view).isChecked()) {
				time_percentage.setEnabled(false);
			} else {
				time_percentage.setEnabled(true);
			}
			break;
		case R.id.driver:
			if (((CheckBox) view).isChecked()) {
				if (is_driver) {
					new AlertDialog.Builder(mContext)
							.setTitle("Warning")
							.setMessage(
									"Are you sure there is more than one driver?")
							.setPositiveButton("Yes",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											// User cancelled the dialog
										}
									})
							.setNegativeButton("No",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											// User cancelled the dialog
											driver.setChecked(false);
										}
									}).create().show();
				}
			}
			break;
		case R.id.noshoot:
			if (((CheckBox) view).isChecked()) {
				rifle.setEnabled(false);
				rifle.setChecked(false);
				shotgun.setEnabled(false);
				shotgun.setChecked(false);
				sighting.setEnabled(false);
				sighting.setChecked(false);
				pattern.setEnabled(false);
				pattern.setChecked(false);
				handgun.setEnabled(false);
				handgun.setChecked(false);
				msr.setEnabled(false);
				msr.setChecked(false);
				muzzleloader.setEnabled(false);
				muzzleloader.setChecked(false);
				self_defense.setEnabled(false);
				self_defense.setChecked(false);
				archery.setEnabled(false);
				archery.setChecked(false);
				observing.setEnabled(false);
				observing.setChecked(false);
				other.setEnabled(false);
				other.setChecked(false);
			} else {
				rifle.setEnabled(true);
				shotgun.setEnabled(true);
				sighting.setEnabled(true);
				pattern.setEnabled(true);
				handgun.setEnabled(true);
				msr.setEnabled(true);
				muzzleloader.setEnabled(true);
				self_defense.setEnabled(true);
				archery.setEnabled(true);
				observing.setEnabled(true);
				other.setEnabled(true);
			}
			break;
		}

	}

	protected boolean page1InputCheck() {
		if (zipcode.getText().toString().length() != 5
				&& !zipcode.getText().toString().equals("")) {
			new AlertDialog.Builder(mContext)
					.setTitle("Warning")
					.setMessage("Invalid zip code.")
					.setNegativeButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// User cancelled the dialog
									tv_zipcode.setTextColor(Color.RED);
								}
							}).create().show();
			return false;
		} else
			tv_zipcode.setTextColor(Color.BLACK);
		if (!spent_hour.equals("")) {
			if (Integer.parseInt(spent_hour) >= 24
					|| (Integer.parseInt(spent_hour) < 0)) {
				new AlertDialog.Builder(mContext)
						.setTitle("Warning")
						.setMessage("Hours must be between 0 and 24.")
						.setNegativeButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// User cancelled the dialog
										tv_time_spent.setTextColor(Color.RED);
									}
								}).create().show();
				return false;
			}
		} else
			tv_time_spent.setTextColor(Color.BLACK);
		if (!spent_min.equals("")) {
			if (Integer.parseInt(spent_min) >= 60
					|| (Integer.parseInt(spent_min) <= 0 && (spent_hour
							.equals("") || Integer.parseInt(spent_hour) <= 0))) {
				new AlertDialog.Builder(mContext)
						.setTitle("Warning")
						.setMessage("Minutes must be between 0 and 60.")
						.setNegativeButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// User cancelled the dialog
										tv_time_spent.setTextColor(Color.RED);
									}
								}).create().show();
				return false;
			}
		} else
			tv_time_spent.setTextColor(Color.BLACK);
		if (!visits.getText().toString().equals("")) {
			if (Integer.parseInt(visits.getText().toString()) > 365) {
				new AlertDialog.Builder(mContext)
						.setTitle("Warning")
						.setMessage("Visits per year cannot be more than 365.")
						.setNegativeButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// User cancelled the dialog
										tv_visits.setTextColor(Color.RED);
									}
								}).create().show();
				return false;
			} else if (Integer.parseInt(visits.getText().toString()) <= 0) {
				new AlertDialog.Builder(mContext)
						.setTitle("Warning")
						.setMessage("Visits per year has to be greater than 0.")
						.setNegativeButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// User cancelled the dialog
										tv_visits.setTextColor(Color.RED);
									}
								}).create().show();
				return false;
			} else if (Integer.parseInt(visits.getText().toString()) > 1
					&& firsttime.isChecked()) {
				new AlertDialog.Builder(mContext)
						.setTitle("Warning")
						.setMessage(
								"Cannot be first time visit if visits per year is greater than 1.")
						.setNegativeButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// User cancelled the dialog
										tv_visits.setTextColor(Color.RED);
										firsttime.setTextColor(Color.RED);
									}
								}).create().show();
				return false;
			} else {
				tv_visits.setTextColor(Color.BLACK);
				firsttime.setTextColor(Color.BLACK);
			}
		}
		return true;
	}

	protected void clearInterviewData() {
		try {
			interview.put("visits", null);
			interview.put("gender", null);
			interview.put("adult", null);
			interview.put("interviewedBefore", null);
			interview.put("firsttime", null);
			interview.put("driver", null);
			interview.put("hunter", null);
			interview.put("shootingtype", null);
			interview.put("compliance_keptaway", null);
			interview.put("compliance_type", null);
			interview.put("compliance", null);
			interview.put("comments", null);
			interview.put("alert", null);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void saveNewPartyData() {
		try {
			party.put("PartyUID", partyUID);
			party.put("SurveyDataUID", surveyDataUID);
			party.put("partytype", partyType);
			party.put("partysize", num_of_people_in_party);
			party.put("entrydate", Utilities.generateDateTag());
			party.put("uploaded", 0);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		interviewee_counter++;
	}

	protected void clearPartyData() {
		try {
			party.put("PartyUID", null);
			party.put("SurveyDataUID", null);
			party.put("partytype", null);
			party.put("partysize", null);
			party.put("comments", null);
			party.put("interviewstatus", null);
			party.put("entrydate", null);
			party.put("lastsave", null);
			party.put("RecordGPS", null);
			party.put("uploaded", null);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void interviewTag() {
		try {
			interview.put("lastsave", Utilities.generateDateTag());
			if (mLocation != null)
				interview.put("RecordGPS", Double.toString(mLocation.getLatitude())
						+ "," + Double.toString(mLocation.getLongitude()));
			interview.put("uploaded", 0);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
