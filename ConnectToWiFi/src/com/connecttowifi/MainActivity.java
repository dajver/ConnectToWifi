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
		
		// ������� ����� ������ ��� ����������� � ���������� �����
		wifiConfig = new WifiConfiguration();
		// ������� ������ ������� ��� ����� �������� ������������ � ������ �����
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		//������ ������� ������  ��� ���
		wifiEnabled = wifiManager.isWifiEnabled();

		//��� �������� ������� ����� ���������� ��� ������� ������� ��� ������������, ���� �� ����� ���������� ������ �����
		wifiResiver = new WifiReceiver();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// ����� ������� �������������� ��� ��� ��� �����
		init();
		
		conect.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				//���� ������ ������� �� ������ �� ������ ����� �������� ���
				if(!wifiEnabled) {
					wifiManager.setWifiEnabled(true);
				}
				
				//��������� ������� ������, � ������������ ���� ����������� ��� ���� ����
				scheduleSendLocation();
				//��������� ��������
				isClick = true;
			}
		});		
	}
	
	/*
	 * ������������ � wifi ��������� � edit text
	 * */
	public void scheduleSendLocation() {
	    
		registerReceiver(wifiResiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
	    wifiManager.startScan();
	}
	
	protected void onPause() {
		
		//���� ���������� ������ � ��� ��� �������� ��������� ��� ������, �� � �������� �������� � ���������.
        unregisterReceiver(wifiResiver);
        super.onPause();
    }
	
	public void onResume() {
		
		//���� �������� �� ��������� ��������, ���� �� isClick = false �� ���� ���� ������ ����� ������
		if(isClick)
			registerReceiver(wifiResiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		super.onResume();
	}
	
	/*
	 * �������� ������� ������ ��� ��������� ������� ���� 
	 * */
	public class WifiReceiver extends BroadcastReceiver {
		
		@Override
        public void onReceive(Context c, Intent intent) {
        	
			//��������� ������ ����� � ������ ����� ��������
           List<ScanResult> results = wifiManager.getScanResults();
           //���������� �� ���� ��������� ������
           for (final ScanResult ap : results) {
        	   //���� ������ ��� ����� � ������� ���, ����� �������� �� ������� �� �����
               if(ap.SSID.toString().trim().equals(url.getText().toString().trim())) {
            	   // ������ �������� �� MAC � �������� ��� ���������, MAC �������� �� ����������
            	   //����� �� ��� �������� ������������
	   				wifiConfig.BSSID = ap.BSSID;
		       		wifiConfig.priority = 1;
		       		wifiConfig.allowedKeyManagement.set(KeyMgmt.NONE);
		       		wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
		       		wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
		       		wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		       		wifiConfig.status = WifiConfiguration.Status.ENABLED;

		       		//�������� ID ���� � �������� � ��� ������������, 
		       		int netId = wifiManager.addNetwork(wifiConfig);
		       		wifiManager.saveConfiguration();
		       		//���� ������ �������� �� �������� ���
		       		wifiManager.enableNetwork(netId, true);
		       		//���� �� �� ������� �� ��������� � ������ ���� �� ����������� ������.
		       		wifiManager.reconnect();       		
		       		break;
               }
           }
        }
	}
}
