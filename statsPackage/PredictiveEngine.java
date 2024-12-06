package statsPackage;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import triePackage.Trie;

public class PredictiveEngine {
    Hasher hasher;
    HashMap<String, Integer> vocabMap;
    HashMap<String, Double> probabilityMap;
    Trie vocabTrie;

    // Constructor
    public PredictiveEngine (
        Hasher hasher,
        Trie vocabTrie
    ) {
        this.hasher = hasher;
        this.probabilityMap = hasher.getProbabilityMap();
        this.vocabTrie = vocabTrie;
    }

    /**
     * Get the keys corresponding to the top expected values
     * @param letter    'guess()' function input char
     * @param charPos   'guess()' function input char index
     * @return          3 expected values corresponding to
     *                  the 3 best guesses.
     */
    public String[] exValKeys(char letter, int charPos, int wordPos) {
        String[] topExValKeys = new String[3];
        hasher.addVocabTrie(vocabTrie);
    
        // Use the hasher's previousWord for context
        String previousWord = hasher.previousWord;
    
        // Fetch relevant words and their probabilities
        HashMap<String, Double> relevants = hasher.relevantHash(letter, charPos, wordPos, previousWord);
    
        // Process the top 3 predictions using a priority queue
        PriorityQueue<Map.Entry<String, Double>> minHeap = new PriorityQueue<>(
            Comparator.comparingDouble(Map.Entry::getValue)
        );
    
        for (Map.Entry<String, Double> entry : relevants.entrySet()) {
            minHeap.offer(entry);
            if (minHeap.size() > 3) {
                minHeap.poll();
            }
        }
    
        for (int i = 0; i < topExValKeys.length; i++) {
            if (!minHeap.isEmpty()) {
                topExValKeys[i] = minHeap.poll().getKey();
            }
        }
        return topExValKeys;
    }
}
