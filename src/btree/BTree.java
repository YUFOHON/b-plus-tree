package btree;

import java.util.List;

//we aim to implement a B+tree.
// We call the interface Btree just to make the name more concise

public interface BTree {

    void load(String datafilename);
    void insert(Integer key);
    void delete(Integer key);
    List<Integer> search(Integer key1, Integer key2);

    void printTree();
    void dumpStatistics();

    /**
     * Node (remember B+Tree has two kinds of nodes: index nodes and leaf node.)
     */
    class Node {
        private boolean isSplit;
        private boolean isMerge;
        
        public boolean isSplit() {
            return isSplit;
        }
        public boolean isMerge() {
            return isMerge;
        }

    }
}
