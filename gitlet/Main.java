package gitlet;

import java.io.File;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Chris Zhan
 */
public class Main {

    /** Current Working Directory. */
    static final File CWD = new File(".");

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        if (args.length == 0) {
            exitWithError("Please enter a command.");
        }
        switch (args[0]) {
        case "init":
            if (args.length != 1) {
                exitWithError("Incorrect operands.");
            }
            init();
            break;
        case "add": add(args);
            break;
        case "commit": commit(args);
            break;
        case "rm": rm(args);
            break;
        case "log":
            if (args.length != 1) {
                exitWithError("Incorrect operands.");
            } else if (!Gitlet.GITLET_DIRECTORY.exists()) {
                exitWithError("Not in an initialized Gitlet directory.");
            }
            log();
            break;
        case "global-log":
            if (args.length > 1) {
                exitWithError("Incorrect operands.");
            } else if (!Gitlet.GITLET_DIRECTORY.exists()) {
                exitWithError("Not in an initialized Gitlet directory.");
            }
            globalLog();
            break;
        case "find": find(args);
            break;
        case "status":
            if (args.length > 1) {
                exitWithError("Incorrect operands.");
            } else if (!Gitlet.GITLET_DIRECTORY.exists()) {
                exitWithError("Not in an initialized Gitlet directory.");
            }
            status();
            break;
        case "checkout": checkout(args);
            break;
        case "branch": branch(args);
            break;
        case "rm-branch": rmBranch(args);
            break;
        case "reset": reset(args);
            break;
        case "merge": merge(args);
            break;
        default:
            exitWithError("No command with that name exists.");
            break;
        }
    }

    public static void init() {
        if (Gitlet.GITLET_DIRECTORY.exists()) {
            exitWithError("A Gitlet version-control system "
                    + "already exists in the current directory.");
        } else {
            _gitlet = new Gitlet();
            _gitlet.saveGitlet();
        }
    }

    /** Adds and stages a file whose name is in ARGS[1].
     * @param args the name of the file. */
    public static void add(String... args) {
        if (args.length != 2) {
            exitWithError("Incorrect operands.");
        } else if (!Gitlet.GITLET_DIRECTORY.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }
        File addedFile = Utils.join(CWD, args[1]);
        if (!addedFile.exists()) {
            exitWithError("File does not exist.");
        }
        _gitlet = Gitlet.readGitlet();
        _gitlet.add(addedFile, args[1]);
        _gitlet.saveGitlet();
    }

    /** Commits all staged files with the message.
     * @param args the arguments. */
    public static void commit(String... args) {
        if (args.length == 1) {
            exitWithError("Please enter a commit message.");
        } else if (args.length > 2) {
            exitWithError("Incorrect operands.");
        } else if (!Gitlet.GITLET_DIRECTORY.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }
        _gitlet = Gitlet.readGitlet();
        if (args[1].equals("")) {
            Main.exitWithError("Please enter a commit message.");
        }
        _gitlet.commit(args[1]);
        _gitlet.saveGitlet();
    }

    public static void rm(String... args) {
        if (args.length != 2) {
            exitWithError("Incorrect operands.");
        } else if (!Gitlet.GITLET_DIRECTORY.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        } else if (!Gitlet.GITLET_DIRECTORY.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }
        _gitlet = Gitlet.readGitlet();
        _gitlet.rm(args[1]);
        _gitlet.saveGitlet();
    }

    public static void log() {
        _gitlet = Gitlet.readGitlet();
        _gitlet.log();
    }

    public static void globalLog() {
        _gitlet = Gitlet.readGitlet();
        _gitlet.globalLog();
    }

    public static void find(String... args) {
        if (args.length > 2) {
            exitWithError("Incorrect operands.");
        } else if (!Gitlet.GITLET_DIRECTORY.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }
        _gitlet = Gitlet.readGitlet();
        _gitlet.find(args[1]);
    }

    public static void status() {
        _gitlet = Gitlet.readGitlet();
        _gitlet.status();
    }

    public static void checkout(String... args) {
        if (args.length > 4) {
            exitWithError("Incorrect operands.");
        } else if (!Gitlet.GITLET_DIRECTORY.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        } else if (!Gitlet.GITLET_DIRECTORY.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }
        _gitlet = Gitlet.readGitlet();
        _gitlet.checkout(args);
        _gitlet.saveGitlet();
    }

    public static void branch(String... args) {
        if (args.length > 2) {
            exitWithError("Incorrect operands.");
        } else if (!Gitlet.GITLET_DIRECTORY.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        } else if (!Gitlet.GITLET_DIRECTORY.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }
        _gitlet = Gitlet.readGitlet();
        _gitlet.branch(args[1]);
        _gitlet.saveGitlet();
    }

    public static void rmBranch(String... args) {
        if (args.length > 2) {
            exitWithError("Incorrect operands.");
        } else if (!Gitlet.GITLET_DIRECTORY.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }
        _gitlet = Gitlet.readGitlet();
        _gitlet.rmBranch(args[1]);
        _gitlet.saveGitlet();
    }

    public static void reset(String... args) {
        if (args.length > 2) {
            exitWithError("Incorrect operands.");
        } else if (!Gitlet.GITLET_DIRECTORY.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }
        _gitlet = Gitlet.readGitlet();
        _gitlet.reset(args[1]);
        _gitlet.saveGitlet();
    }

    public static void merge(String... args) {
        if (args.length > 2) {
            exitWithError("Incorrect operands.");
        } else if (!Gitlet.GITLET_DIRECTORY.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }
        _gitlet = Gitlet.readGitlet();
        _gitlet.merge(args[1]);
        _gitlet.saveGitlet();
    }

    /** Throws a new GitletException with message MESSAGE.
     *  Exits with error code 0.
     * @param message error message to print
     */
    public static void exitWithError(String message) {
        System.out.println(message);
        System.exit(0);
    }
    /** Accessor method for the gitlet object.
     * @return a gitlet object. */
    Gitlet gitlet() {
        return _gitlet;
    }
    /** Variable representing the gitlet object. */
    private static Gitlet _gitlet;

}
