package student;

import game.Node;

/**
 * Provides information on Nodes in the shortest path being built.
 * node - the name of the node.
 * totalDist - its distance from the start node in the path.
 * predecessor - its parent node in the path being taken.
 */
public class NodeInfo implements Comparable<NodeInfo> {
    private final Node node;
    private int totalDist;
    private NodeInfo predecessor;

    public NodeInfo(Node n, int w, NodeInfo pre) {
        node = n;
        totalDist = w;
        predecessor = pre;
    }

    @Override
    public int compareTo(NodeInfo o) {
        return Integer.compare(this.totalDist, o.totalDist);
    }

    public Node getNode() {
        return node;
    }

    public int getTotalDist() {
        return totalDist;
    }

    public void setTotalDist(int totalDist) {
        this.totalDist = totalDist;
    }

    public NodeInfo getPredecessor() {
        return predecessor;
    }

    public void setPredecessor(NodeInfo predecessor) {
        this.predecessor = predecessor;
    }
}
