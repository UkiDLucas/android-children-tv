package com.cyberwalkabout.common.http;

import android.util.Log;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class SaxXmlHandler extends HttpHandler<ContentHandler> {
	private static final String TAG = SaxXmlHandler.class.getSimpleName();

	private final ContentHandler saxHandler;

	public SaxXmlHandler(ContentHandler saxHandler) {
		this.saxHandler = saxHandler;
	}

	@Override
	public void handleInputStream(InputStream in) {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser parser = factory.newSAXParser();
			XMLReader reader = parser.getXMLReader();
			reader.setContentHandler(saxHandler);
			reader.parse(new InputSource(in));
		} catch (ParserConfigurationException e) {
			Log.e(TAG, e.getMessage(), e);
		} catch (SAXException e) {
			Log.e(TAG, e.getMessage(), e);
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

	public ContentHandler getContentHandler() {
		return saxHandler;
	}
}
