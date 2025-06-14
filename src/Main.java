public class Main {
    public static void main(String[] args) {
        // Creating an initial heap like the Python example
        System.out.println("Creating an initial heap");
        FibonacciHeap heap = new FibonacciHeap(3);
        for (int i=1;i<=33;i++){
            heap.insert(i, "");
        }
        heap.visualize();
    }
    
} 