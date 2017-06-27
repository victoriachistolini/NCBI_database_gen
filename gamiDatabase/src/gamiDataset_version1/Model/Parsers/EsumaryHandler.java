/**
 * File: EsumaryHandler.java
 * @author Victoria Chistolini
 * Date: January 16, 2017
 */
package gamiDataset_version1.Model.Parsers;

import gamiDataset_version1.Model.SpeciesData;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.ArrayList;

/**
 * parses Esumary data for the initial BASIC data
 */
public class EsumaryHandler extends MultiParserHandler{


    private boolean geneId;

    private String qName;

    private String geneIdval;
    private ArrayList<SpeciesData> speciesList;

    private String commmonName;
    private int chrStart;
    private int chrStop;
    private String accessVal;
    private String chrLoc;
    private String taxId;




    public EsumaryHandler() {
        super();
        this.geneId = false;
        this.speciesList=new ArrayList<>();
        this.commmonName=null;
        this.chrStop=0;
        this.chrStart=0;
        this.accessVal=null;
        this.taxId=null;
    }

    @Override
    public void startElement(String uri,
                             String localName, String qName, Attributes attributes)
            throws SAXException {

        this.qName = qName;
         if (qName.equalsIgnoreCase("DocumentSummary")){
             String idVal = attributes.getValue("uid");
             this.geneIdval = idVal;
             this.geneId=true;
         }

    }

    @Override
    public void endElement(String uri,
                           String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("DocumentSummary")) {
        }
    }

    @Override
    public void characters(char ch[],
                           int start, int length) throws SAXException {



        String val = new String(ch, start, length);

        if (this.qName.equals("NomenclatureStatus")){ // start case
            this.geneId=true;
        }
        else if (this.qName.equals("ChrStart") && this.geneId){
            chrStart=Integer.parseInt(val);
        }

        else if (this.qName.equals("ChrStop") && this.geneId ){
            chrStop=Integer.parseInt(val);

        }
        else if (this.qName.equals("CommonName")){
            commmonName=val;
        }
        else if (this.qName.equals("ChrAccVer") && this.geneId){
            accessVal=val;
        }
        else if (this.qName.equals("ChrLoc") && this.geneId){
            chrLoc=val;
        }
        else if (this.qName.equals("TaxID")){ //last element to be parsed
            taxId=val;
            int strand=0;
            if(chrStart>chrStop) {
                int temp = chrStart;
                chrStart=chrStop;
                chrStop = temp;
                strand=1;
            }

            SpeciesData newEntry = new SpeciesData(chrLoc,taxId,accessVal,chrStart,chrStop,strand,commmonName,this.geneIdval);
            this.speciesList.add(newEntry);
        }

        else if(this.qName.equals("ExonCount")){ //end case for repetitive chrLoc,...
            this.geneId=false;
        }



    }

    public ArrayList<SpeciesData> getAllResults(){
        return this.speciesList;
    }
}
