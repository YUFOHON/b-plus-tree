package btree;
import java.util.*;

import Utils.Clock;
import Utils.Utils;
import Utils.Utils.KeyNotFoundException;
import Utils.Utils.TreeIsEmptyException;
import Utils.CLI;
import Utils.readFile;
import Utils.Config;


public class MandyTree implements BTree {
    //Tree specific parameters here
    private double MIN_FILL_FACTOR = 0.5;
    private int DEGREE = 4;
    private Node root = null;

    //some internal statistics for debugging
    private int totalNode = 0;
    private int height = 0;
    private int dataEntries = 0;
    private int indexEntries = 0;

    //my constructor
    public MandyTree(double MIN_FILL_FACTOR, int DEGREE) {
        root = null;
        this.MIN_FILL_FACTOR = MIN_FILL_FACTOR;
        this.DEGREE = DEGREE;
    }

    private static abstract class Node {
        protected List<Integer> keys; // List of keys in the node
        protected Node parent;

        public Node() {
            keys = new ArrayList<>();
        }

        public abstract boolean isLeafNode(); // Check if the node is a leaf node

        public abstract boolean isOverflow(int degree); // Check if the node is overflowing

        public abstract boolean isUnderflow(int degree); // Check if the node is underflowing

        public abstract int getKeyCount(); // Get the number of keys in the node

        public abstract boolean insertKey(Integer key); // Insert a key into the node

        public abstract boolean deleteKey(Integer key); // Delete a key from the node

        public abstract List<Integer> searchKeys(Integer key1, Integer key2); // Search for keys within a range

        public abstract List<Integer> getAllKeys(); // Get all the keys in the node

        public abstract Node getChild(int index); // Get the child node at the specified index

        public abstract void setChild(int index, Node child); // Set the child node at the specified index

        public abstract void printNode(); // Print the keys in the node
        public Node getParent() {
            return parent;
        }

        public void setParent(Node parent) {
            this.parent = parent;
        }
    }

    //LeafNode
// LeafNode
    private static class LeafNode extends Node {
        private List<Integer> values; // List of values in the leaf node
        private LeafNode next; // Reference to the next leaf node
        private LeafNode previous; // Reference to the next leaf node

        public LeafNode() {
            super();
            values = new ArrayList<>();
            next = null;
        }

        @Override
        public boolean isLeafNode() {
            return true;
        }

        @Override
        public boolean isOverflow(int degree) {
            return keys.size() > degree - 1;
        }

        @Override
        public boolean isUnderflow(int degree) {
            return keys.size() < Math.ceil((degree - 1) / 2.0);
        }

        @Override
        public int getKeyCount() {
            return keys.size();
        }

        @Override
        public boolean insertKey(Integer key) {
            int index = 0;
            while (index < keys.size() && key.compareTo(keys.get(index)) > 0) {
                index++;
            }
            keys.add(index, key);
            values.add(index, key);
            return true;
        }

        @Override
        public boolean deleteKey(Integer key) {
            int index = keys.indexOf(key);
            if (index != -1) {
                keys.remove(index);
                values.remove(index);
                return true;
            }
            return false;
        }

        @Override
        public List<Integer> searchKeys(Integer key1, Integer key2) {
            List<Integer> result = new ArrayList<>();
            int index = 0;
            while (index < keys.size() && key1.compareTo(keys.get(index)) > 0) {
                index++;
            }
            while (index < keys.size() && key2.compareTo(keys.get(index)) >= 0) {
                result.add(keys.get(index));
                index++;
            }
            return result;
        }

        @Override
        public List<Integer> getAllKeys() {
            return new ArrayList<>(keys);
        }

        @Override
        public Node getChild(int index) {
            return null;
        }

        @Override
        public void setChild(int index, Node child) {
            // No child in the leaf node
        }

