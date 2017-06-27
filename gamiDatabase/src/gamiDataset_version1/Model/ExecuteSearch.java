/**
 * File: ExecuteSearch.java
 * @author Victoria Chistolini
 * Date: January 12, 2017
 */

package gamiDataset_version1.Model;

import gamiDataset_version1.Model.Parsers.EfetchTypeParser;
import gamiDataset_version1.Model.Parsers.EsumaryHandler;
import gamiDataset_version1.Model.Parsers.TaxListSearchHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.io.*;
import static java.util.stream.Collectors.toList;


/**
 * handels search requests
 */
public class ExecuteSearch {

    // data holder for current information know about each species
    private List<SpeciesData> speciesDataList;
    // filename that contains the taxIDs that will be used in the search
    private String speciesListFilename;
    // gene that we will search
    private String geneName;
    // querry containing each species
    private String taxIdQuery;


    /**
     * initial search state collects basic info about gene for each species
     *
     * @param speciesListFileName filename with taxIDs
     * @param geneName gene build dataset for
     */
    public ExecuteSearch(String speciesListFileName, String geneName){

        this.speciesListFilename = speciesListFileName;
        this.geneName = geneName;
        this.speciesDataList = new ArrayList<>();
        this.taxIdQuery = this.generateTaxIdPath();
        this.gatherBasicInformation();

    }

    /**
     * generates the taxID list in search path notation
     * @return search path including all species
     */
    private String generateTaxIdPath(){

        List<String> targSpecies;
        String taxWeb = "%5BTaxonomy+ID%5D+OR+";
        String query="+";

            try {
                String filePath = "src/gamiDataset_version1/SystemFiles/" + this.speciesListFilename;
                targSpecies = Files.lines(Paths.get(filePath))
                        .map(line -> line.split(" "))
                        .flatMap(Arrays::stream)
                        .collect(toList());

                for (String species : targSpecies){
                    query += species + taxWeb;
                }

        } catch (IOException e) {
            e.printStackTrace();
        }

    return query;

    }




    /**
     * Gathers chromosome number, accession number, start and
     * stop gene index and strand number
     * each line in targetGeneInfo corresponds to 1 species.
     * Information is stored in the speciesDataList
     */
    private void gatherBasicInformation(){

        String searchPath = String.format("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?" +
                "db=gene&term=%s[Gene+Name]+AND+(%s+)+AND+alive[prop]&usehistory=y",this.geneName, this.taxIdQuery);
        this.handleNewURL(searchPath, "searchResults.txt");
        HashMap<String,String> parsedResults = XMLHandler.parseXML(
                new File("searchResults.txt"), new TaxListSearchHandler());

        String uploadPath = String.format("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/" +
                        "esummary.fcgi?db=gene&query_key=%s&WebEnv=%s",
                        parsedResults.get("key"), parsedResults.get("web"));


        this.handleNewURL(uploadPath, "uploads.XML");
        this.speciesDataList = XMLHandler.parseXML(new File("uploads.XML"), new EsumaryHandler());


    }


    
    /**
     * download exons for each species, calls for data to be parsed
     */
    public void getExonData(){

        for (SpeciesData entry : this.speciesDataList) {

            String exonDataPath2 = String.format("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?"+
                                                "db=gene&id=%s&rettype=gene_table&retmode=text", entry.getGeneId());
            this.handleNewURL(exonDataPath2, "exonUpload.txt");
            List<String> targSpecies=null;

            try {
                String filePath = "exonUpload.txt";
                targSpecies = Files.lines(Paths.get(filePath))
                        .map(line -> line.split("\n"))
                        .flatMap(Arrays::stream)
                        .collect(toList());



            } catch (IOException e) {
                e.printStackTrace();
            }

            this.parse(targSpecies,entry);
        }

    }


    /**
     * calculate introns different protocol is followed based on strand
     */
    public void getIntrons(){

        for (SpeciesData entry: this.speciesDataList){


            if (entry.getStrand()==1){
                this.findIntronsMinus(entry);
            }
            else {
                this.findIntronsPlus(entry);
            }
        }
    }

