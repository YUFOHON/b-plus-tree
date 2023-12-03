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
//                    parent.setChild(insertionIndex-1, leaf);
//                    parent.setChild(insertionIndex, newLeafNode);
                    if(insertionIndex==0){
                        parent.setChild(insertionIndex, leaf);
                        parent.setChild(insertionIndex+1, newLeafNode);
                    }else {
                        parent.setChild(insertionIndex - 1, leaf);
                        parent.setChild(insertionIndex, newLeafNode);
                    }
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
        // Step 1: Find the node that contains the key
        LeafNode node = findLeafNode(key);
        // Step 2: Delete the key from the node
        if (node==root&&node.keys.size()==1){
            root=null;
            return;
        }
        if (node != null) {
            boolean isDeleted = node.deleteKey(key);
            if (isDeleted) {
                System.out.println("delete success");
                // Step 3: Check for underflow
                if (node.isUnderflow(this.DEGREE)) {
                    System.out.println("underflow");
                    //check is left sibling have space or not

                    LeafNode leftNode=node.getPrevious();
                    LeafNode rightNode=node.getNext();
//                    //check is leftNode or rightNode is point to the same parent Try to re-distribute,
//                    // borrowing from sibling (adjacent node with same parent)
                    if(leftNode!=null && leftNode.parent!=node.parent){
                        leftNode=null;
                    }
                    if(rightNode!=null && rightNode.parent!=node.parent){
                        rightNode=null;
                    }

                    if(leftNode!=null && !leftNode.isUnderflow(this.DEGREE) && leftNode.keys.size()>this.DEGREE/2){
                        //redistribute
                        System.out.println("redistribute to Previous");
                        redistribute(node, node.getPrevious(), true);
                    }else if(rightNode!=null && !rightNode.isUnderflow(this.DEGREE) && rightNode.keys.size()>this.DEGREE/2 ){
                        //redistribute
                        System.out.println("redistribute to Next");
                        redistribute(node, node.getNext(), false);
                    }else{
                        //merge
                        System.out.println("merge");

                        if(leftNode!=null){
                            merge(node, leftNode, true);
                        }else{
                            merge(node, rightNode, false);
                        }
//                        merge(node, siblingNode, isLeftSibling);
                    }

                }
                else updateParentKey(node);
            }
        }
    }
    public void updateParentKey(LeafNode node) {
        if (node.parent != null) {
            IndexNode parent = (IndexNode) node.parent;
            int index = parent.getAllPointer().indexOf(node);
            if (index > 0) {
                node.parent.keys.set(index - 1, node.keys.get(0));
            }
        }
    }
