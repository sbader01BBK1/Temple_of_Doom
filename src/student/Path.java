package student;

import game.Node;

import java.util.List;

/**
 * A Path object containing relevant information about a constructed path.
 * size - the length of the path (in steps) from start to end.
 * nodes - the list of nodes traversed in the path, in order.
 */
public class Path {
    private int size;
    private List<Node> nodes;

    public Path(int size, List<Node> nodes) {
        this.size = size;
        this.nodes = nodes;
    }

    public int getSize() {
        return size;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }
}
