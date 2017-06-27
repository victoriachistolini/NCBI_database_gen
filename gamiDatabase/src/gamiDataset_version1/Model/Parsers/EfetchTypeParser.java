/**
 * File: EfetchTypeParser.java
 * @author Victoria Chistolini
 * Date: January 19, 2017
 */
package gamiDataset_version1.Model.Parsers;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * parses gene type
 */
public class EfetchTypeParser extends ParserHandler {

    private boolean typeVal;


    public EfetchTypeParser() {
        super();
        this.typeVal = false;

    }

    @Override
    public void startElement(String uri,
                             String localName, String qName, Attributes attributes)
            throws SAXException {
        if (qName.equalsIgnoreCase("Entrezgene_type")) {
            this.typeVal = true;
        }
    }

    @Override
    public void endElement(String uri,
                           String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("Entrezgene-Set")) {
        }
    }

    @Override
    public void characters(char ch[],
                           int start, int length) throws SAXException {
        if (this.typeVal) {
            this.result.put("typeVal", new String(ch, start, length));
            this.typeVal = false;


        }
    }


}
