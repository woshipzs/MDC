package com.example.mdc;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class MapList extends Activity {
	private ListView areaList;
	private ListView mapList;
	public static String region = null;
	public static String clerkname = null;
	Button backtomain;
	TextView regionInfo;
	private ArrayList<String> list = new ArrayList<String>();
	private ArrayList<String> list1 = new ArrayList<String>();
	private ArrayList<String> list2 = new ArrayList<String>();

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.maplist);

		Bundle b = getIntent().getExtras();
		region = b.getString("region");
		clerkname = b.getString("name");

		backtomain = (Button) findViewById(R.id.backTomain);

		backtomain.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}
		});

		regionInfo = (TextView) findViewById(R.id.regionInfo);

		regionInfo.setText("Hi " + clerkname + "! You choosed " + region
				+ " region.");
		areaList = (ListView) findViewById(R.id.arealist);
		mapList = (ListView) findViewById(R.id.maplist);

		if (region.equals("Ozark")) {
			list.add("Angeline CA");
			list.add("Caney Mountain");
			list.add("Gist Ranch CA");
			list.add("Indian Trail CA");
			list.add("Peck Ranch CA");
			list.add("White Ranch CA");

			list1.add("map 1");
			list1.add("map 2");
			list1.add("map 3");
		}

		areaList.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_expandable_list_item_1, list));
		final ArrayAdapter<String> addasd = new ArrayAdapter<String>(this,
				android.R.layout.simple_expandable_list_item_1, list1);
		final ArrayAdapter<String> addasd1 = new ArrayAdapter<String>(this,
				android.R.layout.simple_expandable_list_item_1, list2);

		areaList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				if (list.get(arg2).equals("Caney Mountain")) {

					mapList.setAdapter(addasd);
					mapList.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1,
								int arg2, long arg3) {
							// TODO Auto-generated method stub
							if (list1.get(arg2).equals("map 1")) {
								Intent intent = new Intent();
								intent.setClass(MapList.this, MapViewer.class);
								startActivity(intent);
							}
						}

					});
				} else {
					mapList.setAdapter(addasd1);
				}
			}
		});
	}
}
