package org.hpccsystems.dashboard.chart.utils;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.chart.cluster.ClusterData;
import org.hpccsystems.dashboard.chart.entity.ChartData;
import org.hpccsystems.dashboard.chart.entity.InputParam;
import org.hpccsystems.dashboard.chart.entity.RelevantData;
import org.hpccsystems.dashboard.chart.entity.ScoredSearchData;
import org.hpccsystems.dashboard.chart.entity.TableData;
import org.hpccsystems.dashboard.chart.entity.XYChartData;
import org.hpccsystems.dashboard.chart.gauge.GaugeChartData;
import org.hpccsystems.dashboard.chart.tree.entity.TreeData;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.TreeConfiguration;
import org.hpccsystems.dashboard.entity.XYConfiguration;
import org.hpccsystems.dashboard.exception.EncryptDecryptException;
import org.hpccsystems.dashboard.util.EncryptDecrypt;
import org.zkoss.util.resource.Labels;

public class XMLConverter {
    
    private static final  Log LOG = LogFactory.getLog(XMLConverter.class);
    
    public static XYChartData makeXYChartDataObject(String xml) throws JAXBException, EncryptDecryptException {
        String encryptedpassWord="";
        String decryptedPassword="";
        EncryptDecrypt decrypter = null;;
        XYChartData chartData = null;
        JAXBContext jaxbContext;
        try {
            decrypter = new EncryptDecrypt("");
            jaxbContext = JAXBContext.newInstance(XYChartData.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            chartData = (XYChartData) jaxbUnmarshaller.unmarshal(new StringReader(xml));            
            //decrypt password
            encryptedpassWord = chartData.getHpccConnection().getPassword();
            decryptedPassword = decrypter.decrypt(encryptedpassWord);
            chartData.getHpccConnection().setPassword(decryptedPassword);
        } catch (JAXBException e) {
            LOG.error(Constants.EXCEPTION,e);
            throw e;
        }        
        return chartData;
    }
        
    public static String makeXYChartDataXML(XYChartData chartData) throws JAXBException, EncryptDecryptException {
        
        //encrypt password
        String rawPassword = chartData.getHpccConnection().getPassword();
        
        EncryptDecrypt encrypter = null;
        String encrypted = null;
        StringWriter sw = new StringWriter();
        JAXBContext jaxbContext;
        try {
            encrypter = new EncryptDecrypt("");
            encrypted = encrypter.encrypt(rawPassword);
            chartData.getHpccConnection().setPassword(encrypted);
            jaxbContext = JAXBContext.newInstance(XYChartData.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.marshal(chartData, sw);
        } catch (JAXBException e) {
            LOG.error(Constants.EXCEPTION,e);
            throw e;
        }
        
        //reset raw password again to the object
        chartData.getHpccConnection().setPassword(rawPassword);
        
        return sw.toString();
    }
    
    public static GaugeChartData makeGaugeChartDataObject(String xml) throws JAXBException, EncryptDecryptException {
        String encryptedpassWord="";
        String decryptedPassword="";
        EncryptDecrypt decrypter = null;;
        GaugeChartData chartData = null;
        JAXBContext jaxbContext;
        try {
            decrypter = new EncryptDecrypt("");
            jaxbContext = JAXBContext.newInstance(GaugeChartData.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            chartData = (GaugeChartData) jaxbUnmarshaller.unmarshal(new StringReader(xml));            
            //decrypt password
            encryptedpassWord = chartData.getHpccConnection().getPassword();
            decryptedPassword = decrypter.decrypt(encryptedpassWord);
            chartData.getHpccConnection().setPassword(decryptedPassword);
        } catch (JAXBException e) {
            LOG.error(Constants.EXCEPTION,e);
            throw e;
        }        
        return chartData;
    }
        
    public static String makeGaugeChartDataXML(GaugeChartData chartData) throws JAXBException, EncryptDecryptException {
        
        //encrypt password
        String rawPassword = chartData.getHpccConnection().getPassword();
        
        EncryptDecrypt encrypter = null;
        String encrypted = null;
        StringWriter sw = new StringWriter();
        JAXBContext jaxbContext;
        try {
            encrypter = new EncryptDecrypt("");
            encrypted = encrypter.encrypt(rawPassword);
            chartData.getHpccConnection().setPassword(encrypted);
            jaxbContext = JAXBContext.newInstance(GaugeChartData.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.marshal(chartData, sw);
        } catch (JAXBException e) {
            LOG.error(Constants.EXCEPTION,e);
            throw e;
        }
        
        //reset raw password again to the object
        chartData.getHpccConnection().setPassword(rawPassword);
        
        return sw.toString();
    }
    
    public static TableData makeTableDataObject(String xml) throws JAXBException, EncryptDecryptException{
        String encryptedpassWord="";
        String decryptedPassword="";
        EncryptDecrypt decrypter = null;
        TableData chartData = null;
        JAXBContext jaxbContext;
        try {
             decrypter = new EncryptDecrypt("");
            jaxbContext = JAXBContext.newInstance(TableData.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            chartData = (TableData) jaxbUnmarshaller.unmarshal(new StringReader(xml));
            
            //decrypt password
            encryptedpassWord = chartData.getHpccConnection().getPassword();
            decryptedPassword = decrypter.decrypt(encryptedpassWord);
            chartData.getHpccConnection().setPassword(decryptedPassword);
        } catch (JAXBException e) {
            LOG.error(Constants.EXCEPTION,e);
            throw e;
        }         
        return chartData;
    }
        
    public static String makeTableDataXML(TableData chartData) throws JAXBException, EncryptDecryptException {
        
        //encrypt password
        String rawPassword = chartData.getHpccConnection().getPassword();
        
        EncryptDecrypt encrypter = null;
        String encrypted = null;        
        StringWriter sw = new StringWriter();
        JAXBContext jaxbContext;
        try {
            encrypter = new EncryptDecrypt("");
            encrypted = encrypter.encrypt(rawPassword);
            chartData.getHpccConnection().setPassword(encrypted);
            jaxbContext = JAXBContext.newInstance(TableData.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.marshal(chartData, sw);
        } catch (JAXBException e) {
            LOG.error(Constants.EXCEPTION,e);
            throw e;
        }
        
        //reset raw password again to the object
        chartData.getHpccConnection().setPassword(rawPassword);
        
        return sw.toString();
    }
    
    public static TreeData makeTreeDataObject(String xml) throws JAXBException, EncryptDecryptException {
        String encryptedpassWord="";
        String decryptedPassword="";
        EncryptDecrypt decrypter = null;
        TreeData chartData = null;
        JAXBContext jaxbContext;
        try {
            decrypter = new EncryptDecrypt("");
            jaxbContext = JAXBContext.newInstance(TreeData.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            chartData = (TreeData) jaxbUnmarshaller.unmarshal(new StringReader(xml));
            
            //decrypt password
            encryptedpassWord = chartData.getHpccConnection().getPassword();
            decryptedPassword = decrypter.decrypt(encryptedpassWord);
            chartData.getHpccConnection().setPassword(decryptedPassword);
        } catch (JAXBException e) {
            LOG.error(Constants.EXCEPTION,e);
            throw e;
        }    
        return chartData;
    }
        
    public static String makeTreeDataXML(TreeData chartData) throws JAXBException, EncryptDecryptException  {
        
        //encrypt password
        String rawPassword = chartData.getHpccConnection().getPassword();
        
        EncryptDecrypt encrypter = null;
        String encrypted = null;        
        java.io.StringWriter sw = new StringWriter();
        JAXBContext jaxbContext;
        try {
            encrypter = new EncryptDecrypt("");
            encrypted = encrypter.encrypt(rawPassword);
            chartData.getHpccConnection().setPassword(encrypted);
            jaxbContext = JAXBContext.newInstance(TreeData.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.marshal(chartData, sw);
        } catch (JAXBException e) {
            LOG.error(Constants.EXCEPTION,e);
            throw e;
        }
        
        //reset raw password again to the object
        chartData.getHpccConnection().setPassword(rawPassword);
        
        return sw.toString();
    }

    
    public static XYConfiguration makeXYConfigurationObject(String xml) throws JAXBException{
        XYConfiguration configuration = null;
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(XYConfiguration.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            configuration = (XYConfiguration) jaxbUnmarshaller.unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            LOG.error(Constants.EXCEPTION,e);
            throw e;
        } catch (Exception e) {
            LOG.error(Constants.EXCEPTION,e);
            throw e;
        }        
        return configuration;
    }
    
    public static String makeXYConfigurationXML(XYConfiguration configuration) throws JAXBException {
        java.io.StringWriter sw = new StringWriter();
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(XYConfiguration.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.marshal(configuration, sw);
        } catch (JAXBException e) {
            LOG.error(Constants.EXCEPTION,e);
            throw e;
        }
        
        return sw.toString();
    }
    
    public static TreeConfiguration makeTreeConfigurationObject(String xml) throws JAXBException {
        TreeConfiguration configuration = null;
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(TreeConfiguration.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            configuration = (TreeConfiguration) jaxbUnmarshaller.unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            LOG.error(Constants.EXCEPTION,e);
            throw e;
        } catch (Exception e) {
            LOG.error(Constants.EXCEPTION,e);
            throw e;
        }        
        return configuration;
    }
    
    public static String makeTreeConfigurationXML(TreeConfiguration configuration) throws JAXBException {
        java.io.StringWriter sw = new StringWriter();
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(TreeConfiguration.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.marshal(configuration, sw);
        } catch (JAXBException e) {
            LOG.error(Labels.getLabel("exceptioninJAXB"),e);
            throw e;
        }
        
        return sw.toString();
    }
    
    public static ClusterData makeClusterDataObject(String xml) throws JAXBException, EncryptDecryptException {
        String encryptedpassWord="";
        String decryptedPassword="";
        EncryptDecrypt decrypter = null;
        ClusterData chartData = null;
        JAXBContext jaxbContext;
        try {
            decrypter = new EncryptDecrypt("");
            jaxbContext = JAXBContext.newInstance(ClusterData.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            chartData = (ClusterData) jaxbUnmarshaller.unmarshal(new StringReader(xml));
            
            //decrypt password
            encryptedpassWord = chartData.getHpccConnection().getPassword();
            decryptedPassword = decrypter.decrypt(encryptedpassWord);
            chartData.getHpccConnection().setPassword(decryptedPassword);
        } catch (JAXBException e) {
            LOG.error(Constants.EXCEPTION,e);
            throw e;
        }    
        return chartData;
    }
	
    public static String makeClusterChartDataXML(ClusterData  chartData) throws JAXBException, EncryptDecryptException  {
        
        //encrypt password
        String rawPassword = chartData.getHpccConnection().getPassword();
        
        EncryptDecrypt encrypter = null;
        String encrypted = null;        
        java.io.StringWriter sw = new StringWriter();
        JAXBContext jaxbContext;
        try {
            encrypter = new EncryptDecrypt("");
            encrypted = encrypter.encrypt(rawPassword);
            chartData.getHpccConnection().setPassword(encrypted);
            jaxbContext = JAXBContext.newInstance(ClusterData.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.marshal(chartData, sw);
        } catch (JAXBException e) {
            LOG.error(Constants.EXCEPTION,e);
            throw e;
        }
        
        //reset raw password again to the object
        chartData.getHpccConnection().setPassword(rawPassword);
        
        return sw.toString();
    }

	public static String makeScoredSearchDataXML(ScoredSearchData chartData)throws JAXBException, EncryptDecryptException  {
		 //encrypt password
        String rawPassword = chartData.getHpccConnection().getPassword();
        
        EncryptDecrypt encrypter = null;
        String encrypted = null;        
        java.io.StringWriter sw = new StringWriter();
        JAXBContext jaxbContext;
        try {
            encrypter = new EncryptDecrypt("");
            encrypted = encrypter.encrypt(rawPassword);
            chartData.getHpccConnection().setPassword(encrypted);
            jaxbContext = JAXBContext.newInstance(ScoredSearchData.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.marshal(chartData, sw);
        } catch (JAXBException e) {
            LOG.error(Constants.EXCEPTION,e);
            throw e;
        }
        
        //reset raw password again to the object
        chartData.getHpccConnection().setPassword(rawPassword);
        
        return sw.toString();
	}

	public static ChartData makeScoredSearchDataObject(String chartDataXML)  throws JAXBException, EncryptDecryptException{
		
		String encryptedpassWord="";
        String decryptedPassword="";
        EncryptDecrypt decrypter = null;
        ScoredSearchData chartData = null;
        JAXBContext jaxbContext;
        try {
            decrypter = new EncryptDecrypt("");
            jaxbContext = JAXBContext.newInstance(ScoredSearchData.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            chartData = (ScoredSearchData) jaxbUnmarshaller.unmarshal(new StringReader(chartDataXML));            
            //decrypt password
            encryptedpassWord = chartData.getHpccConnection().getPassword();
            decryptedPassword = decrypter.decrypt(encryptedpassWord);
            chartData.getHpccConnection().setPassword(decryptedPassword);
        } catch (JAXBException e) {
            LOG.error(Constants.EXCEPTION,e);
            throw e;
        }    
        return chartData;
	}
	
	/**
	 * Return an XML string containing hpccConnection details and input parameters(if any) for the Relevant graph
	 * @param chartData 
	 * @return XML string
	 * @throws JAXBException
	 * @throws EncryptDecryptException
	 * @author Dinesh
	 */
	public static String makeRelevantChartDataXML(RelevantData  chartData) throws JAXBException, EncryptDecryptException  {
        
        //encrypt password
        String rawPassword = chartData.getHpccConnection().getPassword();
        
        EncryptDecrypt encrypter = null;
        String encrypted = null;        
        java.io.StringWriter sw = new StringWriter();
        JAXBContext jaxbContext;
        try {
            encrypter = new EncryptDecrypt("");
            encrypted = encrypter.encrypt(rawPassword);
            chartData.getHpccConnection().setPassword(encrypted);
            jaxbContext = JAXBContext.newInstance(RelevantData.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.marshal(chartData, sw);
        } catch (JAXBException e) {
            LOG.error(Constants.EXCEPTION,e);
            throw e;
        }
        
        //reset raw password again to the object
        chartData.getHpccConnection().setPassword(rawPassword);
        
        return sw.toString();
    }
	
	public static RelevantData makeRelevantDataObject(String xml) throws JAXBException, EncryptDecryptException {
        String encryptedpassWord="";
        String decryptedPassword="";
        EncryptDecrypt decrypter = null;
        RelevantData chartData = null;
        JAXBContext jaxbContext;
        try {
            decrypter = new EncryptDecrypt("");
            jaxbContext = JAXBContext.newInstance(RelevantData.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            chartData = (RelevantData) jaxbUnmarshaller.unmarshal(new StringReader(xml));
            
            //decrypt password
            encryptedpassWord = chartData.getHpccConnection().getPassword();
            decryptedPassword = decrypter.decrypt(encryptedpassWord);
            chartData.getHpccConnection().setPassword(decryptedPassword);
        } catch (JAXBException e) {
            LOG.error(Constants.EXCEPTION,e);
            throw e;
        }    
        return chartData;
    }

    public static String makeCommonInputXML(List<InputParam> commonInputParams) throws JAXBException {
        java.io.StringWriter sw = new StringWriter();
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(List.class,InputParam.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.marshal(commonInputParams, sw);
        } catch (JAXBException e) {
            LOG.error(Constants.EXCEPTION,e);
            throw e;
        }
        return sw.toString();
    }

    @SuppressWarnings("unchecked")
    public static List<InputParam> makeCommonInputObject(String xml) throws JAXBException, EncryptDecryptException {
        List<InputParam> commonInputParams = null;
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(List.class,InputParam.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            commonInputParams = (List<InputParam>) jaxbUnmarshaller.unmarshal(new StringReader(xml));
            
        } catch (JAXBException e) {
            LOG.error(Constants.EXCEPTION,e);
            throw e;
        }    
        return commonInputParams;
    }
}
