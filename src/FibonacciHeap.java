import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/**
 * FibonacciHeap
 *
 * An implementation of Fibonacci heap over positive integers.
 *
 */
public class FibonacciHeap
{
	public HeapNode min;	
	private int size;
	private int length;
	private final int nodesToCut;
	private int totalLinksCount;
	private int totalCutsCount;
	/**
	 *
	 * Constructor to initialize an empty heap.
	 * pre: c >= 2.
	 *
	 */
	public FibonacciHeap(int c)
	{
		this.min = null;
		this.size = 0;
		this.nodesToCut = c;
		this.totalLinksCount = 0;
		this.totalCutsCount = 0;

	}

	/**
	 * 
	 * pre: key > 0
	 *
	 * Insert (key,info) into the heap and return the newly generated HeapNode.
	 *
	 */	public HeapNode insert(int key, String info) 
	{   		
		if (key <= 0) {
			throw new IllegalArgumentException("Key must be positive");
		}

		// Create and initialize node efficiently
		HeapNode node = new HeapNode();
		node.key = key; 
		node.info = info;
		// Other fields are already initialized to default values (0, false, null)
		
		if (this.min == null) {
			// First node in heap
			min = node;
			node.next = node;
			node.prev = node;
			this.length = 1;
		} else {
			// Add to root list and update min in one step
			addToRootList(node);
			if (key < min.key) {
				min = node;
			}
		}
		
		size++; 
		return node;
	}
	/**
	 * 
	 * pre: node is valid
	 *
	 * Insert node into the rootlist.
	 *
	 * Happens in O(1) always.
	 */
	private void addToRootList(HeapNode node){
		if (length == 0) {
			node.next = node;
			node.prev = node;
		} else {
			node.prev = min;
			node.next = min.next;
			min.next.prev = node;
			min.next = node;
		}
		this.length++;
	}

	/**
	 * 
	 * Return the minimal HeapNode, null if empty.
	 *
	 */
	public HeapNode findMin()
	{
		
		return min;
	}
	/**
	 * 
	 * Delete the minimal item.
	 * Return the number of links.
	 *
	 */
	public int deleteMin()
	{
		HeapNode minNode = min;
		int links = 0;
		if (minNode != null) {
			// First, add children to root list BEFORE removing minNode
			if (minNode.child != null) {
				addChildrenToRootList(minNode);
			}
			
			// Remove minNode from root list
			if (minNode == minNode.next) {
				// Only one node in root list
				min = null;
				length = 0;
			} else {
				// Update min to point to next node before removal
				min = minNode.next;
				removeNodeFromRootList(minNode);
				length--; // Decrease by 1 since we removed minNode
				
				// Now consolidate
				links = consolidate();
			}
			size--;
		}
		return links;

	}

	private void addChildrenToRootList(HeapNode minNode) {
	    if (minNode.child == null) return;
	    
	    HeapNode child = minNode.child;
	    HeapNode firstChild = child;
	    
	    // First, break the children's circular list and reset their parent pointers
	    do {
	        child.parent = null;
	        child = child.next;
	    } while (child != firstChild);
	    
	    // Now splice the entire children list into the root list
	    // Get the last child in the circular list
	    HeapNode lastChild = firstChild.prev;
	    
	    // Insert the children list between minNode.prev and minNode
	    HeapNode prevNode = minNode.prev;
	    
	    // Connect previous node to first child
	    prevNode.next = firstChild;
	    firstChild.prev = prevNode;
	    
	    // Connect last child to minNode
	    lastChild.next = minNode;
	    minNode.prev = lastChild;
	    
	    // Count children for length update
	    HeapNode temp = firstChild;
	    do {
	        this.length++;
	        temp = temp.next;
	    } while (temp != firstChild);
	}

	// Get a list of all root nodes - optimized version
    private List<HeapNode> getRootList() {
        List<HeapNode> rootList = new ArrayList<>(this.length); // Pre-allocate with known size
        if (min == null) return rootList;

        HeapNode current = min;
        int count = 0;
        do {
            rootList.add(current);
            current = current.next;
            count++;
            if (count > this.length + 1) { // Simple bounds check instead of HashSet
                System.err.println("ERROR: Infinite loop detected in root list!");
                break;
            }
        } while (current != min);
        
        return rootList;
    }		// Consolidate the trees in the root list - optimized version
    private int consolidate() {
        if (size == 0) return 0;
        
        // Pre-calculate array size more efficiently
        int arraySize = 45; // log_2(2^31) = 31, but we add some buffer. 45 should be more than enough
        HeapNode[] array = new HeapNode[arraySize]; // Use array instead of ArrayList for better performance

        List<HeapNode> rootList = getRootList();
        int links = 0;

        for (HeapNode node : rootList) {
            int rank = node.rank;
            
            // Simple bounds check - should rarely trigger with size 45
            if (rank >= arraySize) continue; // Skip instead of error logging for performance
            
            while (rank < arraySize && array[rank] != null) {
                HeapNode other = array[rank];
                if (node.key > other.key) {
                    HeapNode temp = node;
                    node = other;
                    other = temp;
                }
              
              	// Link two trees of the same rank
                link(other, node); 
                links++;
                array[rank] = null;
                rank++;
            }
            
            if (rank < arraySize) {
                array[rank] = node;
            }
        }

		this.min = null;
		this.length = 0;
		
		// Rebuild root list from array - optimized
		for (HeapNode node : array) {
			if (node != null) {
				node.next = node;
				node.prev = node;
				
				if (this.min == null) {
					this.min = node;
					this.length = 1;
				} else {
					// Manually insert into root list
					node.prev = this.min.prev;
					node.next = this.min;
					this.min.prev.next = node;
					this.min.prev = node;
					this.length++;
					
					if (node.key < this.min.key) {
						this.min = node;
					}
				}
			}
		}
        return links;
    }


