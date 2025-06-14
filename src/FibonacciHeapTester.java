import java.util.*;

/**
 * Comprehensive tester for the FibonacciHeap implementation
 * Tests various operations and tracks metrics like:
 * - Number of trees
 * - Number of links
 * - Number of cuts
 * - Size consistency
 * - Heap property maintenance
 * - Special cases and edge conditions
 */
public class FibonacciHeapTester {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("=== Starting Fibonacci Heap Comprehensive Tests ===\n");

        // Test different values of cascading cut parameter c
        for (int c = 2; c <= 5; c++) {
            System.out.println("==== Testing with cascading cut parameter c = " + c + " ====");

            // Basic operations tests
            testBasicOperations(c);

            // Advanced operations tests
            testAdvancedOperations(c);

            // Edge cases
            testEdgeCases(c);

            // Stress test
            testStressOperations(c);

            // Metrics test
            testMetrics(c);

            System.out.println();
        }

        // Summary
        System.out.println("=== Test Summary ===");
        System.out.println("Tests passed: " + testsPassed);
        System.out.println("Tests failed: " + testsFailed);
        System.out.println("Total tests: " + (testsPassed + testsFailed));
    }

    /**
     * Tests basic operations like insert, findMin, and deleteMin
     */
    private static void testBasicOperations(int c) {
        System.out.println("\n-> Testing basic operations");
        FibonacciHeap heap = new FibonacciHeap(c);

        // Test empty heap
        assertTrue("Empty heap findMin should return null", heap.findMin() == null);
        assertTrue("Empty heap size should be 0", heap.size() == 0);
        assertTrue("Empty heap numTrees should be 0", heap.numTrees() == 0);

        // Test single insert
        FibonacciHeap.HeapNode node1 = heap.insert(5, "five");
        assertTrue("After single insert, findMin should return the inserted node", heap.findMin() == node1);
        assertTrue("After single insert, size should be 1", heap.size() == 1);
        assertTrue("After single insert, numTrees should be 1", heap.numTrees() == 1);

        // Test multiple inserts
        FibonacciHeap.HeapNode node2 = heap.insert(3, "three");
        FibonacciHeap.HeapNode node3 = heap.insert(7, "seven");
        assertTrue("After multiple inserts, findMin should return min key node", heap.findMin() == node2);
        assertTrue("After multiple inserts, size should be 3", heap.size() == 3);
        assertTrue("After multiple inserts, numTrees should be 3", heap.numTrees() == 3);

        // Test deleteMin
        int linksFromDelete = heap.deleteMin();
        assertTrue("After deleteMin, findMin should update", heap.findMin() == node1);
        assertTrue("After deleteMin, size should decrement", heap.size() == 2);

        // Test another deleteMin
        heap.deleteMin();
        assertTrue("After second deleteMin, findMin should update", heap.findMin() == node3);
        assertTrue("After second deleteMin, size should be 1", heap.size() == 1);

        // Test final deleteMin
        heap.deleteMin();
        assertTrue("After final deleteMin, heap should be empty", heap.findMin() == null);
        assertTrue("After final deleteMin, size should be 0", heap.size() == 0);
        assertTrue("After final deleteMin, numTrees should be 0", heap.numTrees() == 0);
    }

    /**
     * Tests more complex operations like decreaseKey, delete, and meld
     */
    private static void testAdvancedOperations(int c) {
        System.out.println("\n-> Testing advanced operations");
        FibonacciHeap heap = new FibonacciHeap(c);

        // Insert nodes to create a more complex heap structure
        FibonacciHeap.HeapNode[] nodes = new FibonacciHeap.HeapNode[10];
        for (int i = 0; i < 10; i++) {
            nodes[i] = heap.insert(10 - i, "node" + i);
        }

        // Test findMin
        assertTrue("Min should be node with key 1", heap.findMin().key == 1);

        // Test decreaseKey
        int cutsBefore = heap.totalCuts();
        heap.decreaseKey(nodes[5], 3); // Decrease key of node from (10-5) to (10-5)-3 = 2
        int cutsAfter = heap.totalCuts();

        assertTrue("decreaseKey should potentially cut nodes", cutsBefore <= cutsAfter);
        assertTrue("After decreaseKey, min may change if key becomes smaller",
                  heap.findMin().key <= nodes[5].key);

        // Test multiple decreaseKey operations
        cutsBefore = heap.totalCuts();
        for (int i = 0; i < 5; i++) {
            heap.decreaseKey(nodes[i], i + 1);
        }
        cutsAfter = heap.totalCuts();

        assertTrue("Multiple decreaseKey operations should potentially cause cuts",
                  cutsBefore <= cutsAfter);

        // Test delete operation
        int sizeBefore = heap.size();
        int deleteLinks = heap.delete(nodes[3]);

        assertTrue("After delete, size should decrease", heap.size() == sizeBefore - 1);
        assertTrue("Delete may cause links during consolidation", deleteLinks >= 0);

        // Test meld operation
        FibonacciHeap heap2 = new FibonacciHeap(c);
        FibonacciHeap.HeapNode[] nodes2 = new FibonacciHeap.HeapNode[5];
        for (int i = 0; i < 5; i++) {
            nodes2[i] = heap2.insert(20 + i, "heap2_node" + i);
        }

        int size1 = heap.size();
        int size2 = heap2.size();
        int trees1 = heap.numTrees();
        int trees2 = heap2.numTrees();

        heap.meld(heap2);

        assertTrue("After meld, size should be sum of both heaps",
                  heap.size() == size1 + size2);
        assertTrue("After meld, numTrees should be at least sum of both heaps before consolidation",
                  heap.numTrees() == trees1 + trees2);
        assertTrue("Min should be preserved after meld",
                  heap.findMin().key < nodes2[0].key);
    }

    /**
     * Tests edge cases and special scenarios
     */
    private static void testEdgeCases(int c) {
        System.out.println("\n-> Testing edge cases");

        // Test deleting from an empty heap
        FibonacciHeap emptyHeap = new FibonacciHeap(c);
        int links = emptyHeap.deleteMin();
        assertTrue("deleteMin on empty heap should return 0 links", links == 0);
        assertTrue("deleteMin on empty heap should keep size 0", emptyHeap.size() == 0);

        // Test melding with empty heaps
        FibonacciHeap heap1 = new FibonacciHeap(c);
        FibonacciHeap heap2 = new FibonacciHeap(c);
        FibonacciHeap heap3 = new FibonacciHeap(c);

        // Insert into heap1 only
        heap1.insert(5, "five");
        heap1.insert(3, "three");

        // Test meld empty with non-empty
        int heap1Size = heap1.size();
        heap1.meld(heap2);
        assertTrue("Melding with empty heap should not change size",
                  heap1.size() == heap1Size);

        // Test meld non-empty with empty
        heap3.meld(heap1);
        assertTrue("Melding empty with non-empty should adopt all nodes",
                  heap3.size() == heap1Size);
        assertTrue("Melding empty with non-empty should adopt min node",
                  heap3.findMin().key == 3);

        // Test decreaseKey with zero or negative values
        FibonacciHeap heap4 = new FibonacciHeap(c);
        FibonacciHeap.HeapNode node = heap4.insert(10, "ten");

        boolean exceptionCaught = false;
        try {
            heap4.decreaseKey(node, 0);
        } catch (IllegalArgumentException e) {
            exceptionCaught = true;
        }
        assertTrue("decreaseKey with zero should throw exception", exceptionCaught);

        exceptionCaught = false;
        try {
            heap4.decreaseKey(node, -5);
        } catch (IllegalArgumentException e) {
            exceptionCaught = true;
        }
        assertTrue("decreaseKey with negative value should throw exception", exceptionCaught);

        exceptionCaught = false;
        try {
            heap4.decreaseKey(node, 11);
        } catch (IllegalArgumentException e) {
            exceptionCaught = true;
        }
        assertTrue("decreaseKey with value > key should throw exception", exceptionCaught);

        // Test insert with non-positive key
        exceptionCaught = false;
        try {
            heap4.insert(0, "zero");
        } catch (IllegalArgumentException e) {
            exceptionCaught = true;
        }
        assertTrue("insert with non-positive key should throw exception", exceptionCaught);
    }

    /**
     * Stress tests with large number of operations
     */
    private static void testStressOperations(int c) {
        System.out.println("\n-> Running stress tests");
        FibonacciHeap heap = new FibonacciHeap(c);

        // Track metrics before stress
        long startTime = System.currentTimeMillis();

        // Insert many nodes
        int n = 10000;
        List<FibonacciHeap.HeapNode> nodes = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            nodes.add(heap.insert(n - i, "node" + i));
        }

        assertTrue("After " + n + " inserts, size should be " + n, heap.size() == n);
        assertTrue("After sequential inserts, min should be 1", heap.findMin().key == 1);

        // Perform many decreaseKey operations
        int decreaseOps = n / 2;
        Random random = new Random(42); // Use seed for reproducibility
        for (int i = 0; i < decreaseOps; i++) {
            int idx = random.nextInt(nodes.size());
            int currentKey = nodes.get(idx).key;
            if (currentKey > 1) {
                int decrease = random.nextInt(currentKey - 1) + 1;
                heap.decreaseKey(nodes.get(idx), decrease);
            }
        }

        // Check min consistency
        assertTrue("Min should still be valid after many decreaseKey operations",
                  findActualMinimum(nodes) == heap.findMin().key);

        // Perform many deleteMin operations
        int deleteOps = n / 4;
        for (int i = 0; i < deleteOps; i++) {
            heap.deleteMin();
        }

        assertTrue("After " + deleteOps + " deleteMin operations, size should be correct",
                  heap.size() == n - deleteOps);

        // Check final structure
        assertTrue("Heap property should be maintained", checkHeapProperty(heap));

        long endTime = System.currentTimeMillis();
        System.out.println("   Stress test completed in " + (endTime - startTime) + "ms");
        System.out.println("   Final metrics - Trees: " + heap.numTrees() +
                          ", Total links: " + heap.totalLinks() +
                          ", Total cuts: " + heap.totalCuts());
    }

    /**
     * Tests focusing on tracking metrics
     */
    private static void testMetrics(int c) {
        System.out.println("\n-> Testing metrics tracking");
        FibonacciHeap heap = new FibonacciHeap(c);

        // Insert and track trees
        for (int i = 1; i <= 10; i++) {
            heap.insert(i, "node" + i);
            assertTrue("After " + i + " inserts, numTrees should match", heap.numTrees() == i);
        }

        // Track links during consolidation
        int linksBefore = heap.totalLinks();
        heap.deleteMin(); // Should trigger consolidation
        int linksAfter = heap.totalLinks();

        assertTrue("deleteMin should perform links during consolidation", linksAfter > linksBefore);
        System.out.println("   Links from consolidation: " + (linksAfter - linksBefore));

        // Perform operations that should cause cascading cuts
        FibonacciHeap cascadeHeap = new FibonacciHeap(c);
        ArrayList<FibonacciHeap.HeapNode> cascadeNodes = new ArrayList<>();

        // Build a deeper heap structure
        // First insert enough nodes to create a consolidated structure
        for (int i = 0; i < 100; i++) {
            cascadeNodes.add(cascadeHeap.insert(100 + i, "node" + i));
        }

        // Force consolidation
        for (int i = 0; i < 20; i++) {
            cascadeHeap.deleteMin();
        }

        // Now perform decreaseKey operations that should trigger cascading cuts
        int cutsBefore = cascadeHeap.totalCuts();

        // Decrease keys to potentially trigger cascading cuts
        for (int i = 1; i < cascadeNodes.size() && i < 50; i++) {
            if (cascadeNodes.get(i).key > 10) {
                cascadeHeap.decreaseKey(cascadeNodes.get(i), 10);
            }
        }

        int cutsAfter = cascadeHeap.totalCuts();
        System.out.println("   Total cuts performed: " + (cutsAfter - cutsBefore));

        // Compare efficiency with different c values
        if (c == 2) { // Base case for comparison
            System.out.println("\n-> Comparing efficiency with c = " + c);
            testEfficiencyMetrics(c);
        }
    }

    /**
     * Tests the efficiency metrics of the Fibonacci heap
     */
    private static void testEfficiencyMetrics(int c) {
        // Set up identical sequence of operations for different c values
        int[] cValues = {2, 3, 4, 5};
        Map<Integer, Integer> totalCutsMap = new HashMap<>();
        Map<Integer, Integer> totalLinksMap = new HashMap<>();
        Map<Integer, Integer> finalTreesMap = new HashMap<>();

        for (int currentC : cValues) {
            FibonacciHeap heap = new FibonacciHeap(currentC);
            List<FibonacciHeap.HeapNode> nodes = new ArrayList<>();
            Set<FibonacciHeap.HeapNode> deletedNodes = new HashSet<>(); // Track deleted nodes

            // Insert nodes
            for (int i = 0; i < 1000; i++) {
                nodes.add(heap.insert(1000 - i, "node" + i));
            }

            // Perform identical sequence of operations
            Random random = new Random(42); // Same seed for all c values

            // Mix of deleteMin and decreaseKey operations
            for (int i = 0; i < 200 && heap.size() > 0; i++) { // Check heap size
                if (i % 4 == 0) {
                    FibonacciHeap.HeapNode minNode = heap.findMin();
                    if (minNode != null) {
                        deletedNodes.add(minNode); // Track the deleted node
                        heap.deleteMin();
                    }
                } else {
                    // Find a valid node that hasn't been deleted
                    FibonacciHeap.HeapNode node = null;
                    int attempts = 0;
                    while (attempts < 10 && heap.size() > 0) { // Limit attempts
                        int idx = random.nextInt(nodes.size());
                        FibonacciHeap.HeapNode candidate = nodes.get(idx);
                        if (!deletedNodes.contains(candidate)) {
                            node = candidate;
                            break;
                        }
                        attempts++;
                    }
                    
                    if (node != null && node.key > 5) {
                        try {
                            heap.decreaseKey(node, 5);
                        } catch (Exception e) {
                            // Node might have been deleted or other issues
                            System.err.println("Error in decreaseKey: " + e.getMessage());
                        }
                    }
                }
            }

            // Record metrics
            totalCutsMap.put(currentC, heap.totalCuts());
            totalLinksMap.put(currentC, heap.totalLinks());
            finalTreesMap.put(currentC, heap.numTrees());
            
            System.out.println("   Completed c=" + currentC + " (size: " + heap.size() + ")");
        }

        // Print comparison
        System.out.println("   Efficiency Metrics Comparison:");
        System.out.println("   c-value | Total Cuts | Total Links | Final Trees");
        System.out.println("   -------------------------------------------");
        for (int currentC : cValues) {
            System.out.printf("   %-7d | %-10d | %-11d | %-11d%n",
                             currentC,
                             totalCutsMap.get(currentC),
                             totalLinksMap.get(currentC),
                             finalTreesMap.get(currentC));
        }
    }

    /**
     * Utility method to find actual minimum key in a list of nodes
     */
    private static int findActualMinimum(List<FibonacciHeap.HeapNode> nodes) {
        int min = Integer.MAX_VALUE;
        for (FibonacciHeap.HeapNode node : nodes) {
            if (node.key < min) {
                min = node.key;
            }
        }
        return min;
    }

    /**
     * Verify that the heap property is maintained (parent key <= child key)
     */
    private static boolean checkHeapProperty(FibonacciHeap heap) {
        if (heap.findMin() == null) return true;

        FibonacciHeap.HeapNode current = heap.findMin();
        FibonacciHeap.HeapNode start = current;

        // Check each tree in the root list
        do {
            if (!checkHeapPropertyRecursive(current)) {
                return false;
            }
            current = current.next;
        } while (current != start);

        return true;
    }

    /**
     * Recursive helper for heap property checking
     */
    private static boolean checkHeapPropertyRecursive(FibonacciHeap.HeapNode node) {
        if (node.child == null) return true;

        FibonacciHeap.HeapNode child = node.child;
        FibonacciHeap.HeapNode start = child;

        do {
            // Child key must be >= parent key for min-heap property
            if (child.key < node.key) {
                System.out.println("Heap property violation: Parent key " +
                                  node.key + " > Child key " + child.key);
                return false;
            }

            // Check child's subtree
            if (!checkHeapPropertyRecursive(child)) {
                return false;
            }

            child = child.next;
        } while (child != start);

        return true;
    }

    /**
     * Helper method for assertions
     */
    private static void assertTrue(String message, boolean condition) {
        if (condition) {
            testsPassed++;
            System.out.println("   [PASS] " + message);
        } else {
            testsFailed++;
            System.out.println("   [FAIL] " + message);
        }
    }
}
