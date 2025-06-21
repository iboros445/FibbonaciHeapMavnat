import java.util.*;
import java.io.*;

public class Experiments {
    
    static final int n = 464646;

    public static class Result {
        public int[] keys;
        public int[] ithLargestIndexes;

        public Result(int[] keys, int[] ithLargestIndexes) {
            this.keys = keys;
            this.ithLargestIndexes = ithLargestIndexes;
        }
    }

    public static Result generatePermutationAndRanking() {
        // Generate 1..n and shuffle
        List<Integer> keysList = new ArrayList<>();
        for (int i = 1; i <= n; i++) keysList.add(i);
        Collections.shuffle(keysList);
        // Convert to int array
        int[] keys = keysList.stream().mapToInt(Integer::intValue).toArray();
        // Create index array
        Integer[] indices = new Integer[n];
        for (int i = 0; i < n; i++) indices[i] = i;
        // Sort indices by keys value (descending)
        Arrays.sort(indices, (a, b) -> Integer.compare(keys[b], keys[a]));
        // Convert to int[]
        int[] ithLargestIndexes = Arrays.stream(indices).mapToInt(Integer::intValue).toArray();
        return new Result(keys, ithLargestIndexes);
    }

    public static void exp1(FibonacciHeap heap, Result result) {
        
        // initialize random-order keys
        int[] keys = result.keys;
        int[] ithLargestIndexes = result.ithLargestIndexes;
        
        // insert into a heap
        FibonacciHeap.HeapNode[] nodes = new FibonacciHeap.HeapNode[n];
        for (int i = 0; i < n; i++) {
            nodes[i] = heap.insert(keys[i], "");
        }

        // delete the min
        heap.deleteMin();

        // delete max repeatedly
        int i = 0;
        while (heap.size() > 46) {
            heap.delete(nodes[ithLargestIndexes[i]]);
            i++;
        }
    }


    public static void exp2(FibonacciHeap heap, Result result) {
        // initialize random-order keys
        int[] keys = result.keys;
        int[] ithLargestIndexes = result.ithLargestIndexes;
        
        // insert into a heap
        FibonacciHeap.HeapNode[] nodes = new FibonacciHeap.HeapNode[n];
        for (int i = 0; i < n; i++) {
            nodes[i] = heap.insert(keys[i], "");
        }

        // delete the min
        heap.deleteMin();

        // decrese key of max to 0 until only 46 nonzero keys remain
        for (int i=0; i < n-46; i++) {
            FibonacciHeap.HeapNode max = nodes[ithLargestIndexes[i]];
            heap.decreaseKey(max, max.key);
        }

        // delete the min again
        heap.deleteMin();
    }


    public static void main(String[] args) {
        
        final int[] cValues = { 2, 3, 4, 10, 20, 100, 1000, 5000 };
        
        try (PrintWriter writer = new PrintWriter(new FileWriter("results.csv"))) {
            
            // Print table header
            writer.println("c,TimeMillis,Size,TotalLinks,TotalCuts,NumTrees");
            
            for (int c : cValues) {
                System.out.println("Currect c: " + c);
                
                final int REPS = 20;
                int durations = 0, sizes = 0, links = 0, cuts = 0, trees = 0;
                for (int i = 0; i < REPS; i++) { // average over REPS exps.
                    
                    // Initialize
                    FibonacciHeap heap = new FibonacciHeap(c);
                    Result result = generatePermutationAndRanking();
    
                    // Measure runtime
                    long start = System.currentTimeMillis();
                    exp2(heap, result);
                    long end = System.currentTimeMillis();
                    long duration = end - start;
                    
                    // Increase sums
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
