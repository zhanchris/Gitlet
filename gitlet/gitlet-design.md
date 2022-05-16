# Gitlet Design Document
author: Chris Zhan

## Design Document Guidelines

Please use the following format for your Gitlet design document. Your design
document should be written in markdown, a language that allows you to nicely 
format and style a text file. Organize your design document in a way that 
will make it easy for you or a course-staff member to read.  

## 1. Classes and Data Structures

### Main
This will be the primary class that runs user arguments in the command
line. 

**Gitlet:** A static variable that contains the gitlet object initialized
by the user.

### Gitlet:
This class will create the .gitlet directory and create the CommitTree needed
to store all our commits.

**stage**: A field that represents the current files that are "added"

**currentWorkingDirectory**: A field that represents the current directory that
Gitlet is initalized in.

**CommitTree**: A field that holds the CommitTree of this Gitlet

### CommitTree
This class will consist of a tree data structure that holds all the commits. 
Each commit will be stored inside a HashMap that 
uses the SHA-1 hash values as the key, and the values are pointers
to the files in that commit. 

**head**: A pointer that tells us which commit we are currently viewing
in the commit tree.

**branches**: A List that contains the various branches of the 
CommitTree, so that we are able to go between them.

**commits**: A HashMap that contains pointers to the files in that commit, where 
the key is the SHA-1 hash value.

### FileSerializer (Extends serializable)
This class will take care of serializing 

**stage**: This is a field that contains a list of files we will
need to add to a commit.

**add**: A boolean instance field that lets us know if we're adding
or not.

**file**: A blob instance that contains a file that needs to be serialized.

### Blob
A class that represents an individual file object that we are working with.
It will have fields that represent the contents of the file as well
as a pointer to where that file is located. 



## 2. Algorithms


**Main**:  
init - creates a new Gitlet()  
add - calls Gitlet.add()  
commit - calls Gitlet.commit()
rm - gets Gitlet to remove files  
log - prints the log of all commits so far  
status - prints a message to the user displaying tracked/untracked files  
checkout - reverts the currentWorkingDirectory to the passed in commit hash  
branch - creates a new branch in the CommitTree

**Gitlet**:  
Gitlet(): instantiates a new Gitlet object  
add(): adds a file to stage  
commit(): commits all files on stage into a new node on the CommitTree  
rm - removes specified files
checkout - reverts the CWD to the passed in commit
branch - creates a new branch in the CommitTree

**CommitTree**:  
CommitTree(): initializes a new CommitTree  
addCommit(): Takes all files on the stage, serializes them and places them in
the appropriate node on the CommitTree

**FileSerializer**:  
addFile: serializes a file and its contents into bytes

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

The primary strategy for ensuring persistance is to store each commit
in a subdirectory .gitlet. Upon running Main.commit(), a subdirectory with
the commit hash will be created in the .gitlet directory. Any subsequent commits
will also consist of the same process.

However, upon  branching, we will need to store subsequent commit
directories inside of the commit directory where the branch occurred.
This is probably a bad way of doing this, but until I figure out something
better it will have to do.

Here is everytime I need to record the state of the program or files:

* Whenever I call Main.add()
* Whenever I call Main.commit()
* Whenever I call Main.branch()

After calling `java gitlet.Main add wug.txt`, nothing will happen in the
.gitlet directory. wug.txt will be serialized and added to the stage list.
When `java gitlet.Main commit -m “modify wug.txt”`, the file wug.txt which is 
in the stage list will be assigned a SHA-1 hash. This hash will be the name
of the subdirectory in .gitlet that will store this commit. Therefore
we will be able to access and checkout this previous commit by calling Main.checkout, since
the checkout call will allow us to create a pointer to the .gitlet/commit hash directory and
find the serialized wug.txt.

## 4. Design Diagram





