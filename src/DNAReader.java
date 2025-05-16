import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DNAReader {
    private String dna;
    private String dnaReverseComplement;

    public DNAReader(String dna){
        this.dna = dna.toUpperCase();
        this.dnaReverseComplement = computeReverseComplement(this.dna);
    }

    public Map<Integer, List<Integer>> locateATGIndices(String dnaInput) {
        Map<Integer, List<Integer>> atgIndices = new HashMap<>();
        for (int frame = 0; frame < 3; frame++) {
            atgIndices.put(frame, new ArrayList<>());
        }

        int atgIndex = 0;
        while (true) {
            atgIndex = dnaInput.indexOf("ATG", atgIndex);
            if (atgIndex == -1) break;

            int frame = atgIndex % 3;
            atgIndices.get(frame).add(atgIndex);
            atgIndex += 1;
        }

        return atgIndices;
    }

    public Map<Integer, List<Integer>> locateSortedStopCodonIndices(String dna) {
        Map<Integer, List<Integer>> stopCodonIndices = new HashMap<>();
        for (int frame = 0; frame < 3; frame++) {
            stopCodonIndices.put(frame, new ArrayList<>());
        }

        String[] stopCodons = {"TAA", "TAG", "TGA"};

        for (String codon : stopCodons) {
            int index = 0;
            while ((index = dna.indexOf(codon, index)) != -1) {
                int frame = index % 3;
                stopCodonIndices.get(frame).add(index);
                index += 1;
            }
        }

        for (List<Integer> indices : stopCodonIndices.values()) {
            indices.sort(Integer::compareTo);
        }

        return stopCodonIndices;
    }

    public Map<Integer, List<Entry<Integer, Integer>>> getStartStopPairsNoOverlapByFrame(
            Map<Integer, List<Integer>> startCodonsByFrame,
            Map<Integer, List<Integer>> stopCodonsByFrame) {

        Map<Integer, List<Entry<Integer, Integer>>> pairsByFrame = new HashMap<>();

        for (int frame = 0; frame < 3; frame++) {
            List<Integer> startCodons = startCodonsByFrame.getOrDefault(frame, new ArrayList<>());
            List<Integer> stopCodons = stopCodonsByFrame.getOrDefault(frame, new ArrayList<>());

            List<Entry<Integer, Integer>> pairs = new ArrayList<>();
            int stopPtr = 0;
            int lastGeneEnd = -1;

            for (int start : startCodons) {
                if (start < lastGeneEnd) {
                    continue;
                }

                while (stopPtr < stopCodons.size() && stopCodons.get(stopPtr) <= start) {
                    stopPtr++;
                }

                while (stopPtr < stopCodons.size()) {
                    int stop = stopCodons.get(stopPtr);
                    if ((stop - start) % 3 == 0) {
                        pairs.add(new SimpleEntry<>(start, stop));
                        lastGeneEnd = stop + 3;
                        break;
                    }
                    stopPtr++;
                }
            }

            pairsByFrame.put(frame, pairs);
        }

        return pairsByFrame;
    }

    public ArrayList<String> locateGenes() {
        Map<Integer, List<Integer>> startForward = locateATGIndices(dna);
        Map<Integer, List<Integer>> stopForward = locateSortedStopCodonIndices(dna);
        Map<Integer, List<Entry<Integer, Integer>>> pairsForward =
                getStartStopPairsNoOverlapByFrame(startForward, stopForward);

        Map<Integer, List<Integer>> startReverse = locateATGIndices(dnaReverseComplement);
        Map<Integer, List<Integer>> stopReverse = locateSortedStopCodonIndices(dnaReverseComplement);
        Map<Integer, List<Entry<Integer, Integer>>> pairsReverse =
                getStartStopPairsNoOverlapByFrame(startReverse, stopReverse);

        int maxPairs = -1;
        int bestFrame = -1;
        String bestStrand = "";
        List<Entry<Integer, Integer>> bestPairs = null;

        for (Map.Entry<Integer, List<Entry<Integer, Integer>>> entry : pairsForward.entrySet()) {
            if (entry.getValue().size() > maxPairs) {
                maxPairs = entry.getValue().size();
                bestFrame = entry.getKey();
                bestStrand = "Forward";
                bestPairs = entry.getValue();
            }
        }

        for (Map.Entry<Integer, List<Entry<Integer, Integer>>> entry : pairsReverse.entrySet()) {
            if (entry.getValue().size() > maxPairs) {
                maxPairs = entry.getValue().size();
                bestFrame = entry.getKey();
                bestStrand = "Reverse";
                bestPairs = entry.getValue();
            }
        }

        ArrayList<String> genes = new ArrayList<String>();
        if (bestPairs == null || bestPairs.isEmpty()) {
            return genes;
        }

        genes.add(String.format("Frame %d (%s):", bestFrame, bestStrand));

        for (Entry<Integer, Integer> pair : bestPairs) {
            int start = pair.getKey();
            int stop = pair.getValue();
            String geneSeq;

            if (bestStrand.equals("Forward")) {
                geneSeq = dna.substring(start, stop + 3);
            } else {
                geneSeq = dnaReverseComplement.substring(start, stop + 3);
                geneSeq = computeReverseComplement(geneSeq);
            }

            genes.add(geneSeq);
        }

        return genes;
    }


    private String computeReverseComplement(String seq) {
        StringBuilder revComp = new StringBuilder();
        for (int i = seq.length() - 1; i >= 0; i--) {
            char base = seq.charAt(i);
            switch (base) {
                case 'A': revComp.append('T'); break;
                case 'T': revComp.append('A'); break;
                case 'C': revComp.append('G'); break;
                case 'G': revComp.append('C'); break;
                default: revComp.append('N');
            }
        }
        return revComp.toString();
    }


}
