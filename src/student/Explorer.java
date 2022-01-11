package student;

import game.*;

import java.util.*;

public class Explorer {

  private ExplorationState s;


  /**
   * Explore the cavern, trying to find the orb in as few steps as possible.
   * Once you find the orb, you must return from the function in order to pick
   * it up. If you continue to move after finding the orb rather
   * than returning, it will not count.
   * If you return from this function while not standing on top of the orb,
   * it will count as a failure.
   * <p>
   * There is no limit to how many steps you can take, but you will receive
   * a score bonus multiplier for finding the orb in fewer steps.
   * <p>
   * At every step, you only know your current tile's ID and the ID of all
   * open neighbor tiles, as well as the distance to the orb at each of these tiles
   * (ignoring walls and obstacles).
   * <p>
   * To get information about the current state, use functions
   * getCurrentLocation(),
   * getNeighbours(), and
   * getDistanceToTarget()
   * in ExplorationState.
   * You know you are standing on the orb when getDistanceToTarget() is 0.
   * <p>
   * Use function moveTo(long id) in ExplorationState to move to a neighboring
   * tile by its ID. Doing this will change state to reflect your new position.
   * <p>
   * A suggested first implementation that will always find the orb, but likely won't
   * receive a large bonus multiplier, is a depth-first search.
   *
   * @param state the information available at the current state
   */
  public void explore(ExplorationState state) {
    this.s = state;
    dfsExplore(new HashSet<>());

  }

  /**
   * Computes the path to the orb using a recursive Depth-First-Search algorithm.
   * Moves to the 'best' neighbouring node by calling sortedNeighbours(), and repeats until can no longer move.
   * Ignores nodes that have already been visited to avoid loops during exploration.
   * @param visited Set of nodes already visited by the explorer.
   */
  public void dfsExplore(HashSet<Long> visited) {
    int orbDist = s.getDistanceToTarget();
    if (orbDist == 0) { return; } // path to orb has been found

    long currentNode = s.getCurrentLocation();
    visited.add(currentNode); // adds current node to set of visited nodes

    Collection<NodeStatus> neighbours = sortedNeighbours(); // Retrieves neighbouring nodes sorted by distance from orb

    // Iterates through neighbouring nodes, checks if not visited, moves to new node.
    // Recursively calls dfsExplore() until can no longer move, then steps backwards to next possible move.
    for (NodeStatus node : neighbours) {
      if (!visited.contains(node.nodeID())) {
        s.moveTo(node.nodeID());
        dfsExplore(visited);
        if (s.getDistanceToTarget() == 0) { return; }
        s.moveTo(currentNode);
      }
    }
  }

  /**
   * Calculates the best neighbouring node to move to by sorting them by distance from the orb.
   * @return Collection of NodeStatus ordered by ascending distance from the orb.
   */
  public Collection<NodeStatus> sortedNeighbours() {
    Collection<NodeStatus> neighbours = s.getNeighbours();
    // Creates an array of NodeStatus neighbours
    NodeStatus[] neighbourArr = neighbours.toArray(new NodeStatus[neighbours.size()]);
    // Sorts NodeStatus using implemented Comparable
    Arrays.sort(neighbourArr, NodeStatus::compareTo);
    return Arrays.asList(neighbourArr);
  }


  /**
   * Escape from the cavern before the ceiling collapses, trying to collect as much
   * gold as possible along the way. Your solution must ALWAYS escape before time runs
   * out, and this should be prioritized above collecting gold.
   * <p>
   * You now have access to the entire underlying graph, which can be accessed through EscapeState.
   * getCurrentNode() and getExit() will return you Node objects of interest, and getVertices()
   * will return a collection of all nodes on the graph.
   * <p>
   * Note that time is measured entirely in the number of steps taken, and for each step
   * the time remaining is decremented by the weight of the edge taken. You can use
   * getTimeRemaining() to get the time still remaining, pickUpGold() to pick up any gold
   * on your current tile (this will fail if no such gold exists), and moveTo() to move
   * to a destination node adjacent to your current node.
   * <p>
   * You must return from this function while standing at the exit. Failing to do so before time
   * runs out or returning from the wrong location will be considered a failed run.
   * <p>
   * You will always have enough time to escape using the shortest path from the starting
   * position to the exit, although this will not collect much gold.
   *
   * @param state the information available at the current state
   */
  public void escape(EscapeState state) {
    Node current = state.getCurrentNode();
    Node exit = state.getExit();

    Path bestPath = bestPath(state); // the best path to gold or to the exit, depending on time

    if (current.getTile().getGold() > 0) { // pick up gold at start node
      state.pickUpGold();
    }

    while (current != exit) {
      for (Node n : bestPath.getNodes()) {
        state.moveTo(n);
        if (n.getTile().getGold() > 0) {
          state.pickUpGold();
        }
      }

      if (state.getCurrentNode() == state.getExit()) {
        return;
      }

      current = state.getCurrentNode();
      exit = state.getExit();
      bestPath = bestPath(state); // Calculates the new best path to gold or to the exit.
    }

  }

