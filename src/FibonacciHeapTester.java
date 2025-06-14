import java.util.*;

/**
 * Comprehensive tester for FibonacciHeap decreaseKey and delete operations
 */
public class FibonacciHeapTester {
    
    private static int testsPassed = 0;
    private static int testsTotal = 0;
    
    public static void main(String[] args) {
        System.out.println("=== COMPREHENSIVE FIBONACCI HEAP TESTER ===");
        System.out.println("Testing decreaseKey and delete operations\n");
        
        testDecreaseKeyBasic();
        testDecreaseKeyWithCuts();
        testDecreaseKeyEdgeCases();
        testDecreaseKeyMultipleOperations();
        
        testDeleteBasic();
        testDeleteWithCascadingCuts();
        testDeleteEdgeCases();
        testDeleteMultipleOperations();
        
        testCombinedOperations();
        testStressTest();
        
        System.out.println("\n=== TEST SUMMARY ===");
        System.out.println("Tests passed: " + testsPassed + "/" + testsTotal);
        if (testsPassed == testsTotal) {
            System.out.println("V ALL TESTS PASSED!");
        } else {
            System.out.println("X Some tests failed.");
        }
    }
    
    // === DECREASE KEY TESTS ===
    
    private static void testDecreaseKeyBasic() {
        System.out.println("... Testing basic decreaseKey operations...");
        
        // Test 1: Simple decrease key in root list
        testCase("decreaseKey - root node", () -> {
            FibonacciHeap heap = new FibonacciHeap(2);
            FibonacciHeap.HeapNode node1 = heap.insert(10, "10");
            FibonacciHeap.HeapNode node2 = heap.insert(5, "5");
            FibonacciHeap.HeapNode node3 = heap.insert(15, "15");
            
            int cuts = heap.decreaseKey(node3, 8); // 15 -> 7
            
            return cuts == 0 && heap.findMin().key == 5 && node3.key == 7;
        });
        
        // Test 2: Decrease key that becomes new minimum
        testCase("decreaseKey - becomes new minimum", () -> {
            FibonacciHeap heap = new FibonacciHeap(2);
            heap.insert(10, "10");
            FibonacciHeap.HeapNode node = heap.insert(8, "8");
            heap.insert(6, "6");
            
            int cuts = heap.decreaseKey(node, 7); // 8 -> 1
            
            return cuts == 0 && heap.findMin().key == 1 && heap.findMin() == node;
        });
        
        // Test 3: Decrease key but still not minimum
        testCase("decreaseKey - not becoming minimum", () -> {
            FibonacciHeap heap = new FibonacciHeap(2);
            heap.insert(2, "2");
            FibonacciHeap.HeapNode node = heap.insert(10, "10");
            heap.insert(8, "8");
            
            int cuts = heap.decreaseKey(node, 3); // 10 -> 7
            
            return cuts == 0 && heap.findMin().key == 2 && node.key == 7;
        });
    }
    
    private static void testDecreaseKeyWithCuts() {
        System.out.println("... Testing decreaseKey with cuts...");
        
        // Test 1: Force a single cut
        testCase("decreaseKey - single cut", () -> {
            FibonacciHeap heap = new FibonacciHeap(2);
            
            // Create structure that forces cuts
            FibonacciHeap.HeapNode node1 = heap.insert(1, "1");
            FibonacciHeap.HeapNode node5 = heap.insert(5, "5");
            FibonacciHeap.HeapNode node10 = heap.insert(10, "10");
            FibonacciHeap.HeapNode node15 = heap.insert(15, "15");
            
            // Force consolidation by deleting min
            heap.deleteMin();
            
            // Now decrease a child node to trigger cut
            int cuts = heap.decreaseKey(node15, 13); // 15 -> 2
            
            return cuts >= 1 && node15.key == 2 && heap.findMin().key == 2;
        });
        
        // Test 2: Force cascading cuts
        testCase("decreaseKey - cascading cuts", () -> {
            FibonacciHeap heap = new FibonacciHeap(2);
            
            // Build a structure that will cause cascading cuts
            List<FibonacciHeap.HeapNode> nodes = new ArrayList<>();
            for (int i = 1; i <= 20; i++) {
                nodes.add(heap.insert(i, String.valueOf(i)));
            }
            
            // Force some structure by deleting several minimums
            for (int i = 0; i < 5; i++) {
                heap.deleteMin();
            }
            
            // Decrease key to trigger cascading cuts
            int cuts = heap.decreaseKey(nodes.get(19), 18); // 20 -> 2
            
            return cuts >= 1 && nodes.get(19).key == 2;
        });
    }
    
