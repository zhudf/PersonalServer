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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Arrays;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.entity.FileEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import android.content.Context;
import android.os.Environment;


public class DirCommandHandler implements HttpRequestHandler {

	@SuppressWarnings("unused")
	private Context mContext = null;
	private String mHost = "localhost";
	private String FOLDER_SHARE_PATH = null;
	
	public DirCommandHandler(Context context){
		this.mContext = context;
		if(PersonalserverActivity.share_path.trim().length() > 0){
			FOLDER_SHARE_PATH = PersonalserverActivity.share_path;
		} else {
			FOLDER_SHARE_PATH = Environment.getExternalStorageDirectory().toString();
		}
	}
	
	@Override
	public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext)
			throws HttpException, IOException {
		this.mHost = httpRequest.getFirstHeader("Host").getValue();
		String uriString = httpRequest.getRequestLine().getUri().substring(4);
		HttpEntity entity = getEntityFromUri(uriString,httpResponse);
		httpResponse.setEntity(entity);
	}
	
	private HttpEntity getEntityFromUri(String uri,HttpResponse response){
    	String contentType = "text/html";
    	String filepath = FOLDER_SHARE_PATH;
    	
		if(uri.equalsIgnoreCase("/") || uri.length() <= 0){
			filepath = FOLDER_SHARE_PATH + "/";
		}
		else{
			filepath = FOLDER_SHARE_PATH + "/" + URLDecoder.decode(uri);
		}
		
		System.out.println("request uri : " + uri);
		System.out.println("FOLDER SHARE PATH : " + FOLDER_SHARE_PATH);
		System.out.println("filepath : " + filepath);
		
		final File file = new File(filepath);
		
		HttpEntity entity = null;
		
		if(file.isDirectory()){
			entity = new EntityTemplate(new ContentProducer() {
				public void writeTo(final OutputStream outstream) throws IOException {
					OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
					String resp = getDirListingHTML(file);
		        
					writer.write(resp);
					writer.flush();
				}
			});
			
			response.setHeader("Content-Type", contentType);
		}
		else if(file.exists()){
			contentType = URLConnection.guessContentTypeFromName(file.getAbsolutePath());
			
			entity = new FileEntity(file, contentType);
			
			response.setHeader("Content-Type", contentType);
		}
		else{
			entity = new EntityTemplate(new ContentProducer() {
				public void writeTo(final OutputStream outstream) throws IOException {
					OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
					String resp = "<html>" +
							"<head><title>ERROR : NOT FOUND</title></head>" +
							"<body>" +
							"<center><h1>FILE OR DIRECTORY NOT FOUND !</h1></center>" +
							"<p>Sorry, file or directory you request not available<br />" +
							"Contact your administrator<br />" +
							"</p>" +
							"</body></html>";
			    
					writer.write(resp);
					writer.flush();
				}
			});
			response.setHeader("Content-Type", "text/html");
		}
		
	    return entity;
	 }
	
	private String getDirListingHTML(File file) {
		StringBuffer buff = new StringBuffer();
		if (file == null || !file.isDirectory() || !file.canRead()) {
			buff.append("<html><head></head><body>" +
					"<center><p><font color=\"red\">Permission Denied or Error Reading Directory</font><br />" +
					"</p></center>" +
					"</body>" +
					"</html>");
			return buff.toString();
		}
		
		File[] files = file.listFiles();
		Arrays.sort(files);
		buff.append("<html><head><title>Directory Listing</title></head>" +
				"<body><table border=\"0\" width=\"100%\">" +
				"<tr bgcolor=\"silver\" align=\"center\"><td>file name</td><td>size</td><td>type</td></tr>" +
				"<tr bgcolor=\"yellow\"><td><a href=\"http://"+this.mHost+"/dir/"+file.getPath().replaceFirst(FOLDER_SHARE_PATH+"/", "")+"/../\">../</a></td></tr>");
		
		for (File f:files){
			buff.append("<tr bgcolor=\""+(f.isDirectory()?"orange":"white")+"\">");
			buff.append("<td><a href=\"http://");
			buff.append(this.mHost);
			buff.append("/dir/" + f.getAbsolutePath().replaceFirst(FOLDER_SHARE_PATH+"/", "") + "\">" + (f.isDirectory()?f.getName():f.getName()) + "</a><br /></td>");
			buff.append("<td align=\"right\">" + (f.isFile()?formatByte(f.length(),true):"") + "</td>");
			buff.append("<td><center>" + (f.isDirectory()?"dir":"file") + "</center></td>");
			buff.append("</tr>");
		}
		
		buff.append("</table>" +
				"</body></html>");
		return buff.toString();
    }
	
	private String formatByte(long bytes, boolean si) {
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
}
