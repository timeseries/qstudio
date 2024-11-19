/*******************************************************************************
 *
 *   $$$$$$$\            $$\                     
 *   $$  __$$\           $$ |                     
 *   $$ |  $$ |$$\   $$\ $$ | $$$$$$$\  $$$$$$\   
 *   $$$$$$$  |$$ |  $$ |$$ |$$  _____|$$  __$$\  
 *   $$  ____/ $$ |  $$ |$$ |\$$$$$$\  $$$$$$$$ |  
 *   $$ |      $$ |  $$ |$$ | \____$$\ $$   ____|  
 *   $$ |      \$$$$$$  |$$ |$$$$$$$  |\$$$$$$$\  
 *   \__|       \______/ \__|\_______/  \_______|
 *
 *  Copyright c 2022-2023 TimeStored
 *
 *  Licensed under the Reciprocal Public License RPL-1.5
 *  You may obtain a copy of the License at
 *
 *  https://opensource.org/license/rpl-1-5/
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/
 
package com.timestored.babeldb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Data;
import lombok.Setter;

public class Curler {
    @Setter private static String TEST_URL = "http://localhost:8080";
	private static Logger log = LoggerFactory.getLogger(Curler.class);

    @Data
    private static class URLdetails {
    	
    	public URLdetails(String fullURL) {
    		String auth  = "";
    		String base64  = null;
    		String u = Objects.requireNonNull(fullURL);
    		if(u.contains("@") && u.contains("//")) {
    			auth  = fullURL.substring(u.indexOf("//")+2, u.indexOf('@'));
    	        u = fullURL.substring(0, fullURL.indexOf("//")+2) + fullURL.substring(fullURL.indexOf('@')+1);
				try {
					byte[] data1 = auth.getBytes(StandardCharsets.UTF_8.name());
	    	        base64 = Base64.getEncoder().encodeToString(data1);
				} catch (UnsupportedEncodingException e) {
					log.error("URLdetails UnsupportedEncodingException1");
				}
    		}
    		this.u = u;
    		this.auth = auth;
    		this.base64 = base64;
		}
    	
    	private final String u;
    	private final String auth;
    	private final String base64;
    }
    
    private static boolean curlExists = false;
    private static boolean curlTested = false;
    
    private static boolean doesCurlExist() {
    	if(curlTested) {
    		return curlExists;
    	}
    	curlTested = true;
    	try {
    		String s = curlFetchURL(TEST_URL, "GET", "text/plain");
    		curlExists = s!=null && s.length() > 0;
    		if(!curlExists) {
        		log.warn("curl does NOT exist. Pinging testurl failed");
    		}
    	} catch(IOException e) {
    		log.warn("curl failed: " + e.getLocalizedMessage());
    	}
    	return curlExists;
    }

	public static String fetchURL(String url) {
		return fetchURL(url, "GET", "text/plain");
	}
	
	
	public static String fetchURL(String url, String method, String acceptRequestType) {
		String u = url.replace(" ", "%20");
		// Some places use https with custom certs. Those certs may not be installed in the java distribution running pulse.
		// Therefore fall back is to call curl and hope the system has certs and/or proxy setup.
		String r = null;
		try {
			r = javaFetchURL(u, method, acceptRequestType, null, null);
		} catch (Exception e) {
			// hiding url as it would contain password
			log.warn("javaFetchURL failed: " + e.getLocalizedMessage().replace(url, "URLURL"));
		}
		if(r == null && doesCurlExist()) {
			try {
				r = curlFetchURL(u, method, acceptRequestType);
			} catch (IOException e) {
				log.warn("curlFetchURL failed: " + e.getLocalizedMessage().replace(url, "URLURL"));
			}
		}
		return r;
    }

	private static String getUrlArgForCurl(URLdetails ud) {
		boolean isWin = System.getProperty("os.name").toLowerCase().contains("win"); 
		return isWin ? "\""+ud.u.replace("\"", "\\\"") +"\"" : ud.u;
	}
	
	static String curlFetchURL(String url, String method, String acceptRequestType) throws IOException {
		URLdetails ud = new URLdetails(Objects.requireNonNull(url));
		String auth = ud.getAuth() == null || ud.getAuth().length() < 1 ? "a:b" : ud.getAuth();
		List<String> args = new ArrayList<>();
		args.add("curl");
		if(method != null) {
			args.add("-X"); args.add(method);
		}
		if(acceptRequestType != null) {
			args.add("-H"); args.add("Accept: " + acceptRequestType);
		}
		for(String s : new String[] {"--globoff", "--silent", "-u", auth, getUrlArgForCurl(ud)}) {
			args.add(s);
		}
		return runArgs(args.toArray(new String[] {}));
	}

	private static String runArgs(String[] args) throws IOException {
		Process process = new ProcessBuilder(args).redirectErrorStream(true).start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		BufferedReader bre = new BufferedReader (new InputStreamReader(process.getErrorStream()));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ( (line = reader.readLine()) != null) {
		   sb.append(line);
		   sb.append(System.getProperty("line.separator"));
		}
		reader.close();
		while ((line = bre.readLine()) != null) { 
			System.err.println(line); 
		}
	    bre.close();
	    if(process.exitValue()!=0) {
	    	throw new IOException("Non-zero exit value for runArgs: " + args[0]);
	    }
		
//		try {
//			int exitVal = process.waitFor();
//			System.out.println("exitVal = " + exitVal);
//		} catch (InterruptedException e) {}
		return sb.toString();
	}

	public static String POST(String url, String acceptRequestType, String reqContentType, byte[] reqDataSent) throws IOException {
		return javaFetchURL(url, "POST", acceptRequestType, reqContentType, reqDataSent);
	}
		
	static String javaFetchURL(String url, String method, String acceptRequestType, String reqContentType, byte[] reqDataSent) throws IOException {
		URLdetails ud = new URLdetails(Objects.requireNonNull(url));
    	HttpURLConnection urlConnection;
        urlConnection = (HttpURLConnection) ((new URL(ud.u).openConnection()));
        if(ud.base64 != null) {
	        urlConnection.setRequestProperty("Authorization", "Basic "+ud.base64);	
        }
        if(acceptRequestType != null) {
        	urlConnection.setRequestProperty("Accept", acceptRequestType); // application/json
        }
        urlConnection.setRequestMethod(method);
		if(reqContentType != null) {
			urlConnection.setRequestProperty("Content-Type", reqContentType);
		}
		if(reqDataSent != null) {
			urlConnection.setDoOutput(reqDataSent != null);
			try(OutputStream os = urlConnection.getOutputStream()) {
			    os.write(reqDataSent);
			}
		}
		if(urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) { 
			
			StringBuffer response = new StringBuffer();
			try(BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine).append("\n");
				}
			}
			return response.toString();
		} else {
			log.warn("HTTP request failed, code:" + urlConnection.getResponseCode());
		}
		return null;
    }

	
	public static boolean downloadFileTo(String url, File f) throws IOException {
		if(!f.getParentFile().isDirectory() && !f.getParentFile().mkdirs()) {
			throw new IOException(f.getAbsolutePath() + " not creatable");
		}
		log.info("Downloading " + url + " -> " + f.getAbsolutePath());
		// Some places use https with custom certs. Those certs may not be installed in the java distribution running pulse.
		// Therefore fall back is to call curl and hope the system has certs and/or proxy setup.
		boolean r = false;
		try {
			javaDownloadFileTo(url, f);
			r = true;
		} catch (Exception e) {
			// hiding url as it would contain password
			log.warn("javaDownloadFileTo failed: " + e.getLocalizedMessage().replace(url, "URLURL"));
		}
		if(r == false && doesCurlExist()) {
			try {
				curlDownloadFileTo(url, f);
				r = true;
			} catch (Exception e) {
				log.warn("curlDownloadFileTo failed: " + e.getLocalizedMessage().replace(url, "URLURL"));
			}
		}
		return r;
	}

	static void curlDownloadFileTo(String url, File f) throws IOException {
		URLdetails ud = new URLdetails(Objects.requireNonNull(url));
		String auth = ud.getAuth() == null || ud.getAuth().length() < 1 ? "a:b" : ud.getAuth(); 
		// -L is follow redirects, --fail causes 404 etc. to throw errors
		String[] args =  new String[] {"curl", "--globoff", "-L", "--fail", "-o", f.getAbsolutePath(), getUrlArgForCurl(ud)};
		runArgs(args);
	}

	static void javaDownloadFileTo(String url, File f) throws FileNotFoundException, IOException {
		try (InputStream in = new URL(url).openStream();
			ReadableByteChannel rbc = Channels.newChannel(in);
			FileOutputStream fos = new FileOutputStream(f)) {
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		}
	}
}
