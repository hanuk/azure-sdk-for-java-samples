package com.microsoft.samples.federation;

import java.io.IOException;
import java.io.StringReader;
import java.security.cert.X509Certificate;
import java.util.Timer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.opensaml.saml2.metadata.provider.HTTPMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.XMLObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class FederationMetadataDocument {
	//file system or HTTP URL
	private  String  _fmdUrl; 
	private Document _fmdDoc = null; 
	private boolean _loaded = false;
	//singleton instance 
	private static FederationMetadataDocument _instance = null; 
	private FederationMetadataDocument(String fmdUrl) {
		_fmdUrl = fmdUrl;
	}
	//returns entityID of the EntityDescriptor node
	public String getEntityID() {
		Node entityDescriptor = _fmdDoc.getFirstChild();
		return entityDescriptor.getAttributes().getNamedItem("entityID").getTextContent();
	}
	public String getSTSEndPoint() {
		String stsep = getSingleNodeText("/EntityDescriptor/RoleDescriptor/SecurityTokenServiceEndpoint/EndpointReference/Address/text()");
		return stsep;
	}
	public String getX509CertString() {
		
		return getSingleNodeText("/EntityDescriptor/Signature/KeyInfo/X509Data/X509Certificate/text()"); 
	}
	private String getSingleNodeText(String xpathStr) {
		XPathFactory xpf = XPathFactory.newInstance();
		XPath xpath = xpf.newXPath();
			
		try {
			XPathExpression xpe = xpath.compile(xpathStr);
			NodeList nodeList = (NodeList) xpe.evaluate(_fmdDoc, XPathConstants.NODESET);
			if (nodeList.getLength() > 0) {
				return nodeList.item(0).getNodeValue();
			}
				
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	private boolean load() {	
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = factory.newDocumentBuilder();
			_fmdDoc = documentBuilder.parse(_fmdUrl);
			_loaded = true;
		}
		catch (Exception e){
			//todo:revisit for strongly typed exception handling
			_loaded = false;  
		}
		return _loaded; 
	}
	//resets the transient state; once reset is called
	protected void reset() {
		_fmdDoc = null;
		_loaded = false;
	}
	public static FederationMetadataDocument getInstance(String fmdUrl) {
		if(_instance == null) {
			synchronized(FederationMetadataDocument.class) {
			_instance = new FederationMetadataDocument(fmdUrl);
			_instance.load();
			}
		}
		return _instance; 
	}
}
