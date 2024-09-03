# Gitlet - A Version-Control System in Java

## Overview

Gitlet is a lightweight version-control system implemented in Java, modeled after the popular Git version-control system. This project was developed to manage file tracking and restoration, providing essential functionalities similar to those found in Git.

## Features

- **File Tracking and Restoration**: Gitlet allows users to track changes in files and restore previous versions when needed.
- **Hashing Algorithms**: Utilizes hashing to optimize persistence models and ensure efficient file control within the repository.
- **Data Structures**: Employs various data structures such as hashmaps, linked lists, arraylists, and arrays to manage file versions and contents.
- **Core Version-Control Functions**: Implements 13 essential version-control commands including:
  - `init`: Initialize a new Gitlet repository.
  - `add`: Add a file to the staging area.
  - `commit`: Save changes to the repository with a message.
  - `rm`: Remove a file from the staging area and/or the repository.
  - `checkout`: Restore a file or branch to a previous state.
  - `branch`: Create a new branch.
  - `merge`: Merge changes from one branch into another.
  - `log`: Display the commit history.
  - `status`: Show the current status of the repository.

## Implementation Details

- **Codebase**: The project is written in Java and consists of over 1700 lines of code.
- **Hashing**: Hashing is used extensively to manage file versions and ensure data integrity across the system.
- **Persistence Model**: The system is designed to persist file states and metadata efficiently, allowing for quick access and minimal storage overhead.

### Data Structures

Gitlet makes extensive use of Java's built-in data structures to manage its version-control functionalities effectively. Here’s an overview of the primary data structures used:

- **HashMap**: 
  - **Usage**: Central to managing associations between file names and their corresponding content hashes, as well as tracking files staged for addition or removal.
  - **Example**: The `StagingArea` class uses two `HashMap<String, String>` objects, one to track files staged for addition and another for files staged for removal. These mappings allow efficient lookups and updates when files are added, committed, or removed.

- **ArrayList**: 
  - **Usage**: Utilized for dynamic storage of commit history and file names, enabling easy traversal and manipulation of commit data.
  - **Example**: The `getFileNames` method in the `Repo` class returns an `ArrayList<String>` containing all unique file names being tracked by the current and previous commits. This is essential for operations like merging and resetting branches.

- **Serializable Classes**:
  - **Usage**: Classes such as `StagingArea` implement the `Serializable` interface to allow for persistent storage of objects in the file system. This is critical for maintaining the state of the version-control system across sessions.
  - **Example**: The `StagingArea` and `Commit` classes are serialized to save their states in the `.gitlet` directory, allowing for recovery and consistency across different operations.

- **File**: 
  - **Usage**: Represents directories and files within the Gitlet repository. It’s used extensively to manage the file system hierarchy, including working directories, the Gitlet repository, and individual files within commits.
  - **Example**: The `Repo` class defines constants like `CWD` (Current Working Directory) and `GITLET_FOLDER` to represent the base directory of the Gitlet system and the root directory of the repository, respectively.

These data structures work together to create a robust and efficient system for tracking, staging, and committing changes, ensuring the integrity and consistency of the version-control system.
