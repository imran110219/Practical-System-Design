package com.example;

import java.util.*;

class BTreeNode {
    private List<Integer> keys;
    private List<BTreeNode> children;
    private boolean isLeaf;
    private int maxKeys;

    public BTreeNode(boolean isLeaf, int maxKeys) {
        this.keys = new ArrayList<>();
        this.children = new ArrayList<>();
        this.isLeaf = isLeaf;
        this.maxKeys = maxKeys;
    }

    public boolean isFull() {
        return keys.size() == maxKeys;
    }

    public List<Integer> getKeys() {
        return keys;
    }

    public List<BTreeNode> getChildren() {
        return children;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    /**
     * Search for a key in the B-tree
     * Returns a SearchResult containing the node and index if found
     */
    public SearchResult search(int key) {
        int i = 0;
        // Find the first key greater than or equal to the search key
        while (i < keys.size() && key > keys.get(i)) {
            i++;
        }

        // If key found
        if (i < keys.size() && key == keys.get(i)) {
            return new SearchResult(this, i, true);
        }

        // If this is a leaf node, key not found
        if (isLeaf) {
            return new SearchResult(null, -1, false);
        }

        // Recursively search in appropriate child
        return children.get(i).search(key);
    }

    /**
     * Insert key into a non-full node
     */
    public void insertNonFull(int key) {
        int i = keys.size() - 1;

        if (isLeaf) {
            // Insert key into leaf node in sorted order
            keys.add(0); // Add placeholder
            while (i >= 0 && keys.get(i) > key) {
                keys.set(i + 1, keys.get(i));
                i--;
            }
            keys.set(i + 1, key);
        } else {
            // Find child to insert into
            while (i >= 0 && keys.get(i) > key) {
                i--;
            }
            i++; // Move to child index

            // If child is full, split it
            if (children.get(i).isFull()) {
                splitChild(i);
                if (keys.get(i) < key) {
                    i++;
                }
            }

            children.get(i).insertNonFull(key);
        }
    }

    /**
     * Split the full child at index i
     */
    public void splitChild(int i) {
        BTreeNode fullChild = children.get(i);
        BTreeNode newChild = new BTreeNode(fullChild.isLeaf, maxKeys);

        int mid = maxKeys / 2;

        // Move half of keys to new child
        for (int j = mid + 1; j < maxKeys; j++) {
            newChild.keys.add(fullChild.keys.get(j));
        }

        // If not leaf, move half of children too
        if (!fullChild.isLeaf) {
            for (int j = mid + 1; j <= maxKeys; j++) {
                newChild.children.add(fullChild.children.get(j));
            }
        }

        // Insert new child into parent
        children.add(i + 1, newChild);

        // Move middle key up to parent
        keys.add(i, fullChild.keys.get(mid));

        // Remove moved keys and children from original child
        fullChild.keys.subList(mid, fullChild.keys.size()).clear();
        if (!fullChild.isLeaf) {
            fullChild.children.subList(mid + 1, fullChild.children.size()).clear();
        }
    }

    /**
     * Print the B-tree structure with indentation
     */
    public void printTree(int level) {
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < level; i++) {
            indent.append("  ");
        }

        System.out.println(indent + "Keys: " + keys);

        if (!isLeaf) {
            for (BTreeNode child : children) {
                child.printTree(level + 1);
            }
        }
    }

    /**
     * Delete a key from the B-tree (simplified implementation)
     */
    public boolean delete(int key) {
        int i = 0;
        while (i < keys.size() && key > keys.get(i)) {
            i++;
        }

        if (i < keys.size() && key == keys.get(i)) {
            // Key found in this node
            if (isLeaf) {
                // Simple case: remove from leaf
                keys.remove(i);
                return true;
            } else {
                // Complex case: key in internal node
                // For simplicity, we'll just mark it as deleted
                // In practice, you'd replace with predecessor/successor
                System.out.println("Deletion from internal nodes not fully implemented in this demo");
                return false;
            }
        } else if (isLeaf) {
            // Key not found and we're at a leaf
            return false;
        } else {
            // Recursively delete from child
            return children.get(i).delete(key);
        }
    }
}

/**
 * Helper class to return search results
 */
class SearchResult {
    private BTreeNode node;
    private int index;
    private boolean found;

    public SearchResult(BTreeNode node, int index, boolean found) {
        this.node = node;
        this.index = index;
        this.found = found;
    }

    public BTreeNode getNode() { return node; }
    public int getIndex() { return index; }
    public boolean isFound() { return found; }
}

/**
 * Main B-Tree class
 */
