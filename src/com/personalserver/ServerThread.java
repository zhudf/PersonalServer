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

import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.content.Context;
import android.util.Log;

public class ServerThread extends Thread{
	
	public static final int PORT = 8080;

	private static final String TAG = ServerThread.class.getSimpleName();
	
	public Context mContext;
	public boolean mRun = false;
	
	public ServerSocket mServerSocket = null;
	
	public ServerThread(Context context, String threadName){
		this.mContext = context;
		this.setName(threadName);
	}
	
	@Override
	public void run(){
		super.run();
		Socket socket = null;
		try {
			mServerSocket = new ServerSocket(PORT);
			mServerSocket.setReuseAddress(true);
			mServerSocket.setSoTimeout(5000);
			mRun = true;
			Log.e(TAG, "ServerThread start...");
			while(mRun){
				try{
				socket = mServerSocket.accept();
				System.out.println("client connected!");
				
				Thread httpThread = new HttpThread(mContext, socket, "httpthread");
				httpThread.start();
				} catch (InterruptedIOException eiox){					
				} catch (Exception exc){
					System.err.println(exc.getMessage());
					exc.printStackTrace();
				}
			}	
	    } catch (Exception e) {
	    	/*System.err.println("Exception in Server.java:socket");
	    	e.printStackTrace();
	    	try{
		    	if(soket != null) soket.close();
		    	server.close();
	    	} catch(Exception ex){
	    		System.err.println("Exception in Server.java:cannot close socket");
	    		ex.printStackTrace();
	    	}*/
	    	e.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (Exception e) {
			}
			try {
				mServerSocket.close();
			} catch (Exception e) {
			}
		}
		Log.e(TAG, "ServerThread end...");
	}

	public void stopServer() {
		mRun = false;
		try {
			mServerSocket.close();
		} catch (Exception e) {
		}
	}
}