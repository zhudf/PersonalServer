/*   
 *   Copyright (C) 2012  Alvin Aditya H,
 *   					 Shanti F,
 *   					 Selviana 
 *   
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *       
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *       
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 *    MA 02110-1301, USA.
 */

package com.personalserver;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class PersonalserverActivity extends Activity {
	
	private TextView infoBox;
	private EditText edit;
	private TextView ipbox;
	public static String share_path = null;
	private boolean server_running = false;
	ServerThread serv;
	Intent intent = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        
        infoBox = (TextView) findViewById(R.id.infoBox);
        ipbox = (TextView) findViewById(R.id.ipbox);
        edit = (EditText) findViewById(R.id.editText);
        
        // Get the between instance stored values 
        String infobox_txt = preferences.getString("infobox_txt",null);
		share_path = preferences.getString("share_path","/sdcard");
		Boolean edit_state = preferences.getBoolean("edit_state",true);
		server_running = preferences.getBoolean("run",false);
		server_running = false;
		
		//restore value
		infoBox.setText(infobox_txt);
		if(share_path.length() <= 0){
			edit.setText("/sdcard");
		} else {
			edit.setText(share_path);
		}
		edit.setEnabled(edit_state);
		
		toggleServer(null);
		
    }
    
    @Override
    protected void onPause() 
    {
        super.onPause();
        // Store values between instances here
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        
        editor.putString("infobox_txt", (String) infoBox.getText());
        editor.putString("share_path", share_path);
        editor.putBoolean("edit_state", edit.isEnabled());
        editor.putBoolean("run", server_running);
        
        // Commit to storage
        editor.commit();
    }
    
    public void toggleServer(View view){
    	intent = new Intent(this,PersonalService.class);
    	if(server_running == false){
    		server_running = true;
    		share_path = edit.getText().toString();
    		edit.setEnabled(false);
    		infoBox.setText("server is running");
    		setServerStatus(true);
    		startService(intent);
    		refreshIP(null);
    	} else {
    		server_running = false;
    		edit.setEnabled(true);
    		infoBox.setText("server is stopped");
    		setServerStatus(false);
    		stopService(intent);
    		ipbox.setText("");
    		
    	}
    }
    
    private void setServerStatus(boolean setStatus){
    	// Store values between instances here
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        
        editor.putBoolean("run", setStatus);
        
        // Commit to storage
        editor.commit();
    }
    
    private String getIPAddress(){
    	try{
    		for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
    			NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
    		}
    	} catch(SocketException e){
    		
    	}
    	return null;
    }
    
    public void refreshIP(View view){
    	String ip = getIPAddress();
    	if(server_running == false){
    		ipbox.setText("Web Server is not running," +
    				" please turn on the web server first");
    		return;
    	}
    	if(ip == null || ip.trim().length() <= 0){
    		ipbox.setText("Web Server is not accessible from external network" +
    				"please connect your device to wifi");
    	} else {
    		ipbox.setText("you can access web server from :\n" +
    				"http://"+ip+":8080");
    	}
    	
    }
    
    public void viewAbout(View view){
    	Intent intention = new Intent(this, AboutActivity.class);
    	startActivity(intention);
    }
    
    public void exitApp(View view){
    	SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        
        editor.putString("infobox_txt", "");
        editor.putString("share_path", "");
        editor.putBoolean("edit_state", true);
        editor.putBoolean("run", false);
        if(intent != null) stopService(intent);
        // Commit to storage
        editor.commit();
    	finish();
//    	System.exit(0);
    	
    	startActivity(new Intent(this, PersonalserverActivity.class)/*.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)*/);
    }
}