//    private void handleUnderflow(IndexNode underflowNode) {
//        IndexNode grandParentNode = (IndexNode) underflowNode.parent;
//        int underflowNodeIndex = grandParentNode.getAllPointer().indexOf(underflowNode);
//        //check if is same parent
//        if (underflowNodeIndex > 0) { // If underflowNode has a left sibling
//            IndexNode leftSibling = (IndexNode) grandParentNode.getAllPointer().get(underflowNodeIndex - 1);
//
//            if (leftSibling.keys.size() > this.DEGREE/2) { // If redistribution is possible
//                redistribute_Index(underflowNode, leftSibling, true);
//            } else { // If redistribution is not possible
//                merge_Index(underflowNode, leftSibling, true);
//            }
//        } else if (underflowNodeIndex < grandParentNode.getAllPointer().size() - 1) { // If underflowNode has a right sibling
//            IndexNode rightSibling = (IndexNode) grandParentNode.getAllPointer().get(underflowNodeIndex + 1);
//
//            if (rightSibling.keys.size() > this.DEGREE/2) { // If redistribution is possible
//                redistribute_Index(underflowNode, rightSibling, false);
//            } else { // If redistribution is not possible
//                merge_Index(underflowNode, rightSibling, false);
//            }
//        } else { // If underflowNode has no siblings (must be the root)
//            merge_Index(underflowNode, null, false);
//        }
//        // If the parent underflows as a result, handle it recursively
////        if (grandParentNode.isUnderflow(this.DEGREE)) {
////            handleUnderflow(grandParentNode);
////        }
//    }
private void handleUnderflow(IndexNode underflowNode) {
    IndexNode grandParentNode = (IndexNode) underflowNode.parent;
    int underflowNodeIndex = grandParentNode.getAllPointer().indexOf(underflowNode);

    // Check if underflowNode has a left sibling
    if (underflowNodeIndex > 0) {
        IndexNode leftSibling = (IndexNode) grandParentNode.getAllPointer().get(underflowNodeIndex - 1);

        // If left sibling can redistribute
        if (leftSibling.keys.size() > this.DEGREE / 2) {
            redistribute_Index(underflowNode, leftSibling, true);
            return;
        }
    }

    // Check if underflowNode has a right sibling
    if (underflowNodeIndex < grandParentNode.getAllPointer().size() - 1) {
        IndexNode rightSibling = (IndexNode) grandParentNode.getAllPointer().get(underflowNodeIndex + 1);

        // If right sibling can redistribute
        if (rightSibling.keys.size() > this.DEGREE / 2) {
            redistribute_Index(underflowNode, rightSibling, false);
            return;
        }
    }

    // If neither left nor right sibling can redistribute, then merge
    if (underflowNodeIndex > 0) { // If there is a left sibling, merge with left
        IndexNode leftSibling = (IndexNode) grandParentNode.getAllPointer().get(underflowNodeIndex - 1);
        merge_Index(underflowNode, leftSibling, true);
    } else if (underflowNodeIndex < grandParentNode.getAllPointer().size() - 1) { // If there is a right sibling, merge with right
        IndexNode rightSibling = (IndexNode) grandParentNode.getAllPointer().get(underflowNodeIndex + 1);
        merge_Index(underflowNode, rightSibling, false);
    } else { // If underflowNode has no siblings (must be the root)
        merge_Index(underflowNode, null, false);
    }
}
    private void redistribute(LeafNode underflowNode, LeafNode siblingNode, boolean isLeftSibling) {
        IndexNode parentNode = (IndexNode) underflowNode.parent;
        int underflowNodeIndex = parentNode.getAllPointer().indexOf(underflowNode);

        if (isLeftSibling) {
            // Borrow the largest key from the left sibling
            Integer borrowedKey = siblingNode.keys.remove(siblingNode.keys.size() - 1);
            Integer borrowedValue = siblingNode.values.remove(siblingNode.values.size() - 1);

            // Move the separating key from the parent down to the underflow node
//            Integer separatingKey = parentNode.keys.get(underflowNodeIndex - 1);
            underflowNode.keys.add(0, borrowedValue);
            underflowNode.values.add(0, borrowedValue);

            // Move the borrowed key up to the parent
            parentNode.keys.set(underflowNodeIndex-1, borrowedKey);

        } else {
            // Borrow the smallest key from the right sibling
            Integer borrowedKey = siblingNode.keys.remove(0);
            Integer borrowedValue = siblingNode.values.remove(0);
            // Move the separating key from the parent down to the underflow node
            underflowNode.keys.add(borrowedValue);
            underflowNode.values.add(borrowedValue);
            // Move the next node key up to the parent
            parentNode.keys.set(underflowNodeIndex, siblingNode.keys.get(0));
//            parentNode.keys.set(underflowNodeIndex, borrowedKey);
        }

    }
    private void merge(LeafNode underflowNode, LeafNode siblingNode, boolean isLeftSibling) {
        IndexNode parentNode = (IndexNode) underflowNode.parent;
//        List<Node> pointers = parentNode.getAllPointer();
        int underflowNodeIndex = parentNode.getAllPointer().indexOf(underflowNode);

        if (isLeftSibling) {
            // Move all keys and values from underflowNode to the sibling
            siblingNode.keys.addAll(underflowNode.keys);
            siblingNode.values.addAll(underflowNode.values);

            // Remove the separating key from the parent and the underflowNode from the children
            parentNode.keys.remove(underflowNodeIndex - 1);
//            parentNode.getAllPointer().remove(underflowNodeIndex);
            parentNode.deletePointer(underflowNodeIndex);
            System.out.println("merge from left");
            if (underflowNode.getNext() != null) {
                underflowNode.getNext().setPrevious(siblingNode);
            }
//            siblingNode.setParent(parentNode);
            siblingNode.setNext(underflowNode.getNext());

        } else {
            // Move all keys and values from the sibling to underflowNode
            underflowNode.keys.addAll(siblingNode.keys);
            underflowNode.values.addAll(siblingNode.values);

            // Remove the separating key from the parent and the siblingNode from the children
            parentNode.keys.remove(underflowNodeIndex);
            parentNode.deletePointer(underflowNodeIndex + 1);
            if (siblingNode.getNext() != null) {
                siblingNode.getNext().setPrevious(underflowNode);
            }
            underflowNode.setNext(siblingNode.getNext());

            System.out.println("merge from right");

        }

        // If the parent node is not root,underflows as a result, handle it recursively
        if (parentNode!=root&&parentNode.isUnderflow(this.DEGREE)) {
            System.out.println("parent underflow");
            handleUnderflow(parentNode);
        }
        //if parent node is root and parent node is empty, set root to sibling node
        if(parentNode==root&& parentNode.keys.isEmpty()){
            root=siblingNode;
        }
    }
    private void redistribute_Index(IndexNode underflowNode, IndexNode siblingNode, boolean isLeftSibling) {
        IndexNode parentNode = (IndexNode) underflowNode.parent;
        int underflowNodeIndex = parentNode.getAllPointer().indexOf(underflowNode);

        if (isLeftSibling) {
            // Borrow the largest key from the left sibling
            Integer borrowedKey = siblingNode.keys.remove(siblingNode.keys.size() - 1);
            Node borrowedChild = siblingNode.getAllPointer().remove(siblingNode.getAllPointer().size() - 1);

            // Move the separating key from the parent down to the underflow node
            Integer separatingKey = parentNode.keys.get(underflowNodeIndex - 1);
            underflowNode.keys.add(0, separatingKey);
            underflowNode.getAllPointer().add(0, borrowedChild);

            // Move the borrowed key up to the parent
            parentNode.keys.set(underflowNodeIndex - 1, borrowedKey);
        } else {
            // Borrow the smallest key from the right sibling
            Integer borrowedKey = siblingNode.keys.remove(0);
            Node borrowedChild = siblingNode.getAllPointer().remove(0);

            // Move the separating key from the parent down to the underflow node
            Integer separatingKey = parentNode.keys.get(underflowNodeIndex);
            underflowNode.keys.add(separatingKey);
            underflowNode.getAllPointer().add(borrowedChild);
            //from the pointer of underflowNode, add the key from it's child

            borrowedChild.parent=underflowNode;
            // set al the child
            // Move the borrowed key up to the parent
            parentNode.keys.set(underflowNodeIndex, borrowedKey);
        }
    }

    private void merge_Index(IndexNode underflowNode, IndexNode siblingNode, boolean isLeftSibling) {
        IndexNode parentNode = (IndexNode) underflowNode.parent;
        List<Node> pointers = parentNode.getAllPointer();
        int underflowNodeIndex = pointers.indexOf(underflowNode);

        if (isLeftSibling) {
            // Move the separating key from the parent down to the sibling (pull down operation)
            Integer separatingKey = parentNode.keys.remove(underflowNodeIndex - 1);
            siblingNode.keys.add(separatingKey);

            // Move all keys and children from underflowNode to the sibling
            siblingNode.keys.addAll(underflowNode.keys);
//            siblingNode.getAllPointer().addAll(underflowNode.getAllPointer());
            siblingNode.addPointer(underflowNode.getAllPointer());
            // for all the pointer in underflowNode, set parent to siblingNode
            underflowNode.getAllPointer().forEach((item)->item.setParent(siblingNode));
            // Remove the underflowNode from the children
            parentNode.deletePointer(underflowNodeIndex);
        } else {
            // Move the separating key from the parent down to the underflowNode
            Integer separatingKey = parentNode.keys.remove(underflowNodeIndex);
            underflowNode.keys.add(separatingKey);

            // Move all keys and children from the sibling to underflowNode
            underflowNode.keys.addAll(siblingNode.keys);
//            underflowNode.getAllPointer().addAll(siblingNode.getAllPointer());
            underflowNode.addPointer(siblingNode.getAllPointer());
            underflowNode.getAllPointer().forEach((item)->item.setParent(underflowNode));
            // Remove the siblingNode from the children
//            pointers.remove(underflowNodeIndex + 1);
        }

        // If the parent underflows as a result, handle it recursively
        if (parentNode.isUnderflow(this.DEGREE)&&parentNode!=root) {
            System.out.println("parent underflow");
            handleUnderflow(parentNode);
        }
        if(parentNode==root&& parentNode.keys.isEmpty()){
            if(isLeftSibling) {
                root = siblingNode;
                siblingNode.parent = null;
            }else{
                root=underflowNode;
                underflowNode.parent=null;
            }


        }
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
        // Create an empty list to store the result
        ArrayList<Integer> result = new ArrayList<>();
        // If the tree is empty, return the empty list
        if (root == null) {
            return result;
        }

        // Start searching from the root node
        searchRecursive(root, key1, key2, result);
        return result;
    }

    private void searchRecursive(Node node, Integer key1, Integer key2, ArrayList<Integer> result) {
        // Check if the node is a leaf node
        if (node.isLeafNode()) {
            // In a leaf node, simply add all keys in the range to the result
            for (Integer key : node.keys) {
                if (key >= key1 && key <= key2) {
                    result.add(key);
                }
            }
        } else {
            // In an inner node, use binary search to find the first key >= key1
            int i = Collections.binarySearch(node.keys, key1);
            if (i < 0) {
                i = -i - 1; // key1 not found, so start from the next greater key
            }
            // Iterate over all keys and children in the range
            while (i < node.keys.size() && node.keys.get(i) <= key2) {
                searchRecursive(node.getChild(i), key1, key2, result);
//                if (node.keys.get(i) >= key1) {
//                    result.add(node.keys.get(i));
//                }
                i++;
            }
            // The last child could also have keys in the range
            if (i <= node.keys.size()) {
                searchRecursive(node.getChild(i), key1, key2, result);
            }
        }
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
//    public void printTree() {
//        printTree(root);
//    }
//
//    /**
//     * Print tree from node
//     * @param node starting node to print
//     */
//    public void printTree(Node node) {
//        if (node == null) {
//            System.out.println("Empty tree");
//            return;
//        }
//
//        if (node.isLeafNode()) {
//            LeafNode leafNode = (LeafNode) node;
//            System.out.print("[");
//            leafNode.printNode();
//            System.out.print("]");
//        } else {
//            IndexNode indexNode = (IndexNode) node;
//            System.out.print("[");
//            indexNode.printNode();
//            System.out.println("]");
//
//            for (int i = 0; i < indexNode.getKeyCount() + 1; i++) {
//                printTree(indexNode.getChild(i));
//            }
//            System.out.println();
//        }
//    }
    public void printTree() {
        printTree(root,0);
    }

    /**
     * Print tree from node
     * @param node starting node to print
     */
    public void printTree(Node node, int level) {
        if (node == null) {
            System.out.println("Empty tree");
            return;
        }

        // Create a prefix String of spaces for indentation
        String prefix = String.join("", Collections.nCopies(level, "   "));

        // Create a label for leaf or index node
        String nodeLabel = node.isLeafNode() ? "Leaf" :node==root?"Root": "Index";
        String connection = node.isLeafNode() ? "___ " :node==root?"": "|___";
        // Print node type, level, and keys
        System.out.println(prefix +connection+ nodeLabel + " Node (Level " + level + "): " + node.keys);

        if (!node.isLeafNode()) {
            IndexNode indexNode = (IndexNode) node;
            for (int i = 0; i < indexNode.getKeyCount() + 1; i++) {
                printTree(indexNode.getChild(i), level + 1); // Increase level for child nodes
            }
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
        //delete 110 120 130 46 80 5 23 31 45 28 100
//        mandyTree.delete(110);
//        mandyTree.delete(120);
//        mandyTree.delete(130);
//        mandyTree.delete(46);
//        mandyTree.delete(80);
//        mandyTree.delete(5);
//        mandyTree.delete(23);
//        mandyTree.delete(31);
//        mandyTree.delete(45);
//        mandyTree.delete(28);
//        mandyTree.delete(100);
        mandyTree.printTree();
        //interact with the tree via a text interface.
        CLI.shell(mandyTree);

    }
}
