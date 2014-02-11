package com.chb.addon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private boolean mIsExit = false;

	private double latitude = 0.0;
	private double longitude = 0.0;
	private TextView tx1;


	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		tx1 = (TextView) findViewById(R.id.tx1);
		tx1.setTextSize(18);
		tx1.setText("我是主进程，我的进程 PID = " + Process.myPid());
		tx1.setTextColor(Color.BLUE);
		Button btn = (Button) findViewById(R.id.btn1);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
//				tx1.setText("启动中...");
//				startOtherApp();
				tx1.setText("定位中...");
				startLocate();
			}
		});

	}

	@Override
	public void onBackPressed() {
		if (mIsExit) {
			this.finish();
			System.exit(0);
			return;
		}
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				mIsExit = false;
			}
		}, 3000);
		Toast.makeText(this, "连续点击退出", Toast.LENGTH_SHORT).show();
		mIsExit = true;

	}

	private void startOtherApp() {
		Context context = this.getApplicationContext();
		Intent intent = new Intent(context, InnerAppActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	private void startLocate() {
		Log.i("LBS", "startLocate");
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, new LocationListener() {
			public void onLocationChanged(Location location) { //当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
				// log it when the location changes
				if (location != null) {
					Log.i("LBS", "Location changed : Lat: "
							                  + location.getLatitude() + " Lon: "
							                  + location.getLongitude());
					tx1.setText("定位中...\n经度： " + location.getLongitude() + "\n纬度： " + location.getLatitude());
				}
			}

			public void onProviderDisabled(String provider) {
				// Provider被disable时触发此函数，比如GPS被关闭
				Log.i("LBS", "onProviderDisabled");
			}

			public void onProviderEnabled(String provider) {
				//  Provider被enable时触发此函数，比如GPS被打开
				Log.i("LBS", "onProviderEnabled");
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {
				// Provider的转态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
				Log.i("LBS", "onStatusChanged");
			}
		});
	}

	private void startLbs() {
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			Log.i("LBS", "LocationManager.GPS_PROVIDER is enable");
			Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (location != null) {
				latitude = location.getLatitude();
				longitude = location.getLongitude();
				Log.i("LBS", "LocationManager.GPS_PROVIDER Location: Lat: "
						                  + location.getLatitude() + " Lon: "
						                  + location.getLongitude());
				tx1.setText("定位中...\n经度： " + location.getLongitude() + "\n纬度： " + location.getLatitude());
			} else {
				startLbsWithNetwork(locationManager);
			}
		} else {
			Log.i("LBS", "LocationManager.GPS_PROVIDER is disable");
			startLbsWithNetwork(locationManager);
		}
	}

	private void startLbsWithNetwork(LocationManager locationManager){
		LocationListener locationListener = new LocationListener() {

			// Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {

			}

			// Provider被enable时触发此函数，比如GPS被打开
			@Override
			public void onProviderEnabled(String provider) {

			}

			// Provider被disable时触发此函数，比如GPS被关闭
			@Override
			public void onProviderDisabled(String provider) {

			}

			//当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
			@Override
			public void onLocationChanged(Location location) {
				if (location != null) {
					Log.i("LBS", "LocationManager.NETWORK_PROVIDER Location changed : Lat: "
							             + location.getLatitude() + " Lon: "
							             + location.getLongitude());
					tx1.setText("定位中...\n经度： " + location.getLongitude() + "\n纬度： " + location.getLatitude());
				}
			}
		};
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
		Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if (location != null) {
			latitude = location.getLatitude(); //经度
			longitude = location.getLongitude(); //纬度
			Log.i("LBS", "LocationManager.NETWORK_PROVIDER Location changed : Lat: "
					             + location.getLatitude() + " Lon: "
					             + location.getLongitude());
			tx1.setText("定位中...\n经度： " + location.getLongitude() + "\n纬度： " + location.getLatitude());
		}
	}

}
