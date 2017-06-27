/**
 * File: MultiParserHandler.java
 * @author Victoria Chistolini
 * Date: January 16, 2017
 */
package gamiDataset_version1.Model.Parsers;

import gamiDataset_version1.Model.SpeciesData;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Datatype class for parsers that return multiple results
 */
public abstract class MultiParserHandler extends ParserHandler {

    public abstract ArrayList<SpeciesData> getAllResults();


}
