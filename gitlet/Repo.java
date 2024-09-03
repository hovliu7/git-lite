package gitlet;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Repo {

    /** Stores current working directory. */
    private static final File CWD = new File(System.getProperty("user.dir"));

    /** Stores directory of gitlet repository. */
    private static final File GITLET_FOLDER =
            Utils.join(CWD, ".gitlet");

    /** Stores directory of commits. */
    private static final File COMMITS_DIR =
            Utils.join(GITLET_FOLDER, "commits");

    /** Stores directory of blobs. */
    private static final File BLOBS_DIR =
            Utils.join(GITLET_FOLDER, "blobs");

    /** Stores directory of staging area. */
    private static final File STAGING_DIR =
            Utils.join(GITLET_FOLDER, "staging");

    /** Stores directory of branches. */
    private static final File BRANCHES_DIR =
            Utils.join(GITLET_FOLDER, "branches");

    /** Stores staging area object. */
    private StagingArea stage;

    /** Stores if there exists a merge conflict. */
    private boolean mergeConflict;

    /**
     * Creates new repository.
     */
    public Repo() {
        File stageFile = Utils.join(STAGING_DIR, "staging_area");
        if (stageFile.exists()) {
            stage = getStage();
        }
        mergeConflict = false;
    }

    /**
     * Creates a new Gitlet version-control system in the current directory.
     * This system will automatically start with one commit: a commit that
     * contains no files and has the commit message initial commit (just like
     * that, with no punctuation). It will have a single branch: master, which
     * initially points to this initial commit, and master will be the current
     * branch. The timestamp for this initial commit will be 00:00:00 UTC,
     * Thursday, 1 January 1970 in whatever format you choose for dates (this
     * is called "The (Unix) Epoch", represented internally by the time 0.)
     * Since the initial commit in all repositories created by Gitlet will have
     * exactly the same content, it follows that all repositories will
     * automatically share this commit (they will all have the same UID) and
     * all commits in all repositories will trace back to it.
     */
    public void init() {
        if (GITLET_FOLDER.exists()) {
            System.out.println("A Gitlet version-control system already exists "
                    + "in the current directory.");
            System.exit(0);
        } else {
            GITLET_FOLDER.mkdir();
            COMMITS_DIR.mkdir();
            BLOBS_DIR.mkdir();
            STAGING_DIR.mkdir();
            BRANCHES_DIR.mkdir();
            Commit c = new Commit("initial commit", "",
                    new HashMap<String, String>(), "");
            File cFile = Utils.join(COMMITS_DIR, c.getHash());
            Utils.writeObject(cFile, c);
            this.stage = new StagingArea();
            saveStage();
            File masterBranch = Utils.join(BRANCHES_DIR, "master");
            Utils.writeContents(masterBranch, c.getHash());
            File headFile = Utils.join(BRANCHES_DIR, "HEAD");
            Utils.writeContents(headFile, "master");


        }
    }

    /**
     * Adds a copy of the file as it currently exists to the staging area (see
     * the description of the commit command). For this reason, adding a file
     * is also called staging the file for addition. Staging an already-staged
     * file overwrites the previous entry in the staging area with the new
     * contents. The staging area should be somewhere in .gitlet. If the current
     * working version of the file is identical to the version in the current
     * commit, do not stage it to be added, and remove it from the staging area
     * if it is already there (as can happen when a file is changed, added, and
     * then changed back). The file will no longer be staged for removal (see
     * gitlet rm), if it was at the time of the command.
     * @param fileName
     */
    public void add(String fileName) {
        File copyOfFile = new File(fileName);
        if (copyOfFile.exists()) {
            Commit currCommit = getCurrCommit();
            HashMap<String, String> currBlobs = currCommit.getBlobs();
            byte[] serializedFile = Utils.readContents(copyOfFile);
            String fileHash = Utils.sha1(serializedFile);
            if (currBlobs.containsKey(fileName)
                    && currBlobs.get(fileName).equals(fileHash)) {
                if (stage.getAdded().containsKey(fileName)) {
                    stage.getAdded().remove(fileName);
                    saveStage();
                }
            } else if (currBlobs.containsKey(fileName)
                    && !currBlobs.get(fileName).equals(fileHash)) {
                stage.getAdded().remove(fileName);
                File newBlob = Utils.join(BLOBS_DIR, fileHash);
                Utils.writeContents(newBlob, serializedFile);
                this.stage.add(fileName, fileHash);
                saveStage();
            } else {
                File newBlob = Utils.join(BLOBS_DIR, fileHash);
                Utils.writeContents(newBlob, serializedFile);
                this.stage.add(fileName, fileHash);
                saveStage();
            }
            if (stage.getRemoved().containsKey(fileName)) {
                stage.getRemoved().remove(fileName);
                saveStage();
            }

        } else {
            System.out.println("File does not exist");
        }

    }

    /**
     * Saves the current stage to the staging_area file.
     */
    public void saveStage() {
        File stageFile = Utils.join(STAGING_DIR, "staging_area");
        Utils.writeObject(stageFile, stage);
    }

    /**
     * Return the current stage within the staging_area file.
     * @return
     */
    public StagingArea getStage() {
        File stageFile = Utils.join(STAGING_DIR, "staging_area");
        return Utils.readObject(stageFile, StagingArea.class);
    }

    /**
     * Returns the current commit tracked by the head branch.
     * @return
     */
    public Commit getCurrCommit() {
        File currBranchFile = getCurrBranch();
        String currCommitHash = Utils.readContentsAsString(currBranchFile);
        File currCommitFile = Utils.join(COMMITS_DIR, currCommitHash);
        Commit c = Utils.readObject(currCommitFile, Commit.class);
        return c;
    }


    /**
     * Saves a snapshot of tracked files in the current commit and staging area
     * so they can be restored at a later time, creating a new commit. The
     * commit is said to be tracking the saved files. By default, each commit's
     * snapshot of files will be exactly the same as its parent commit's
     * snapshot of files; it will keep versions of files exactly as they are,
     * and not update them. A commit will only update the contents of files it
     * is tracking that have been staged for addition at the time of commit, in
     * which case the commit will now include the version of the file that was
     * staged instead of the version it got from its parent. A commit will save
     * and start tracking any files that were staged for addition but weren't
     * tracked by its parent. Finally, files tracked in the current commit may
     * be untracked in the new commit as a result being staged for removal by
     * the rm command (below).
     * @param message
     * @param mParentHash
     */
    public void commit(String message, String mParentHash) {
        StagingArea currStage = getStage();
        HashMap<String, String> added = stage.getAdded();
        HashMap<String, String> removed = stage.getRemoved();
        if (added.isEmpty() && removed.isEmpty()) {
            System.out.println("No changes added to the commit.");
        } else if (message.length() == 0) {
            System.out.println("Please enter a commit message.");
        } else {
            Commit currCommit = getCurrCommit();
            String currCommitHash = currCommit.getHash();
            HashMap<String, String> currBlobs = currCommit.getBlobs();
            HashMap<String, String> newBlobs =
                    new HashMap<String, String>(currBlobs);

            for (Map.Entry mapElement : added.entrySet()) {
                String fileName = (String) mapElement.getKey();
                String fileHash = (String) mapElement.getValue();
                newBlobs.put(fileName, fileHash);
            }

            for (Map.Entry mapElement : removed.entrySet()) {
                String fileName = (String) mapElement.getKey();
                newBlobs.remove(fileName);
            }
            Commit newCommit = new Commit(message, currCommitHash, newBlobs,
                    mParentHash);
            File newCommitFile = Utils.join(COMMITS_DIR, newCommit.getHash());
            Utils.writeObject(newCommitFile, newCommit);
            File headFile = Utils.join(BRANCHES_DIR, "HEAD");
            String currBranchName = Utils.readContentsAsString(headFile);
            File currBranchFile = Utils.join(BRANCHES_DIR, currBranchName);
            Utils.writeContents(currBranchFile, newCommit.getHash());
            stage.clear();
            saveStage();
        }
    }

    /**
     * Takes the version of the file as it exists in the head commit, the front
     * of the current branch, and puts it in the working directory,
     * overwriting the version of the file that's already there if there is one.
     * The new version of the file is not staged.
     * @param fileName
     */
    public void checkout1(String fileName) {
        Commit currCommit = getCurrCommit();
        File currFile = Utils.join(CWD, fileName);
        HashMap<String, String> currBlobs = currCommit.getBlobs();
        if (currBlobs.containsKey(fileName)) {
            String fileHash = currBlobs.get(fileName);
            File currBlob = Utils.join(BLOBS_DIR, fileHash);
            byte[] serializedFile = Utils.readContents(currBlob);
            Utils.writeContents(currFile, serializedFile);
        } else {
            System.out.println("File does not exist in that commit.");
        }
    }

    /**
     *Takes the version of the file as it exists in the commit with the given
     * id, and puts it in the working directory, overwriting the version of
     * the file that's already there if there is one.
     * The new version of the file is not staged.
     * @param commitID
     * @param fileName
     */
    public void checkout2(String commitID, String fileName) {
        File originalFile = Utils.join(CWD, fileName);
        List<String> commitFileNames = Utils.plainFilenamesIn(COMMITS_DIR);
        File commitFile = null;
        for (String commitFileName : commitFileNames) {
            if (commitFileName.indexOf(commitID) >= 0) {
                commitFile = Utils.join(COMMITS_DIR, commitFileName);
            }
        }
        if (commitFile == null) {
            System.out.println("No commit with that id exists.");
        } else {
            Commit c = Utils.readObject(commitFile, Commit.class);
            HashMap<String, String> blobs = c.getBlobs();
            if (blobs.containsKey(fileName)) {
                String fileHash = blobs.get(fileName);
                File currBlob = Utils.join(BLOBS_DIR, fileHash);
                byte[] serializedFile = Utils.readContents(currBlob);
                Utils.writeContents(originalFile, serializedFile);
            } else {
                System.out.println("File does not exist in that commit.");
            }
        }

    }

    /**
     *Takes all files in the commit at the head of the given branch, and puts
     * them in the working directory, overwriting the versions of the files
     * that are already there if they exist. Also, at the end of this command,
     * the given branch will now be considered the current branch (HEAD). Any
     * files that are tracked in the current branch but are not present in
     * the checked-out branch are deleted. The staging area is cleared, unless
     * the checked-out branch is the current branch (see Failure cases below).
     * @param branchName
     */
    public void checkout3(String branchName) {
        File branchFile = Utils.join(BRANCHES_DIR, branchName);
        File currBranch = getCurrBranch();
        if (!branchFile.exists()) {
            System.out.println("No such branch exists");
        } else if (branchFile.equals(currBranch)) {
            System.out.println("No need to checkout the current branch.");
        } else {
            String checkedOutCommitHash =
                    Utils.readContentsAsString(branchFile);
            File checkedOutCommitFile =
                    Utils.join(COMMITS_DIR, checkedOutCommitHash);
            Commit checkedOutCommit =
                    Utils.readObject(checkedOutCommitFile, Commit.class);
            HashMap<String, String> checkedOutBlobs =
                    checkedOutCommit.getBlobs();
            Commit currCommit = getCurrCommit();
            HashMap<String, String> currBlobs = currCommit.getBlobs();
            List<String> cwdFileNames = Utils.plainFilenamesIn(CWD);
            for (String fileName : cwdFileNames) {
                if (!currBlobs.containsKey(fileName)
                        && checkedOutBlobs.containsKey(fileName)) {
                    System.out.println("There is an untracked file in the "
                            + "way; delete it, or add and commit it first.");
                    System.exit(0);
                } else if (currBlobs.containsKey(fileName)
                        && !checkedOutBlobs.containsKey(fileName)) {
                    File currFile = Utils.join(CWD, fileName);
                    Utils.restrictedDelete(currFile);
                }
            }
            for (Map.Entry mapElement : checkedOutBlobs.entrySet()) {
                String fileName = (String) mapElement.getKey();
                String fileHash = (String) mapElement.getValue();
                File currBlob = Utils.join(BLOBS_DIR, fileHash);
                byte[] serializedFile = Utils.readContents(currBlob);
                File destination = Utils.join(CWD, fileName);
                Utils.writeContents(destination, serializedFile);
            }
            stage.clear();
            saveStage();
            File headFile = Utils.join(BRANCHES_DIR, "HEAD");
            Utils.writeContents(headFile, branchName);
        }

    }

    /**
     * Returns current branch tracked by HEAD pointer.
     * @return
     */
    public File getCurrBranch() {
        File headFile = Utils.join(BRANCHES_DIR, "HEAD");
        String currBranchName = Utils.readContentsAsString(headFile);
        File currBranchFile = Utils.join(BRANCHES_DIR, currBranchName);
        return currBranchFile;
    }

    /**
     * Starting at the current head commit, display information about each
     * commit backwards along the commit tree until the initial commit,
     * following the first parent commit links, ignoring any second parents
     * found in merge commits. (In regular Git, this is what you get with git
     * log --first-parent). This set of commit nodes is called the commit's
     * history. For every node in this history, the information it should
     * display is the commit id, the time the commit was made, and the commit
     * message.
     */
    public void log() {
        Commit c = getCurrCommit();
        while (c.getParentHash() != "") {
            System.out.println("===");
            System.out.println("commit " + c.getHash());
            System.out.println("Date: " + c.getTimestamp());
            System.out.println(c.getMessage());
            System.out.println();
            File parentFile = Utils.join(COMMITS_DIR, c.getParentHash());
            c = Utils.readObject(parentFile, Commit.class);
        }
        System.out.println("===");
        System.out.println("commit " + c.getHash());
        System.out.println("Date: " + c.getTimestamp());
        System.out.println(c.getMessage());
        System.out.println();
    }

    /**
     * Merges.
     * @param branchName
     */
    public void merge(String branchName) {
        Commit currCommit = getCurrCommit();
        String cCommitHash = currCommit.getHash();
        HashMap<String, String> currBlobs = currCommit.getBlobs();
        File givenBranch = Utils.join(BRANCHES_DIR, branchName);
        if (!givenBranch.exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        String gCommitHash = Utils.readContentsAsString(givenBranch);
        File gCommitFile = Utils.join(COMMITS_DIR, gCommitHash);
        Commit givenCommit = Utils.readObject(gCommitFile, Commit.class);
        HashMap<String, String> givenBlobs = givenCommit.getBlobs();
        Commit splitPoint = getSplitPoint(givenCommit);
        String sCommitHash = splitPoint.getHash();
        HashMap<String, String> splitBlobs = splitPoint.getBlobs();
        if (cCommitHash.equals(gCommitHash)) {
            System.out.println("Cannot merge a branch with itself.");
        } else if (!stage.getAdded().isEmpty()
                || !stage.getRemoved().isEmpty()) {
            System.out.println("You have uncommitted changes.");
        } else if (untrackedFileHelper(givenBlobs)) {
            return;
        } else if (sCommitHash.equals(gCommitHash)) {
            System.out.println("Given branch is an ancestor of the current "
                    + "branch.");
        } else if (sCommitHash.equals(cCommitHash)) {
            checkout3(branchName);
            System.out.println("Current branch fast-forwarded.");
        } else {
            ArrayList<String> fileNames = getFileNames(currCommit,
                    givenCommit, splitPoint);
            for (String fileName : fileNames) {
                mergeHelper(currBlobs, givenBlobs, splitBlobs,
                        fileName, givenCommit);
            }
            String currBranchName =
                    Utils.readContentsAsString(
                            Utils.join(BRANCHES_DIR, "HEAD"));
            String msg =
                    "Merged " + branchName + " into " + currBranchName + ".";
            commit(msg, givenCommit.getHash());
            if (mergeConflict) {
                System.out.println("Encountered a merge conflict.");
            }
        }
    }

    public void mergeHelper(HashMap<String, String> cBlobs, HashMap<String,
            String> gBlobs, HashMap<String, String> sBlobs,
                            String fName, Commit givenCommit) {
        if (cBlobs.containsKey(fName) && gBlobs.containsKey(fName)
                && sBlobs.containsKey(fName)) {
            String cFileHash = cBlobs.get(fName);
            String gFileHash = gBlobs.get(fName);
            String sFileHash = sBlobs.get(fName);
            if (!gFileHash.equals(sFileHash) && cFileHash.equals(sFileHash)) {
                checkout2(givenCommit.getHash(), fName);
                add(fName);
            } else if (!gFileHash.equals(sFileHash)) {
                if (!gFileHash.equals(cFileHash)) {
                    mergeConflict = true;
                    File cBlobFile = Utils.join(BLOBS_DIR, cFileHash);
                    String cFContents = Utils.readContentsAsString(cBlobFile);
                    File gBlobFile = Utils.join(BLOBS_DIR, gFileHash);
                    String gFContents = Utils.readContentsAsString(gBlobFile);
                    conflictHelper(cFContents, gFContents, fName);
                    add(fName);
                }
            }
        } else if (!cBlobs.containsKey(fName) && gBlobs.containsKey(fName)
                && !sBlobs.containsKey(fName)) {
            checkout2(givenCommit.getHash(), fName);
            add(fName);
        } else if (sBlobs.containsKey(fName) && cBlobs.containsKey(fName)
                && !gBlobs.containsKey(fName)) {
            if (sBlobs.get(fName).equals(cBlobs.get(fName))) {
                rm(fName);
            } else if (!sBlobs.get(fName).equals(cBlobs.get(fName))) {
                mergeConflict = true;
                File cBlobFile = Utils.join(BLOBS_DIR, cBlobs.get(fName));
                String cFileContents =
                        Utils.readContentsAsString(cBlobFile);
                conflictHelper(cFileContents, "", fName);
                add(fName);
            }
        } else if (sBlobs.containsKey(fName) && gBlobs.containsKey(fName)
                && !cBlobs.containsKey(fName)) {
            if (!sBlobs.get(fName).equals(gBlobs.get(fName))) {
                mergeConflict = true;
                File gFile = Utils.join(BLOBS_DIR, gBlobs.get(fName));
                String gFileContents = Utils.readContentsAsString(gFile);
                conflictHelper("", gFileContents, fName);
                add(fName);
            }
        } else if (!sBlobs.containsKey(fName) && gBlobs.containsKey(fName)
                && cBlobs.containsKey(fName)) {
            if (!gBlobs.get(fName).equals(cBlobs.get(fName))) {
                mergeConflict = true;
                File cBlobFile = Utils.join(BLOBS_DIR, cBlobs.get(fName));
                String cFileContents = Utils.readContentsAsString(cBlobFile);
                File gBlobFile = Utils.join(BLOBS_DIR, gBlobs.get(fName));
                String gFileContents = Utils.readContentsAsString(gBlobFile);
                conflictHelper(cFileContents, gFileContents, fName);
                add(fName);
            }
        }

    }

    /**
     * Helper method for merge. Overwrites current file with new merged file.
     * @param cFileContents
     * @param gFileContents
     * @param fileName
     */
    public void conflictHelper(String cFileContents, String gFileContents,
                               String fileName) {
        File f = Utils.join(CWD, fileName);
        String replacement =
                "<<<<<<< HEAD\n" + cFileContents + "=======\n"
                        + gFileContents + ">>>>>>>\n";
        Utils.writeContents(f, replacement);
    }

    /**
     * Helper method for merge. Returns true if there are untracked files.
     * @param givenBlobs
     * @return
     */
    public boolean untrackedFileHelper(HashMap<String, String> givenBlobs) {
        List<String> allFileNames = Utils.plainFilenamesIn(CWD);
        HashMap<String, String> currBlobs = getCurrCommit().getBlobs();
        for (String fileName : allFileNames) {
            if (!currBlobs.containsKey(fileName)
                    && givenBlobs.containsKey(fileName)) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                return true;
            }
        }
        return false;
    }

    /**
     * Returns all file names being tracked by the given commits. Does not
     * contain duplicate file names.
     * @param c1
     * @param c2
     * @param c3
     * @return
     */
    public ArrayList<String> getFileNames(Commit c1, Commit c2, Commit c3) {
        ArrayList<String> fileNames = new ArrayList<String>();
        fileNames.addAll(c1.getBlobs().keySet());
        for (String fileName: c2.getBlobs().keySet()) {
            if (!fileNames.contains(fileName)) {
                fileNames.add(fileName);
            }
        }
        for (String fileName: c3.getBlobs().keySet()) {
            if (!fileNames.contains(fileName)) {
                fileNames.add(fileName);
            }
        }
        return fileNames;
    }

    /**
     * Returns split point commit object of the current commit and given commit.
     * @param givenCommit
     * @return
     */
    public Commit getSplitPoint(Commit givenCommit) {
        Commit currCommit = getCurrCommit();
        ArrayList<Commit> givenQueue = new ArrayList<Commit>();
        ArrayList<String> givenList = new ArrayList<String>();
        givenQueue.add(givenCommit);
        while (!givenQueue.isEmpty()) {
            Commit c = givenQueue.remove(0);
            givenList.add(c.getHash());
            if (!c.getParentHash().equals("")) {
                String parentHash = c.getParentHash();
                File parentFile = Utils.join(COMMITS_DIR, parentHash);
                Commit parent = Utils.readObject(parentFile, Commit.class);
                givenQueue.add(parent);
            }
            if (!c.getMergeParentHash().equals("")) {
                String mParentHash = c.getMergeParentHash();
                File mParentFile = Utils.join(COMMITS_DIR, mParentHash);
                Commit mParent = Utils.readObject(mParentFile, Commit.class);
                givenQueue.add(mParent);
            }
        }
        ArrayList<Commit> currQueue = new ArrayList<Commit>();
        currQueue.add(currCommit);
        while (!currQueue.isEmpty()) {
            Commit c = currQueue.remove(0);
            if (givenList.contains(c.getHash())) {
                return c;
            }
            if (!c.getParentHash().equals("")) {
                String parentHash = c.getParentHash();
                File parentFile = Utils.join(COMMITS_DIR, parentHash);
                Commit parent = Utils.readObject(parentFile, Commit.class);
                currQueue.add(parent);
            }
            if (!c.getMergeParentHash().equals("")) {
                String mParentHash = c.getMergeParentHash();
                File mParentFile = Utils.join(COMMITS_DIR, mParentHash);
                Commit mParent = Utils.readObject(mParentFile, Commit.class);
                currQueue.add(mParent);
            }
        }
        return null;
    }

    /**
     * Unstage the file if it is currently staged for addition. If the file is
     * tracked in the current commit, stage it for removal and remove the
     * file from the working directory if the user has not already done so
     * (do not remove it unless it is tracked in the current commit).
     * @param fileName
     */
    public void rm(String fileName) {
        HashMap<String, String> added = stage.getAdded();
        Commit currCommit = getCurrCommit();
        HashMap<String, String> currBlobs = currCommit.getBlobs();
        if (!added.containsKey(fileName) && !currBlobs.containsKey(fileName)) {
            System.out.println("No reason to remove the file.");
        } else {
            if (added.containsKey(fileName)) {
                added.remove(fileName);
            }
            if (currBlobs.containsKey(fileName)) {
                stage.addToRemoved(fileName, currBlobs.get(fileName));
                File f = Utils.join(CWD, fileName);
                if (f.exists()) {
                    Utils.restrictedDelete(f);
                }
            }
            saveStage();
        }
    }

    /**
     * Like log, except displays information about all commits ever made. The
     * order of the commits does not matter. Hint: there is a useful method
     * in gitlet.Utils that will help you iterate over files within a directory.
     */
    public void globalLog() {
        List<String> allCommits = Utils.plainFilenamesIn(COMMITS_DIR);
        for (String commitFileName: allCommits) {
            File commitFile = Utils.join(COMMITS_DIR, commitFileName);
            Commit c = Utils.readObject(commitFile, Commit.class);
            System.out.println("===");
            System.out.println("commit " + c.getHash());
            System.out.println("Date: " + c.getTimestamp());
            System.out.println(c.getMessage());
            System.out.println();
        }
    }


    /**
     * Prints out the ids of all commits that have the given commit message,
     * one per line. If there are multiple such commits, it prints the ids
     * out on separate lines. The commit message is a single operand; to
     * indicate a multiword message, put the operand in quotation marks,
     * as for the commit command above.
     * @param commitMessage
     */
    public void find(String commitMessage) {
        boolean foundCommit = false;
        List<String> allCommits = Utils.plainFilenamesIn(COMMITS_DIR);
        for (String commitFileName: allCommits) {
            File commitFile = Utils.join(COMMITS_DIR, commitFileName);
            Commit c = Utils.readObject(commitFile, Commit.class);
            if (c.getMessage().equals(commitMessage)) {
                foundCommit = true;
                System.out.println(c.getHash());
            }
        }
        if (!foundCommit) {
            System.out.println("Found no commit with that message.");
        }
    }

    /**
     * Displays what branches currently exist, and marks the current branch with
     * a *. Also displays what files have been staged for addition or removal.
     */
    public void status() {
        System.out.println("=== Branches ===");
        List<String> branchNames = Utils.plainFilenamesIn(BRANCHES_DIR);
        File head = Utils.join(BRANCHES_DIR, "HEAD");
        String masterBranchName = Utils.readContentsAsString(head);
        System.out.println("*" + masterBranchName);
        for (String branchName: branchNames) {
            if (!branchName.equals("HEAD")
                    && !branchName.equals(masterBranchName)) {
                System.out.println(branchName);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        HashMap<String, String> staged = stage.getAdded();
        for (Map.Entry mapElement : staged.entrySet()) {
            String fileName = (String) mapElement.getKey();
            System.out.println(fileName);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        HashMap<String, String> removed = stage.getRemoved();
        for (Map.Entry mapElement : removed.entrySet()) {
            String fileName = (String) mapElement.getKey();
            System.out.println(fileName);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        List<String> fileNames = Utils.plainFilenamesIn(CWD);
        Commit currCommit = getCurrCommit();
        HashMap<String, String> currBlobs = currCommit.getBlobs();
        for (String fileName : fileNames) {
            if (!currBlobs.containsKey(fileName)
                    && !staged.containsKey(fileName)) {
                System.out.println(fileName);
            } else if (removed.containsKey(fileName)) {
                System.out.println(fileName);
            }
        }
    }

    /**
     * Creates a new branch with the given name, and points it at the current
     * head node. A branch is nothing more than a name for a reference (a
     * SHA-1 identifier) to a commit node. This command does NOT immediately
     * switch to the newly created branch (just as in real Git). Before you
     * ever call branch, your code should be running with a default branch
     * called "master".
     * @param branchName
     */
    public void branch(String branchName) {
        File newBranchFile = Utils.join(BRANCHES_DIR, branchName);
        if (newBranchFile.exists()) {
            System.out.println("A branch with that name already exists");
        } else {
            Commit currCommit = getCurrCommit();
            Utils.writeContents(newBranchFile, currCommit.getHash());
        }
    }

    /**
     * Deletes the branch with the given name. This only means to delete the
     * pointer associated with the branch; it does not mean to delete all
     * commits that were created under the branch, or anything like that.
     * @param branchName
     */
    public void rmBranch(String branchName) {
        File branchFile = Utils.join(BRANCHES_DIR, branchName);
        File currBranchFile = getCurrBranch();

        if (!branchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
        } else if (branchFile.equals(currBranchFile)) {
            System.out.println("Cannot remove the current branch.");
        } else {
            branchFile.delete();
        }
    }

    /**
     * Checks out all the files tracked by the given commit. Removes tracked
     * files that are not present in that commit. Also moves the current
     * branch's head to that commit node. See the intro for an example of what
     * happens to the head pointer after using reset. The [commit id] may be
     * abbreviated as for checkout. The staging area is cleared. The command
     * is essentially checkout of an arbitrary commit that also changes the
     * current branch head.
     * @param commitID
     */
    public void reset(String commitID) {
        File commitFile = Utils.join(COMMITS_DIR, commitID);
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
        } else {
            Commit c = Utils.readObject(commitFile, Commit.class);
            HashMap<String, String> blobs = c.getBlobs();
            Commit currCommit = getCurrCommit();
            HashMap<String, String> currBlobs = currCommit.getBlobs();

            List<String> fileNames = Utils.plainFilenamesIn(CWD);
            for (String fileName : fileNames) {
                if (!currBlobs.containsKey(fileName)
                        && blobs.containsKey(fileName)) {
                    System.out.println("There is an untracked file in the "
                            + "way; delete it, or add and commit it first.");
                    return;
                } else if (currBlobs.containsKey(fileName)
                        && !blobs.containsKey(fileName)) {
                    File f = Utils.join(CWD, fileName);
                    Utils.restrictedDelete(f);
                }
            }
            for (Map.Entry mapElement : blobs.entrySet()) {
                String fileName = (String) mapElement.getKey();
                checkout2(commitID, fileName);
            }
            File currBranch = getCurrBranch();
            Utils.writeContents(currBranch, commitID);
            stage.clear();
            saveStage();
        }
    }
}
