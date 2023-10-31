package btree;

import java.util.ArrayList;
import java.util.List;


 class LeafNode extends MandyTree.Node {
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
    public MandyTree.Node getChild(int index) {
        return null;
    }

    @Override
    public void setChild(int index, MandyTree.Node child) {
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

    }
}

