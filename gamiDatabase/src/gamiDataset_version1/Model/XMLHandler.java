/**
 * File: XMLHandler.java
 * @author Victoria Chistolini
 * Date: January 13, 2017
 */
package gamiDataset_version1.Model;

import gamiDataset_version1.Model.Parsers.MultiParserHandler;
import gamiDataset_version1.Model.Parsers.ParserHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * sets up XML parser and returns results 
 */
public final class XMLHandler {

    public static HashMap<String, String> parseXML(File xmlToParse, ParserHandler handler){


        try {

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(xmlToParse, handler);
            return handler.getResult();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<SpeciesData> parseXML(File xmlToParse, MultiParserHandler handler){
        try {

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(xmlToParse, handler);
            return handler.getAllResults();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
