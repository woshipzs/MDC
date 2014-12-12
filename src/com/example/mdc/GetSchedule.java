package com.example.mdc;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

public class GetSchedule extends AsyncTask<Void, Void, JSONObject> {

	private static String NAMESPACE = "http://tempuri.org/";
	private static String METHOD = "downloadScheduleObjProxy";
	private static String URL = "http://mdc7test.mdc.mo.gov/webservices/SRSProxyWebApi/api/Schedule";
	private static String SOAP_ACTION = "http://tempuri.org/downloadScheduleObjProxy";
	private static String responseJSON;
	
	//tag for schedule arrays
	private static final String TAG_SCHEDULE = "scheduleRecs";

	
	@Override
	protected JSONObject doInBackground(Void... voids) {
		/*
		 * code to get Json objects from SOAP web service SoapObject request =
		 * new SoapObject(NAMESPACE, METHOD); SoapSerializationEnvelope envelope
		 * = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		 * envelope.setOutputSoapObject(request); envelope.dotNet = true;
		 */
		// code to get Json objects from REST web service
		HttpGet request = new HttpGet(URL);
		try {

			// code to get Json objects from REST web service
			HttpResponse response = new DefaultHttpClient().execute(request);
			if (response.getStatusLine().getStatusCode() == 200) {
				responseJSON = EntityUtils.toString(response.getEntity());
				//Log.d("Schedule Info", responseJSON);
				if (responseJSON != null){
					try{
						JSONObject jsonObj = new JSONObject(responseJSON);
						return jsonObj;
					}catch (JSONException e){
						e.printStackTrace();
					}
				}
			} else {
				Log.d("Schedule Info", "couldn't get schedule data from server");
			}

			/*
			 * code to get Json objects from SOAP web service HttpTransportSE
			 * androidHttpTransport = new HttpTransportSE(URL);
			 * androidHttpTransport.call(SOAP_ACTION, envelope); Object response
			 * = (Object) envelope.getResponse(); responseJSON =
			 * response.toString();
			 * 
			 * if (responseJSON != null){ Log.d("Schedule Info", "Success"); }
			 * else { Log.d("Schedule Info", "failed"); } return null;
			 */
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}

	@Override
	protected void onPostExecute(JSONObject result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		
		Boolean success = false;
		
		try {
			JSONArray schedule_array = result.getJSONArray(TAG_SCHEDULE);
			for(int i = 0; i < schedule_array.length(); i++){
				success = MainPage.myDB.insert_schedule_into_table(schedule_array.getJSONObject(i));
				if(success.equals(false)){
					new AlertDialog.Builder(MainPage.mContext)
					.setTitle("Warning")
					.setMessage("Failed to get schedule into database")
					.setNegativeButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// User cancelled the dialog
								}
							}).create().show();
					MainPage.progressBar.dismiss();
					MainPage.updateInterface();
					return;
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (MainPage.progressBar.isShowing()){
			MainPage.progressBar.dismiss();
			MainPage.already_got_schedule = true;
			MainPage.updateInterface();
		}
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		MainPage.progressBar.show();
	}

	@Override
	protected void onProgressUpdate(Void... values) {
		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
	}

}
