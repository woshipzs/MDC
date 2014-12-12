package com.example.mdc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class Login extends Activity {

	Spinner spinner;
	ArrayAdapter<String> adapter;
	Button sign_in;
	EditText name;
	TextView nameTV;
	TextView regionTV;

	Context mContext;
	private String clerk_name;
	private String clerk_region;

	public static String[] regions;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		mContext = this;
		regions = getResources().getStringArray(R.array.regions);
		spinner = (Spinner) findViewById(R.id.region);
		nameTV = (TextView) findViewById(R.id.tv2);
		regionTV = (TextView) findViewById(R.id.tv1);
		adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, regions) {
			@Override
			public int getCount() {
				int count = super.getCount();
				return count > 0 ? count - 1 : count;
			}
		};
		spinner.setAdapter(adapter);
		spinner.setSelection(adapter.getCount());
		adapter.setDropDownViewResource(R.layout.spinner_item);
		spinner.setPrompt("Select one below");
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				clerk_region = parent.getItemAtPosition(position).toString();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});

		name = (EditText) findViewById(R.id.clerk_name);

		sign_in = (Button) findViewById(R.id.sign_in);
		sign_in.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				clerk_name = name.getText().toString();
				if(clerk_name.trim().length() <= 0){
					new AlertDialog.Builder(mContext)
					.setTitle("Warning")
					.setMessage(
							"Please enter your name.")
					.setNegativeButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// User cancelled the dialog
									nameTV.setTextColor(Color.RED);
								}
							}).create().show();
				}else if(clerk_region.equals("Select a region")){
					nameTV.setTextColor(Color.BLACK);
					new AlertDialog.Builder(mContext)
					.setTitle("Warning")
					.setMessage(
							"Please enter your region.")
					.setNegativeButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// User cancelled the dialog
									regionTV.setTextColor(Color.RED);
								}
							}).create().show();
				}else{
					main_page();
				}
			}
		});

	}

	private void main_page() {
		Intent i = new Intent(getApplicationContext(), MainPage.class);
		Bundle b = new Bundle();
		b.putString("region", getClerkRegion());
		b.putString("name", getClerkName());
		i.putExtras(b);
		startActivity(i);
	}

	public String getClerkRegion() {
		return clerk_region;
	}

	public String getClerkName() {
		return clerk_name;
	}

}