    /**
     * when we calculate introns on minus strand
     * we do it in reverse
     * @param entry data holder for current species
     */
    private void findIntronsMinus(SpeciesData entry){
        ArrayList<String> exons = entry.getExonData();

        for(int i=1;i<exons.size();i++){

            String[] exon1 = exons.get(exons.size()-i).split("-");
            String[] exon2 = exons.get(exons.size()-i-1).split("-");

            int intronStart = Integer.parseInt(exon1[0])+1;
            int intronStop = Integer.parseInt(exon2[1])-1;

            IntronData newIntron = new IntronData(intronStart,intronStop);
            entry.addIntron(newIntron);



        }
    }


    /**
     * for plus strand introns are calculated from
     * end of previous exon to start of next exon
     */
    private void findIntronsPlus(SpeciesData entry){
        //add 1 to both start and stop (zero indexed)

        ArrayList<String> exons = entry.getExonData();

        for(int i=1;i<exons.size();i++){
            String[] exon1 = exons.get(i-1).split("-");
            String[] exon2 = exons.get(i).split("-");

            int intronStart = Integer.parseInt(exon1[1])+1;
            int intronStop = Integer.parseInt(exon2[0])-1;

            IntronData newIntron = new IntronData(intronStart,intronStop);
            entry.addIntron(newIntron);

        }

    }

    /**
     * search range upstream and downstream of genes
     * until upstream/downstream genes are found
     *
     */
    public void findUpstreamDownstreamGenes() {

        for (SpeciesData entry : this.speciesDataList) {


          //while loop over return of upstreamSearch...
            //index keeps track of offset
            // max iterations = 10 ?? (maybe)
            // change the range with each search
            // do not re-search the provious range


            boolean upstreamFound=false;
            int startRange=300000;
            int increaseRange=0;


            while(!upstreamFound) {

                String upstreamSearchPath = String.format("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?" +
                                "db=gene&term=%s[chr]+AND+%d:%d[chrpos]+AND+%s[Taxonomy+ID]+AND+alive[prop]&usehistory=y",
                        entry.getCharLoc(), entry.getStart() - startRange, entry.getStart()-increaseRange, entry.getTaxID()

                );

                upstreamFound=this.upstreamSearch(entry, upstreamSearchPath);

                startRange+=100000;
                increaseRange+=100000;
            }

            boolean downstreamFound=false;
            startRange=300000;
            increaseRange=0;

            while(!downstreamFound) {

                String upstreamSearchPath = String.format("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?" +
                                "db=gene&term=%s[chr]+AND+%d:%d[chrpos]+AND+%s[Taxonomy+ID]+AND+alive[prop]&usehistory=y",
                        entry.getCharLoc(), entry.getStop() + increaseRange, entry.getStop()+startRange, entry.getTaxID()

                );

                downstreamFound=this.downstreamSearch(entry, upstreamSearchPath);

                startRange+=100000;
                increaseRange+=100000;
            }


            //swap upstream and downstream if on the 'non-coding strand'
            if(entry.getStrand()==1){

                SpeciesData trueUp = entry.getDownstreamGene();
                entry.setDownstreamGene(entry.getUpstreamGene());
                entry.setUpstreamGene(trueUp);
            }

        }//for each




        }



	/*
	 * for each candidate in the current upstream region, determine if 
	 * one of these candidates is actually the true upstream gene
	 * @return true if upstream gene found
	 */
    private boolean upstreamSearch(SpeciesData entry, String searchPath){

       ArrayList<SpeciesData> candidates =
               this.handelSearching(searchPath, "upstreamResults.XML","upstreamUploads.XML");

        boolean geneFound=false;

        while(!geneFound){

            int typeResult=this.candidateUpstreamSearch(candidates,entry);

            if(typeResult==-1){
                return false;
            } else if (typeResult==1){
                //geneFound=true;
                return true;
            }


        }


        return geneFound;
    }


