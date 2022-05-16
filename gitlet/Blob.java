package gitlet;
import java.io.Serializable;
import java.io.File;

/** The blob class consists of a serialized representation of the file.
 * @author Chris Zhan */
public class Blob implements Serializable {

    /** Initializing a blob reads in a file.
     * @param file A regular file. */
    Blob(File file) {
        byteContents = Utils.readContents(file);
        stringContents = Utils.readContentsAsString(file);
    }
    byte[] byteContents() {
        return byteContents;
    }

    String stringContents() {
        return stringContents;
    }

    /** The byte contents of a blob. */
    private final byte[] byteContents;
    /** The string contents of a blob. */
    private final String stringContents;
}