    private static void testDecreaseKeyEdgeCases() {
        System.out.println("... Testing decreaseKey edge cases...");
        
        // Test 1: Decrease key on single node heap
        testCase("decreaseKey - single node", () -> {
            FibonacciHeap heap = new FibonacciHeap(2);
            FibonacciHeap.HeapNode node = heap.insert(10, "10");
            
            int cuts = heap.decreaseKey(node, 5); // 10 -> 5
            
            return cuts == 0 && heap.findMin().key == 5 && heap.size() == 1;
        });
        
        // Test 2: Decrease by 1
        testCase("decreaseKey - minimal decrease", () -> {
            FibonacciHeap heap = new FibonacciHeap(2);
            FibonacciHeap.HeapNode node = heap.insert(10, "10");
            heap.insert(5, "5");
            
            int cuts = heap.decreaseKey(node, 1); // 10 -> 9
            
            return cuts == 0 && node.key == 9 && heap.findMin().key == 5;
        });
        
        // Test 3: Decrease key on minimum node
        testCase("decreaseKey - on minimum node", () -> {
            FibonacciHeap heap = new FibonacciHeap(2);
            FibonacciHeap.HeapNode min = heap.insert(3, "3");
            heap.insert(8, "8");
            heap.insert(12, "12");
            
            int cuts = heap.decreaseKey(min, 2); // 3 -> 1
            
            return cuts == 0 && heap.findMin().key == 1 && heap.findMin() == min;
        });
    }
    
    private static void testDecreaseKeyMultipleOperations() {
        System.out.println("... Testing multiple decreaseKey operations...");
        
        testCase("decreaseKey - multiple operations", () -> {
            FibonacciHeap heap = new FibonacciHeap(2);
            
            List<FibonacciHeap.HeapNode> nodes = new ArrayList<>();
            for (int i = 10; i >= 1; i--) {
                nodes.add(heap.insert(i * 10, String.valueOf(i * 10)));
            }
            
            boolean success = true;
            int totalCuts = 0;
            
            // Perform multiple decrease operations
            for (int i = 0; i < nodes.size(); i++) {
                int cuts = heap.decreaseKey(nodes.get(i), 5);
                totalCuts += cuts;
                success = success && (nodes.get(i).key == (10 - i) * 10 - 5);
            }
            
            return success && heap.findMin().key == 5; // 10 - 5 = 5
        });
    }
    
    // === DELETE TESTS ===
    
    private static void testDeleteBasic() {
        System.out.println("... Testing basic delete operations...");
        
        // Test 1: Delete from root list
        testCase("delete - root node", () -> {
            FibonacciHeap heap = new FibonacciHeap(2);
            heap.insert(5, "5");
            FibonacciHeap.HeapNode nodeToDelete = heap.insert(10, "10");
            heap.insert(3, "3");
            
            int originalSize = heap.size();
            int links = heap.delete(nodeToDelete);
            
            return heap.size() == originalSize - 1 && heap.findMin().key == 3;
        });
        
        // Test 2: Delete minimum node
        testCase("delete - minimum node", () -> {
            FibonacciHeap heap = new FibonacciHeap(2);
            FibonacciHeap.HeapNode min = heap.insert(2, "2");
            heap.insert(8, "8");
            heap.insert(15, "15");
            
            int originalSize = heap.size();
            int links = heap.delete(min);
            
            return heap.size() == originalSize - 1 && heap.findMin().key == 8;
        });
        
        // Test 3: Delete non-minimum node
        testCase("delete - non-minimum node", () -> {
            FibonacciHeap heap = new FibonacciHeap(2);
            heap.insert(2, "2");
            FibonacciHeap.HeapNode nodeToDelete = heap.insert(8, "8");
            heap.insert(15, "15");
            
            int originalSize = heap.size();
            int links = heap.delete(nodeToDelete);
            
            return heap.size() == originalSize - 1 && heap.findMin().key == 2;
        });
    }
    
    private static void testDeleteWithCascadingCuts() {
        System.out.println("... Testing delete with cascading cuts...");
        
        testCase("delete - with cascading cuts", () -> {
            FibonacciHeap heap = new FibonacciHeap(2);
            
            // Create complex structure
            List<FibonacciHeap.HeapNode> nodes = new ArrayList<>();
            for (int i = 1; i <= 15; i++) {
                nodes.add(heap.insert(i, String.valueOf(i)));
            }
            
            // Force some consolidation
            for (int i = 0; i < 3; i++) {
                heap.deleteMin();
            }
            
            int originalSize = heap.size();
            int links = heap.delete(nodes.get(10)); // Delete node with value 11
            
            return heap.size() == originalSize - 1 && heap.findMin().key == 4;
        });
    }
    