    /*
     * find clostest candidate upstream gene search it to see if it is valid type
     * @return -1 if no upstream gene, 1 if found 
     */
    private int candidateUpstreamSearch(ArrayList<SpeciesData> candidates, SpeciesData entry){

        SpeciesData upstream=null;

        for(SpeciesData candidate : candidates) {
            if (candidate.getStart() < entry.getStart() && candidate.getStop() < entry.getStart()) {

                if (upstream == null) {
                    upstream = candidate;
                } else {
                    if (upstream.getStop() < candidate.getStop()) {
                        upstream = candidate;
                    }
                }

            }
        }

        if (upstream==null){
            return -1;
        }

        candidates.remove(upstream);

        if(this.searchType(upstream,entry,"upSearch.XML",1)){
            return 1;
        }

        return 0;

    }


	/*
	 * for each closest non-overlapping downstream candidate verify type
	 * @return true if downstream gene found
	 */
    private boolean downstreamSearch(SpeciesData entry, String searchPath){

        ArrayList<SpeciesData> candidates =
                this.handelSearching(searchPath, "downstreamResults.XML","downstreamUploads.XML");

        boolean geneFound=false;

        while(!geneFound){

            int typeResult=this.candidateDownstreamSearch(candidates,entry);

            if(typeResult==-1){
                return false;
            } else if (typeResult==1){
                //geneFound=true;
                return true;
            }


        }


        return geneFound;
    }



    /*
     * find clostest candidate upstream gene search it to see if it is valid
     * @return -1 if no upstream gene, 1 if upstream gene found 
     */
    private int candidateDownstreamSearch(ArrayList<SpeciesData> candidates, SpeciesData entry){

        SpeciesData downstream=null;

        for(SpeciesData candidate : candidates) {


            if (candidate.getStart()>entry.getStart() && candidate.getStop()>entry.getStart()) {

                if(downstream==null){
                    downstream=candidate;
                }
                else {
                    if (downstream.getStart()>candidate.getStart()){
                        downstream=candidate;

                    }
                }

            }

        } //for each

        if (downstream==null){
            return -1;
        }

        candidates.remove(downstream);

        if(this.searchType(downstream,entry,"downSearch.XML",0)){
            return 1;
        }

        return 0;

    }


    /**
     * Searches to see if candidate upstream / downstream gene is valid type
     * Filter out genes classified as 0 = unknown, 7 = pseudo, 10 = noncoding RNA
     * 11 = biological region, 12 = other
     *
     * @param candidate potential upstream or downstream gene
     * @param entry target gene
     * @param fileName files to store uploaded content for parsing
     * @param upstreamInd if 1 put valid candidate into upstream field in entry otherwise in downstream field
     * @return true if valid gene type was sucessfully found
     */
    private boolean searchType(SpeciesData candidate, SpeciesData entry, String fileName,int upstreamInd){


        String searchPath = String.format("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?" +
                "db=gene&term=%s[uid]+AND+alive[prop]&usehistory=y", candidate.getGeneId());

        this.handleNewURL(searchPath,fileName);

        HashMap<String,String> parsedResults = XMLHandler.parseXML(
                new File(fileName), new TaxListSearchHandler());

        String uploadPath = String.format("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/" +
                        "efetch.fcgi?db=gene&query_key=%s&WebEnv=%s&format=xml",
                parsedResults.get("key"), parsedResults.get("web"));

        this.handleNewURL(uploadPath, fileName);

        HashMap<String,String> typeResult = XMLHandler.parseXML(
                    new File(fileName), new EfetchTypeParser());

        int result = Integer.parseInt(typeResult.get("typeVal"));

        if (result==0 || result==7 || result==12 || result==10 || result==11){
            return false;
        }

        if(upstreamInd==1){
            entry.setUpstreamGene(candidate);
        } else {
            entry.setDownstreamGene(candidate);
        }

        return true;

    }

