package gitlet;

import java.io.File;

/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 *
 * @author Hovan Liu
 */
public class Main {
    /**
     * Stores current working directory.
     */
    private static final File CWD = new File(System.getProperty("user.dir"));

    /**
     * Stores directory of gitlet repository.
     */
    private static final File GITLET_FOLDER = Utils.join(CWD, ".gitlet");

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND> ....
     */
    public static void main(String... args) {
        Repo repo = new Repo();
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        switch (args[0]) {
        case "init":
            if (args.length == 1) {
                repo.init();
            } else {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
            break;
        case "commit":
            if (!GITLET_FOLDER.exists()) {
                System.out.println("Not in an initialized Gitlet directory.");
            } else if (args.length == 2) {
                repo.commit(args[1], "");
            } else {
                System.out.println("Incorrect operands.");
            }
            break;
        case "add":
            if (!GITLET_FOLDER.exists()) {
                System.out.println("Not in an initialized Gitlet directory.");
            } else if (args.length == 2) {
                repo.add(args[1]);
            } else {
                System.out.println("Incorrect operands.");
            }
            break;
        case "checkout":
            if (!GITLET_FOLDER.exists()) {
                System.out.println("Not in an initialized Gitlet directory.");
            } else if (args.length == 3) {
                String fileName = args[2];
                repo.checkout1(fileName);
            } else if (args.length == 4) {
                if (args[2].equals("--")) {
                    String commitID = args[1];
                    String fileName = args[3];
                    repo.checkout2(commitID, fileName);
                } else {
                    System.out.println("Incorrect operands.");
                }
            } else if (args.length == 2) {
                String branchName = args[1];
                repo.checkout3(branchName);
            } else {
                System.out.println("Incorrect operands.");
            }
            break;
        default: helper1(args, repo);
        }
    }

    private static void helper1(String[] args, Repo repo) {
        switch (args[0]) {
        case "log":
            if (!GITLET_FOLDER.exists()) {
                System.out.println("Not in an initialized Gitlet directory.");
            } else if (args.length == 1) {
                repo.log();
            } else {
                System.out.println("Incorrect operands.");
            }
            break;
        case "merge":
            if (!GITLET_FOLDER.exists()) {
                System.out.println("Not in an initialized Gitlet directory.");
            } else if (args.length == 2) {
                String branchName = args[1];
                repo.merge(branchName);
            } else {
                System.out.println("Incorrect operands.");
            }
            break;
        case "rm":
            if (!GITLET_FOLDER.exists()) {
                System.out.println("Not in an initialized Gitlet directory.");
            } else if (args.length == 2) {
                String fileName = args[1];
                repo.rm(fileName);
            } else {
                System.out.println("Incorrect operands.");
            }
            break;
        case "global-log":
            if (!GITLET_FOLDER.exists()) {
                System.out.println("Not in an initialized Gitlet directory.");
            } else if (args.length == 1) {
                repo.globalLog();
            } else {
                System.out.println("Incorrect operands.");
            }
            break;
        default: helper2(args, repo);
        }
    }

    private static void helper2(String[] args, Repo repo) {
        switch (args[0]) {
        case "find":
            if (!GITLET_FOLDER.exists()) {
                System.out.println("Not in an initialized Gitlet directory.");
            } else if (args.length == 2) {
                String commitMessage = args[1];
                repo.find(commitMessage);
            } else {
                System.out.println("Incorrect operands.");
            }
            break;
        case "status":
            if (!GITLET_FOLDER.exists()) {
                System.out.println("Not in an initialized Gitlet directory.");
            } else if (args.length == 1) {
                repo.status();
            } else {
                System.out.println("Incorrect operands.");
            }
            break;
        case "branch":
            if (!GITLET_FOLDER.exists()) {
                System.out.println("Not in an initialized Gitlet directory.");
            } else if (args.length == 2) {
                String branchName = args[1];
                repo.branch(branchName);
            } else {
                System.out.println("Incorrect operands.");
            }
            break;
        case "rm-branch":
            if (!GITLET_FOLDER.exists()) {
                System.out.println("Not in an initialized Gitlet directory.");
            } else if (args.length == 2) {
                String branchName = args[1];
                repo.rmBranch(branchName);
            } else {
                System.out.println("Incorrect operands.");
            }
            break;
        case "reset":
            if (!GITLET_FOLDER.exists()) {
                System.out.println("Not in an initialized Gitlet directory.");
            } else if (args.length == 2) {
                String commitID = args[1];
                repo.reset(commitID);
            } else {
                System.out.println("Incorrect operands.");
            }
            break;
        default:
            System.out.println("No command with that name exists");
            System.exit(0);
            break;
        }
    }
}