public class BTree {
    private BTreeNode root;
    private int maxKeys;

    public BTree(int maxKeys) {
        this.root = new BTreeNode(true, maxKeys);
        this.maxKeys = maxKeys;
    }

    /**
     * Search for a key in the B-tree
     */
    public SearchResult search(int key) {
        return root.search(key);
    }

    /**
     * Insert a key into the B-tree
     */
    public void insert(int key) {
        // If root is full, create new root
        if (root.isFull()) {
            BTreeNode newRoot = new BTreeNode(false, maxKeys);
            newRoot.getChildren().add(root);
            newRoot.splitChild(0);
            root = newRoot;
        }

        root.insertNonFull(key);
    }

    /**
     * Delete a key from the B-tree
     */
    public boolean delete(int key) {
        boolean deleted = root.delete(key);

        // If root becomes empty after deletion
        if (!root.isLeaf() && root.getKeys().isEmpty()) {
            root = root.getChildren().get(0);
        }

        return deleted;
    }

    /**
     * Print the entire B-tree
     */
    public void printTree() {
        System.out.println("B-Tree Structure:");
        root.printTree(0);
        System.out.println();
    }

    /**
     * Get tree height for analysis
     */
    public int getHeight() {
        return getHeight(root);
    }

    private int getHeight(BTreeNode node) {
        if (node.isLeaf()) {
            return 1;
        } else {
            return 1 + getHeight(node.getChildren().get(0));
        }
    }

    /**
     * Count total nodes in the tree
     */
    public int countNodes() {
        return countNodes(root);
    }

    private int countNodes(BTreeNode node) {
        int count = 1;
        if (!node.isLeaf()) {
            for (BTreeNode child : node.getChildren()) {
                count += countNodes(child);
            }
        }
        return count;
    }

    /**
     * Demo method with detailed output for presentation
     */
    public static void runDemo() {
        System.out.println("=== B-Tree Implementation Demo ===");
        System.out.println("Creating B-tree with maximum 3 keys per node (order 2)");

        BTree btree = new BTree(3);

        // Insert keys with detailed explanation
        int[] keysToInsert = {10, 20, 5, 6, 12, 30, 7, 17, 25, 40, 50, 60};

        for (int key : keysToInsert) {
            System.out.println("\n--- Inserting " + key + " ---");
            btree.insert(key);
            btree.printTree();
            System.out.println("Tree height: " + btree.getHeight() +
                    ", Total nodes: " + btree.countNodes());
        }

        // Search demonstration
        System.out.println("\n=== Search Operations ===");
        int[] searchKeys = {6, 15, 20, 25, 100};

        for (int key : searchKeys) {
            SearchResult result = btree.search(key);
            if (result.isFound()) {
                System.out.println("Key " + key + " found at position " +
                        result.getIndex() + " in node with keys " +
                        result.getNode().getKeys());
            } else {
                System.out.println("Key " + key + " not found");
            }
        }

        // Range query simulation
        System.out.println("\n=== Range Query Simulation (15 to 35) ===");
        performRangeQuery(btree.root, 15, 35);

        // Tree analysis
        System.out.println("\n=== Tree Analysis ===");
        System.out.println("Final tree height: " + btree.getHeight());
        System.out.println("Total nodes: " + btree.countNodes());
        System.out.println("Keys per level analysis:");
        analyzeTreeLevels(btree.root, 0);
    }

    /**
     * Simulate range query for presentation
     */
    private static void performRangeQuery(BTreeNode node, int min, int max) {
        for (int i = 0; i < node.getKeys().size(); i++) {
            int key = node.getKeys().get(i);

            if (!node.isLeaf() && i < node.getChildren().size()) {
                performRangeQuery(node.getChildren().get(i), min, max);
            }

            if (key >= min && key <= max) {
                System.out.print(key + " ");
            }
        }

        if (!node.isLeaf() && node.getChildren().size() > node.getKeys().size()) {
            performRangeQuery(node.getChildren().get(node.getChildren().size() - 1), min, max);
        }
    }

    /**
     * Analyze keys per level for presentation
     */
    private static void analyzeTreeLevels(BTreeNode node, int level) {
        System.out.println("Level " + level + ": " + node.getKeys().size() + " keys");

        if (!node.isLeaf()) {
            for (BTreeNode child : node.getChildren()) {
                analyzeTreeLevels(child, level + 1);
                break; // Just show first child for each level
            }
        }
    }

    /**
     * Main method to run the demonstration
     */
    public static void main(String[] args) {
        runDemo();
    }
}