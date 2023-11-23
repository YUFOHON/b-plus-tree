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

    static abstract class Node {
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

                IndexNode parent = (IndexNode) leaf.getParent();

                if (parent == null) {
                    // If the root node is the leaf node, create a new root node
                    LeafNode newLeafNode = splitLeafNode((LeafNode) leaf);
                    IndexNode newRootNode = new IndexNode();
                    newRootNode.insertKey(newLeafNode.getFirstLeafKey());
                    newRootNode.addChild(0, leaf);
                    newRootNode.addChild(1, newLeafNode);
                    root = newRootNode;
                    leaf.setParent(root);
                    newLeafNode.setParent(root);
                }
                else if(((LeafNode) leaf).getPrevious()!=null && !((LeafNode) leaf).getPrevious().isOverflow(DEGREE)) {
                    //if leafNode's previous have space, redistribute
                    System.out.println("redistribute to Previous");
                    LeafNode redistributeTarget=((LeafNode) leaf).getPrevious();
                    redistributeTarget.insertKey(leaf.keys.get(0));
                    leaf.keys.remove(leaf.keys.get(0));
                    int redistributeIndex=parent.getPrevRedistributeKeyIndex(leaf.keys.get(0));
                    parent.keys.set(redistributeIndex,leaf.keys.get(0));


                }
                else if(((LeafNode) leaf).getNext()!=null && !((LeafNode) leaf).getNext().isOverflow(DEGREE)) {
                    //if leafNode's previous have space, redistribute
                    System.out.println("redistribute to Next");
                    LeafNode redistributeTarget=((LeafNode) leaf).getNext();
                    redistributeTarget.insertKey(leaf.keys.get(leaf.keys.size()-1));
                    int redistributeIndex=parent.getNextRedistributeKeyIndex(leaf.keys.get(leaf.keys.size()-1));
                    parent.keys.set(redistributeIndex,leaf.keys.get(leaf.keys.size()-1));
                    leaf.keys.remove(leaf.keys.get(leaf.keys.size()-1));

                }
                else{
                    // Insert the new key into the parent index node
                    LeafNode newLeafNode = splitLeafNode((LeafNode) leaf);
                    parent.insertKey(newLeafNode.getFirstLeafKey());
                    int insertionIndex = parent.getInsertionIndex(key);
                    parent.setChild(insertionIndex-1, leaf);
                    parent.setChild(insertionIndex, newLeafNode);
                    newLeafNode.setParent(parent);



                    // Handle potential overflow of the parent index node
                    while(parent!=null&&parent.isOverflow(this.DEGREE)) {
                        System.out.println("parent overflow");
                        int splitIndex = parent.getAllKeys().size() / 2;
                        int tempKey=parent.getAllKeys().get(splitIndex);
                        IndexNode[] indexNodeArray = splitIndexNode(parent);
                        if(parent.getParent()==null){
                            //new root node
                            IndexNode newRootNode=new IndexNode();
                            newRootNode.insertKey(tempKey);
                            indexNodeArray[0].setParent(newRootNode);
                            indexNodeArray[1].setParent(newRootNode);
                            newRootNode.setChild(0,indexNodeArray[0]);
                            newRootNode.setChild(1,indexNodeArray[1]);
                            root=newRootNode;
                            break;
                        }else{
                            IndexNode grandParent= (IndexNode) parent.getParent();
                            grandParent.insertKey(tempKey);
                            indexNodeArray[0].setParent(grandParent);
                            indexNodeArray[1].setParent(grandParent);
                            int lastKeyOfIndexNode0=indexNodeArray[0].keys.get(indexNodeArray[0].keys.size()-1);
                            int oInsertionIndex= grandParent.getInsertionIndex(lastKeyOfIndexNode0);
                            grandParent.setChild(oInsertionIndex,indexNodeArray[0]);
                            grandParent.removeChild(oInsertionIndex+1);
                            grandParent.setChild(oInsertionIndex+1,indexNodeArray[1]);

                            parent = (IndexNode) parent.getParent();
                        }





                    }
                }
            } else {
                // If the leaf node has enough space, insert the key directly
                ((LeafNode) leaf).insertKey(key);
            }

        }

    }
    private IndexNode[] splitIndexNode(IndexNode indexNode) {
        // Create a new leaf node
        IndexNode leftIndexNode = new IndexNode();
        IndexNode rightIndexNode=new IndexNode();

        // Find the index to split the keys
        int splitIndex = indexNode.getAllKeys().size() / 2;

        // Move half of the keys from the original index node to the new index node
        List<Integer> originalKeys = indexNode.getAllKeys();
        List<Integer> leftIndexNodeKeys = new ArrayList<>(originalKeys.subList(0, splitIndex));
        List<Integer> rightIndexNodeKeys = new ArrayList<>(originalKeys.subList(splitIndex+1, originalKeys.size()));
        leftIndexNode.setKeys(leftIndexNodeKeys);
        rightIndexNode.setKeys(rightIndexNodeKeys);

        // Update pointers of the index nodes
        List<MandyTree.Node> pointers= indexNode.getAllPointer();
        List<MandyTree.Node> leftIndexNodePointers = new ArrayList<>(pointers.subList(0, splitIndex+1));
        List<MandyTree.Node> rightIndexNodePointers = new ArrayList<>(pointers.subList(splitIndex+1, originalKeys.size()+1));
        leftIndexNode.setPointers(leftIndexNodePointers);
        rightIndexNode.setPointers(rightIndexNodePointers);

        // set parents of each leaf nodes
        leftIndexNodePointers.forEach((item)->item.setParent(leftIndexNode));
        rightIndexNodePointers.forEach((item)->item.setParent(rightIndexNode));

        return new IndexNode[]{leftIndexNode,rightIndexNode};
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
            System.out.print("[");
            leafNode.printNode();
            System.out.print("]");
        } else {
            IndexNode indexNode = (IndexNode) node;
            System.out.print("[");
            indexNode.printNode();
            System.out.println("]");

            for (int i = 0; i < indexNode.getKeyCount() + 1; i++) {
                printTree(indexNode.getChild(i));
            }
            System.out.println();
        }
    }

    @Override
    public void load(String datafilename) {
        String[] readLines = readFile.readData(datafilename);
        //Fill in you work here
        for(int i=0; i<readLines.length;i++){
            insert(Integer.parseInt(readLines[i]));
        }

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
