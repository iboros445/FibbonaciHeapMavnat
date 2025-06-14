import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CancellationException; // Added for SwingWorker error handling
import java.util.concurrent.ExecutionException; // Added for SwingWorker error handling

/**
 * Enhanced Swing-based visualization for FibonacciHeap with responsive layout
 */
class FibonacciHeapVisualizer extends JFrame {
    private FibonacciHeap heap;
    private HeapPanel heapPanel;
    private JLabel statsLabel;
    private JSlider zoomSlider;
    private JCheckBox autoFitCheckbox;

    public FibonacciHeapVisualizer(FibonacciHeap heap) {
        this.heap = heap;
        initializeGUI();
    }

    private void initializeGUI() {
        setTitle("Fibonacci Heap Visualizer - Responsive");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create control panel
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);

        // Create heap visualization panel
        heapPanel = new HeapPanel();
        JScrollPane scrollPane = new JScrollPane(heapPanel);
        scrollPane.setPreferredSize(new Dimension(1000, 700));
        scrollPane.getHorizontalScrollBar().setUnitIncrement(20);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        add(scrollPane, BorderLayout.CENTER);

        // Create stats and controls panel
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);

        // Add component listener for window resize
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (autoFitCheckbox.isSelected()) {
                    heapPanel.adjustLayoutToWindow();
                }
                heapPanel.repaint();
            }
        });

        pack();
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        // Initial layout calculation and centering after GUI is visible
        SwingUtilities.invokeLater(() -> {
            heapPanel.updateLayout();
            heapPanel.centerView(); // Center the view on startup
        });
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout());

        // Insert controls
        JTextField keyField = new JTextField(5);
        JTextField infoField = new JTextField(10);
        JButton insertBtn = new JButton("Insert");

        insertBtn.addActionListener(e -> {
            try {
                int key = Integer.parseInt(keyField.getText());
                String info = infoField.getText().isEmpty() ? "" : infoField.getText();
                heap.insert(key, info);
                heapPanel.updateLayout();
                updateStats();
                keyField.setText("");
                infoField.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number");
            }
        });

        // Delete min button
        JButton deleteMinBtn = new JButton("Delete Min");
        deleteMinBtn.addActionListener(e -> {
            if (heap.size() > 0) {
                heap.deleteMin();
                heapPanel.updateLayout();
                updateStats();
            }
        });

        // Decrease key controls
        JTextField currentKeyField = new JTextField(5);
        JTextField newKeyField = new JTextField(5);
        JButton decreaseKeyBtn = new JButton("Decrease Key");

        decreaseKeyBtn.addActionListener(e -> {
            try {
                int currentKey = Integer.parseInt(currentKeyField.getText());
                int delta = Integer.parseInt(newKeyField.getText());

                if (delta <= 0 || delta >= currentKey) { // Changed condition for valid delta
                    JOptionPane.showMessageDialog(this, "Delta must be positive and less than current key");
                    return;
                }

                FibonacciHeap.HeapNode node = findNodeByKey(currentKey);
                if (node == null) {
                    JOptionPane.showMessageDialog(this, "Node with key " + currentKey + " not found");
                    return;
                }

                // Check if the new key would be less than or equal to 0
                if (node.key - delta <= 0) {
                    JOptionPane.showMessageDialog(this, "Decreasing by " + delta + " would result in a non-positive key. Only positive keys are allowed in this heap.");
                    return;
                }

                heap.decreaseKey(node, delta);
                heapPanel.updateLayout();
                updateStats();

                currentKeyField.setText("");
                newKeyField.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });

        JTextField deleteKeyField = new JTextField(5);
        JButton deleteNodeBtn = new JButton("Delete Node");

        deleteNodeBtn.addActionListener(e -> {
            try {
                int keyToDelete = Integer.parseInt(deleteKeyField.getText());
                FibonacciHeap.HeapNode nodeToDelete = findNodeByKey(keyToDelete);
                if (nodeToDelete == null) {
                    JOptionPane.showMessageDialog(this, "Node with key " + keyToDelete + " not found.");
                    return;
                }
                heap.delete(nodeToDelete); // Call the heap's delete function
                heapPanel.updateLayout();
                updateStats();
                deleteKeyField.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number for key to delete.");
            } catch (NoSuchElementException ex) {
                // This might happen if the heap is empty, though findNodeByKey should handle it
                JOptionPane.showMessageDialog(this, "Heap is empty or node not found.");
            }
        });


        // Center view button
        JButton centerBtn = new JButton("Center View");
        centerBtn.addActionListener(e -> heapPanel.centerView());

        // Fit to window button
        JButton fitBtn = new JButton("Fit to Window");
        fitBtn.addActionListener(e -> heapPanel.fitToWindow());

        panel.add(new JLabel("Key:"));
        panel.add(keyField);
        panel.add(new JLabel("Info:"));
        panel.add(infoField);
        panel.add(insertBtn);

        panel.add(deleteMinBtn);

        // Add decrease key controls
        panel.add(new JLabel("Current Key:"));
        panel.add(currentKeyField);
        panel.add(new JLabel("Delta:"));
        panel.add(newKeyField);
        panel.add(decreaseKeyBtn);

        // Add delete specific node controls
        panel.add(new JLabel("Delete Key:"));
        panel.add(deleteKeyField);
        panel.add(deleteNodeBtn);

        panel.add(centerBtn);
        panel.add(fitBtn);

        return panel;
    }

    private FibonacciHeap.HeapNode findNodeByKey(int key) {
        if (heap.min == null) return null;

        Set<FibonacciHeap.HeapNode> visited = new HashSet<>();
        Deque<FibonacciHeap.HeapNode> queue = new ArrayDeque<>(); // Changed to Queue for BFS-like traversal

        // Start from all roots in the root list
        FibonacciHeap.HeapNode current = heap.min;
        do {
            queue.offer(current);
            current = current.next;
        } while (current != heap.min);

        while (!queue.isEmpty()) {
            FibonacciHeap.HeapNode node = queue.poll(); // Use poll for Queue
            if (visited.contains(node)) continue;
            visited.add(node);

            if (node.key == key) {
                return node;
            }

            // Add children to queue
            if (node.child != null) {
                FibonacciHeap.HeapNode child = node.child;
                do {
                    if (!visited.contains(child)) {
                        queue.offer(child); // Use offer for Queue
                    }
                    child = child.next;
                } while (child != node.child);
            }
        }
        return null;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Stats label
        statsLabel = new JLabel();
        updateStats();
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statsPanel.add(statsLabel);

        // Zoom and layout controls
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // Zoom slider
        controlsPanel.add(new JLabel("Zoom:"));
        zoomSlider = new JSlider(25, 200, 100);
        zoomSlider.setMajorTickSpacing(25);
        zoomSlider.setPaintTicks(true);
        zoomSlider.addChangeListener(e -> {
            heapPanel.setZoomFactor(zoomSlider.getValue() / 100.0);
            heapPanel.repaint();
        });
        controlsPanel.add(zoomSlider);

        // Auto-fit checkbox
        autoFitCheckbox = new JCheckBox("Auto-fit", true);
        autoFitCheckbox.addActionListener(e -> {
            if (autoFitCheckbox.isSelected()) {
                heapPanel.adjustLayoutToWindow();
            }
            heapPanel.repaint(); // Repaint even if not auto-fitting, to reflect state change
        });
        controlsPanel.add(autoFitCheckbox);

        // Layout style combo
        JComboBox<String> layoutCombo = new JComboBox<>(new String[]{"Compact", "Spread", "Circular"});
        layoutCombo.addActionListener(e -> {
            heapPanel.setLayoutStyle((String) layoutCombo.getSelectedItem());
            heapPanel.updateLayout();
        });
        controlsPanel.add(new JLabel("Layout:"));
        controlsPanel.add(layoutCombo);

        panel.add(statsPanel, BorderLayout.WEST);
        panel.add(controlsPanel, BorderLayout.EAST);

        return panel;
    }

    private void updateStats() {
        String stats = String.format(
                "Size: %d | Trees: %d | Links: %d | Cuts: %d",
                heap.size(), heap.numTrees(), heap.totalLinks(), heap.totalCuts()
        );
        if (heap.findMin() != null) {
            stats += " | Min: " + heap.findMin().key;
        }
        statsLabel.setText(stats);
    }

    /**
     * Enhanced JPanel for drawing the heap with responsive features
     */
    class HeapPanel extends JPanel {
        private int baseNodeRadius = 25;
        // Adjusted minimum spacing values
        private int minHorizontalSpacing = 60;
        private int minVerticalSpacing = 80;
        private double zoomFactor = 1.0;
        private String layoutStyle = "Compact";

        private final Color NODE_COLOR = new Color(102, 126, 234);
        private final Color MIN_NODE_COLOR = new Color(231, 76, 60);
        private final Color EDGE_COLOR = new Color(52, 73, 94);
        private final Color HORIZONTAL_EDGE_COLOR = new Color(39, 174, 96);
        private final Color MARKED_NODE_COLOR = new Color(255, 165, 0); // Orange for marked nodes

        private Map<FibonacciHeap.HeapNode, Point> nodePositions = new HashMap<>();
        private Dimension contentSize = new Dimension(800, 600);

        // Keep track of the current SwingWorker to cancel previous ones
        private SwingWorker<Map<FibonacciHeap.HeapNode, Point>, Void> currentLayoutWorker;

        public HeapPanel() {
            setBackground(Color.WHITE);

            // Add mouse wheel zoom
            addMouseWheelListener(e -> {
                int rotation = e.getWheelRotation();
                int currentZoom = zoomSlider.getValue();
                int newZoom = Math.max(25, Math.min(200, currentZoom - rotation * 10));
                zoomSlider.setValue(newZoom);
            });

            // Add mouse drag for panning
            MouseAdapter mouseAdapter = new MouseAdapter() {
                private Point lastPoint;
                private boolean dragging = false;

                @Override
                public void mousePressed(MouseEvent e) {
                    lastPoint = e.getPoint();
                    dragging = true;
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    dragging = false;
                    setCursor(Cursor.getDefaultCursor());
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (dragging && lastPoint != null) {
                        JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, HeapPanel.this);
                        if (scrollPane != null) {
                            JScrollBar hBar = scrollPane.getHorizontalScrollBar();
                            JScrollBar vBar = scrollPane.getVerticalScrollBar();

                            Point currentPoint = e.getPoint();
                            int deltaX = lastPoint.x - currentPoint.x;
                            int deltaY = lastPoint.y - currentPoint.y;

                            hBar.setValue(hBar.getValue() + deltaX);
                            vBar.setValue(vBar.getValue() + deltaY);
                        }
                    }
                }
            };

            addMouseListener(mouseAdapter);
            addMouseMotionListener(mouseAdapter);
        }

        public void setZoomFactor(double factor) {
            this.zoomFactor = factor;
            updateLayout();
        }

        public void setLayoutStyle(String style) {
            this.layoutStyle = style;
        }

        public void updateLayout() {
            // Cancel any pending layout calculation
            if (currentLayoutWorker != null && !currentLayoutWorker.isDone()) {
                currentLayoutWorker.cancel(true); // true means interrupt if running
            }

            currentLayoutWorker = new SwingWorker<>() {
                private Map<FibonacciHeap.HeapNode, Point> newPositions;

                @Override
                protected Map<FibonacciHeap.HeapNode, Point> doInBackground() throws Exception {
                    // This runs on a background thread
                    return calculateLayoutInternal();
                }

                @Override
                protected void done() {
                    // This runs on the EDT
                    try {
                        newPositions = get(); // Get the result from doInBackground
                        nodePositions = newPositions; // Update the main map
                        updatePreferredSize(); // Update preferred size based on new layout
                        revalidate(); // Revalidate the component's layout
                        repaint(); // Request a repaint
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // Restore interrupt status
                        System.err.println("Layout calculation interrupted.");
                    } catch (CancellationException e) {
                        System.out.println("Layout calculation cancelled.");
                        // This is expected if a new updateLayout is called before this one finishes
                    } catch (ExecutionException e) {
                        System.err.println("Error during layout calculation: " + e.getCause());
                        e.getCause().printStackTrace();
                        JOptionPane.showMessageDialog(FibonacciHeapVisualizer.this,
                                "An error occurred during layout: " + e.getCause().getMessage(),
                                "Layout Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            currentLayoutWorker.execute(); // Start the background task
        }

        public void adjustLayoutToWindow() {
            if (heap.min == null) return;

            JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, this);
            if (scrollPane == null) return;

            Dimension viewportSize = scrollPane.getViewport().getExtentSize();
            if (viewportSize.width <= 0 || viewportSize.height <= 0) return;

            int estimatedRootCount = heap.numTrees();
            int estimatedOverallWidth = (int) (Math.max(estimatedRootCount, 1) * (baseNodeRadius * 2 + minHorizontalSpacing * 1.5) * zoomFactor);
            int estimatedMaxDepth = getMaxDepth(getRootNodes());
            int estimatedTotalHeight = (int) (Math.max(estimatedMaxDepth + 1, 2) * (baseNodeRadius * 2 + minVerticalSpacing) * zoomFactor);


            double scaleX = (double) viewportSize.width / (estimatedOverallWidth > 0 ? estimatedOverallWidth : 1);
            double scaleY = (double) viewportSize.height / (estimatedTotalHeight > 0 ? estimatedTotalHeight : 1);

            double optimalScale = Math.min(scaleX, scaleY) * 0.9; // 90% to leave margin
            int newZoom = (int) Math.max(25, Math.min(200, optimalScale * 100));
            zoomSlider.setValue(newZoom);

            // Re-calculate layout with new zoom, then center
            // This will call updateLayout, which will trigger a SwingWorker
            SwingUtilities.invokeLater(this::centerView);
        }


        public void centerView() {
            JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, this);
            if (scrollPane != null && !nodePositions.isEmpty()) {
                // Find bounds of all nodes
                int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
                for (Point p : nodePositions.values()) {
                    minX = Math.min(minX, p.x);
                    maxX = Math.max(maxX, p.x);
                    minY = Math.min(minY, p.y);
                    maxY = Math.max(maxY, p.y);
                }

                // Add node radius to bounds to get full extent
                int contentWidth = maxX - minX + (int)(baseNodeRadius * 2 * zoomFactor);
                int contentHeight = maxY - minY + (int)(baseNodeRadius * 2 * zoomFactor);

                Rectangle viewRect = scrollPane.getViewport().getViewRect();

                // Calculate position to center the content within the viewport
                // We also subtract the initial padding (offset) we added when calculating the layout
                // A more robust way would be to get the actual minX, minY from layout for precise centering.
                // For now, we'll aim to center the calculated content bounds.
                int newX = minX - (viewRect.width - contentWidth) / 2;
                int newY = minY - (viewRect.height - contentHeight) / 2;

                // Adjust to keep within scrollable bounds (0 to max scroll value)
                newX = Math.max(0, Math.min(newX, getPreferredSize().width - viewRect.width));
                newY = Math.max(0, Math.min(newY, getPreferredSize().height - viewRect.height));

                scrollPane.getViewport().setViewPosition(new Point(newX, newY));
            }
        }


        public void fitToWindow() {
            if (heap.min == null) return;

            JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, this);
            if (scrollPane == null) return;

            Rectangle viewRect = scrollPane.getViewport().getViewRect();

            // Find effective bounds of the drawn content
            int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
            if (!nodePositions.isEmpty()) {
                for (Point p : nodePositions.values()) {
                    minX = Math.min(minX, p.x);
                    maxX = Math.max(maxX, p.x);
                    minY = Math.min(minY, p.y);
                    maxY = Math.max(maxY, p.y);
                }
            } else {
                minX = 0; maxX = 1; minY = 0; maxY = 1; // Default small bounds if no nodes
            }


            int contentWidth = maxX - minX + (int)(baseNodeRadius * 2 * zoomFactor);
            int contentHeight = maxY - minY + (int)(baseNodeRadius * 2 * zoomFactor);


            if (contentWidth > 0 && contentHeight > 0 && viewRect.width > 0 && viewRect.height > 0) {
                double scaleX = (double) viewRect.width / contentWidth;
                double scaleY = (double) viewRect.height / contentHeight;
                double scale = Math.min(scaleX, scaleY) * 0.9; // 90% to leave some margin

                int newZoom = (int) Math.max(25, Math.min(200, scale * 100));
                zoomSlider.setValue(newZoom);

                // After setting new zoom, the layout recalculates, then center it.
                // This will call updateLayout, which will trigger a SwingWorker
                SwingUtilities.invokeLater(this::centerView);
            }
        }

        private void updatePreferredSize() {
            if (nodePositions.isEmpty()) {
                setPreferredSize(new Dimension(800, 600)); // Default size for empty heap
                return;
            }

            int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
            for (Point p : nodePositions.values()) {
                minX = Math.min(minX, p.x);
                maxX = Math.max(maxX, p.x);
                minY = Math.min(minY, p.y);
                maxY = Math.max(maxY, p.y);
            }

            // Add padding around the content
            int padding = (int)(baseNodeRadius * 3 * zoomFactor);
            int preferredWidth = (maxX - minX) + 2 * padding;
            int preferredHeight = (maxY - minY) + 2 * padding;

            // Ensure minimum preferred size is at least the parent viewport size for proper scrolling
            JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, this);
            if (scrollPane != null) {
                preferredWidth = Math.max(preferredWidth, scrollPane.getViewport().getWidth());
                preferredHeight = Math.max(preferredHeight, scrollPane.getViewport().getHeight());
            } else {
                preferredWidth = Math.max(preferredWidth, 800); // Fallback if no scrollpane
                preferredHeight = Math.max(preferredHeight, 600); // Fallback if no scrollpane
            }

            contentSize = new Dimension(preferredWidth, preferredHeight);
            setPreferredSize(contentSize);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (heap.min == null) {
                g2d.setFont(new Font("Arial", Font.BOLD, (int)(20 * zoomFactor)));
                g2d.setColor(Color.GRAY);
                FontMetrics fm = g2d.getFontMetrics();
                String text = "Heap is empty - Insert some nodes to begin";
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = getHeight() / 2;
                g2d.drawString(text, x, y);
                return;
            }

            // DO NOT recalculate layout in paintComponent.
            // Layout should be calculated only when heap data changes or zoom/layout style changes.
            // If nodePositions is empty, it means updateLayout was called but hasn't finished yet.
            // In that case, we simply don't draw anything until it's ready.
            if (nodePositions.isEmpty()) {
                // Optionally, draw a "Calculating layout..." message
                g2d.setFont(new Font("Arial", Font.BOLD, (int)(16 * zoomFactor)));
                g2d.setColor(Color.LIGHT_GRAY);
                FontMetrics fm = g2d.getFontMetrics();
                String text = "Calculating layout...";
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = getHeight() / 2;
                g2d.drawString(text, x, y);
                return;
            }

            // Draw edges first
            drawEdges(g2d);

            // Draw nodes
            drawNodes(g2d);
        }

        private List<FibonacciHeap.HeapNode> getRootNodes() {
            List<FibonacciHeap.HeapNode> roots = new ArrayList<>();
            if (heap.min == null) return roots;

            FibonacciHeap.HeapNode current = heap.min;
            do {
                roots.add(current);
                current = current.next;
            } while (current != heap.min);
            return roots;
        }

        // This method now performs the layout calculation off the EDT
        private Map<FibonacciHeap.HeapNode, Point> calculateLayoutInternal() {
            Map<FibonacciHeap.HeapNode, Point> newPositions = new HashMap<>();

            if (heap.min == null) {
                return newPositions;
            }

            List<FibonacciHeap.HeapNode> roots = getRootNodes();

            int nodeRadius = (int) (baseNodeRadius * zoomFactor);
            int effectiveHorizontalSpacing = (int) (minHorizontalSpacing * zoomFactor);
            int effectiveVerticalSpacing = (int) (minVerticalSpacing * zoomFactor);

            // Initial offset to ensure content is not at the very left/top
            int initialXOffset = (int)(50 * zoomFactor);
            int initialYOffset = (int)(50 * zoomFactor);

            // Layout based on selected style
            switch (layoutStyle) {
                case "Circular":
                    layoutCircularInternal(roots, nodeRadius, effectiveHorizontalSpacing, effectiveVerticalSpacing, initialXOffset, initialYOffset, newPositions);
                    break;
                case "Spread":
                    layoutCompactInternal(roots, effectiveHorizontalSpacing * 2, effectiveVerticalSpacing, initialXOffset, initialYOffset, newPositions);
                    break;
                default: // Compact
                    layoutCompactInternal(roots, effectiveHorizontalSpacing, effectiveVerticalSpacing, initialXOffset, initialYOffset, newPositions);
                    break;
            }
            return newPositions;
        }

        // --- Layout Helper methods, now taking a map to populate ---

        private int getMaxDepth(List<FibonacciHeap.HeapNode> roots) {
            int maxDepth = 0;
            for (FibonacciHeap.HeapNode root : roots) {
                maxDepth = Math.max(maxDepth, getSubtreeMaxDepth(root, 0));
            }
            return maxDepth;
        }

        private int getSubtreeMaxDepth(FibonacciHeap.HeapNode node, int currentDepth) {
            if (node == null) return currentDepth - 1;
            int maxChildDepth = currentDepth;
            if (node.child != null) {
                FibonacciHeap.HeapNode child = node.child;
                do {
                    maxChildDepth = Math.max(maxChildDepth, getSubtreeMaxDepth(child, currentDepth + 1));
                    child = child.next;
                } while (child != node.child);
            }
            return maxChildDepth;
        }

        private int calculateSubtreeDisplayWidth(FibonacciHeap.HeapNode node, int horizontalSpacing) {
            int nodeRadius = (int)(baseNodeRadius * zoomFactor);
            int nodeDiameter = nodeRadius * 2;

            if (node.child == null) {
                return nodeDiameter;
            }

            List<FibonacciHeap.HeapNode> children = new ArrayList<>();
            FibonacciHeap.HeapNode child = node.child;
            do {
                children.add(child);
                child = child.next;
            } while (child != node.child);

            int childrenTotalWidth = 0;
            for (FibonacciHeap.HeapNode c : children) {
                childrenTotalWidth += calculateSubtreeDisplayWidth(c, horizontalSpacing);
            }
            childrenTotalWidth += (children.size() - 1) * horizontalSpacing;

            return Math.max(nodeDiameter, childrenTotalWidth);
        }


        private void layoutCompactInternal(List<FibonacciHeap.HeapNode> roots, int horizontalSpacing, int verticalSpacing, int initialXOffset, int initialYOffset, Map<FibonacciHeap.HeapNode, Point> positionsMap) {
            int currentX = initialXOffset;
            int rootY = initialYOffset;

            for (FibonacciHeap.HeapNode root : roots) {
                int subtreeWidth = calculateSubtreeDisplayWidth(root, horizontalSpacing);
                int rootNodeX = currentX + subtreeWidth / 2;

                positionsMap.put(root, new Point(rootNodeX, rootY));
                layoutChildrenInternal(root, rootNodeX, rootY + verticalSpacing, horizontalSpacing, verticalSpacing, positionsMap);

                currentX += subtreeWidth + horizontalSpacing;
            }
        }

        private void layoutChildrenInternal(FibonacciHeap.HeapNode parent, int parentX, int startY, int horizontalSpacing, int verticalSpacing, Map<FibonacciHeap.HeapNode, Point> positionsMap) {
            if (parent.child == null) return;

            List<FibonacciHeap.HeapNode> children = new ArrayList<>();
            FibonacciHeap.HeapNode child = parent.child;
            do {
                children.add(child);
                child = child.next;
            } while (child != parent.child);

            int totalChildrenWidth = 0;
            List<Integer> childWidths = new ArrayList<>();
            for (FibonacciHeap.HeapNode c : children) {
                int width = calculateSubtreeDisplayWidth(c, horizontalSpacing);
                childWidths.add(width);
                totalChildrenWidth += width;
            }
            totalChildrenWidth += (children.size() - 1) * horizontalSpacing;

            int currentChildXOffset = parentX - totalChildrenWidth / 2;

            for (int i = 0; i < children.size(); i++) {
                FibonacciHeap.HeapNode currentChild = children.get(i);
                int childWidth = childWidths.get(i);

                int childNodeX = currentChildXOffset + childWidth / 2;
                positionsMap.put(currentChild, new Point(childNodeX, startY));

                layoutChildrenInternal(currentChild, childNodeX, startY + verticalSpacing, horizontalSpacing, verticalSpacing, positionsMap);

                currentChildXOffset += childWidth + horizontalSpacing;
            }
        }

        private void layoutCircularInternal(List<FibonacciHeap.HeapNode> roots, int nodeRadius, int horizontalSpacing, int verticalSpacing, int initialXOffset, int initialYOffset, Map<FibonacciHeap.HeapNode, Point> positionsMap) {
            if (roots.size() == 1) {
                positionsMap.put(roots.get(0), new Point(initialXOffset + 300, initialYOffset + 200));
                layoutChildrenInternal(roots.get(0), initialXOffset + 300, initialYOffset + 200 + verticalSpacing, horizontalSpacing, verticalSpacing, positionsMap);
                return;
            }

            int centerX = initialXOffset + (getWidth() - 2 * initialXOffset) / 2;
            int centerY = initialYOffset + (getHeight() - 2 * initialYOffset) / 3;

            int calculatedRadius = (int) (Math.max(150, roots.size() * nodeRadius * 1.5) * zoomFactor);
            calculatedRadius = Math.min(calculatedRadius, (int) (Math.min(getWidth(), getHeight()) / 3 * zoomFactor));


            for (int i = 0; i < roots.size(); i++) {
                double angle = 2 * Math.PI * i / roots.size();
                int x = centerX + (int) (calculatedRadius * Math.cos(angle));
                int y = centerY + (int) (calculatedRadius * Math.sin(angle));
                positionsMap.put(roots.get(i), new Point(x, y));

                layoutChildrenInternal(roots.get(i), x, y + verticalSpacing, horizontalSpacing, verticalSpacing, positionsMap);
            }
        }


        private void drawEdges(Graphics2D g2d) {
            int nodeRadius = (int) (baseNodeRadius * zoomFactor);
            g2d.setStroke(new BasicStroke(Math.max(1, (int) (2 * zoomFactor))));

            for (Map.Entry<FibonacciHeap.HeapNode, Point> entry : nodePositions.entrySet()) {
                FibonacciHeap.HeapNode node = entry.getKey();
                Point nodePos = entry.getValue();

                // Draw edges to children (solid)
                if (node.child != null) {
                    g2d.setColor(EDGE_COLOR);
                    FibonacciHeap.HeapNode child = node.child;
                    do {
                        Point childPos = nodePositions.get(child);
                        if (childPos != null) {
                            // Draw line from bottom center of parent to top center of child
                            g2d.drawLine(nodePos.x, nodePos.y + nodeRadius,
                                    childPos.x, childPos.y - nodeRadius);
                        }
                        child = child.next;
                    } while (child != node.child);
                }
            }

            // Draw horizontal connections between siblings (dashed)
            g2d.setColor(HORIZONTAL_EDGE_COLOR);
            float dashLength = Math.max(3, (int) (5 * zoomFactor));
            g2d.setStroke(new BasicStroke(Math.max(1, (int) zoomFactor), BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                    0, new float[]{dashLength}, 0));

            // Iterate through roots for horizontal connections
            if (heap.min != null) {
                FibonacciHeap.HeapNode currentRoot = heap.min;
                do {
                    // Draw sibling connections for children if they exist
                    if (currentRoot.child != null) {
                        FibonacciHeap.HeapNode currentChild = currentRoot.child;
                        do {
                            // Only draw if currentChild and nextChild are in nodePositions and are not the same node
                            if (currentChild.next != currentRoot.child && nodePositions.containsKey(currentChild) && nodePositions.containsKey(currentChild.next)) {
                                Point currentChildPos = nodePositions.get(currentChild);
                                Point nextChildPos = nodePositions.get(currentChild.next);
                                g2d.drawLine(currentChildPos.x + nodeRadius, currentChildPos.y,
                                        nextChildPos.x - nodeRadius, nextChildPos.y);
                            }
                            currentChild = currentChild.next;
                        } while (currentChild != currentRoot.child);
                    }

                    // Draw horizontal connections for root nodes themselves
                    // Only draw if currentRoot and nextRoot are in nodePositions and are not the same node
                    if (currentRoot.next != heap.min && nodePositions.containsKey(currentRoot) && nodePositions.containsKey(currentRoot.next)) {
                        Point currentRootPos = nodePositions.get(currentRoot);
                        Point nextRootPos = nodePositions.get(currentRoot.next);
                        g2d.drawLine(currentRootPos.x + nodeRadius, currentRootPos.y,
                                nextRootPos.x - nodeRadius, nextRootPos.y);
                    }
                    currentRoot = currentRoot.next;
                } while (currentRoot != heap.min);
            }
        }

        private void drawNodes(Graphics2D g2d) {
            int nodeRadius = (int) (baseNodeRadius * zoomFactor);
            g2d.setStroke(new BasicStroke(Math.max(1, (int) (2 * zoomFactor))));

            for (Map.Entry<FibonacciHeap.HeapNode, Point> entry : nodePositions.entrySet()) {
                FibonacciHeap.HeapNode node = entry.getKey();
                Point nodePos = entry.getValue();

                // Determine node color
                Color nodeColor = NODE_COLOR;
                if (node == heap.min) {
                    nodeColor = MIN_NODE_COLOR;
                } else if (node.markCnt > 0) { // Mark colored if mark count is greater than 0
                    nodeColor = MARKED_NODE_COLOR;
                }

                // Draw node circle
                g2d.setColor(nodeColor);
                g2d.fillOval(nodePos.x - nodeRadius, nodePos.y - nodeRadius, nodeRadius * 2, nodeRadius * 2);

                // Draw node border
                g2d.setColor(Color.BLACK);
                g2d.drawOval(nodePos.x - nodeRadius, nodePos.y - nodeRadius, nodeRadius * 2, nodeRadius * 2);

                // Draw text (key and info)
                g2d.setColor(Color.WHITE);
                // Increased font size for the key
                g2d.setFont(new Font("Arial", Font.BOLD, (int) (18 * zoomFactor))); // Increased font size
                FontMetrics fm = g2d.getFontMetrics();

                String keyText = String.valueOf(node.key);
                int keyWidth = fm.stringWidth(keyText);
                // Adjusted vertical position for the key to be more centered
                int keyY = nodePos.y - fm.getHeight() / 2 + fm.getAscent();
                g2d.drawString(keyText, nodePos.x - keyWidth / 2, keyY);

                // Draw info below key
                if (node.info != null && !node.info.isEmpty()) {
                    g2d.setFont(new Font("Arial", Font.PLAIN, (int) (11 * zoomFactor))); // Slightly increased info font
                    fm = g2d.getFontMetrics();
                    int infoWidth = fm.stringWidth(node.info);
                    g2d.drawString(node.info, nodePos.x - infoWidth / 2, keyY + fm.getHeight() + 2); // Position below key
                }
            }
        }
    }
}