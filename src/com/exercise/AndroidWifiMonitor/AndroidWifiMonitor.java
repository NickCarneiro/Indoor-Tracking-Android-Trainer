package com.exercise.AndroidWifiMonitor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.exercise.AndroidWifiMonitor.PointWithRSSI.Point2D;

public class AndroidWifiMonitor extends Activity {

	WifiManager mainWifi;
	static DesiredFunctionality df;		//this will be used to do calibration
	WifiReceiver receiverWifi;
	List<ScanResult> wifiList;
	int x;
	int y;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		df = new DesiredFunctionality();


		//moves focus to next editText
		//    	EditText edtView=(EditText)findViewById(R.id.xPosition);
		//    	edtView.setInputType(0);
		//    	
		//    	edtView=(EditText)findViewById(R.id.yPosition);
		//    	edtView.setInputType(0);

		//	DisplayWifiState();

		this.registerReceiver(this.myWifiReceiver,
				new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

	}

	private BroadcastReceiver myWifiReceiver
	= new BroadcastReceiver(){

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			//  Auto-generated method stub
			NetworkInfo networkInfo = (NetworkInfo) arg1.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
			if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI){
				//DisplayWifiState();
			}
		}};

		public boolean DisplayWifiState(){

			ConnectivityManager myConnManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			NetworkInfo myNetworkInfo = myConnManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			WifiManager myWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
			WifiInfo myWifiInfo = myWifiManager.getConnectionInfo();
			//myWifiManager.

			mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			receiverWifi = new WifiReceiver();
			registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
			mainWifi.startScan();

			boolean check = false;
			if (myNetworkInfo.isConnected()){
				//myWifiManager.startScan();			//breaks code for some reason
				//while (!myWifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals("android.net.wifi.SCAN_RESULTS"));


				List<ScanResult> accessPoints= myWifiManager.getScanResults();	//use this to get all access points' information.
				//StringBuffer str = new StringBuffer();
				for (ScanResult sr: accessPoints) {
					String test = sr.BSSID;
					if (df.getCurrentPoint().getWeightedaccessPoints().containsKey(test)) {
						AccessPoint ap = df.getCurrentPoint().getWeightedaccessPoints().get(test); 
						//float check = ap.getRssi()*(float)ap.getWeight()+(float)sr.level;
						float a = ap.getRssi();
						float c = (float)sr.level;
						if (a != c ){
							check = true;
						}
					}
					else {
						check = true;
					}
				}
				if (check == true) {
					for (ScanResult sr: accessPoints) {
						String test = sr.BSSID;
						if (df.getCurrentPoint().getWeightedaccessPoints().containsKey(test)) { //Current Point's HashMap contains current accesspoint's bssid
							AccessPoint ap = df.getCurrentPoint().getWeightedaccessPoints().get(test); 
							ap.setRssi((ap.getRssi()*(float)ap.getWeight()+(float)sr.level)/((float)(ap.getWeight()+1.0)));		//add to running average						
							ap.setWeight (ap.getWeight()+1);
						}
						else {
							df.getCurrentPoint().getWeightedaccessPoints().put(sr.BSSID, new AccessPoint((float)sr.level, sr.BSSID, 1));
						}
					}
				}
				return check;
			}
			else{

				return false;
			}
		}

		public void refresh(View view) {
			DisplayWifiState();
		}

		@Override
		public boolean dispatchTouchEvent(MotionEvent event) {		// to get rid of the keyboard when you click outside

			View v = getCurrentFocus();
			boolean ret = super.dispatchTouchEvent(event);

			if (v instanceof EditText) {
				View w = getCurrentFocus();
				int scrcoords[] = new int[2];
				w.getLocationOnScreen(scrcoords);
				float x = event.getRawX() + w.getLeft() - scrcoords[0];
				float y = event.getRawY() + w.getTop() - scrcoords[1];

				Log.d("Activity", "Touch event "+event.getRawX()+","+event.getRawY()+" "+x+","+y+" rect "+w.getLeft()+","+w.getTop()+","+w.getRight()+","+w.getBottom()+" coords "+scrcoords[0]+","+scrcoords[1]);
				if (event.getAction() == MotionEvent.ACTION_UP && (x < w.getLeft() || x >= w.getRight() || y < w.getTop() || y > w.getBottom()) ) { 

					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
				}
			}
			return ret;
		}

		//    public void clearX() {
		//        //  Auto-generated method stub
		//    	EditText edtView=(EditText)findViewById(R.id.xPosition);
		//    	edtView.setText("");
		//    }

		public void average(View view) {
			EditText s =  (EditText)findViewById(R.id.xPosition);
			x = Integer.parseInt(s.getText().toString());
			s =  (EditText)findViewById(R.id.yPosition);
			y = Integer.parseInt(s.getText().toString());
			df.setCurrentPoint(new PointWithRSSI(x,y, "Weight"));
			//check to see if point was already in list
			for (PointWithRSSI p: df.getPoints()) {
				if (p.getPoint().equals(df.getCurrentPoint().getPoint())) {
					df.setCurrentPoint(p);
				}
			}			
			if (!df.getPoints().contains(df.getCurrentPoint())) {
				//If point wasn't in list, add point to list
				df.getPoints().add(df.getCurrentPoint());
			}
			int i=0;
			while (i<50) {
				if (DisplayWifiState()) {
					i++;
				}
			}
			Context context = getApplicationContext();
			CharSequence text = "Finished averaging";
			Toast toast = Toast.makeText(context, text, 1);
			toast.show();
		}


		public void saveState(View view) throws IOException {
			int i=0;
			if (i==0);
			try {
				df.Print();
				//df.Output();
				serializeMap();
			}
			catch (Exception e) {
				System.out.println("Error doing File I/O");
			}
		}

		public void readData(View view) {
			try {
				FileInputStream fIn = openFileInput("samplefile.txt");
				InputStreamReader isr = new InputStreamReader(fIn);
				/* Prepare a char-Array that will
				 * hold the chars we read back in. */
				char[] inputBuffer = new char[100000];
				CharBuffer c = null;
				// Fill the Buffer with data from the file
				isr.read(inputBuffer);
				// Transform the chars to a String
				int index = 0;
				for (int i =0; i< inputBuffer.length; i++) {
					char test = inputBuffer[i];
					if (test == '\u0000') {
						index = i;
						break;
					}
				}
				String readString = new String(inputBuffer, 0, index);
				//				if (readString.equals(df.Output())) {
				//					//
				//					System.out.println("It works!!");
				//				}

				df = DesiredFunctionality.Read(readString);		//lesser (absolute) decibel value means im closer to the access point..
				String s = df.newOutput();
				if (readString.equals(df.newOutput())) {
					System.out.println("It works !!");
				}

			}
			catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

		public boolean lookupAverage(DesiredFunctionality curr) {
			ConnectivityManager myConnManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			NetworkInfo myNetworkInfo = myConnManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			WifiManager myWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
			WifiInfo myWifiInfo = myWifiManager.getConnectionInfo();
			//myWifiManager.

			mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			receiverWifi = new WifiReceiver();
			registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
			mainWifi.startScan();

			boolean check = false;
			if (myNetworkInfo.isConnected()){
				//myWifiManager.startScan();			//breaks code for some reason
				//while (!myWifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals("android.net.wifi.SCAN_RESULTS"));


				List<ScanResult> accessPoints= myWifiManager.getScanResults();	//use this to get all access points' information.
				//StringBuffer str = new StringBuffer();
				for (ScanResult sr: accessPoints) {
					String test = sr.BSSID;
					if (curr.getCurrentPoint().getWeightedaccessPoints().containsKey(test)) {
						AccessPoint ap = curr.getCurrentPoint().getWeightedaccessPoints().get(test); 
						//float check = ap.getRssi()*(float)ap.getWeight()+(float)sr.level;
						float a = ap.getRssi();
						float c = (float)sr.level;
						if (a != c ){
							check = true;
						}
					}
					else {
						check = true;
					}
				}
				if (check == true) {
					for (ScanResult sr: accessPoints) {
						//str.append (sr.BSSID + '\t' + sr.level + '\n');
						String test = sr.BSSID;
						//curr.getCurrentPoint().getWeightedaccessPoints().clear();		//empty hash map -- probably not needed
						if (curr.getCurrentPoint().getWeightedaccessPoints().containsKey(test)) { //Current Point's HashMap contains current accesspoint's bssid
							AccessPoint ap = curr.getCurrentPoint().getWeightedaccessPoints().get(test); 
							//float check = ap.getRssi()*(float)ap.getWeight()+(float)sr.level;
							float a = ap.getRssi();
							float b = sr.level;
							ap.setRssi((ap.getRssi()*(float)ap.getWeight()+(float)sr.level)/((float)(ap.getWeight()+1.0)));		//add to running average
							ap.setWeight (ap.getWeight()+1);
						}
						else {
							curr.getCurrentPoint().getWeightedaccessPoints().put(sr.BSSID, new AccessPoint((float)sr.level, sr.BSSID, 1));
						}
					}
				}
			}
			return check;
		}

		//TODO

		public void lookup (View view) {
			//first fo a five times average to just get the values..
			DesiredFunctionality temp = new DesiredFunctionality();		//the co-ordinate part of this object is meaningless
			int i=0;
			while (i<10) {
				if (lookupAverage(temp)) {
					i++;
				}
			}
			Context context = getApplicationContext();
			CharSequence text = "Finished lookup averaging";
			Toast toast = Toast.makeText(context, text, 1);
			toast.show();
			//now read df object
			//readData(view);
			//do containment first, then do closeness
			boolean containment = true;
			ArrayList<PointWithRSSI> matches = new ArrayList<PointWithRSSI> ();
			ArrayList<PointWithRSSI> compare= df.getPoints();
			Set<String> keys= temp.getCurrentPoint().WeightedaccessPoints.keySet();
			String as1 = temp.lookupOutput();
			//Set<String> keys = (Set<String>) temp.getPoints().get(0).getWeightedaccessPoints().keySet();		//this is from the real-time data
			//check containment using keys
			for (PointWithRSSI p : compare) {
				containment = true;
				Set<String> c = p.getAccessPoints().keySet();
				//if (c.size() == keys.size()) {
				for (String s: keys) {
					if (!c.contains(s)) {
						containment = false;		//s is all the bssid's, which is unique for all the APs
					}
				}
				if (containment == true ) {		//containment probably not being used
					matches.add(p);
				}

				//}
			}	//end for
			int x;
			int y;

			PointWithRSSI minPoint = null;
			//dot product choose the minimum value
			if (matches.size() == 1) {		//never going to happen, it seems (in ENS at least)
				Point2D currPosition = matches.get(0).point;
				x = currPosition.getX();
				y = currPosition.getY();
			}
			else {
				//do some logic - try a simple difference for now
				/*
					float minDiff = Float.MAX_VALUE;
					int minIndex = 0;
					for (int i =0 ; i< matches.size(); i++) {
						float currDiff = 0;
						HashMap<String, AccessPoint> currValues = temp.getPoints().get(0).getWeightedaccessPoints();
						HashMap<String, AccessPoint> pValues = matches.get(i).WeightedaccessPoints;
						for (String s: currValues.keySet()) {
							currDiff += (currValues.get(s).getRssi() + pValues.get(s).getRssi());
						}
						if (currDiff < minDiff) {
							minDiff = currDiff;
							minIndex = i;
						}
					}
					//get the point at matches.i
					matches.get(minIndex).point.getX();
					matches.get(minIndex).point.getY();		*/

				float minDot = Float.MAX_VALUE;
				//apply logic from above else loop here also
				for (PointWithRSSI p : compare) {
					float currDot = 0;
					Set<String> c = p.getAccessPoints().keySet();
					//if (c.size() == keys.size()) {
					for (String s: keys) {
						if (c.contains(s)) {
							currDot += p.getAccessPoints().get(s)*p.getAccessPoints().get(s) ;
							currDot -= (Math.abs(temp.getCurrentPoint().getWeightedaccessPoints().get(s).getRssi()) * Math.abs(p.getAccessPoints().get(s)));		//add to the dot product
						}
					}
					currDot = Math.abs(currDot);
					if (currDot < minDot) {
						minDot = currDot;
						minPoint = p;
					}

					//}
				}	//end for

			}	//end else


			//TODO: find the smallest dot product
			/*
			if (matches.size() == 0) {
				float minDot = Float.MAX_VALUE;
				//apply logic from above else loop here also
				for (PointWithRSSI p : compare) {
					float currDot = 0;
					Set<String> c = p.getAccessPoints().keySet();
					//if (c.size() == keys.size()) {
					for (String s: keys) {
						if (c.contains(s)) {
							currDot += p.getAccessPoints().get(s)*p.getAccessPoints().get(s) ;
							currDot -= (Math.abs(temp.getCurrentPoint().getWeightedaccessPoints().get(s).getRssi()) * Math.abs(p.getAccessPoints().get(s)));		//add to the dot product
						}
					}
					if (currDot < minDot) {
						minDot = currDot;
						minPoint = p;
					}

					//}
				}	//end for
			}	//end if

			 */

			System.out.println(minPoint.getPoint().getY() + minPoint.getPoint().getX());

		}

		public void serializeMap() {//HashMap<String,String> hm) {
			try { // catches IOException below
				//final String TESTSTRING = new String("Hello Android");
				String output = df.Output();
				// ##### Write a file to the disk #####
				/* We have to use the openFileOutput()-method
				 * the ActivityContext provides, to
				 * protect your file from others and
				 * This is done for security-reasons.
				 * We chose MODE_WORLD_READABLE, because
				 *  we have nothing to hide in our file */             
				FileOutputStream fOut = openFileOutput("samplefile.txt",
						MODE_WORLD_READABLE);
				OutputStreamWriter osw = new OutputStreamWriter(fOut); 

				// Write the string to the file
				osw.write(output);
				/* ensure that everything is
				 * really written out and close */
				osw.flush();
				osw.close();
			}
			catch (Exception e) {
				Log.v("IO Exception", e.getMessage());
			}

			try {
				FileInputStream fIn = openFileInput("samplefile.txt");
				InputStreamReader isr = new InputStreamReader(fIn);
				/* Prepare a char-Array that will
				 * hold the chars we read back in. */
				char[] inputBuffer = new char[100000];
				CharBuffer c = null;
				// Fill the Buffer with data from the file
				isr.read(inputBuffer);
				// Transform the chars to a String
				int index = 0;
				for (int i =0; i< inputBuffer.length; i++) {
					char test = inputBuffer[i];
					if (test == '\u0000') {
						index = i;
						break;
					}
				}
				String readString = new String(inputBuffer, 0, index);
				if (readString.equals(df.Output())) {
					//
					System.out.println("It works!!");
				}

				df = DesiredFunctionality.Read(readString);
				String s = df.newOutput();
				if (readString.equals(df.newOutput())) {
					System.out.println("It works !!");
				}

			}
			catch (IOException ioe) {
				ioe.printStackTrace();
			}

			/*
			try {
				FileOutputStream fStream = openFileOutput("test.bin", Context.MODE_PRIVATE) ;
				ObjectOutputStream oStream = new ObjectOutputStream(fStream);

				oStream.writeObject(df);        
				oStream.flush();
				oStream.close();

				Log.v("Serialization success", "Success");
			} catch (Exception e) {
				Log.v("IO Exception", e.getMessage());
			}

			 */

		}   

		class WifiReceiver extends BroadcastReceiver {
			public void onReceive(Context c, Intent intent) {
				// mainText.setText(sb);
			}
		}


}