    private static void testDeleteEdgeCases() {
        System.out.println("... Testing delete edge cases...");
        
        // Test 1: Delete from single node heap
        testCase("delete - single node heap", () -> {
            FibonacciHeap heap = new FibonacciHeap(2);
            FibonacciHeap.HeapNode node = heap.insert(42, "42");
            
            int links = heap.delete(node);
            
            return heap.size() == 0 && heap.findMin() == null;
        });
        
        // Test 2: Delete from two node heap
        testCase("delete - two node heap", () -> {
            FibonacciHeap heap = new FibonacciHeap(2);
            FibonacciHeap.HeapNode node1 = heap.insert(5, "5");
            FibonacciHeap.HeapNode node2 = heap.insert(10, "10");
            
            int links = heap.delete(node2);
            
            return heap.size() == 1 && heap.findMin().key == 5;
        });
        
        // Test 3: Delete all nodes one by one
        testCase("delete - all nodes", () -> {
            FibonacciHeap heap = new FibonacciHeap(2);
            
            List<FibonacciHeap.HeapNode> nodes = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                nodes.add(heap.insert(i * 3, String.valueOf(i * 3)));
            }
            
            boolean success = true;
            for (int i = 0; i < nodes.size(); i++) {
                int originalSize = heap.size();
                heap.delete(nodes.get(i));
                success = success && (heap.size() == originalSize - 1);
            }
            
            return success && heap.size() == 0 && heap.findMin() == null;
        });
    }
    
    private static void testDeleteMultipleOperations() {
        System.out.println(" Testing multiple delete operations...");
        
        testCase("delete - multiple random deletions", () -> {
            FibonacciHeap heap = new FibonacciHeap(2);
            
            List<FibonacciHeap.HeapNode> nodes = new ArrayList<>();
            for (int i = 1; i <= 20; i++) {
                nodes.add(heap.insert(i, String.valueOf(i)));
            }
            
            // Delete every other node
            boolean success = true;
            int expectedSize = 20;
            
            for (int i = 1; i < nodes.size(); i += 2) {
                int originalSize = heap.size();
                heap.delete(nodes.get(i));
                expectedSize--;
                success = success && (heap.size() == expectedSize);
            }
            
            return success && heap.findMin().key == 1;
        });
    }
    
    // === COMBINED OPERATIONS TESTS ===
    
    private static void testCombinedOperations() {
        System.out.println("... Testing combined operations...");
        
        testCase("combined - decrease then delete", () -> {
            FibonacciHeap heap = new FibonacciHeap(2);
            
            FibonacciHeap.HeapNode node1 = heap.insert(10, "10");
            FibonacciHeap.HeapNode node2 = heap.insert(20, "20");
            FibonacciHeap.HeapNode node3 = heap.insert(5, "5");
            
            // Decrease key first
            int cuts = heap.decreaseKey(node2, 18); // 20 -> 2
            
            // Then delete the decreased node
            int links = heap.delete(node2);
            
            return heap.size() == 2 && heap.findMin().key == 5;
        });
        
        testCase("combined - interleaved operations", () -> {
            FibonacciHeap heap = new FibonacciHeap(2);
            
            List<FibonacciHeap.HeapNode> nodes = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                nodes.add(heap.insert(i * 5, String.valueOf(i * 5)));
            }
            
            boolean success = true;
            
            // Decrease some keys
            heap.decreaseKey(nodes.get(5), 10); // 30 -> 20
            heap.decreaseKey(nodes.get(8), 20); // 45 -> 25
            
            // Delete some nodes
            heap.delete(nodes.get(2)); // Delete 15
            heap.delete(nodes.get(7)); // Delete 40
            
            // More operations
            heap.decreaseKey(nodes.get(9), 30); // 50 -> 20
            heap.delete(nodes.get(0)); // Delete 5
            
            return heap.size() == 7 && heap.findMin().key == 10;
        });
    }
    
    // === STRESS TEST ===
    
    private static void testStressTest() {
        System.out.println(" Running stress test...");
        
        testCase("stress - large scale operations", () -> {
            FibonacciHeap heap = new FibonacciHeap(3);
            
            List<FibonacciHeap.HeapNode> nodes = new ArrayList<>();
            
            // Insert many nodes
            for (int i = 1; i <= 100; i++) {
                nodes.add(heap.insert(i, String.valueOf(i)));
            }
            
            boolean success = true;
            
            // Perform many decrease operations
            for (int i = 50; i < 100; i++) {
                try {
                    heap.decreaseKey(nodes.get(i), Math.min(10, nodes.get(i).key - 1));
                } catch (Exception e) {
                    success = false;
                    break;
                }
            }
            
            // Delete many nodes
            for (int i = 10; i < 50; i += 3) {
                try {
                    heap.delete(nodes.get(i));
                } catch (Exception e) {
                    success = false;
                    break;
                }
            }
            
            return success && heap.size() > 0 && heap.findMin() != null;
        });
    }
    
    // === UTILITY METHODS ===
    
    private static void testCase(String testName, TestFunction test) {
        testsTotal++;
        try {
            boolean result = test.run();
            if (result) {
                System.out.println("V " + testName);
                testsPassed++;
            } else {
                System.out.println("X " + testName + " - FAILED");
            }
        } catch (Exception e) {
            System.out.println("X " + testName + " - ERROR: " + e.getMessage());
        }
    }
    
    @FunctionalInterface
    private interface TestFunction {
        boolean run() throws Exception;
    }
    
    // Helper method to validate heap properties
    private static boolean validateHeapProperty(FibonacciHeap heap) {
        if (heap.findMin() == null) {
            return heap.size() == 0;
        }
        
        // Additional validation could be added here
        return true;
    }
}
