//-----------------------------------------------------------------------
// <copyright file="FederatedConfiguration.java" company="Microsoft">
//     Copyright (c) Microsoft Corporation.  All rights reserved.
//
// 
//    Copyright 2012 Microsoft Corporation
//    All rights reserved.
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//      http://www.apache.org/licenses/LICENSE-2.0
//
// THIS CODE IS PROVIDED *AS IS* BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
// EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION ANY IMPLIED WARRANTIES OR 
// CONDITIONS OF TITLE, FITNESS FOR A PARTICULAR PURPOSE, MERCHANTABLITY OR NON-INFRINGEMENT.
//
// See the Apache Version 2.0 License for specific language governing 
// permissions and limitations under the License.
// </copyright>
//
// <summary>
//     
//
// </summary>
//----------------------------------------------------------------------------------------------

package com.microsoft.samples.federation;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.microsoft.samples.waad.federation.Constants;
import com.microsoft.samples.waad.federation.TrustedIssuer;

public class FederatedConfiguration {
	public static final String TRUSTED_ISSUER_FILE_NAME = "/trusted.issuers.xml";
    public static final String TRUSTED_ISSUER_NAME_ATTRIBUTE = "name";        
    public static final String TRUSTED_ISSUER_DISPLAY_NAME_ATTRIBUTE = "displayname";    
    public static final String TRUSTED_ISSUER_METADATA_URL_ATTRIBUTE = "metadataurl";
    public static final String TRUSTED_ISSUER_REALM_ATTRIBUTE = "realm";
    public static final String TRUSTED_ISSUER_REPLYURL_ATTRIBUTE="replyurl";
    public static final String TRUSTED_ISSUER_AUDIENCE_URI_ATTRIBUTE = "audienceuri";
    public static final String TRUSTED_ISSUER_TO_USE = "federation.trusted.issuer.to.use";
    public static final String TRUSTED_ISSUER_XPATH_TEMPLATE = "//issuer[@name='%s']";
    
	private static FederatedConfiguration instance = null;
	private Properties properties;
	private Document _trustedIssuerDoc = null;
	public static FederatedConfiguration getInstance() {
		if (instance == null) {
			synchronized (FederatedConfiguration.class) {
				try {
				instance = loadTrustedIssuers();
				loadProperties();
				}
				catch (Exception e)
				{
					instance = null; 
				}
			}
		}

		return instance;
	}
	private static FederatedConfiguration loadTrustedIssuers()
			throws SAXException, IOException, ParserConfigurationException {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = factory.newDocumentBuilder();
		Document document = documentBuilder.parse(FederatedConfiguration.class.getResourceAsStream(TRUSTED_ISSUER_FILE_NAME));
		return new FederatedConfiguration(document);
	}
	
	private String getProperty(String propertyName) {
		return this.properties.getProperty(propertyName).trim();
	}
	
	private String getTrustedIssuerAttributeValue(String attributeName) {
		String trustedIssuerName = getProperty(FederatedConfiguration.TRUSTED_ISSUER_TO_USE);
		XPathFactory xpf = XPathFactory.newInstance();
		XPath xpath = xpf.newXPath();
		try {
			XPathExpression xpe = xpath.compile(String.format(TRUSTED_ISSUER_XPATH_TEMPLATE,trustedIssuerName));	
			Node node = (Node)xpe.evaluate(_trustedIssuerDoc, XPathConstants.NODE);
			if (node !=null) {
				return node.getAttributes().getNamedItem(attributeName).getTextContent();
			}
				
		} catch (XPathExpressionException e) {}
		return null;
	}
	
	private static void loadProperties() {
		java.util.Properties props = new java.util.Properties();

		try {
			InputStream is = FederatedConfiguration.class.getResourceAsStream("/federation.properties");
			props.load(is);
			FederatedConfiguration.getInstance().setProperties(props);
		} catch (IOException e) {
			throw new RuntimeException("Configuration could not be loaded", e);
		}
	}
	private void setProperties(Properties props) {
		this.properties = props;
	}

	private FederatedConfiguration(Properties properties) {
		this.properties = properties;
	}
	private FederatedConfiguration(Document trustedIssuerDoc)
	{
		_trustedIssuerDoc = trustedIssuerDoc;
	}
	
	public String getFederationMetadataUrl() {
		return getTrustedIssuerAttributeValue(TRUSTED_ISSUER_METADATA_URL_ATTRIBUTE);
	}

	public String getRealm() {
		return getTrustedIssuerAttributeValue(TRUSTED_ISSUER_REALM_ATTRIBUTE);
	}
	public String[] getAudienceUris() {
		return getTrustedIssuerAttributeValue(TRUSTED_ISSUER_AUDIENCE_URI_ATTRIBUTE).split("\\|");
	}
	public String getReply() {
		return getTrustedIssuerAttributeValue(TRUSTED_ISSUER_REPLYURL_ATTRIBUTE);
	}
	
	public String getStsFriendlyName() {
		return getTrustedIssuerAttributeValue(TRUSTED_ISSUER_DISPLAY_NAME_ATTRIBUTE);
	}

	
	//methods for getting information from metadata document
	public String getStsUrl() {
		//return this.properties.getProperty("federation.trustedissuers.issuer");
		String fmdUrl = getFederationMetadataUrl();		
		return FederationMetadataDocument.getInstance(fmdUrl).getSTSEndPoint();
	}
	public String getThumbprint() {
		String fmdUrl = getFederationMetadataUrl();		
		return FederationMetadataParser.GetThumbPrint(FederationMetadataDocument.getInstance(fmdUrl));
	}

	public String[] getTrustedIssuers() {
		return null; 
	}

}