        public LeafNode getNext() {
            return next;
        }
        public LeafNode getPrevious() {
            return previous;
        }
        public Integer getFirstLeafKey() {
            return keys.get(0);
        }
        public void setNext(LeafNode next) {
            this.next = next;
        }
        public void setPrevious(LeafNode previous) {
            this.previous = previous;
        }
        public void setKeys(List<Integer> keys) {
            this.keys = new ArrayList<>(keys);
            this.values = new ArrayList<>(keys);
        }
        @Override
        public void printNode() {
            for (int i = 0; i < keys.size(); i++) {
                System.out.print(keys.get(i));
                if (i != keys.size() - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println();
        }
    }

    //IndexNode
// IndexNode
    private static class IndexNode extends Node {
        private List<Node> pointers; // List of pointers to child nodes

        public IndexNode() {
            super();
            pointers = new ArrayList<>();
        }

        @Override
        public boolean isLeafNode() {
            return false;
        }

        @Override
        public boolean isOverflow(int degree) {
            return keys.size() > degree;
        }

        @Override
        public boolean isUnderflow(int degree) {
            return keys.size() < Math.ceil((degree - 1) / 2.0);
        }

        @Override
        public int getKeyCount() {
            return keys.size();
        }

        @Override
        public boolean insertKey(Integer key) {
            int index = 0;
            while (index < keys.size() && key.compareTo(keys.get(index)) > 0) {
                index++;
            }
            keys.add(index, key);
            return true;
        }

        @Override
        public boolean deleteKey(Integer key) {
            int index = keys.indexOf(key);
            if (index != -1) {
                keys.remove(index);
                return true;
            }
            return false;
        }

        @Override
        public List<Integer> searchKeys(Integer key1, Integer key2) {
            List<Integer> result = new ArrayList<>();
            int index = 0;
            while (index < keys.size() && key1.compareTo(keys.get(index)) > 0) {
                index++;
            }
            while (index < keys.size() && key2.compareTo(keys.get(index)) >= 0) {
                result.add(keys.get(index));
                index++;
            }
            return result;
        }

        @Override
        public List<Integer> getAllKeys() {
            return new ArrayList<>(keys);
        }

        @Override
        public Node getChild(int index) {
            if (index >= 0 && index < pointers.size()) {
                return pointers.get(index);
            }
            return null;
        }

        @Override
        public void setChild(int index, Node child) {
            if (index >= 0 && index <= getKeyCount()) {
                pointers.set(index, child);
            }
        }
        /**
         * Given a pointer to a Node object and an integer index, this method
         * inserts the pointer at the specified index within the childPointers
         * instance variable. As a result of the insert, some pointers may be
         * shifted to the right of the index.
         * @param child: the Node pointer to be inserted
         * @param index: the index at which the insert is to take place
         */
        public void addChild(int index, Node child) {
            if (index >= 0 && index <= getKeyCount()) {
                pointers.add(index, child);
            }
        }
        @Override
        public void printNode() {
            for (int i = 0; i < keys.size(); i++) {
                System.out.print(keys.get(i));
                if (i != keys.size() - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println();
        }
        public int getInsertionIndex(Integer key) {
            int index = 0;
            while (index < keys.size() && key.compareTo(keys.get(index)) > 0) {
                index++;
            }
            return index;
        }

    }
    /**
     * Insert key to tree
     * @param key
     */
    public void insert(Integer key) {
        if (root == null) {
            // If the tree is empty, create a new leaf node as the root
            LeafNode leafNode = new LeafNode();
            leafNode.insertKey(key);
            root = leafNode;
        } else {
            // Find the appropriate leaf node to insert the key
            Node leaf = findLeafNode(key);

            if (leaf.isOverflow(this.DEGREE)) {
                // If the leaf node is full, split it and propagate the split upwards
                System.out.println("overflow");
                leaf.insertKey(key);
                LeafNode newLeafNode = splitLeafNode((LeafNode) leaf);
                IndexNode parent = (IndexNode) leaf.getParent();

                if (parent == null) {
                    // If the root node is the leaf node, create a new root node
                    IndexNode newRootNode = new IndexNode();
                    newRootNode.insertKey(newLeafNode.getFirstLeafKey());
                    newRootNode.addChild(0, leaf);
                    newRootNode.addChild(1, newLeafNode);
                    root = newRootNode;
                } else {
                    // Insert the new key into the parent index node
                    parent.insertKey(newLeafNode.getFirstLeafKey());
                    int insertionIndex = parent.getInsertionIndex(key);
                    parent.setChild(insertionIndex, leaf);
                    parent.setChild(insertionIndex + 1, newLeafNode);

                    // Handle potential overflow of the parent index node
                    if (parent.isOverflow(this.DEGREE)) {
//                        splitIndexNode(parent);
                        System.out.println("parent overflow");
                    }
                }
            } else {
                // If the leaf node has enough space, insert the key directly
                ((LeafNode) leaf).insertKey(key);
            }

        }

    }

    /**
     * Delete a key from the tree starting from root
     * @param key key to be deleted
     */
    public void delete(Integer key) {
    }
    /**
     * Delete a key from the tree starting from root
     * @param leaf leaf node to be spilt
     */
    private LeafNode splitLeafNode(LeafNode leaf) {
        // Create a new leaf node
        LeafNode newLeafNode = new LeafNode();

        // Find the index to split the keys
        int splitIndex = leaf.getAllKeys().size() / 2;

        // Move half of the keys from the original leaf node to the new leaf node
        List<Integer> originalKeys = leaf.getAllKeys();
        List<Integer> newKeys = new ArrayList<>(originalKeys.subList(splitIndex, originalKeys.size()));
        originalKeys.subList(splitIndex, originalKeys.size()).clear();
        newLeafNode.setKeys(newKeys);
        leaf.setKeys(originalKeys);
        // Update the next and previous pointers of the leaf nodes
        newLeafNode.setNext(leaf.getNext());
        newLeafNode.setPrevious(leaf);
        leaf.setNext(newLeafNode);

        // If the new leaf node has a next node, update its previous pointer
        if (newLeafNode.getNext() != null) {
            newLeafNode.getNext().setPrevious(newLeafNode);
        }

        return newLeafNode;
    }
    /**
     * Search tree by range
     * @param key1 First key
     * @param key2 Second key
     * @return List of keys
     */
    public List<Integer> search(Integer key1, Integer key2) {
        return (new ArrayList<Integer>());
    }

    private LeafNode findLeafNode(Integer key) {
        Node currentNode = root;

        while (!currentNode.isLeafNode()) {
            IndexNode indexNode = (IndexNode) currentNode;
            int childIndex = indexNode.getInsertionIndex(key);
            currentNode = indexNode.getChild(childIndex);
        }

        return (LeafNode) currentNode;
    }
    /**
     * Print statistics of the current tree
     */
    @Override
    public void dumpStatistics() {
        System.out.println("Statistics of the B+ Tree:");
        System.out.println("Total number of nodes: ");
        System.out.println("Total number of data entries: ");
        System.out.println("Total number of index entries: ");
        System.out.print("Average fill factor: ");
        System.out.println("%");
        System.out.println("Height of tree: ");
    }
    /**
     * Print tree from root
     */
    public void printTree() {
        printTree(root);
    }

    /**
     * Print tree from node
     * @param node starting node to print
     */
    public void printTree(Node node) {
        if (node == null) {
            System.out.println("Empty tree");
            return;
        }

        if (node.isLeafNode()) {
            LeafNode leafNode = (LeafNode) node;
            leafNode.printNode();
        } else {
            IndexNode indexNode = (IndexNode) node;
            indexNode.printNode();

            for (int i = 0; i < indexNode.getKeyCount() + 1; i++) {
                System.out.println();
                printTree(indexNode.getChild(i));
            }
        }
    }

    @Override
    public void load(String datafilename) {
        String[] readLines = readFile.readData(datafilename);
        //Fill in you work here

    }
    
    public static void main(String[] args) {
        //we hardcode the fill factor and degree for this project
        BTree mandyTree = new MandyTree(0.5, 4);
        //the value is stored in Config.java
        //build a mandyTree from the data file
        mandyTree.load(Config.dataFileName);

        //interact with the tree via a text interface.
        CLI.shell(mandyTree);

    }
}