    // Link two trees of the same degree
    private void link(HeapNode y, HeapNode x) {
      	// Remove y from the root list
        removeNodeFromRootList(y); 
        y.prev = y.next = y;
        y.parent = x;
      	
        if (x.child == null) {
          	// Make y a child of x
            x.child = y; 
        } else {
            y.next = x.child;
            y.prev = x.child.prev;
            x.child.prev.next = y;
            x.child.prev = y;
        }
        x.rank++;
		this.totalLinksCount++;
        y.isLoser = false;
    }
	
	    // Remove a node from the root list
    private void removeNodeFromRootList(HeapNode node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }
	/**
	 * Cut a node from its parent and add it to the root list
	 */
	private int cut(HeapNode x, HeapNode y) {
		// Remove x from child list of y
		if (x.next == x) {
			y.child = null;
		} else {
			if (y.child == x) {
				y.child = x.next;
			}
			x.prev.next = x.next;
			x.next.prev = x.prev;
		}
		
		y.rank--;
		
		// Add x to root list efficiently
		addToRootList(x);
		
		x.parent = null;
		x.isLoser = false;
		y.markCnt++;
		this.totalCutsCount++;
		
		return 1;
	}


	/**
	 * Perform cascading cut operation
	 */
	private int cascadingCut(HeapNode y) {
		HeapNode parent = y.parent;
		int cuts = 0;
		
		if (parent != null) {
			if (!y.isLoser) {
				// C-1 time this node loses a child - mark it
				if (y.markCnt>=this.nodesToCut-1){
					y.isLoser = true;
				}
			} else {
				// Node was already marked - cut it and cascade
				cuts += cut(y, parent);
				y.markCnt = 0;
				cuts += cascadingCut(parent);
			}
		}
		
		return cuts;
	}
	/**
	 * 
	 * pre: 0<diff<x.key
	 * 
	 * Decrease the key of x by diff and fix the heap.
	 * Return the number of cuts.
	 * 
	 */
	public int decreaseKey(HeapNode x, int diff) 
	{    
	    // Validate input - simplified for performance
	    if (x == null || diff <= 0 || diff > x.key) {
	        throw new IllegalArgumentException("Invalid decrease key operation");
	    }
	    
	    x.key = x.key - diff;
	    HeapNode parent = x.parent;
	    int cuts = 0;
	    
	    if (parent != null && x.key < parent.key) {
	        cuts += cut(x, parent);
	        cuts += cascadingCut(parent);
	    }
	    
	    if (x.key < min.key) {
	        min = x;
	    }
	    
	    return cuts;
	}/**
	 * 
	 * Delete the x from the heap.
	 * Return the number of links.
	 *
	 */	public int delete(HeapNode x) 
	{    
	    // Make x smaller than current minimum
	    int diff = x.key - min.key + 1;
	    if (diff > 0) {
	        decreaseKey(x, diff);
	    }
	    
	    // Delete minimum (which is now x)
	    int links = deleteMin();
	    return links;
	}

	/**
	 * 
	 * Return the total number of links.
	 * 
	 */
	public int totalLinks()
	{
		return totalLinksCount;
	}

	/**
	 * 
	 * Return the total number of cuts.
	 * 
	 */
	public int totalCuts()
	{
		return totalCutsCount;
	}

	/**
	 * 
	 * Meld the heap with heap2
	 *
	 */
	public void meld(FibonacciHeap heap2)
	{
		if (heap2 == null || heap2.min == null) {
			return;
		}
		
		if (this.min == null) {
			this.min = heap2.min;
			this.size = heap2.size;
			this.totalLinksCount += heap2.totalLinksCount;
			this.totalCutsCount += heap2.totalCutsCount;
			return;
		}
		
		// Connect the two root lists
		HeapNode thisLast = this.min.prev;
		HeapNode heap2Last = heap2.min.prev;
		
		thisLast.next = heap2.min;
		heap2.min.prev = thisLast;
		heap2Last.next = this.min;
		this.min.prev = heap2Last;
		
		// Update minimum if necessary
		if (heap2.min.key < this.min.key) {
			this.min = heap2.min;
		}
		
		// Update counters
		this.size += heap2.size;
		this.length += heap2.length;
		this.totalLinksCount += heap2.totalLinksCount;
		this.totalCutsCount += heap2.totalCutsCount;
	}

	/**
	 * 
	 * Return the number of elements in the heap
	 *   
	 */
	public int size()
	{
		return size;
	}


	/**
	 * 
	 * Return the number of trees in the heap.
	 * 
	 */
	public int numTrees()
	{
		return this.length;
	}	
	
	public void visualize() {
        System.out.println("Visualizer currently disabled for testing.");
        try {
            SwingUtilities.invokeLater(() -> {
                new FibonacciHeapVisualizer(this).setVisible(true);
            });
        } catch (Exception e) {
            System.out.println("Visualizer not available: " + e.getMessage());
        }
    }
	/**
	 * Class implementing a node in a Fibonacci Heap.
	 *  
	 */
	public static class HeapNode{
		public int key;
		public String info;
		public HeapNode child;
		public HeapNode next;
		public HeapNode prev;
		public HeapNode parent;
		public int rank;
		public boolean isLoser;
		public int markCnt; // For visualizer compatibility
	}
}
