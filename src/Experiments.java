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
            
            writer.println("c,TimeMillis,Size,TotalLinks,TotalCuts,NumTrees");
            for (int c : cValues) {
                System.out.println("Currect c: " + c);
                
                int durations = 0, sizes = 0, links = 0, cuts = 0, trees = 0;
                final int REPS = 20;
                for (int i = 0; i < REPS; i++) { // average over REPS exps.
                    FibonacciHeap heap = new FibonacciHeap(c);
    
                    // Measure runtime
                    long start = System.currentTimeMillis();
                    exp1(heap);
                    long end = System.currentTimeMillis();
                    long duration = end - start;
                    
                    durations += duration;
                    sizes += heap.size();
                    links += heap.totalLinks();
                    cuts += heap.totalCuts();
                    trees += heap.numTrees();
                }
                // Write values to CSV
                writer.printf("%d,%d,%d,%d,%d,%d%n",
                    c, durations/REPS, sizes/REPS, links/REPS, cuts/REPS, trees/REPS);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
