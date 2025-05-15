import java.util.ArrayList;

public class DNAReader {
    private String dna;

    public DNAReader(String dna){
        this.dna = dna.toUpperCase();
    }

    public ArrayList<String> locateGenes(){
        int atgIndex = 0;
        ArrayList<String> genes = new ArrayList<>();

        while(true){
            atgIndex = dna.indexOf("ATG", atgIndex);
            if(atgIndex == -1){break;}

            int stopIndex = findStopCodon(dna, atgIndex);
            if(stopIndex != -1) {
                genes.add( "Frame offset: " + (atgIndex % 3) + " | Gene: " + dna.substring(atgIndex, stopIndex + 3) + " | Index: " + atgIndex);
            }

            atgIndex = atgIndex + 1;

        }
        return genes;
    }

    public int findStopCodon(String dna, int startIndex){
        int stopIndex = Integer.MAX_VALUE;
        boolean found = false;

        for(String codon : new String[]{"TAA", "TAG", "TGA"}){
            int compareIndex = dna.indexOf(codon, startIndex + 3);
            while (compareIndex != -1) {
                if ((compareIndex - startIndex) % 3 == 0) {
                    if (compareIndex < stopIndex) {
                        stopIndex = compareIndex;
                        found = true;
                    }
                    break;
                }
                compareIndex = dna.indexOf(codon, compareIndex + 1);
            }
        }
        if(!found){
            stopIndex = -1;
        }

        return stopIndex;
    }

}
