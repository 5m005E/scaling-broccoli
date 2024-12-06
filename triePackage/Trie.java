package triePackage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Trie {
    private final TrieNode root;
    public ArrayList<String> resultant;
    private ArrayList<String> accessibleVocab;

    // Constructor
    public Trie() {
        this.root = new TrieNode();
        this.resultant = new ArrayList<>();
        this.accessibleVocab = new ArrayList<>();
    }

    // Accessors
    /**
     * @return  get the root of the trie.
     */
    public TrieNode getRoot() {
        return root;
    }

    /**
     * Retrieve a list of strings pertaining to a character and its index.
     * @param targetChar    target character
     * @param targetPos     target character position
     * @return              list of valid resultant strings.
     */
    public List<String> find(char targetChar, int targetPos) {
        if (targetChar == '\0' || targetPos < 0) {
            throw new IllegalArgumentException("Invalid targetChar, targetPos");
        }

        this.resultant = new ArrayList<>();
        StringBuilder currentString = new StringBuilder();
        collect(currentString, root, 0, targetChar, targetPos);
        return resultant;
    }

    /**
     * Recursive function; collect valid search options
     * @param currentString     StringBuilder object representing current string
     * @param currentNode       current node in the iteration
     * @param currentIndex      current index in the iteration
     * @param targetChar        target character
     * @param targetPos         target character position
     */
    public void collect (
        StringBuilder currentString,
        TrieNode currentNode,
        int currentIndex,
        char targetChar,
        int targetPos
    ) {
        if (currentNode == null) {
            return;
        }

        if (currentIndex == targetPos) {
            if (currentNode.getChildren().containsKey(targetChar)) {
                currentString.append(targetChar);
                collectAll(currentNode.getChildren().get(targetChar), currentString);

                if (currentString.length() > 0) {
                    currentString.deleteCharAt(currentString.length() - 1);
                }
            }
            return;
        }

        for (Map.Entry<Character, TrieNode> tempEntry : currentNode.getChildren().entrySet()) {
            currentString.append(tempEntry.getKey());
            collect(currentString, tempEntry.getValue(), currentIndex + 1, targetChar, targetPos);
            if (currentString.length() > 0) {
                currentString.deleteCharAt(currentString.length() - 1);
            }
        }
    }

    /**
     * Recursive function; collect & store all valid search options
     * @param currentNode
     * @param currentString
     */
    public void collectAll (
        TrieNode currentNode,
        StringBuilder currentString
    ) {
        if (currentNode == null) {
            return;
        }

        if (currentNode.pathEnd) {
            resultant.add(currentString.toString());
        }

        for (Map.Entry<Character, TrieNode> tempEntry : currentNode.getChildren().entrySet()) {
            TrieNode child = tempEntry.getValue();

            currentString.append(tempEntry.getKey());
            collectAll(child, currentString);

            if (currentString.length() > 0) {
                currentString.deleteCharAt(currentString.length() - 1);
            }
        }
    }

    // Modifiers
    public void insert (final char[] wordCharArray) {
        if (wordCharArray == null || wordCharArray.length == 0) {
            return;
        }

        TrieNode current = root;
        for (char temp : wordCharArray) {
            current.children.putIfAbsent(temp, new TrieNode());
            current = current.children.get(temp);
        }
        current.pathEnd = true;
    }

    // Utilities
    /**
     * @param word  target word
     * @return      whether the target word exists in the trie.
     */
    public boolean contains (String word) {
        TrieNode current = root;
        char[] wordCharArray = word.toCharArray();
        for (char temp : wordCharArray) {
            current = current.children.get(temp);
            if (current == null) {
                return false;
            }
        }
        return current.pathEnd;
    }

    public ArrayList<String> toStringArrayList () {
        accessNode(root, new StringBuilder());
        return accessibleVocab;
    }

    public void accessNode (TrieNode node, StringBuilder currentString) {
        if (node.pathEnd) {
            accessibleVocab.add(currentString.toString());
        }

        for (Map.Entry<Character, TrieNode> entry : node.getChildren().entrySet()) {
            currentString.append(entry.getKey());
            accessNode(entry.getValue(), currentString);

            if (currentString.length() > 0) {
                currentString.deleteCharAt(currentString.length() - 1);
            }
        }
    }

    // Predicates
    public boolean isEmpty() {
        return root.children.isEmpty();
    }
}