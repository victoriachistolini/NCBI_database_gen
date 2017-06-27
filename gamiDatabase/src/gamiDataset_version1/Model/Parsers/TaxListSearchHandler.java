/**
 * File: FileMenuController.java
 * @author Victoria Chistolini
 * Date: January 13, 2017
 */
package gamiDataset_version1.Model.Parsers;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * gets the QueryKey and WebEnv from an esearch
 */
public class TaxListSearchHandler extends ParserHandler {

    private boolean querykey;
    private boolean webEnv;


    public TaxListSearchHandler() {
        super();
        this.querykey = false;
        this.webEnv = false;
    }

    @Override
    public void startElement(String uri,
                             String localName, String qName, Attributes attributes)
            throws SAXException {
        if (qName.equalsIgnoreCase("QueryKey")) {
            this.querykey = true;
        } else if (qName.equalsIgnoreCase("WebEnv")) {
            this.webEnv = true;
        }
    }

    @Override
    public void endElement(String uri,
                           String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("IdList")) {
        }
    }

    @Override
    public void characters(char ch[],
                           int start, int length) throws SAXException {
        if (this.querykey) {
            this.result.put("key", new String(ch, start, length));
            this.querykey = false;
        } else if (this.webEnv) {
            this.result.put("web", new String(ch, start, length));
            this.webEnv = false;
        }


    }
}

