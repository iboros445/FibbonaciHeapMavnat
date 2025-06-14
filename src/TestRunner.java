/**
 * Simple test runner for the Fibonacci Heap tester
 */
public class TestRunner {
    public static void main(String[] args) {
        System.out.println("Choose test mode:");
        System.out.println("1. Run comprehensive tester");
        System.out.println("2. Run basic manual test");
        System.out.println("3. Run visualization test");
        
        // For now, let's run the comprehensive tester by default
        if (args.length == 0 || args[0].equals("1")) {
            FibonacciHeapTester.main(args);
        } else if (args[0].equals("2")) {
            runBasicTest();
        } else if (args[0].equals("3")) {
            runVisualizationTest();
        }
    }
    
    private static void runBasicTest() {
        System.out.println("=== BASIC MANUAL TEST ===");
        
        FibonacciHeap heap = new FibonacciHeap(2);
        
        // Insert some values
        System.out.println("Inserting values: 10, 5, 15, 8, 20");
        FibonacciHeap.HeapNode node10 = heap.insert(10, "10");
        FibonacciHeap.HeapNode node5 = heap.insert(5, "5");
        FibonacciHeap.HeapNode node15 = heap.insert(15, "15");
        FibonacciHeap.HeapNode node8 = heap.insert(8, "8");
        FibonacciHeap.HeapNode node20 = heap.insert(20, "20");
        
        System.out.println("Initial heap - Min: " + heap.findMin().key + ", Size: " + heap.size());
        
        // Test decreaseKey
        System.out.println("\nTesting decreaseKey...");
        System.out.println("Decreasing 15 to 3 (decrease by 12)");
        int cuts1 = heap.decreaseKey(node15, 12);
        System.out.println("Cuts made: " + cuts1);
        System.out.println("New min: " + heap.findMin().key + " (should be 3)");
        
        System.out.println("Decreasing 20 to 16 (decrease by 4)");
        int cuts2 = heap.decreaseKey(node20, 4);
        System.out.println("Cuts made: " + cuts2);
        System.out.println("Current min: " + heap.findMin().key + " (should still be 3)");
        
        // Test delete
        System.out.println("\nTesting delete...");
        System.out.println("Deleting node with key 8");
        int links1 = heap.delete(node8);
        System.out.println("Links made: " + links1);
        System.out.println("Size after delete: " + heap.size() + " (should be 4)");
        System.out.println("Min after delete: " + heap.findMin().key + " (should still be 3)");
        
        System.out.println("Deleting current minimum (3)");
        int links2 = heap.delete(node15); // This was decreased to 3
        System.out.println("Links made: " + links2);
        System.out.println("Size after delete: " + heap.size() + " (should be 3)");
        System.out.println("New min: " + heap.findMin().key + " (should be 5)");
        
        // Final state
        System.out.println("\nFinal state:");
        System.out.println("Total links: " + heap.totalLinks());
        System.out.println("Total cuts: " + heap.totalCuts());
        System.out.println("Final size: " + heap.size());
        System.out.println("Final min: " + heap.findMin().key);
    }
    
    private static void runVisualizationTest() {
        System.out.println("=== VISUALIZATION TEST ===");
        System.out.println("This will open a visual representation of the heap operations.");
        
        FibonacciHeap heap = new FibonacciHeap(2);
        
        // Insert some values
        FibonacciHeap.HeapNode node10 = heap.insert(10, "10");
        FibonacciHeap.HeapNode node5 = heap.insert(5, "5");
        FibonacciHeap.HeapNode node15 = heap.insert(15, "15");
        FibonacciHeap.HeapNode node8 = heap.insert(8, "8");
        
        System.out.println("Initial heap created. Opening visualizer...");
        try {
            heap.visualize();
            
            // Wait a bit then perform operations
            Thread.sleep(2000);
            
            System.out.println("Performing decreaseKey(15, 12) - 15 becomes 3");
            heap.decreaseKey(node15, 12);
            
            Thread.sleep(2000);
            
            System.out.println("Performing delete(8)");
            heap.delete(node8);
            
            System.out.println("Operations complete. Check the visualizer.");
            
        } catch (Exception e) {
            System.err.println("Error with visualization: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
