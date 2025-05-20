# Emergency Route Finder Documentation

## Table of Contents
1. [Overview](#overview)
2. [System Architecture](#system-architecture)
3. [Key Components](#key-components)
4. [Code Architecture](#code-architecture)
5. [Algorithm Implementation](#algorithm-implementation)
6. [User Interface Guide](#user-interface-guide)
7. [System Usage](#system-usage)
8. [Technical Details](#technical-details)

## Overview

The Emergency Route Finder is a Java application designed to simulate emergency response routing on map images. It analyzes road networks in map images, identifies optimal paths for emergency vehicles, and visualizes these routes based on different emergency priorities. The system employs multiple pathfinding algorithms to determine the best routes depending on the urgency of the emergency.

The application processes images to identify roads and obstacles, builds a graph representation of the road network, and implements different pathfinding algorithms (A*, Dijkstra's, and BFS) for routing emergency services. It also provides an interactive UI to place service centers, add emergencies of varying priorities, and visualize the optimal routes.

## System Architecture

The system follows an object-oriented architecture with these core components:

1. **Image Processing**: Converts map images into a traversable graph representation
2. **Graph Structure**: Represents the road network with nodes and weighted edges
3. **Pathfinding Algorithms**: Three different algorithms for route optimization
4. **Interactive UI**: Provides user interactions with the map and visualization

The application employs the Model-View-Controller pattern:
- **Model**: `Graph`, `Node`, `Edge`, and `Emergency` classes
- **View**: UI components in `EmergencyRouteFinderUI`
- **Controller**: `ImageProcessor` handling route calculations and graph manipulations

## Key Components

### Image Processor
The `ImageProcessor` class is responsible for:
- Converting map images into binary road/obstacle representations
- Building the graph representation of the road network
- Implementing pathfinding algorithms (A*, Dijkstra's, BFS)
- Handling blocked roads and graph updates

### Graph Structure
The road network is represented as a graph with:
- `Node` class representing intersection points on roads
- `Edge` class representing road segments connecting nodes
- `Graph` class managing the overall graph structure and operations

### Emergency Response
Emergency handling is managed through:
- Different emergency types (URGENT, STANDARD, ROUTINE)
- Algorithm selection based on emergency priority
- Animated route visualization for response vehicles

### User Interface
The UI provides:
- Interactive map visualization with clickable elements
- Controls for loading maps, placing service centers, and managing emergencies
- Animation of emergency vehicle movement along calculated routes
- Road blocking functionality for simulating obstacles

## Code Architecture

### Package Structure
The application is organized in the `application` package containing these primary classes:

- `EmergencyRouteFinder`: Application entry point
- `EmergencyRouteFinderUI`: Main UI class
- `ImageProcessor`: Image processing and pathfinding
- `Graph`: Graph data structure
- `Node`: Graph node representation
- `Edge`: Graph edge representation
- `Emergency`: Emergency event representation
- `ImageProcessorDebug`: Debug utilities (optional)

### Class Relationships
- `EmergencyRouteFinderUI` uses `ImageProcessor` to process maps
- `ImageProcessor` builds and manages a `Graph`
- `Graph` contains collections of `Node` and `Edge` objects
- `Emergency` objects are created and managed by `EmergencyRouteFinderUI`

## Algorithm Implementation

The system implements three pathfinding algorithms for different emergency situations:

### 1. A* Algorithm (for URGENT emergencies)
```java
private List<Node> findPathAStar(Node start, Node target) {
    Map<Node, Double> gScore = new HashMap<>();
    Map<Node, Double> fScore = new HashMap<>();
    Map<Node, Node> predecessors = new HashMap<>();
    PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(fScore::get));
    Set<Node> closedSet = new HashSet<>();
    
    // Initialize scores
    for (Node node : graph.getAllNodes()) {
        gScore.put(node, Double.POSITIVE_INFINITY);
        fScore.put(node, Double.POSITIVE_INFINITY);
    }
    
    gScore.put(start, 0.0);
    fScore.put(start, heuristic(start, target));
    openSet.add(start);
    
    // A* search algorithm implementation
    // ...
}
```

The A* algorithm is used for urgent emergencies as it finds optimal paths efficiently using a heuristic function to guide the search toward the goal.

### 2. Dijkstra's Algorithm (for STANDARD emergencies)
```java
private List<Node> findPathDijkstra(Node start, Node target) {
    Map<Node, Double> distance = new HashMap<>();
    Map<Node, Node> predecessors = new HashMap<>();
    PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingDouble(distance::get));
    Set<Node> settled = new HashSet<>();
    
    // Initialize distances
    for (Node node : graph.getAllNodes()) {
        distance.put(node, Double.POSITIVE_INFINITY);
    }
    distance.put(start, 0.0);
    queue.add(start);
    
    // Dijkstra's algorithm implementation
    // ...
}
```

Dijkstra's algorithm is used for standard emergencies, providing the shortest path in terms of total distance.

### 3. BFS Algorithm (for ROUTINE emergencies)
```java
private List<Node> findPathBFS(Node start, Node target) {
    Queue<Node> queue = new LinkedList<>();
    Set<Node> visited = new HashSet<>();
    Map<Node, Node> predecessors = new HashMap<>();
    
    queue.add(start);
    visited.add(start);
    
    // BFS algorithm implementation
    // ...
}
```

Breadth-First Search is used for routine emergencies where finding any valid path is sufficient.

## User Interface Guide

### Main UI Components

1. **Image Panel**: Displays the map and interactive elements
2. **Control Panel**: Contains buttons and controls
   - Load Image button
   - Process Image button
   - Visualize Graph button
   - Clear button
   - Return to Station button
3. **Mode Selection**: Radio buttons for different interaction modes
   - Add Service Center
   - Add Emergency
   - Resolve Emergency
   - Block Road
4. **Emergency Type Selection**: Dropdown for selecting emergency type/algorithm
5. **Log Area**: Displays operation logs and status messages

### UI Workflow

![UI Workflow Diagram]

The UI follows this typical workflow:
1. Load a map image
2. Process the image to build the graph
3. Place a service center
4. Add emergencies of different types
5. Resolve emergencies by dispatching vehicles
6. View animated route visualization

## System Usage

### Basic Usage Steps

1. **Start the Application**
   ```
   java -jar EmergencyRouteFinder.jar
   ```

2. **Load a Map Image**
   - Click "Load Image"
   - Select a map image file (roads should be light-colored, obstacles dark)

3. **Process the Image**
   - Click "Process Image" to analyze the map
   - Optionally click "Visualize Graph" to see the road network graph

4. **Place a Service Center**
   - Ensure "Add Service Center" mode is selected
   - Click on a road location to place the service center (blue circle)

5. **Add Emergencies**
   - Select "Add Emergency" mode
   - Choose emergency type from dropdown (URGENT, STANDARD, ROUTINE)
   - Click on a road location to add an emergency
   - Enter a description when prompted

6. **Resolve Emergencies**
   - Select "Resolve Emergency" mode
   - Click near an emergency to select and resolve it
   - Watch the animated route from service center to emergency

7. **Additional Features**
   - "Block Road": Click to simulate road closures
   - "Return to Station": Return vehicle to service center
   - "Clear": Reset the application

### Example Scenario

1. Load a city map image
2. Process the image
3. Place a hospital as a service center
4. Add an URGENT emergency (heart attack) using A* routing
5. Add a STANDARD emergency (injured person) using Dijkstra's algorithm
6. Add a ROUTINE emergency (wellness check) using BFS
7. Resolve emergencies in priority order
8. Block roads to simulate traffic congestion
9. Test alternate routes after road blocks

## Technical Details

### Image Processing

The image processing works by:
1. Converting the image to a binary representation (roads vs. obstacles)
2. Detecting road pixels (light-colored) using a brightness threshold
3. Creating graph nodes for road pixels
4. Connecting adjacent road pixels with weighted edges
5. Handling blocked roads by removing them from the graph

```java
private void processImage() {
    // Create a pixel map where:
    // - 0 represents obstacles (buildings, etc.)
    // - 1 represents passable roads
    for (int x = 0; x < width; x++) {
        for (int y = 0; y < height; y++) {
            Color pixelColor = new Color(originalImage.getRGB(x, y));
            int brightness = (pixelColor.getRed() + pixelColor.getGreen() + pixelColor.getBlue()) / 3;
            
            if (brightness > ROAD_COLOR_THRESHOLD) {
                pixelMap[x][y] = 1; // Road
            } else {
                pixelMap[x][y] = 0; // Obstacle
            }
        }
    }
    
    // Create a graph from the pixel map
    createGraph();
}
```

### Graph Creation

The graph is created by:
1. Adding nodes for each road pixel
2. Connecting adjacent nodes with edges
3. Assigning weights based on distance (1.0 for orthogonal, √2 for diagonal)

```java
private void createGraph() {
    graph = new Graph();
    
    // Add nodes for each road pixel
    for (int x = 0; x < width; x++) {
        for (int y = 0; y < height; y++) {
            if (pixelMap[x][y] == 1) {
                Node node = new Node(x, y);
                graph.addNode(node);
            }
        }
    }
    
    // Connect adjacent road pixels with edges
    for (int x = 0; x < width; x++) {
        for (int y = 0; y < height; y++) {
            if (pixelMap[x][y] == 1) {
                Node current = graph.getNode(x, y);
                
                // Check 8 adjacent pixels
                int[] dx = {-1, 0, 1, -1, 1, -1, 0, 1};
                int[] dy = {-1, -1, -1, 0, 0, 1, 1, 1};
                
                for (int i = 0; i < 8; i++) {
                    int nx = x + dx[i];
                    int ny = y + dy[i];
                    
                    // Ensure we're within bounds
                    if (nx >= 0 && nx < width && ny >= 0 && ny < height && pixelMap[nx][ny] == 1) {
                        Node neighbor = graph.getNode(nx, ny);
                        
                        // Calculate distance (1 for orthogonal, sqrt(2) for diagonal)
                        double weight = (dx[i] == 0 || dy[i] == 0) ? 1.0 : Math.sqrt(2);
                        graph.addEdge(current, neighbor, weight);
                    }
                }
            }
        }
    }
}
```

### Path Animation

The path animation is implemented using a Swing Timer:

```java
animationTimer = new javax.swing.Timer(20, new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        if (pathIndex < currentPath.size()) {
            currentAmbulancePosition = currentPath.get(pathIndex);
            pathIndex++;
            repaintImagePanel();
        } else {
            animationTimer.stop();
            currentAmbulancePosition = null;
            selectedEmergency.setResolved(true);
            log("Emergency resolved!");
        }
    }
});
animationTimer.start();
```

### Road Blocking

The system allows simulating road blockages:

```java
public void blockPixel(int x, int y) {
    if (x >= 0 && x < isBlocked.length && y >= 0 && y < isBlocked[0].length) {
        isBlocked[x][y] = true;
        pixelMap[x][y] = 0; // treat it as an obstacle now
        createGraph(); // regenerate graph without this node
    }
}
```

---

This documentation provides a comprehensive overview of the Emergency Route Finder system, its architecture, implementation details, and usage instructions. For further development or modifications, refer to the code comments and class documentation.
