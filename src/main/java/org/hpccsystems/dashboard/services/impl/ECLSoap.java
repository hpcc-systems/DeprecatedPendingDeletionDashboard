package org.hpccsystems.dashboard.services.impl;

/*
 * Class to hit Hpcc Service and parsing the result
 */
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sun.misc.BASE64Encoder;
        
public class ECLSoap {
	
	private static final long serialVersionUID = 1L;	
	private static final  Log LOG = LogFactory.getLog(ECLSoap.class); 
	boolean isLogonFail = false;
    private String hostname = "";
    private String user = "";
    private String pass = "";
	private int port;
	private String tempDir;
    

    public String getTempDir(String tempDir) {
		return tempDir;
	}

	public void setTempDir(String tempDir) {
		this.tempDir = tempDir;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
	
    //Static block to get rid of HandShake error
	static {
	        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier()  {
	                public boolean verify(String hostname, SSLSession session) {	
	                    return true;
	                }
	            });
	    }
	
    public ECLSoap() {
    	if (System.getProperty("os.name").startsWith("Windows")) {
    		this.tempDir = System.getProperty("java.io.tmpdir");
        } else {
        	this.tempDir = System.getProperty("java.io.tmpdir") + "/";
        } 

        
    }
   
    /*
     * doSoap
     * @accepts String, String
     * @returns InputStream
     * 
     * Accepts two strings xmldata and path to soap call (hostname is a global variable)
     * returns InputStream from the  URLConnection
     */
    public InputStream doSoap(String xmldata, String path){
       URLConnection conn = null;
       boolean isSuccess = false;
       int errorCnt = 0;
       InputStream is = null;
       while(errorCnt < 5 && !isSuccess && !isLogonFail){
			try {

				ECLAuthenticator eclauth = new ECLAuthenticator(user, pass);

				Authenticator.setDefault(eclauth);
				String host = "https://" + hostname + ":" + port + path;
				if(LOG.isDebugEnabled())
				{
					LOG.debug("HOST: " + host);
				}
				URL url = new URL(host);
				// Send data
				conn = url.openConnection();
				conn.setDoOutput(true);
				// added back in since Authenticator isn't allways called and
				// the user wasn't passed if the server didn't require auth
				if (!user.equals("")) {
					String authStr = user + ":" + pass;
					BASE64Encoder encoder = new BASE64Encoder();
					String encoded = encoder.encode(authStr.getBytes());

					conn.setRequestProperty("Authorization", "Basic " + encoded);
				}
				if(LOG.isDebugEnabled())
				{
					LOG.debug("xmldata -->" + xmldata);
				}
				conn.setRequestProperty("Post", path + " HTTP/1.0");
				conn.setRequestProperty("Host", hostname);
				conn.setRequestProperty("Content-Length", "" + xmldata.length());
				conn.setRequestProperty("Content-Type",
						"text/xml; charset=\"utf-8\"");

				OutputStreamWriter wr = new OutputStreamWriter(
						conn.getOutputStream());
				wr.write(xmldata);
				wr.flush();
				if (conn instanceof HttpURLConnection) {
					HttpURLConnection httpConn = (HttpURLConnection) conn;
					int code = httpConn.getResponseCode();
					if(LOG.isDebugEnabled())
					{
						LOG.debug("Connection code: " + code);
					}
					if (code == 200) {
						is = conn.getInputStream();
						isSuccess = true;
						if(LOG.isDebugEnabled())
						{
							LOG.debug("Connection success code 200 ");
						}
					} else if (code == 401) {
						isSuccess = false;
						isLogonFail = true;
						if(LOG.isDebugEnabled())
						{
							LOG.debug("Permission Denied");
						}
					}
				}

			} catch (Exception e) {
	            e.printStackTrace();
	            errorCnt++;
	        }finally{
	        	if(conn != null){
	        		
	        	}
	        }
	        if(!isSuccess){
	        	try{
	        		Thread.sleep(3500);
	        	}catch (Exception e){
						LOG.error("couldn't sleep thread", e);
	        	}
	        }
       }
          return is;
    }
   

    /*
     * ECLAuthenticator
     * 
     * Handles the http authentication for the soap request
     */
    static class ECLAuthenticator extends Authenticator {
        public String user;
        public String pass;
        String hostname = getRequestingHost();        
        public ECLAuthenticator(String kuser,String kpass){
            user=kuser;
            pass=kpass;
        }
        public PasswordAuthentication getPasswordAuthentication() {
            PasswordAuthentication p = new PasswordAuthentication(user, pass.toCharArray());
            return p;
        }
    }
 
 
 }