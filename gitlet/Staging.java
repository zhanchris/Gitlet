package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeMap;

public class Staging implements Serializable {

    /** Staging Folder that stores files that are gitlet added. */
    static final File STAGING_DIRECTORY = Utils.join(
            Gitlet.GITLET_DIRECTORY, "staging");
    Staging() {
        STAGING_DIRECTORY.mkdir();
        stagedFiles = new TreeMap<>();
    }

    /** Adds a file to the staging area by placing it in the
     * staging directory.
     * @param fileBlob the blob representing the file.
     * @param name the name of the file. */
    void addFile(Blob fileBlob, String name) {
        String sha1 = Utils.sha1(fileBlob.stringContents());
        stagedFiles.put(name, fileBlob);
        File addedFile = Utils.join(STAGING_DIRECTORY, sha1);
        try {
            addedFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.writeObject(addedFile, fileBlob);
    }

    /** Removes a specified file from the staging area.
     * @param fileName the name of the file to be removed. */
    void removeFile(String fileName) {
        File toRemove = Utils.join(STAGING_DIRECTORY, fileName);
        toRemove.delete();
        stagedFiles.remove(fileName);
    }

    /** Clears the stage. This will copy all files in the stagind directory to
     * .gitlet/files. It will also delete all files in the staging directory and
     * clear the TreeMap stagedFiles to prepare for new stagings.
     * This should be called after a commit. */
    void clearStage() {
        List<String> stagedFilesSHA1 = Utils.plainFilenamesIn(
                Staging.STAGING_DIRECTORY);
        for (String name : stagedFilesSHA1) {
            File stagedFile = Utils.join(Staging.STAGING_DIRECTORY, name);
            File newTrackedFile = Utils.join(Gitlet.FILES_DIRECTORY, name);
            try {
                newTrackedFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Blob stagedContents = Utils.readObject(stagedFile, Blob.class);
            Utils.writeObject(newTrackedFile, stagedContents);
            stagedFile.delete();
        }
        stagedFiles.clear();
    }

    /** Clears all files staged for removal. This should only be called after
     * a commit. */
    void clearRemovalStage() {
        stagedForRemoval.clear();
    }

    /** Accessor method for the stageMap.
     * @return the list of staged files */
    TreeMap<String, Blob> stagedFiles() {
        return stagedFiles;
    }

    /** Accessor method for the removed files.
     * @return the list of files staged for removals.
     */
    ArrayList<String> stagedForRemoval() {
        return stagedForRemoval;
    }
    /** The list of files that need to be staged. */
    private TreeMap<String, Blob> stagedFiles;
    /** The list of files that are staged for removal. */
    private ArrayList<String> stagedForRemoval = new ArrayList<>();
}
