import java.util.*;
import java.io.*;

public class Experiments {
    
    static final int n = 464646;

    public static List<Integer> getPermutation() {
        List<Integer> permutation = new ArrayList<>();
        for (int i=1; i<=n; i++) {
            permutation.add(i);
        }
        Collections.shuffle(permutation);
        return permutation;
    }

    public static void exp1(FibonacciHeap heap) {
        
        // initialize random-order keys
        List<Integer> keys = getPermutation();
        
        // insert into a heap
        for (int key: keys) {
            heap.insert(key, "");
        }

        // delete the min
        heap.deleteMin();

        // delete max repeatedly
        while (heap.size() > 46) {
            heap.delete(heap.findMin().prev);
        }
    }

    public static void main(String[] args) {
        
        final int[] cValues = { 2, 3, 4, 10, 20, 100, 1000, 5000 };
        
        try (PrintWriter writer = new PrintWriter(new FileWriter("results.csv"))) {
            for (int c : cValues) {
                
                FibonacciHeap heap = new FibonacciHeap(c);
                
                // measure runtime
                long start = System.currentTimeMillis();
                exp1(heap);
                long end = System.currentTimeMillis();

                // print to 
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
