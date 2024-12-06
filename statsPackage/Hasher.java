package statsPackage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import triePackage.Trie;

public class Hasher {
    private final boolean DEBUG = false;

    public String previousWord ;
    public HashMap<String, Double> bigramProbabilities; // Bigram probabilities
    public HashMap<String, Double> probabilityMap;
    public HashMap<String, Double> correspondenceMatrix;
    Trie vocabTrie;

    // Constructor
    public Hasher() {
        this.previousWord = null;
        this.bigramProbabilities = new HashMap<>();
        this.probabilityMap = new HashMap<>();
        this.correspondenceMatrix = new HashMap<>();
        this.vocabTrie = new Trie();
    }

    /**
     * @return  probability map
     */
    public HashMap<String, Double> getProbabilityMap() {
        if (DEBUG) {
            System.out.println("probability map:");
            System.out.println(probabilityMap);
        }

        return probabilityMap;
    }

    /**
     * Adjust probabilities based on letter, char position, and relative word position.
     * @param letter    input letter from 'guess()'
     * @param charPos   input char position from 'guess()'
     * @param wordPos   input relative word position from 'guess()'
     * @return          HashMap of words and adjusted probabilities
     */
    public HashMap<String, Double> relevantHash (char letter, int charPos, int wordPos) {
        HashMap<String, Double> hashed = new HashMap<>();
        List<String> relevants = new ArrayList<>(vocabTrie.find(letter, charPos));

        if (DEBUG) {
            System.out.println("probability map:");
            for (Entry<String, Double> entry : probabilityMap.entrySet()) {
                System.out.println("\tKey:" + entry.getKey() + "\tValue:" + entry.getValue());
            }
        }

        for (String temp : relevants) {
            double delta = relativePos(wordPos, temp);
            if (delta == 1.0) { // Skip words with no correspondence
                continue;
            }

            final double deltaWeight = 0.01; // Tunable parameter for scaling delta
            double adjustedProbability = probabilityMap.get(temp) * (1 - (deltaWeight * delta));

            hashed.put(temp, adjustedProbability);
        }
        return hashed;
    }

    /**
     * Add the probability of a word to the probabilityMap.
     * @param word      individual word from the 'old messages' file
     * @param ratio     (occurrences):(total word count)
     */
    public void addProbability(String word, double ratio) {
        probabilityMap.put(word, ratio);

        if (DEBUG) {
            System.out.println("Attempt to insert probability<Word, Ratio>:");
            System.out.println("\tWORD:" + word + "\tRATIO:" + ratio);
            if (probabilityMap.containsKey(word)) {
                System.out.println("SUCCESS.");
            }
        }

    }

    /**
     * Add vocab trie to the hasher
     * @param trie  input vocab trie
     */
    public void addVocabTrie(Trie trie) {
        vocabTrie = trie;
    }

    /**
     * Update or initialize the relative position of a word in the correspondence matrix.
     * @param a           input word
     * @param pos         input word's position in the line
     * @param lineLength  total length of the line
     */
    public void addCorrespondence (String a, int pos, int lineLength) {
        double stringPos = 0;
        if (lineLength > 1) {
            stringPos = (double) pos / (lineLength - 1);
        }
        correspondenceMatrix.merge(a, stringPos, (oldPos, newPos) -> (oldPos + newPos) / 2);
    }

    // Utilities
    /**
     * Update probabilities based on word occurrences in old messages.
     * @param words list of words from old messages
     */
    public void hashOldMessages (ArrayList<String> words) {
        int totalWordCount = words.size();

        for (String temp : vocabTrie.toStringArrayList()) {
            int occurrences = 0;
            for (String tempWord : words) {
                if (tempWord.equals(temp)) {
                    occurrences++;
                }
            }

            double occurrencesDouble = occurrences;
            double wordCountDouble = totalWordCount;
            double ratio = 0;
            if (occurrences > 0) {
                ratio = (occurrencesDouble / wordCountDouble);
            }
            addProbability(temp, ratio);
        }
    }

    /**
     * Compute the relative displacement between a target word and the expected word position.
     * @param wordPos       relative position of the target word in the current line
     * @param target        word being evaluated
     * @return              normalized difference in positions (0 = most identical, 1 = most mismatched)
     */
    public double relativePos (int wordPos, String target) {
        if (!correspondenceMatrix.containsKey(target)) {
            return 1.0;
        }

        double targetRelativePos = correspondenceMatrix.get(target);
        double displacement = Math.abs(targetRelativePos - wordPos);

        return displacement;
    }
}