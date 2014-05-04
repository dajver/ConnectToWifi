package com.connecttowifi;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	private WifiManager wifiManager;
	private WifiConfiguration wifiConfig;
	private WifiReceiver wifiResiver;
	private boolean wifiEnabled;
	private EditText url;
	private Button conect;
	private boolean isClick = false;
	
	public void init() {
		
		conect = (Button) findViewById(R.id.button1);
		url = (EditText) findViewById(R.id.editText1);
		
		// создаем новый объект для подключения к конкретной точке
		wifiConfig = new WifiConfiguration();
		// сканнер вайфая который нам будет помогать подключаться к нужной точке
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		//узнаем включен вайфай  или нет
		wifiEnabled = wifiManager.isWifiEnabled();

		//наш рессивер который будем подключать нас столько сколько нам понадобиться, пока не будет подключена нужная точка
		wifiResiver = new WifiReceiver();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// метод который инициализирует все что нам нужно
		init();
		
		conect.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				//если файвай включен то ничего не делаем иначе включаем его
				if(!wifiEnabled) {
					wifiManager.setWifiEnabled(true);
				}
				
				//запускаем сканнер вайфая, и подключаемся если подкходящая нам есть есть
				scheduleSendLocation();
				//запускаем рессивер
				isClick = true;
			}
		});		
	}
	
	/*
	 * Подключаемся к wifi указаному в edit text
	 * */
	public void scheduleSendLocation() {
	    
		registerReceiver(wifiResiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
	    wifiManager.startScan();
	}
	
	protected void onPause() {
		
		//если приложение уходит в фон или например выключаем его вообще, то и рессивер тормазим и выключаем.
        unregisterReceiver(wifiResiver);
        super.onPause();
    }
	
	public void onResume() {
		
		//если кликнули то запускаем рессивер, если же isClick = false то ждем пока кнопка будет нажата
		if(isClick)
			registerReceiver(wifiResiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		super.onResume();
	}
	
	/*
	 * Рессивер который каждый раз запускает сканнер сети 
	 * */
	public class WifiReceiver extends BroadcastReceiver {
		
		@Override
        public void onReceive(Context c, Intent intent) {
        	
			//сканируем вайфай точки и узнаем какие доступны
           List<ScanResult> results = wifiManager.getScanResults();
           //проходимся по всем возможным точкам
           for (final ScanResult ap : results) {
        	   //ищем нужную нам точку с помощью ифа, будет находить то которую вы ввели
               if(ap.SSID.toString().trim().equals(url.getText().toString().trim())) {
            	   // дальше получаем ее MAC и передаем для коннекрта, MAC получаем из результата
            	   //здесь мы уже начинаем коннектиться
	   				wifiConfig.BSSID = ap.BSSID;
		       		wifiConfig.priority = 1;
		       		wifiConfig.allowedKeyManagement.set(KeyMgmt.NONE);
		       		wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
		       		wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
		       		wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		       		wifiConfig.status = WifiConfiguration.Status.ENABLED;

		       		//получаем ID сети и пытаемся к ней подключиться, 
		       		int netId = wifiManager.addNetwork(wifiConfig);
		       		wifiManager.saveConfiguration();
		       		//если вайфай выключен то включаем его
		       		wifiManager.enableNetwork(netId, true);
		       		//если же он включен но подключен к другой сети то перегружаем вайфай.
		       		wifiManager.reconnect();       		
		       		break;
               }
           }
        }
	}
}
