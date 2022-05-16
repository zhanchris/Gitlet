package gitlet;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/** The class representing the local file version control system of Gitlet.
 * @author Chris Zhan **/

public class Gitlet implements Serializable {

    /** Gitlet Folder that stores all commits and history. */
    static final File GITLET_DIRECTORY = new File(Main.CWD, ".gitlet");

    /** Folder that stores all serialized FILES. */
    static final File FILES_DIRECTORY = new File(GITLET_DIRECTORY, "files");

    /** File that stores the Gitlet repository object. */
    static final File REPO = new File(GITLET_DIRECTORY, "repo");

    /** Creates a new Gitlet repository in the current working directory.
     * Does this by creating a .gitlet directory and creating a new CommitTree.
     * Errors if .gitlet already exists. */
    Gitlet() {
        GITLET_DIRECTORY.mkdir();
        FILES_DIRECTORY.mkdir();
        _commitTree = new CommitTree();
    }

    /** Adds this file to the stage.
     * @param file the file to be added.
     * @param name the name of the file. */
    void add(File file, String name) {
        _commitTree.addFile(file, name);
    }

    /** Commits all files from the stage to the CommitTree.
     * @param logMessage the message associated with the tree. */
    void commit(String logMessage) {
        _commitTree.commit(logMessage);
    }

    /** Prints out a message displaying information about each commit,
     * going backwards along the CommitTree until the initial commit.
     */
    void log() {
        _commitTree.log();
    }

    void globalLog() {
        _commitTree.globalLog();
    }

    /** Does three things depending on its arguments for checkouts.
     * @param args the arguments to checkout.
     */
    void checkout(String... args) {
        if (args.length == 2) {
            _commitTree.checkoutBranch(args[1]);
        } else if (args[1].equals("--")) {
            _commitTree.checkoutCommitNodeFile(
                    _commitTree.head().node(), args[2]);
        } else if (args[2].equals("--")) {
            File commitFile = null;
            if (args[1].length() == 8) {
                List<String> commitFileNames = Utils.plainFilenamesIn(
                        CommitTree.COMMIT_DIRECTORY);
                boolean foundFile = false;
                for (String commitFileName : commitFileNames) {
                    if (args[1].equals(commitFileName.substring(0, 8))) {
                        commitFile = Utils.join(
                                CommitTree.COMMIT_DIRECTORY, commitFileName);
                        foundFile = true;
                    }
                } if (!foundFile) {
                    Main.exitWithError("No commit with that id exists.");
                }
            } else {
                commitFile = Utils.join(CommitTree.COMMIT_DIRECTORY, args[1]);
            }
            if (!commitFile.exists()) {
                Main.exitWithError("No commit with that id exists.");
            }
            CommitTree.CommitNode commitNode = Utils.readObject(commitFile,
                        CommitTree.CommitNode.class);
            _commitTree.checkoutCommitNodeFile(commitNode, args[3]);
        } else {
            Main.exitWithError("Incorrect operands.");
        }
    }

    void find(String commitMessage) {
        _commitTree.find(commitMessage);
    }

    void rm(String fileName) {
        _commitTree.rm(fileName);
    }

    void branch(String branchName) {
        _commitTree.createBranch(branchName);
    }

    void rmBranch(String branchName) {
        _commitTree.removeBranch(branchName);
    }

    void status() {
        _commitTree.status();
    }

    void reset(String commitID) {
        _commitTree.reset(commitID);
    }

    void merge(String branchName) {
        _commitTree.merge(branchName);
    }
    /** Reads in and deserializes a Gitlet object from a file named repo in
     * .gitlet directory.
     * @return the gitlet object.
     */
    public static Gitlet readGitlet() {
        return Utils.readObject(REPO, Gitlet.class);
    }

    /**
     * Saves the Gitlet object to a file named repo for future use.
     */
    public void saveGitlet() {
        try {
            REPO.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.writeObject(REPO, this);
    }

    /** Accessor method for commit tree.
     * @return the commit tree. */
    CommitTree commitTree() {
        return _commitTree;
    }
    /** The commit tree of this gitlet. */
    private CommitTree _commitTree;
}
