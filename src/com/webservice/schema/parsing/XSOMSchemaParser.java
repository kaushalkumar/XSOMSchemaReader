package com.webservice.schema.parsing;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xml.sax.SAXException;

import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.parser.XSOMParser;

/**
 * @author KaushalKumar
 *
 */
public class XSOMSchemaParser {
	
	private static Map<String, String> namespaceMap = new HashMap<String, String>();
	private static final String ROOT_ELEMENT_NAME = "customer";

	public static void main(String[] args) throws SAXException, IOException {
		
		XSOMParser parser = new XSOMParser();
		
		String schemaDir = "C:/lunaWorkspace/SchemaReader/sample/";
		
		//load all schema and parse it using XSOMParser
		File xsdFolderLocation = new File(schemaDir);
		String[] list = xsdFolderLocation.list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				if (name.endsWith(".xsd")) {
					return true;
				}
				return false;
			}
		});
		for (String string : list) {
			File xsdFile = new File(schemaDir + "/" + string);
			parser.parse(xsdFile);
		}

		XSSchemaSet result = parser.getResult();

		String absPath = "";
		
		XSElementDecl rootElement = null;
		
		// iterate each XSSchema object and create a hashmap of namespace and also populate the root element.
		Iterator<XSSchema> itr = result.iterateSchema();
		int namespaceCounter = 0;
		while (itr.hasNext()) {
			XSSchema s = (XSSchema) itr.next();
			namespaceMap.put(s.getTargetNamespace(), "ns"+namespaceCounter);
			namespaceCounter++;
			if (rootElement == null) {
				rootElement = s.getElementDecl(ROOT_ELEMENT_NAME);
			}
		}

		//print the entire element with XPATH
		printElement(rootElement, absPath);

	}

	private static void printElement(XSElementDecl element, String absPath) {
		absPath += "/" + getElementWithNamespace(element);
		System.out.println("Element Name:" + element.getName() + "\t\tXPath:" + absPath + "\t\tNamespace:" + element.getTargetNamespace());
		if (element.getType().isComplexType()) {
			printComplexType(element.getType().asComplexType(), absPath);
		}
	}
	
	private static void printParticle(XSParticle particle, String absPath) {
		XSTerm term = particle.getTerm();
		if (term.isModelGroup()) {
			printGroup(term.asModelGroup(), absPath);
		} else if (term.isModelGroupDecl()) {
			printGroupDecl(term.asModelGroupDecl(), absPath);
		} else if (term.isElementDecl()) {
			printElement(term.asElementDecl(), absPath);
		}
	}

	private static void printGroup(XSModelGroup modelGroup, String absPath) {
		for (XSParticle particle : modelGroup.getChildren()) {
			printParticle(particle, absPath);
		}
	}

	private static void printGroupDecl(XSModelGroupDecl modelGroupDecl, String absPath) {
		printGroup(modelGroupDecl.getModelGroup(), absPath);
	}

	private static void printComplexType(XSComplexType complexType, String absPath) {
		XSParticle particle = complexType.getContentType().asParticle();
		if (particle != null) {
			printParticle(particle, absPath);
		}
	}

	private static String getElementWithNamespace(XSElementDecl element) {
		return namespaceMap.get(element.getTargetNamespace()) + ":" + element.getName();
	}
}