  /**
   * Returns the best path to a node containing gold, if there's enough time remaining.
   * If there's not enough time left, then return the path to the exit.
   * @param state Boris' current state
   * @return Path object of the best path to gold or to the exit.
   */
  private Path bestPath(EscapeState state) {
    Path bestPath = new Path(0, new ArrayList<>());

    for (Node n : state.getVertices()) {
      if (n.getTile().getGold() > 0) { // get all nodes containing gold
        Path toGold = shortestPath(state.getCurrentNode(), n); // get shortest path to that node
        if (toGold != null) { // checks if the path exists
          int distToGold = toGold.getSize(); // get the distance in steps to that node
          // if that distance is less than the best path
          if (bestPath.getNodes().isEmpty() || distToGold < bestPath.getSize()) {
            Path toExit = shortestPath(n, state.getExit());
            // check if dist to node then to exit is achievable in time remaining
            if (distToGold + toExit.getSize() < state.getTimeRemaining()) {
              bestPath = toGold;
            }
          }
        }
      }
    }
    if (bestPath.getNodes().isEmpty()) { // if no such path exists, return path to exit
      return shortestPath(state.getCurrentNode(), state.getExit());
    }
    else {
      return bestPath;
    }
  }


  /**
   * Calculates and builds the shortest path from point A to point B using Dijkstra algorithm.
   * @param start the source tile
   * @param end the destination tile
   * @return the shortest path as a Path object
   */
  private Path shortestPath(Node start, Node end) {
    PriorityQueue<NodeInfo> queue = new PriorityQueue<>();

    Map<Node, NodeInfo> nodeTable = new HashMap<>(); // table of each node, its predecessor and distance from start
    Set<Node> visited = new HashSet<>();

    NodeInfo startInfo = new NodeInfo(start, 0, null); // start node's distance from start is 0, predecessor is null
    nodeTable.put(start, startInfo);
    queue.add(startInfo);

    while (!queue.isEmpty()) {
      NodeInfo nodeInfo = queue.poll(); // retrieves the top element from priority queue
      Node node = nodeInfo.getNode();
      visited.add(node);

      if (node.equals(end)) {
        return buildShortestPath(nodeInfo); // build a path from the most recent visited node back to start
      }

      Set<Node> neighbours = node.getNeighbours();
      for (Node n : neighbours) {
        if (visited.contains(n)) {
          continue;
        }

        int distance = node.getEdge(n).length;
        int totalDistance = nodeInfo.getTotalDist() + distance;

        // retrieves the neighbours info from the table to see if a path has been built to it before
        NodeInfo neighbourInfo = nodeTable.get(n);
        if (neighbourInfo == null) { // if no path has been built to this node then add it to the table
          neighbourInfo = new NodeInfo(n, totalDistance, nodeInfo);
          nodeTable.put(n, neighbourInfo);
          queue.add(neighbourInfo);
        } // if this path to the node is shorter than the previous path then set this path as the better one
        else if (totalDistance < neighbourInfo.getTotalDist()) {
          neighbourInfo.setTotalDist(totalDistance);
          neighbourInfo.setPredecessor(nodeInfo);
          queue.remove(neighbourInfo); // remove/re-add the node to order it in the priority queue
          queue.add(neighbourInfo);
        }
      }
    }
    return null; // return null if no path found
  }

  /**
   * Builds a Path from a destination back to the source and flips it to put it in traversal order.
   * @param nodeInfo the destination node's information
   * @return a Path object of the shortest path to the destination
   */
  private static Path buildShortestPath(NodeInfo nodeInfo) {
    List<Node> path = new ArrayList<>(); // holds the nodes in the path
    int pathDist = nodeInfo.getTotalDist(); // the total weighted length of the path

    while (nodeInfo != null) {
      path.add(nodeInfo.getNode()); // reverses through the path and adds each node to the list
      nodeInfo = nodeInfo.getPredecessor(); // obtains each node's predecessor
    }
    path.remove(path.size()-1); // removes the source node as this is Boris' current location
    Collections.reverse(path); // reverses the elements in path to traversal order

    return new Path(pathDist, path);
  }
}
