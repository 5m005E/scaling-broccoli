package triePackage;
import java.util.HashMap;

public class TrieNode {
    HashMap<Character, TrieNode> children;
    boolean pathEnd;

    public TrieNode () {
        this.children = new HashMap<>();
        this.pathEnd = false;
    }

    // Accessors
    public HashMap<Character, TrieNode> getChildren () {
        return children;
    }

    // Precicates
    /**
     * @return  true if the node has no children.
     */
    public boolean leafPredicate () {
        return (children.isEmpty());
    }
}

