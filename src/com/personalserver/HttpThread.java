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

import java.io.IOException;
import java.net.Socket;

import org.apache.http.HttpException;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

import android.content.Context;

public class HttpThread extends Thread {
	
	private static final String ALL_PATTERN = "*";
	private static final String DIR_PATTERN = "/dir*";
//	private static final String DIR_PATTERN = "/config*";
	
	@SuppressWarnings("unused")
	private Context mContext;
	
	private BasicHttpProcessor 	mHttpProcessor;
	private HttpService 		mHttpService;
	private BasicHttpContext 	mHttpContext;
	private HttpRequestHandlerRegistry mHttpRequestHandlerRegistry;
	
	private Socket mSocket = null;
	
	public HttpThread(Context ctx, Socket soket, String threadName) {
		this.mContext = ctx;
		this.mSocket = soket;
		this.setName(threadName);
		
		mHttpProcessor = new BasicHttpProcessor();
		mHttpContext = new BasicHttpContext();
		
		mHttpProcessor.addInterceptor(new ResponseDate());
	    mHttpProcessor.addInterceptor(new ResponseServer());
	    mHttpProcessor.addInterceptor(new ResponseContent());
	    mHttpProcessor.addInterceptor(new ResponseConnControl());
	    
	    mHttpService = new HttpService(mHttpProcessor, new DefaultConnectionReuseStrategy(), new DefaultHttpResponseFactory());
	    
	    mHttpRequestHandlerRegistry = new HttpRequestHandlerRegistry();
		mHttpRequestHandlerRegistry.register(ALL_PATTERN, new HomeCommandHandler(ctx));
		mHttpRequestHandlerRegistry.register(DIR_PATTERN, new DirCommandHandler(ctx));
		
		mHttpService.setHandlerResolver(mHttpRequestHandlerRegistry);
	}

	public void run(){
		DefaultHttpServerConnection httpServer = new DefaultHttpServerConnection();
		try {
			httpServer.bind(mSocket, new BasicHttpParams());
			mHttpService.handleRequest(httpServer, mHttpContext);
		} catch (IOException e) {
			System.err.println("Exception in HttpThread.java:can't bind");
			e.printStackTrace();
		} catch (HttpException e) {
			System.err.println("Exception in HttpThread.java:handle request");
			e.printStackTrace();
		} catch (Exception exce){
			System.err.println("debug : error again !");
			System.err.println(exce.getMessage());
			exce.printStackTrace();
		} finally {
			try {
				httpServer.close();
			} catch (IOException e) {
				System.err.println("Excetion in HttpThread.java:can't shutdown");
				e.printStackTrace();
			}
		}
	}
}
