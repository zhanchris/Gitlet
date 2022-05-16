package gitlet;

import java.io.Serializable;

public class Branch implements Serializable {
    Branch(String branchName, CommitTree.CommitNode newNode) {
        name = branchName;
        node = newNode;
    }

    void setNode(CommitTree.CommitNode newNode) {
        node = newNode;
    }

    String name() {
        return name;
    }
    CommitTree.CommitNode node() {
        return node;
    }

    /** The name of this branch. */
    private final String name;

    /** The node that this branch points to. */
    private CommitTree.CommitNode node;
}