	/*
	 * special search for upstream/downstream to create list of candidate upstream/downstream
	 * in specific search area
	 * @return list of candidates 
	 */
    private ArrayList<SpeciesData> handelSearching(String path, String searchResultsFile, String uploadsFile){
        this.handleNewURL(path, searchResultsFile);

        HashMap<String,String> parsedResults = XMLHandler.parseXML(
                new File(searchResultsFile), new TaxListSearchHandler());

        String uploadPath = String.format("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/" +
                        "esummary.fcgi?db=gene&query_key=%s&WebEnv=%s",
                parsedResults.get("key"), parsedResults.get("web"));


        this.handleNewURL(uploadPath, uploadsFile);
        ArrayList<SpeciesData> candidates = XMLHandler.parseXML(new File(uploadsFile), new EsumaryHandler());
        return candidates;
    }


    /**
     * The parser assumes that the first column of the tabel Genomic Interval Exon
     * is completely filled out
     * @param lines
     * @param entry
     * @return 1 if successful parse
     */
    private int parse(List<String> lines, SpeciesData entry){
        boolean parse = false;

        for (String line : lines) {

            //indexes come after this line
            if (line.contains("--------------------")) {
                parse = true;
            }


            if (parse) {

                //break after first tabel (sometimes there are more tables)
                if(line.isEmpty()){
                    return 1;
                }
                String[] splitStr = line.split("\\s+");
                if (splitStr.length > 1) {
                   entry.addExon(splitStr[0]);

                }
            }

        }

        return 0;

    }

    /**
     * builds new URL request, writes results to file 
     *
     * @param urlPath
     * @param filename
     */
    private void handleNewURL(String urlPath, String filename) {

        URL url = null;
        try {
            url = new URL(urlPath);
            URLConnection con = url.openConnection();
            InputStream is = con.getInputStream();


            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(filename), "utf-8"));

            String line = null;

            // read each line and write to System.out
            while ((line = br.readLine()) != null) {
                line+="\n";
                writer.write(line);
            }
            writer.close();


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

	/*
	 * writes out summary file of dataset parameters 
	 */
    public void writeNotesFile(){

        try {
            Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream("Gene_Summary_file.txt"), "utf-8"));

            for (SpeciesData entry: this.speciesDataList){

                String targetGeneInfo=String.format("Species: %s\nTaxonomy ID: %s\n\nTarget Gene ID: %s\n"+
                "Chromosome #: %s       Accession #: %s\nStart Index: %d      Stop Index: %d        Strand: %d\n\n",
                        entry.getCommonName(), entry.getTaxID(),entry.getGeneId(),entry.getCharLoc(), entry.getCharAcc(),
                        entry.getStart(),entry.getStop(),entry.getStrand()
                        );


                writer.write(targetGeneInfo);

                SpeciesData upstreamGene = entry.getUpstreamGene();
                String upstreamGeneInfo=String.format("Upstream Gene ID: %s\n"+
                "Start Index: %d      Stop Index: %d        Strand: %d\n\n",upstreamGene.getGeneId(),
                upstreamGene.getStart(),upstreamGene.getStart(),upstreamGene.getStrand());

                writer.write(upstreamGeneInfo);


                SpeciesData downGene = entry.getDownstreamGene();
                String downGeneInfo=String.format("Downstream Gene ID: %s\n"+
                                "Start Index: %d      Stop Index: %d        Strand: %d\n\n",downGene.getGeneId(),
                        downGene.getStart(),downGene.getStart(),downGene.getStrand());

                writer.write(downGeneInfo);

                String sep="——————————————————————————————————————————————————————————————————————————————\n\n";

                writer.write(sep);

            }

            writer.close();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



