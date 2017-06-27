/**
 * File: SpeciesData.java
 * @author Victoria Chistolini
 * Date: January 12, 2017
 */
package gamiDataset_version1.Model;

import java.util.ArrayList;

/**
 * Data holder class for each species
 */
public class SpeciesData {

    //taxonomic ID for organism
    private String taxID;
    //chromosome #
    private String charLoc;
    // accession # 
    private String charAcc;
    // low index of gene
    private int start;
    // high index of gene
    private int stop;
    // strand of gene
    private int strand;
    // name of species
    private String commonName;
    // unique ID of gene
    private String geneId;
    // upstream gene
    private SpeciesData upstreamGene;
    // downstream gene
    private SpeciesData downstreamGene;
    // type of gene
    private String type;
    //string exonstart-exonstop, need to subtract chrm start.
    private ArrayList<String> exons;
    // intron regions of gene
    private ArrayList<IntronData> introns;




    public SpeciesData(String loc, String taxID, String accession,int start, int stop, int strand,
                            String name, String geneId  ){
        this.charLoc = loc;
        this.taxID = taxID;
        this.charAcc = accession;
        this.start=start;
        this.stop = stop;
        this.strand = strand;
        this.commonName = name;
        this.geneId = geneId;
        this.exons=new ArrayList<>();
        this.type="default";
        this.introns=new ArrayList<>();

    }



    public String getTaxID(){
        return this.taxID;
    }

    public String getCharLoc(){ return this.charLoc;}

    public String getCharAcc(){return  this.charAcc;}

    public String getCommonName() {return  this.commonName;}

    public String getGeneId() { return  this.geneId;}

    public int getStart(){return  this.start;}

    public int getStop() {return stop;}

    public int getStrand() {return strand;}

    public void addExon(String exonPair){
        this.exons.add(exonPair);
    }

    public String getType(){return this.type;}

    public void setUpstreamGene(SpeciesData upstreamGene1){this.upstreamGene=upstreamGene1;}

    public void setDownstreamGene(SpeciesData downGene1){this.downstreamGene=downGene1;}

    public SpeciesData getUpstreamGene(){return this.upstreamGene;}

    public SpeciesData getDownstreamGene(){return this.downstreamGene;}

    public ArrayList<String> getExonData(){return this.exons;}

    public void addIntron(IntronData newIntron){this.introns.add(newIntron);}

    public ArrayList<IntronData> getIntrons(){return this.introns;}



}
