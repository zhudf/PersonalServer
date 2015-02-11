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

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

/*
 * 后台自起
 * 开机自起
 * AlarmManager检测
 */
public class PersonalService extends Service {
	
	private ServerThread mServerThread = null;
	private Handler mHander = new Handler();
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(mServerThread != null) {
			mServerThread.stopServer();
			mServerThread = null;
			
			// 保证8080端口释放了, 2s足够
			startServerDelay(5 * 1000);
		} else {
			startServerDelay(0);
		}
		return START_STICKY;
	}
	
	private void startServerDelay(int delayMillis) {
		mHander.removeCallbacksAndMessages(delayTask);
		mHander.postDelayed(delayTask, delayMillis);
	}
	
	private Runnable delayTask = new Runnable() {
		@Override
		public void run() {
			mServerThread = new ServerThread(PersonalService.this, "server");
			mServerThread.start();		
		}
	};

	@Override
	public void onDestroy() {
		if(mServerThread != null) {
			mServerThread.stopServer();
			mServerThread = null;
		}
		super.onDestroy();
	}
}
