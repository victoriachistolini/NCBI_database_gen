/**
 * File: FileMenuController.java
 * @author Victoria Chistolini
 * Date: January 14, 2017
 */
package gamiDataset_version1.Model.Parsers;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;

/**
 * data type for generic single type data parse-return.
 */
public abstract class ParserHandler extends DefaultHandler {

    public HashMap<String,String> result;

    public ParserHandler(){
        this.result = new HashMap<>();
    }



    @Override
    public abstract void startElement(String uri,
                             String localName, String qName, Attributes attributes)
            throws SAXException;

    @Override
    public abstract void endElement(String uri,
                           String localName, String qName) throws SAXException;

    @Override
    public abstract void characters(char ch[],
                           int start, int length) throws SAXException;


    public HashMap<String,String> getResult(){
        return this.result;
    }


}
