# Gitlet Design Document
author: Hovan Liu

## Design Document Guidelines

Please use the following format for your Gitlet design document. Your design
document should be written in markdown, a language that allows you to nicely 
format and style a text file. Organize your design document in a way that 
will make it easy for you or a course-staff member to read.  

## 1. Classes and Data Structures

Include here any class definitions. For each class list the instance
variables and static variables (if any). Include a ***brief description***
of each variable and its purpose in the class. Your explanations in
this section should be as concise as possible. Leave the full
explanation to the following sections. You may cut this section short
if you find your document is too wordy.

### File Structure
#### Blobs folder
* blob's name = SHA1 of file contents
  * byte[] serializedFile = Utils.readContents(file);
  * String fileHash = Utils.sha1(serializedFile);
* blob's contents = serialized file contents
  * byte[] serializedFile = Utils.readContents(file);

#### Commits folder
* commit's name = SHA1 of commit object
  * commit.getHash();
* commit's contents = serialized commit object
  * File commitFile = Utils.join(COMMIT_DIR, commit.getHash);
  * Utils.writeObject(commitFile, commit);

#### Branches folder
* branch's name = branch name (eg. "master")
* branch's contents = SHA1 of commit object (name of commit file)

#### Head File
* head's name = "HEAD"
* head's contents = name of current branch







###Main
Handles command inputs: init, add, commit, rm, log, global-log, checkout, 
find, status, branch, rm-branch, merge

###Repo
Central directory structure/repository that contains commits. Keeps
track of commit versions.

####Instance Variables
* CWD
* .gitlet directory
* commits directory
* blobs directory
* HEAD pointer

####Methods
* init - initializes gitlet repository inside CWD
* add - adds file to staging area to be committed. 
  * If identical file exists, do not stage to be added,
remove from staging area
  * If identical file does not exist, overwrite previous entry
with new contents
  * 
* commit - saves blobs in staging area
* checkout - changes HEAD pointer
* log - 

###Commit
Contains files and metadata of commit

####Instance Variables
* Parent - hashcode of parent
* Message - message of commit
* Timestamp
* Blobs - HashMap to keep track of <File name, Hashcode>

####Methods
* getParent
* getMessage
* getTimestamp
* getBlobs - returns HashMap of blobs


###StagingArea
Contains files that should be tracked in next commit

####Instance Variables
* added - HashMap to keep track of <File name, Hashcode>
* 

####Methods
* clear - clears staging area after commit


## 2. Algorithms

This is where you tell us how your code works. For each class, include
a high-level description of the methods in that class. That is, do not
include a line-by-line breakdown of your code, but something you would
write in a javadoc comment above a method, ***including any edge cases
you are accounting for***. We have read the project spec too, so make
sure you do not repeat or rephrase what is stated there.  This should
be a description of how your code accomplishes what is stated in the
spec.


The length of this section depends on the complexity of the task and
the complexity of your design. However, simple explanations are
preferred. Here are some formatting tips:

* For complex tasks, like determining merge conflicts, we recommend
  that you split the task into parts. Describe your algorithm for each
  part in a separate section. Start with the simplest component and
  build up your design, one piece at a time. For example, your
  algorithms section for Merge Conflicts could have sections for:

   * Checking if a merge is necessary.
   * Determining which files (if any) have a conflict.
   * Representing the conflict in the file.
  
* Try to clearly mark titles or names of classes with white space or
  some other symbols.

Algorithm for determining merge conflicts:

## 3. Persistence

Describe your strategy for ensuring that you don’t lose the state of your program
across multiple runs. Here are some tips for writing this section:

* This section should be structured as a list of all the times you
  will need to record the state of the program or files. For each
  case, you must prove that your design ensures correct behavior. For
  example, explain how you intend to make sure that after we call
       `java gitlet.Main add wug.txt`,
  on the next execution of
       `java gitlet.Main commit -m “modify wug.txt”`, 
  the correct commit will be made.
  
* A good strategy for reasoning about persistence is to identify which
  pieces of data are needed across multiple calls to Gitlet. Then,
  prove that the data remains consistent for all future calls.
  
* This section should also include a description of your .gitlet
  directory and any files or subdirectories you intend on including
  there.



## 4. Design Diagram

Attach a picture of your design diagram illustrating the structure of your
classes and data structures. The design diagram should make it easy to 
visualize the structure and workflow of your program.

