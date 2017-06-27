/**
 * File: IntronData.java
 * @author Victoria Chistolini
 * Date: January 23, 2017
 */
package gamiDataset_version1.Model;

/**
 * Holds data for intronic regions
 */
public class IntronData {

        private int start;
        private int stop;

        public IntronData(int start, int stop){
            this.start=start;
            this.stop=stop;

        }

        public int getStart(){return this.start;}

        public int getStop(){return this.stop;}


    }

