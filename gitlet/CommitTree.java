package gitlet;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Collections;

/** The CommitTree is a tree-like data structure that stores Gitlet Commits.
 * @author Chris Zhan
 */
public class CommitTree implements Serializable {
    /** The head pointer for head. */
    private Branch head;
    /** The branches of this CommitTree. */
    private TreeMap<String, Branch> branches = new TreeMap<>();
    /** The current branch of this CommitTree. */
    private Branch currentBranch;
    /** The stage of this CommitTree. */
    private Staging stage;
    /** Whether the next commit is the InitialCommit. */
    private boolean isInitialCommit = true;
    /** Inital commit year. */
    private final int initialCommitYear = 1970;


    /** Gitlet Folder that stores all commits and history. */
    static final File COMMIT_DIRECTORY = new File(Gitlet.GITLET_DIRECTORY,
            "commits");

    /** Initializes a CommitTree with ONE inital commit. */
    CommitTree() {
        createStage();
        CommitNode initialCommit = new CommitNode(
                ZonedDateTime.of(initialCommitYear, 1, 1, 0,
                        0, 0, 0,
                        ZoneId.of("UTC")),
                "initial commit",
                null,
                null);
        currentBranch = new Branch("master", initialCommit);
        branches.put("master", currentBranch);
        head = currentBranch;
        COMMIT_DIRECTORY.mkdir();
        createCommitFile(initialCommit);
    }

    /** Creates the stage. */
    private void createStage() {
        stage = new Staging();
    }

    /** Accessor method for head.
     * @return The head of the CommitTree. */
    Branch head() {
        return head;
    }

