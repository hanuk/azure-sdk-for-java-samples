package com.microsoft.samples.federation;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Timer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.HttpClient;
import org.opensaml.saml2.metadata.provider.HTTPMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.util.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.microsoft.samples.federation.*; 

public class FederationMetadataParser {
	
	public static String GetThumbPrint(FederationMetadataDocument fmd) {
		String x509b64Str = fmd.getX509CertString();
		String thumbPrint = null; 
		CertificateFactory cf = null; 
		try {
			cf = CertificateFactory.getInstance("X.509");
			InputStream is = new ByteArrayInputStream(Base64.decode(x509b64Str));
			X509Certificate cert = (X509Certificate)cf.generateCertificate(is);
			thumbPrint =  getThumbPrintFromCert(cert);
		}
		catch (CertificateEncodingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		}
		return thumbPrint; 
	}
	private static String getSTSEndpoint(FederationMetadataDocument fmd)
	{
		return "";
	}

	private static String getSubjectName(X509Certificate cert)
	{
		return ""; 
	}
	
	private static String getThumbPrintFromCert(X509Certificate cert)
			throws NoSuchAlgorithmException, CertificateEncodingException {
		
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		byte[] der = cert.getEncoded();
		md.update(der);
		byte[] digest = md.digest();
		return hexify(digest);
	}
	
	private static String hexify(byte bytes[]) {
		char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };

		StringBuffer buf = new StringBuffer(bytes.length * 2);

		for (int i = 0; i < bytes.length; ++i) {
			buf.append(hexDigits[(bytes[i] & 0xf0) >> 4]);
			buf.append(hexDigits[bytes[i] & 0x0f]);
		}

		return buf.toString();
	}	
}
