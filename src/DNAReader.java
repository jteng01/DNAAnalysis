import java.util.ArrayList;

public class DNAReader {
    private String dna;

    public DNAReader(String dna){
        this.dna = dna.toUpperCase();
    }

    public ArrayList<String> locateGenes(){
        ArrayList<String> genes = new ArrayList<>();

        genes.addAll(locateGenesInStrand(dna, false));

        String reverseComp = getReverseComplement(dna);
        genes.addAll(locateGenesInStrand(reverseComp, true));

        return genes;
    }

    private ArrayList<String> locateGenesInStrand(String strand, boolean isReverse) {
        int atgIndex = 0;
        ArrayList<String> genes = new ArrayList<>();

        while(true){
            atgIndex = strand.indexOf("ATG", atgIndex);
            if(atgIndex == -1){break;}

            int stopIndex = findStopCodon(strand, atgIndex);
            if(stopIndex != -1) {
                genes.add("Gene: " + strand.substring(atgIndex, stopIndex + 3) + " | Index: " + atgIndex + " | Reverse: " + isReverse
                );
            }

            atgIndex = (stopIndex != -1) ? stopIndex + 1 : atgIndex + 3;
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
        return found ? stopIndex : -1;
    }

    public String getReverseComplement(String sequence){
        StringBuilder reverseComp = new StringBuilder();

        for (int i = sequence.length() - 1; i >= 0; i--) {
            char base = sequence.charAt(i);
            switch(base){
                case 'A': reverseComp.append('T'); break;
                case 'T': reverseComp.append('A'); break;
                case 'C': reverseComp.append('G'); break;
                case 'G': reverseComp.append('C'); break;
                default: reverseComp.append('N');
            }
        }

        return reverseComp.toString();
    }
}