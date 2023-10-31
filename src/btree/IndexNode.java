package btree;

import java.util.ArrayList;
import java.util.List;

 class IndexNode extends MandyTree.Node {

    private List<MandyTree.Node> pointers; // List of pointers to child nodes

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

     public void setKeys(List<Integer> keys) {
         this.keys = new ArrayList<>(keys);
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

     public List<MandyTree.Node> getAllPointer() {
         return new ArrayList<>(pointers);
     }

     @Override
    public MandyTree.Node getChild(int index) {
        if (index >= 0 && index < pointers.size()) {
            return pointers.get(index);
        }
        return null;
    }

    @Override
    public void setChild(int index, MandyTree.Node child) {
        if (index >= 0 && index <= getKeyCount() && pointers.size()>index) {
            pointers.set(index, child);
        }else{
            pointers.add(index,child);
        }
    }



     public void setPointers(List<MandyTree.Node> pointers) {
        this.pointers=pointers;
     }
    /**
     * Given a pointer to a Node object and an integer index, this method
     * inserts the pointer at the specified index within the childPointers
     * instance variable. As a result of the insert, some pointers may be
     * shifted to the right of the index.
     * @param child: the Node pointer to be inserted
     * @param index: the index at which the insert is to take place
     */
    public void addChild(int index, MandyTree.Node child) {
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

    }
    public int getInsertionIndex(Integer key) {
        int index = 0;
        while (index < keys.size() && key.compareTo(keys.get(index)) > 0) {
            index++;
        }
        return index;
    }

}
