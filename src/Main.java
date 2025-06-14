public class Main {
    public static void main(String[] args) {
        // Creating an initial heap like the Python example
        System.out.println("Creating an initial heap");
        FibonacciHeap heap = new FibonacciHeap(2);
        
        // Insert 3 nodes like in Python example
        FibonacciHeap.HeapNode node5 = heap.insert(5, "info5");
        FibonacciHeap.HeapNode node2 = heap.insert(2, "info2");
        FibonacciHeap.HeapNode node8 = heap.insert(8, "info8");
        
        // Display the heap
        displayHeap(heap);
        
        // Extract minimum
        System.out.println("Extracting min");
        int links = heap.deleteMin();
        System.out.println("Number of links in deleteMin: " + links);
        displayHeap(heap);
        
        // Decrease value of 8 to 7 (decrease by 1)
        System.out.println("Decrease value of 8 to 7");
        int cuts = heap.decreaseKey(node8, 1);
        System.out.println("Number of cuts in decreaseKey: " + cuts);
        displayHeap(heap);
        
        // Delete the node with key 7 (previously 8)
        System.out.println("Now we will delete the node '7'");
        int deleteLinks = heap.delete(node8);
        System.out.println("Number of links in delete: " + deleteLinks);
        displayHeap(heap);
        
        System.out.println("Total links: " + heap.totalLinks());
        System.out.println("Total cuts: " + heap.totalCuts());
    }
    
    private static void displayHeap(FibonacciHeap heap) {
        if (heap.findMin() == null) {
            System.out.println("The heap is empty");
        } else {
            System.out.println("Minimum element: " + heap.findMin().key);
            System.out.println("Heap size: " + heap.size());
            System.out.println("Number of trees: " + heap.numTrees());
        }
        System.out.println();
    }
}