package statsPackage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import triePackage.Trie;

public class Hasher {
    private final boolean DEBUG = false;

    public String previousWord ;

    public HashMap<String, Double> 
        bigramProbabilities, 
        probabilityMap, 
        correspondenceMatrix;

    public HashMap<String, Integer> newMessageMap;

    Trie vocabTrie;

    // Constructor
    public Hasher() {
        this.previousWord = null;
        this.bigramProbabilities = new HashMap<>();
        this.probabilityMap = new HashMap<>();
        this.correspondenceMatrix = new HashMap<>();
        this.newMessageMap = new HashMap<>();
        this.vocabTrie = new Trie();
    }

    // Accessors
    /**
     * @return  probability map
     */
    public HashMap<String, Double> getProbabilityMap() {
        return probabilityMap;
    }

    public double getAvgProbability () {
        return probabilityMap.values().stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.5);
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
            if (delta == 1.0) {
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
    public void addProbability (String word, double ratio) {
        probabilityMap.put(word, ratio);

        // if (DEBUG) {
        //     System.out.println("Attempt to insert probability<Word, Ratio>:");
        //     System.out.println("\tWORD:" + word + "\tRATIO:" + ratio);
        //     if (probabilityMap.containsKey(word)) {
        //         System.out.println("SUCCESS.");
        //     }
        // }
    }

    public void addNewMessage (String newWord, int count) {
        newMessageMap.put(newWord, count);
    }

    /**
     * Add vocab trie to the hasher
     * @param trie  input vocab trie
     */
    public void addVocabTrie(Trie trie) {
        if (DEBUG) {
            System.out.println("Adding vocabTrie.");
            System.out.println("EMPTY:" + trie.isEmpty());
        }
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
    public void hashOldMessages (ArrayList<String> words, double totalWordCount) {

        for (String temp : vocabTrie.toStringArrayList()) {
            double occurrences = 0;
            for (String tempWord : words) {
                if (tempWord.equals(temp)) {
                    occurrences++;
                }
            }

            double ratio = 0;
            if (occurrences > 0) {
                ratio = (occurrences / totalWordCount);
            }
            addProbability(temp, ratio);
        }
    }

    /**
     * 
     * @param newWord
     * @param isCorrectGuess
     */
    public void hashNewMessage (String newWord, boolean isCorrectGuess) {
        if (newMessageMap.containsKey(newWord)) {
            newMessageMap.compute(newWord, (_, value) -> value++);

            // Tunable bonus & penalty parameters
            double bonus = 1.0001;
            double penalty = 0.9999;

            if (isCorrectGuess) {
                probabilityMap.compute(
                    newWord,
                    (_, probability) -> probability * (bonus + (1 / (bonus + (newMessageMap.get(newWord)))))
                );
                newMessageMap.compute(newWord, (_, count) -> count * 0);
            } else {
                probabilityMap.compute(
                    newWord, 
                    (_, probability) -> probability * (penalty / newMessageMap.get(newWord))
                );
            }

        } else {
            addNewMessage(newWord, 1);
        }
    }

    public void computeNewProbabilities (String[] guesses, boolean isCorrectGuess) {
        for (String temp : guesses) {
            hashNewMessage(temp, isCorrectGuess);
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
            return 0.5;
        }

        double deltaRoundPlace = 1000;
        double targetRelativePos = correspondenceMatrix.get(target);
        double displacement = Math.round(
            Math.abs(targetRelativePos - wordPos) * deltaRoundPlace
            ) / deltaRoundPlace;

        return displacement;
    }
}