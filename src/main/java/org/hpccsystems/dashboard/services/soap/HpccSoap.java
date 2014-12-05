package org.hpccsystems.dashboard.services.soap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.chart.entity.HpccConnection;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.exception.HpccConnectionException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class HpccSoap {

    private static final  Log LOG = LogFactory.getLog(HpccSoap.class); 
    
    public HpccSoap(HpccConnection hpccConnection) {
        setHpccConnection(hpccConnection);
    }
    
    private HpccConnection hpccConnection;

    /*
     * Hnadles the http authentication for the soap request
     */
    static class ECLAuthenticator extends Authenticator {
        private String user;
        private String pass;
        String hostname = getRequestingHost();

        public ECLAuthenticator(String kuser, String kpass) {
            user = kuser;
            pass = kpass;
        }

        public PasswordAuthentication getPasswordAuthentication() {
            // I haven't checked getRequestingScheme() here, since for NTLM
            // and Negotiate, the usrname and password are all the same.
            PasswordAuthentication p = new PasswordAuthentication(user,
                    pass.toCharArray());
            return p;
        }
    }

    public List<String> getClusters() throws HpccConnectionException {
        StringBuilder requestXML = new StringBuilder();
        requestXML.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:hpccsystems:ws:wstopology\">     <soapenv:Header/>     <soapenv:Body>        <urn:TpListTargetClustersRequest/>     </soapenv:Body>  </soapenv:Envelope>");
        
        InputStream inputStream = doSoap(requestXML.toString(), "/WsTopology/TpListTargetClusters?ver_=1.2",hpccConnection.getEspPort());
        
        List<String> result = new ArrayList<String>();
        
        try {
            
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputStream);
            
            doc.getDocumentElement().normalize();
            
            NodeList nList = doc.getElementsByTagName("TpClusterNameType");
            
            for (int temp = 0; temp < nList.getLength(); temp++) {
                
                Node nNode = nList.item(temp);
                
                NodeList children = nNode.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    Node node = children.item(i);
                    
                    if("Name".equals(node.getNodeName())) {
                        result.add(node.getTextContent());
                    }
                }
            }
        } catch (SAXException | IOException | ParserConfigurationException e) {
            LOG.error(Constants.EXCEPTION, e);
            throw new HpccConnectionException("Unable to fetch cluster names");
        }
        
        return result;
    }
    
    public String getColumnSchema(String fileName) throws HpccConnectionException{
        StringBuilder requestXML = new StringBuilder();
        requestXML.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:hpccsystems:ws:wsdfu\">     <soapenv:Header/>     <soapenv:Body>        <urn:DFUDefFileRequest>           <urn:Name>")
                .append(fileName)
                .append("</urn:Name> <urn:Format>xml</urn:Format> </urn:DFUDefFileRequest> </soapenv:Body>  </soapenv:Envelope>");
        
        InputStream inputStream = doSoap(requestXML.toString(), "/WsDfu/DFUDefFile?ver_=1.26",hpccConnection.getEspPort());
        
        String result = null;
        
        try {

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputStream);

            doc.getDocumentElement().normalize();

            result = new String(DatatypeConverter.parseBase64Binary(
                    doc.getElementsByTagName("defFile").item(0).getTextContent()));
            
        } catch (SAXException | IOException | ParserConfigurationException e) {
            LOG.error(Constants.EXCEPTION, e);
            throw new HpccConnectionException("Unable to fetch cluster names");
        }

        if(LOG.isDebugEnabled()) {
            LOG.debug("Schema SOAP result -> " + result);
        }
        
        return result;
    }
    
    private InputStream doSoap(String xmldata, String path, int port) throws HpccConnectionException {

        URLConnection conn = null;
        boolean isSuccess = false;

        int errorCnt = 0;
        InputStream is = null;
        while (errorCnt < 3 && !isSuccess) {
            try {

                ECLAuthenticator eclauth = new ECLAuthenticator(
                        hpccConnection.getUsername(),
                        hpccConnection.getPassword());

                Authenticator.setDefault(eclauth);

                // String encoding = new sun.misc.BASE64Encoder().encode
                // ((user+":"+pass).getBytes());
                String host = "http://" + hpccConnection.getHostIp() + ":"    + port + path; 

                if (hpccConnection.getIsSSL()) {
                    host = "https://" + hpccConnection.getHostIp() + ":" + port    + path;
                }

                //Temp FIX
                if(hpccConnection.getAllowInvalidCerts() == null) {
                    hpccConnection.setAllowInvalidCerts(true);
                }
                if(LOG.isDebugEnabled()){
                    LOG.debug("Host Address -->"+host);
                }
                
                if (hpccConnection.getIsSSL()
                        && hpccConnection.getAllowInvalidCerts()) {
                    SSLUtilities.trustAllHttpsCertificates();
                    SSLUtilities.trustAllHostnames();
                }

                URL url = new URL(host);

                // Send data
                conn = url.openConnection();
                conn.setDoOutput(true);
                // added back in since Authenticator isn't allways called and
                // the user wasn't passed if the server didn't require auth

                if (!"".equals(hpccConnection.getUsername())) {
                    String authStr = hpccConnection.getUsername() + ":"
                            + hpccConnection.getPassword();
                    String encoded = new String(Base64.encodeBase64(authStr
                            .getBytes()));

                    conn.setRequestProperty("Authorization", "Basic " + encoded);
                }

                conn.setRequestProperty("Post", path + " HTTP/1.0");
                conn.setRequestProperty("Host", hpccConnection.getHostIp());
                conn.setRequestProperty("Content-Length", "" + xmldata.length());
                conn.setRequestProperty("Content-Type",    "text/xml; charset=\"utf-8\"");

                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(xmldata);
                wr.flush();
                if (conn instanceof HttpURLConnection) {
                    HttpURLConnection httpConn = (HttpURLConnection) conn;
                    if (hpccConnection.getIsSSL()) {
                        httpConn = (HttpsURLConnection) conn;
                    }
                    
                    //Block to handle Connection exception
                        int code = httpConn.getResponseCode();
                        if (code == 200) {
                            is = conn.getInputStream();
                            isSuccess = true;
                        } else if (code == 401) {
                            isSuccess = false;
                        } else if (code == -1) {
                            isSuccess = false;
                        }
                    
                }

            }catch (IOException e) {
                LOG.error(Constants.EXCEPTION, e);
                throw new HpccConnectionException();
            } finally {
                if (conn != null) {

                }
            }
            if (!isSuccess) {
                errorCnt++;
                try {
                    Thread.sleep(3500);
                } catch (Exception e) {
                    LOG.error(Constants.EXCEPTION, e);
                }
            }
        }
        return is;
    }

    public HpccConnection getHpccConnection() {
        return hpccConnection;
    }

    public void setHpccConnection(HpccConnection hpccConnection) {
        this.hpccConnection = hpccConnection;
    }
}