	/*
	 * downloads sequence to fasta.txt
	 * uses three part download process
	 * first download 'upstream', then introns then 'downstream' sequence.
	 */
    public void fetchSequence(){


        //delete preexisting append final sequence file!!!

        try {
            Files.delete(Paths.get("fasta.txt"));
        } catch (IOException e) {
            System.out.println("no pre-existing fasta file");
        }

        File fastaOut = new File("fasta.txt");
        try {
            fastaOut.createNewFile();
        } catch (IOException e) {
            System.out.println("could not creat file");
        }

        for (SpeciesData entry: this.speciesDataList){

            String nucSearch = String.format("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?" +
                    "db=nucleotide&term=%s[Accession]&usehistory=y", entry.getCharAcc());

            this.handleNewURL(nucSearch,"nucSearch.txt");

            HashMap<String,String> parsedResults = XMLHandler.parseXML(
                    new File("nucSearch.txt"), new TaxListSearchHandler());


            String key = parsedResults.get("key");
            String web = parsedResults.get("web");

            this.handleUpstreamSequenceUpload(entry,key,web);
            this.handleIntronSequenceUploads(entry,key,web);
            this.handleDownstreamSequenceUpload(entry,key,web);




        }

    }


	/*
	 * either upstream gene or downstream gene will be start of upper sequence 
	 * this depends of strand.
	 */
    private void handleUpstreamSequenceUpload(SpeciesData entry, String key, String web){

        int startIndex;
        int stopIndex = entry.getStart();

        if (entry.getStrand()==1) {
            startIndex = entry.getDownstreamGene().getStop() + 1;
        } else {
            startIndex = entry.getUpstreamGene().getStop() + 1;
        }

        String downloadPath = String.format("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?" +
                        "db=nucleotide&query_key=%s&WebEnv=%s&rettype=fasta&retmode=text&from=%d&to=%d",
                key, web, startIndex,stopIndex );

        this.handleAppendUrl(downloadPath,false);

    }



	/*
	 * intron sequence upload is straight forward thanks to pre-calculated intron regions
	 */
    private void handleIntronSequenceUploads(SpeciesData entry, String key, String web){
        ArrayList<IntronData> introns = entry.getIntrons();

        for (IntronData intron : introns){
            System.out.println(String.format("%d - %d", intron.getStart(), intron.getStop()));
            String downloadPath = String.format("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?" +
                            "db=nucleotide&query_key=%s&WebEnv=%s&rettype=fasta&retmode=text&from=%d&to=%d",
                    key, web, intron.getStart(),intron.getStop());
            this.handleAppendUrl(downloadPath,true);

        }


    }


	/*
	 * either upstream gene or downstream gene will be stop of lower sequence 
	 * this depends of strand.
	 */
    private void handleDownstreamSequenceUpload(SpeciesData entry, String key, String web){

        int stopIndex;
        int startIndex = entry.getStop()+2;

        if (entry.getStrand()==1) {
            stopIndex = entry.getUpstreamGene().getStart() - 1;
        } else {
            stopIndex = entry.getDownstreamGene().getStart() - 1;
        }

        String downloadPath = String.format("https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?" +
                        "db=nucleotide&query_key=%s&WebEnv=%s&rettype=fasta&retmode=text&from=%d&to=%d",
                key, web, startIndex,stopIndex );
        this.handleAppendUrl(downloadPath,true);


    }



	/*
	 * used to create a url request that appends to a file rather than overwrites.
	 */
    private void handleAppendUrl(String urlp, boolean ExcludeHeader){
        URL url = null;
        try {
            url = new URL(urlp);
            URLConnection con = url.openConnection();
            InputStream is = con.getInputStream();
            Path filePath = Paths.get("fasta.txt");

            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String line = null;

            // read each line and write to System.out
            while ((line = br.readLine()) != null) {
                line+="\n";
                if(line.contains(">") && ExcludeHeader){
                    continue;
                }
                else if(!line.isEmpty()) {
                    Files.write(filePath, line.getBytes(), StandardOpenOption.APPEND);
                }
            }





        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


	/*
	 * @return the species data list, which holds all information about the gene
	 *         for each species.
	 */
    public List<SpeciesData> getSpeciesDataList(){
        return this.speciesDataList;
    }

	/*
	 * @return the gene to be searched
	 */
    public String getGeneName(){return this.geneName;}
    
	/*
	 * @return the file which contains all taxIds.
	 */
    public String getSpeciesListFilename(){return this.speciesListFilename;}



}

