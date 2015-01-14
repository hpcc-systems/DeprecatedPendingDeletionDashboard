package org.hpccsystems.dashboard.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpcc.HIPIE.utils.HPCCConnection;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("*.do")
public class HPCCPassThrough {

	private static final Log LOG = LogFactory.getLog(HPCCPassThrough.class);

	static {
		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		});
	}
	
	@RequestMapping(value = "/{hpcc}/proxy-WUResult.do")
	public void proxyWUResult(@PathVariable String hpcc, HttpServletRequest request, HttpServletResponse response) {
		if(LOG.isDebugEnabled()) {
			LOG.debug("WUResult Proxy - Params - " + request.getParameterMap());
		}
		
		passthrough(hpcc, "/WsWorkunits/WUResult.json", request, response);
	}
	
	@RequestMapping(value = "/{hpcc}/proxy-WUInfo.do")
	public void proxyWUInfo(@PathVariable String hpcc, HttpServletRequest request, HttpServletResponse response) {
		if(LOG.isDebugEnabled()) {
			LOG.debug("WUInfo Proxy - Params - " + request.getParameterMap());
		}
		
		passthrough(hpcc, "/WsWorkunits/WUInfo.json", request, response);
	}
	
	private void passthrough(String hpccId, String endPoint, HttpServletRequest request, HttpServletResponse response) {
		HPCCConnection hpccConnection = HipieSingleton.getHipie().getHpccManager().getConnection(hpccId);
		
		StringBuilder urlBuilder = new StringBuilder(hpccConnection.getESPUrl())
				.append(endPoint)
				.append("?")
				.append(request.getQueryString());

		writeJSONResponse(response, hpccConnection, urlBuilder.toString());
	}
	
	@RequestMapping(value = "/{hpcc}/proxy-WsEcl.do")
	public void proxyWsEcl(@PathVariable String hpcc, HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
		Map<String, String[]> params = request.getParameterMap();
		Map<String, String[]> copiedParams = new HashMap<String, String[]>(params);
		
		
		if(LOG.isDebugEnabled()) {
			LOG.debug("WsEcl Proxy - Params - " + params);
		}
		
		HPCCConnection hpccConnection = HipieSingleton.getHipie().getHpccManager().getConnection(hpcc);
		
		StringBuilder urlBuilder = new StringBuilder();
		
		if(hpccConnection.isHttps) {
			urlBuilder.append("https://");
		} else {
			urlBuilder.append("http://");
		}
		
		urlBuilder.append(copiedParams.get("IP")[0])
				.append(":")
				.append(copiedParams.get("PORT")[0])
				.append("/WsEcl/submit/query/")
				.append(copiedParams.get("PATH")[0]);
		
		copiedParams.remove("IP"); 
		copiedParams.remove("PORT"); 
		copiedParams.remove("PATH"); 

		boolean isFirstArg = true;
		for (Entry<String, String[]> entry : copiedParams.entrySet()) {
			if(isFirstArg) {
				urlBuilder.append("?");
				isFirstArg = false;
			} else {
				urlBuilder.append("&");
			}
			
			urlBuilder.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
				.append('=')
				.append(URLEncoder.encode(entry.getValue()[0], "UTF-8"));
		}
		  
		writeJSONResponse(response, hpccConnection, urlBuilder.toString());
	}
	
	private void writeJSONResponse(HttpServletResponse response, HPCCConnection hpccConnection, String urlString) {
	    if (LOG.isDebugEnabled()) {
            LOG.debug("JSON URL " + urlString);
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        URL url;
        try {
            url = new URL(urlString);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setRequestProperty("Authorization", "Basic "
                    + hpccConnection.getAuthString());

            Scanner scanner = new Scanner(urlConnection.getInputStream())
                    .useDelimiter("//A");
            String json = scanner.hasNext() ? scanner.next() : "";
            
            response.getWriter().write(json);
        } catch (IOException e) {
            LOG.error("Error - " + e);
        }
	}
	
}