    /** Creates a file in the COMMIT_DIRECTORY that represents a COMMITNODE.
     * @param commit the CommitNode we are representing as a file inside
     *               COMMIT_DIRECTORY. */
    private void createCommitFile(CommitNode commit) {
        File commitFile = Utils.join(COMMIT_DIRECTORY, commit.sha1);
        try {
            commitFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.writeObject(commitFile, commit);
    }

    /** Adds the file to the Staging area in preparation to be committed.
     * @param file a file to be added.
     * @param name the name of the file.*/
    void addFile(File file, String name) {
        Blob fileBlob = new Blob(file);
        if (head.node().files.containsKey(name)) {
            String currentCommitSHA1 = Utils.sha1(head.node().files.get(name)
                    .stringContents());
            String toBeStagedFileSHA1 = Utils.sha1(fileBlob.stringContents());
            if (currentCommitSHA1.equals(toBeStagedFileSHA1)) {
                stage.stagedFiles().remove(name);
                stage.stagedForRemoval().remove(name);
            } else {
                stage.addFile(fileBlob, name);
            }
        } else {
            stage.addFile(fileBlob, name);
        }
    }

    /** Adds a CommitNode to the CommitTree.
     * @param message the log message.
     */
    void commit(String message) {
        ZonedDateTime now = ZonedDateTime
                .now(ZoneId.of(ZoneId.SHORT_IDS.get("PST")));
        CommitNode newCommit = new CommitNode(now, message, head.node(), null);
        createCommitFile(newCommit);
        currentBranch.setNode(newCommit);
        stage.clearStage();
        stage.clearRemovalStage();
    }

    /** Prints out a message displaying information about each commit,
     * going backwards along the CommitTree until the initial commit.
     */
    void log() {
        CommitNode pointer = head.node();
        while (pointer != null) {
            System.out.println("===");
            System.out.println("commit " + pointer.sha1);
            System.out.println("Date: " + pointer.strTimestamp);
            System.out.println(pointer.logMessage);
            System.out.println();
            pointer = pointer.commitParent;
        }
    }

    /** Prints out the global log. Every single
     * CommitNode has printed details. */
    void globalLog() {
        List<String> commitFiles = Utils.plainFilenamesIn(COMMIT_DIRECTORY);
        for (String commitFileName : commitFiles) {
            File commitFile = Utils.join(COMMIT_DIRECTORY, commitFileName);
            CommitNode oneCommit = Utils
                    .readObject(commitFile, CommitNode.class);
            System.out.println("===");
            System.out.println("commit " + oneCommit.sha1);
            System.out.println("Date: " + oneCommit.strTimestamp);
            System.out.println(oneCommit.logMessage);
            System.out.println();
        }
    }

    /** Finds a specific Commit passing in a log message.
     * @param commitMessage the log message used to find the commit. */
    void find(String commitMessage) {
        List<String> commitFiles = Utils.plainFilenamesIn(COMMIT_DIRECTORY);
        boolean foundCommit = false;
        for (String commitFileName : commitFiles) {
            File commitFile = Utils.join(COMMIT_DIRECTORY, commitFileName);
            CommitNode oneCommit = Utils
                    .readObject(commitFile, CommitNode.class);
            if (commitMessage.equals(oneCommit.logMessage)) {
                System.out.println(oneCommit.sha1);
                foundCommit = true;
            }
        }
        if (!foundCommit) {
            Main.exitWithError("Found no commit with that message.");
        }
    }

    /** Unstages the file if it is currently staged for addition.
     * ALSO, if the file is tracked in the current commit, stage it for
     * removal and remove the file from
     * the CWD.
     * @param fileName is the name of the file*/
    void rm(String fileName) {
        if (stage.stagedFiles().containsKey(fileName)) {
            stage.removeFile(fileName);
        } else if (head.node().files.containsKey(fileName)) {
            stage.stagedForRemoval().add(fileName);
            File removeInCWD = Utils.join(Main.CWD, fileName);
            Utils.restrictedDelete(removeInCWD);
        } else {
            Main.exitWithError("No reason to remove the file.");
        }
    }

    /** Prints out a message describing the status of the Gitlet repository. */
    void status() {
        statusBranches();
        statusStagedFiles();
        statusRemovedFiles();
        statusUnstagedMods();
        statusUntracked();
    }

    /** Helper method for status that prints out the branches of CommitTree. */
    void statusBranches() {
        System.out.println("=== Branches ===");
        System.out.println("*" + currentBranch.name());
        for (String branchName : branches.keySet()) {
            if (!currentBranch.name().equals(branchName)) {
                System.out.println(branchName);
            }
        }
        System.out.println();
    }

    /** Prints out the staged files when gitlet status is called. */
    void statusStagedFiles() {
        System.out.println("=== Staged Files ===");
        for (String fileName : stage.stagedFiles().keySet()) {
            System.out.println(fileName);
        }
        System.out.println();
    }

    /** Prints out removed files when gitlet status is called. */
    void statusRemovedFiles() {
        System.out.println("=== Removed Files ===");
        for (String fileName : stage.stagedForRemoval()) {
            System.out.println(fileName);
        }
        System.out.println();
    }

    /** Helper method for status(), prints out unstaged modifications. */
    void statusUnstagedMods() {
        System.out.println("=== Modifications Not Staged For Commit ===");
        ArrayList<String> printQueue = new ArrayList<>();
        List<String> filesCWD = Utils.plainFilenamesIn(Main.CWD);
        for (String file : filesCWD) {
            Blob currFile = new Blob(Utils.join(Main.CWD, file));
            if (currentBranch.node().files.containsKey(file)) {
                String commitSHA1 = Utils.sha1(currentBranch
                        .node().files.get(file).stringContents());
                if (!commitSHA1.equals(Utils.sha1(currFile.stringContents()))
                        & !stage.stagedFiles().containsKey(file)) {
                    printQueue.add(file + "(modified)");
                }
            } else if (stage.stagedFiles().containsKey(file)) {
                String stageSHA1 = Utils.sha1(stage.stagedFiles()
                        .get(file).stringContents());
                if (!stageSHA1.equals(Utils.sha1(currFile.stringContents()))) {
                    printQueue.add(file + "(modified)");
                }
            }
        }
        for (String file : stage.stagedFiles().keySet()) {
            if (!filesCWD.contains(file)) {
                printQueue.add(file + "(deleted)");
            }
        }
        for (String file : currentBranch.node().files.keySet()) {
            if (!stage.stagedForRemoval()
                    .contains(file) & !filesCWD.contains(file)) {
                printQueue.add(file + "(deleted)");
            }
        }
        Collections.sort(printQueue);
        for (String s : printQueue) {
            System.out.println(s);
        }
        System.out.println();

    }

    /** Helper method for status, prints out untracked files. */
    void statusUntracked() {
        System.out.println("=== Untracked Files ===");
        ArrayList<String> printQueue = new ArrayList<>();
        List<String> filesCWD = Utils.plainFilenamesIn(Main.CWD);
        List<String> trackedCWD = Utils.plainFilenamesIn
                (Gitlet.FILES_DIRECTORY);
        for (String file : filesCWD) {
            File oneFile = Utils.join(Main.CWD, file);
            Blob blob = new Blob(oneFile);
            String fileSHA1 = Utils.sha1(blob.stringContents());
            if (!trackedCWD.contains(fileSHA1)
                    & !stage.stagedFiles().containsKey(file)) {
                printQueue.add(file);
            }
        }
        Collections.sort(printQueue);
        for (String s : printQueue) {
            System.out.println(s);
        }
        System.out.println();
    }

    /** Takes the version of the file in the head commit the
     * front of the current branch,
     * and puts it in the working directory, overwriting
     * that file if there is one.
     * The file is not staged.
     * @param commit The commit to be checked out from.
     * @param fileName The fileName from the commit to be checked out. */
    void checkoutCommitNodeFile(CommitNode commit, String fileName) {
        if (!commit.files.containsKey(fileName)) {
            Main.exitWithError("File does not exist in that commit.");
        }
        Blob commitFile = commit.files.get(fileName);
        String commitFileContents = commitFile.stringContents();
        File cwdFile = Utils.join(Main.CWD, fileName);
        if (cwdFile.exists()) {
            Utils.writeContents(cwdFile, commitFileContents);
        } else {
            try {
                cwdFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Utils.writeContents(cwdFile, commitFileContents);
        }
    }

    /** Creates a branch pointing at the head CommitNode.
     * @param branchName the name of the new branch. */
    void createBranch(String branchName) {
        if (branches.containsKey(branchName)) {
            Main.exitWithError("A branch with that name already exists.");
        }
        Branch newBranch = new Branch(branchName, head.node());
        branches.put(branchName, newBranch);
    }

    /** Removes the branch from the CommitTree with the given name.
     * @param branchName the name of the branch to be removed. */
    void removeBranch(String branchName) {
        if (!branches.containsKey(branchName)) {
            Main.exitWithError("A branch with that name does not exist.");
        } else if (currentBranch.name().equals(branchName)) {
            Main.exitWithError("Cannot remove the current branch.");
        } else {
            branches.remove(branchName);
        }
    }

    /** Checks out the branch with the given name. *
     * @param branchName the name of the branch to be checked out. */
    void checkoutBranch(String branchName) {
        if (!branches.containsKey(branchName)) {
            Main.exitWithError("No such branch exists.");
        } else if (currentBranch.name().equals(branchName)) {
            Main.exitWithError("No need to checkout the current branch.");
        }
        CommitNode branchCommit = branches.get(branchName).node();
        for (String fileName
                : Objects.requireNonNull(Utils.plainFilenamesIn(Main.CWD))) {
            if (!head.node().files.containsKey(fileName)) {
                Main.exitWithError("There is an "
                        + "untracked file in the way; "
                        + "delete it, or add and commit it first.");
            }
            if (!branchCommit.files.containsKey(fileName)) {
                Utils.join(Main.CWD, fileName).delete();
            }
        }
        for (String fileName : branchCommit.files.keySet()) {
            checkoutCommitNodeFile(branchCommit, fileName);
        }
        head = branches.get(branchName);
        currentBranch = branches.get(branchName);
        stage.clearStage();
    }

    /** Resets the Commit with the given CommitID, checks out all of its files.
     * @param commitID the commitID corresponding to the desired CommitNode. */
    void reset(String commitID) {
        List<String> commitFiles = Utils.plainFilenamesIn(COMMIT_DIRECTORY);
        if (commitFiles.size() == 0) {
            Main.exitWithError("No commit with that id exists.");
        }
        boolean commitExists = false;
        for (String name : commitFiles) {
            if (commitID.equals(name)) {
                commitExists = true;
            }
        }
        if (!commitExists) {
            Main.exitWithError("No commit with that id exists.");
        }
        File commitFile = Utils.join(COMMIT_DIRECTORY, commitID);
        CommitNode resetThisCommit = Utils
                .readObject(commitFile, CommitNode.class);
        List<String> trackedFiles = Utils
                .plainFilenamesIn(Gitlet.FILES_DIRECTORY);
        for (String fileName : Objects
                .requireNonNull(Utils.plainFilenamesIn(Main.CWD))) {
            File cwdFile = Utils.join(Main.CWD, fileName);
            Blob blobbedCWDFile = new Blob(cwdFile);
            String cwdsha1 = Utils.sha1(blobbedCWDFile.stringContents());
            if (!trackedFiles.contains(cwdsha1)) {
                Main.exitWithError("There is an untracked "
                        + "file in the way; delete it, "
                        + "or add and commit it first.");
            }
            if (!resetThisCommit.files.containsKey(fileName)) {
                Utils.join(Main.CWD, fileName).delete();
            }
        }
        for (String fileName : resetThisCommit.files.keySet()) {
            checkoutCommitNodeFile(resetThisCommit, fileName);
        }
        currentBranch.setNode(resetThisCommit);
        stage.clearStage();
    }

    /** Merges the provided branch name with the current branch, according
     * to a series of rules.
     * @param branchName the given branch name to be merged into current.*/
    void merge(String branchName) {
        checkUntrackedFilesCurrentCommit(branchName);
        Branch givenBranch = branches.get(branchName);
        CommitNode latestCommonAncestor = crissCrossSelector(branchName);
        if (latestCommonAncestor == currentBranch.node()) {
            checkoutBranch(branchName);
            System.out.println("Current branch fast-forwarded.");
            return;
        }
        boolean mergeConflict = false;
        for (String maybeModdedFile : givenBranch.node().files.keySet()) {
            if (latestCommonAncestor.files.containsKey(maybeModdedFile)) {
                if (!checkFileContents(givenBranch.node()
                                .files.get(maybeModdedFile).stringContents(),
                        latestCommonAncestor.files.
                                get(maybeModdedFile).stringContents())) {
                    Blob currentBranchFile = null;
                    if (currentBranch.node()
                            .files.containsKey(maybeModdedFile)) {
                        if (checkFileContents(currentBranch.node()
                                        .files.get(maybeModdedFile)
                                        .stringContents(),
                                latestCommonAncestor.files
                                        .get(maybeModdedFile)
                                        .stringContents())) {
                            checkoutCommitNodeFile(givenBranch.node(),
                                    maybeModdedFile);
                            File moddedFile = Utils
                                    .join(Main.CWD, maybeModdedFile);
                            addFile(moddedFile, maybeModdedFile);
                            continue;
                        } else {
                            currentBranchFile = currentBranch
                                    .node().files.get(maybeModdedFile);
                        }
                    }
                    mergeConflict = thirdMergeHelper(givenBranch,
                            maybeModdedFile, currentBranchFile, mergeConflict);
                }
            } else {
                mergeConflict = secondMergeHelper(maybeModdedFile,
                        givenBranch, mergeConflict);
            }
        }
        mergeConflict = mergeHelper(latestCommonAncestor,
                givenBranch, mergeConflict);
        mergeCommit("Merged " + branchName + " into "
                        + currentBranch.name() + ".",
                currentBranch.node(),
                givenBranch.node());
        if (mergeConflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    boolean mergeHelper(CommitNode latestCommonAncestor,
                        Branch givenBranch, boolean mergeConflict) {
        for (String splitFile : latestCommonAncestor.files.keySet()) {
            if (currentBranch.node().files.containsKey(splitFile)) {
                String splitFileSHA1 = Utils.sha1(latestCommonAncestor
                        .files.get(splitFile).stringContents());
                String currentBranchFileSHA1 = Utils
                        .sha1(currentBranch.node()
                                .files.get(splitFile).stringContents());
                if (splitFileSHA1.equals(currentBranchFileSHA1)
                        && !givenBranch.node().files.containsKey(splitFile)) {
                    rm(splitFile);
                }
            }
        }
        for (String currFile : currentBranch.node().files.keySet()) {
            if (latestCommonAncestor.files.containsKey(currFile)) {
                String splitContent = latestCommonAncestor
                        .files.get(currFile).stringContents();
                String currContent = currentBranch.node()
                        .files.get(currFile).stringContents();
                if (!checkFileContents(splitContent, currContent)) {
                    if (!givenBranch.node().files.containsKey(currFile)) {
                        mergeConflictedFile(currContent, "", currFile);
                        mergeConflict = true;
                        addFile(Utils.join(Main.CWD, currFile), currFile);
                    }
                }
            }
        }
        return mergeConflict;
    }

    boolean secondMergeHelper(String maybeModdedFile,
                              Branch givenBranch, boolean mergeConflict) {
        if (currentBranch.node().files.containsKey(maybeModdedFile)) {
            String currFile = currentBranch.node()
                    .files.get(maybeModdedFile).stringContents();
            String branchFile = givenBranch.node()
                    .files.get(maybeModdedFile).stringContents();
            if (!checkFileContents(branchFile, currFile)) {
                mergeConflictedFile(currFile,
                        branchFile, maybeModdedFile);
                mergeConflict = true;
                addFile(Utils.join(Main.CWD,
                        maybeModdedFile), maybeModdedFile);
            } else {
                checkoutCommitNodeFile(givenBranch.node(),
                        maybeModdedFile);
                File moddedFile = Utils.join(Main.CWD, maybeModdedFile);
                addFile(moddedFile, maybeModdedFile);
            }
        } else {
            checkoutCommitNodeFile(givenBranch.node(), maybeModdedFile);
            File moddedFile = Utils.join(Main.CWD, maybeModdedFile);
            addFile(moddedFile, maybeModdedFile);
        }
        return mergeConflict;
    }

    boolean thirdMergeHelper(Branch givenBranch, String maybeModdedFile,
                             Blob currentBranchFile, boolean mergeConflict) {
        boolean yay = mergeConflict;
        Blob givenBranchFile = givenBranch
                .node().files.get(maybeModdedFile);
        File splitConflictedFile = Utils
                .join(Main.CWD, maybeModdedFile);
        String currentBranchContents;
        if (currentBranchFile != null) {
            currentBranchContents = currentBranchFile
                    .stringContents();
        } else {
            currentBranchContents = "";
        }
        String givenBranchContents = givenBranchFile
                .stringContents();
        mergeConflictedFile(currentBranchContents,
                givenBranchContents, maybeModdedFile);
        yay = true;
        addFile(splitConflictedFile, maybeModdedFile);
        return yay;
    }

    /** Helper method for merge that returns the split point CommitNode.
     * @param givenBranchName the given branch name.
     * @return the split node, the last common ancestor. */
    private CommitNode crissCrossSelector(String givenBranchName) {
        CommitNode givenCommonAncestor =
                latestCommonAncestorGiven(givenBranchName);
        CommitNode currentCommonAncestor =
                latestCommonAncestorCurrent(givenBranchName);
        if (currentCommonAncestor == currentBranch.node()) {
            return currentCommonAncestor;
        }
        int givenCADist = currentBranch.node()
                .ancestors.indexOf(givenCommonAncestor);
        int currentCADist = currentBranch.node()
                .ancestors.indexOf(currentCommonAncestor);
        if (givenCADist == currentCADist) {
            return currentCommonAncestor;
        } else if (givenCADist > currentCADist) {
            return givenCommonAncestor;
        } else {
            return currentCommonAncestor;
        }
    }

    /** Finds the common ancestor starting from the givenBranch node.
     * @param givenBranchName the given branch.
     * @return returns the common ancestor searching from given node.*/
    private CommitNode latestCommonAncestorGiven(String givenBranchName) {
        Branch givenBranch = branches.get(givenBranchName);
        CommitNode givenNode = givenBranch.node();
        LinkedList<CommitNode> work = new LinkedList<>();
        work.push(givenNode);
        while (!work.isEmpty()) {
            CommitNode node = work.remove();
            if (node != null) {
                if (currentBranch.node().ancestors.contains(node)) {
                    return node;
                } else {
                    work.push(node.commitParent);
                    if (node.commitParentTwo != null) {
                        work.push(node.commitParentTwo);
                    }
                }
            }
        }
        return null;
    }

    /** Finds the common ancestor starting from the currentBranch node.
     * @param givenBranchName the given branch name.
     * @return the latest common ancestor searching from current node. */
    private CommitNode latestCommonAncestorCurrent(String givenBranchName) {
        Branch givenBranch = branches.get(givenBranchName);
        CommitNode currentNode = currentBranch.node();
        LinkedList<CommitNode> work = new LinkedList<>();
        work.push(currentNode);
        while (!work.isEmpty()) {
            CommitNode node = work.remove();
            if (node != null) {
                if (givenBranch.node().ancestors.contains(node)) {
                    return node;
                } else {
                    work.push(node.commitParent);
                    if (node.commitParentTwo != null) {
                        work.push(node.commitParentTwo);
                    }
                }
            }
        }
        return null;
    }

    /** Helper method for merge, checks merges and exits with error messages.
     * @param branchName the given branch name from merge.
     */
    void checkUntrackedFilesCurrentCommit(String branchName) {
        for (String fileName
                : Objects.requireNonNull(Utils.plainFilenamesIn(Main.CWD))) {
            if (!currentBranch.node().files.containsKey(fileName)
                    & !stage.stagedFiles().containsKey(fileName)) {
                Main.exitWithError("There is an "
                        + "untracked file in the way; "
                        + "delete it, or add and commit it first.");
            }
        }
        if (stage.stagedFiles().size() > 0 | stage.stagedFiles().size() > 0) {
            Main.exitWithError("You have uncommitted changes.");
        } else if (!branches.containsKey(branchName)) {
            Main.exitWithError("A branch with that name does not exist.");
        } else if (currentBranch.name().equals(branchName)) {
            Main.exitWithError("Cannot merge a branch with itself.");
        } else if (currentBranch.node().ancestors.
                contains(branches.get(branchName).node())) {
            Main.exitWithError("Given branch is "
                    + "an ancestor of the current branch.");
        }
    }

    /** Handles merges with conflicted files from merge.
     * @param currentContents contents of the file from the current node.
     * @param givenContents contents of file from given branch node.
     * @param fileName is the name of the conflicted file.*/
    void mergeConflictedFile(String currentContents,
                             String givenContents, String fileName) {
        String text = "<<<<<<< HEAD\n"
                + currentContents
                + "=======\n"
                + givenContents
                + ">>>>>>>\n";
        Utils.writeContents(Utils.join(Main.CWD, fileName), text);
    }

    /** Checks if two files have the same contents.
     * @param fileOne the string contents of one file.
     * @param fileTwo the string contents of a second file.
     * @return true if files are equal, false if files are not equal. */
    boolean checkFileContents(String fileOne, String fileTwo) {
        return Utils.sha1(fileOne).equals(Utils.sha1(fileTwo));
    }

    /** Special commit specifically for merges.
     * @param message the log message
     * @param firstParent the current branch node.
     * @param secondParent the given branch node. */
    void mergeCommit(String message, CommitNode firstParent,
                     CommitNode secondParent) {
        ZonedDateTime now = ZonedDateTime
                .now(ZoneId.of(ZoneId.SHORT_IDS.get("PST")));
        CommitNode newCommit = new CommitNode(now,
                message, firstParent, secondParent);
        createCommitFile(newCommit);
        currentBranch.setNode(newCommit);
        stage.clearStage();
        stage.clearRemovalStage();
    }


    /** The internal representaion of a Gitlet Commit. Commits are stored
     * inside the Commit directory in .gitlet */
    public class CommitNode implements Serializable {
        /** Creates a Commit.
         * @param timestamp is a LocalDateTime object.
         * @param message is a message.
         * @param parent is a pointer that points to the parent.
         * @param parentTwo is a pointer that points branch after merge.*/
        private CommitNode(ZonedDateTime timestamp, String message,
                           CommitNode parent, CommitNode parentTwo) {
            strTimestamp = formatTime(timestamp);
            logMessage = message;
            commitParent = parent;
            commitParentTwo = parentTwo;
            files = new TreeMap<>();
            fileNameToSHA1 = new TreeMap<>();
            ancestors = new ArrayList<CommitNode>();
            if (isInitialCommit) {
                isInitialCommit = false;
            } else {
                if (stage.stagedFiles().size() == 0
                        && stage.stagedForRemoval().size() == 0) {
                    Main.exitWithError("No changes added to the commit.");
                }
                files.putAll(commitParent.files);
                fileNameToSHA1.putAll(commitParent.fileNameToSHA1);
                ancestors.addAll(parent.ancestors);
                ancestors.add(parent);
                if (parentTwo != null) {
                    ancestors.add(parentTwo);
                }
            }
            for (String stageFileName : stage.stagedFiles().keySet()) {
                if (files.containsKey(stageFileName)) {
                    for (String commitFileName : files.keySet()) {
                        if (commitFileName.equals(stageFileName)) {
                            String commitFileSHA1 = Utils.sha1(files
                                    .get(commitFileName).stringContents());
                            String stageFileSHA1 = Utils
                                    .sha1(stage.stagedFiles()
                                    .get(stageFileName).stringContents());
                            if (!commitFileSHA1.equals(stageFileSHA1)) {
                                files.replace(commitFileName,
                                        stage.stagedFiles().get(stageFileName));
                                fileNameToSHA1
                                        .replace(commitFileName, stageFileSHA1);
                            }
                        }
                    }
                } else {
                    files.put(stageFileName, stage.stagedFiles()
                            .get(stageFileName));
                    String stageFileSHA1 = Utils.sha1(stage.stagedFiles()
                            .get(stageFileName).stringContents());
                    fileNameToSHA1.put(stageFileName, stageFileSHA1);
                }
            }
            for (String removeFileName : stage.stagedForRemoval()) {
                files.remove(removeFileName);
            }
            byte[] serializedCommit = Utils.serialize(this);
            sha1 = Utils.sha1(serializedCommit);
        }

        /** Takes a string representation of a LocalDateTime object.
         * Formatted time example: Thu Nov 9 20:00:05 2017 -0800
         * @param timestamp A LocalDateTime to be formatted.
         * @return the string of a formatted LocalDateTime.
         */
        public static String formatTime(ZonedDateTime timestamp) {
            DateTimeFormatter formatter = DateTimeFormatter
                    .ofPattern("ccc LLL d HH:mm:ss yyyy Z");
            return timestamp.format(formatter);
        }

        /** Accessor method for files.
         * @return the file treemap. */
        TreeMap<String, Blob> files() {
            return files;
        }

        /** Accessor method for file to SHA1 map.
         * @return the map of filenames to SHA1. */
        TreeMap<String, String> fileNameToSHA1() {
            return fileNameToSHA1;
        }
        /** The unique SHA1 for this commit node. */
        private final String sha1;
        /** The timestamp formatted for this commit node. */
        private final String strTimestamp;
        /** The message associated with this commit node. */
        private final String logMessage;
        /** The parent of this commit node. */
        private final CommitNode commitParent;
        /** The second parent of this commit node, from a given branch merge. */
        private final CommitNode commitParentTwo;
        /** The ancestors of this commit node. */
        private final ArrayList<CommitNode> ancestors;


        /** The map of names to blobs. */
        private TreeMap<String, Blob> files;
        /** The map of file names to SHA1. */
        private TreeMap<String, String> fileNameToSHA1;
    }
}
