# CHAINEX — Blockchain Management System

A Java-based blockchain implementation with a desktop graphical interface built using JavaFX. CHAINEX demonstrates the core principles of blockchain technology, including cryptographic hashing, Proof of Work mining, block linking, chain validation, and tampering detection.

The application provides an interactive environment to create blocks, mine them, inspect the blockchain, validate its integrity, and manage blockchain data through JSON storage.

---

## Overview

CHAINEX is an educational blockchain management application designed to demonstrate how a blockchain works internally.

Each block contains transaction data, a timestamp, a nonce, its own cryptographic hash, and the hash of the previous block. Blocks are connected sequentially, forming a chain where modifications to stored data can be detected through validation.

The application combines a Java blockchain backend with a JavaFX desktop interface.

## Features

### 1. Blockchain Explorer
- View all blocks stored in the blockchain.
- Inspect individual block details.
- View block indexes, timestamps, data, nonces, and hashes.
- Examine previous-hash connections.

### 2. Block Creation & Mining
- Create new blocks by entering custom data.
- Mine blocks using the Proof of Work mechanism.
- Generate SHA-256 hashes.
- Maintain connections between consecutive blocks.

### 3. Blockchain Validation
- Verify stored block hashes.
- Check previous-hash connections.
- Validate block indexes.
- Verify Proof of Work requirements.
- Detect unauthorized modifications.

### 4. Tampering Demonstration
- Modify the data of an existing block.
- Observe how changes affect its stored hash.
- Validate the chain to identify integrity violations.

### 5. JSON Storage
- Save the blockchain locally in JSON format.
- Load previously saved blockchain data.
- Reset the blockchain to a new chain.
- Preserve block information across application restarts.

---

## Technology Stack

| Technology | Purpose |
|---|---|
| Java | Core application and blockchain implementation |
| JavaFX | Desktop graphical user interface |
| Maven | Dependency and build management |
| SHA-256 | Cryptographic hashing |
| JSON | Blockchain data persistence |
| Java Collections | Block and chain management |

---

## Project Structure

```text
JavaBlockchain/
│
├── src/
│   └── main/
│       └── java/
│           └── edu/college/blockchain/
│               ├── Block.java
│               ├── StringUtil.java
│               ├── BlockchainManager.java
│               ├── BlockchainValidator.java
│               ├── BlockchainJsonStore.java
│               ├── BlockchainDesktopApp.java
│               ├── NoobChain.java
│               └── BlockchainSelfTest.java
│
├── data/
│   └── blockchain.json
│
├── pom.xml
├── README.md
└── .gitignore
```

*Generated build files and locally stored blockchain data may be excluded from the Git repository.*

---

## How It Works

### Block Structure

Each block maintains the following information:

- **Index:** Position of the block in the chain.
- **Timestamp:** Time at which the block was created.
- **Data:** Information stored inside the block.
- **Previous Hash:** Hash of the preceding block.
- **Hash:** SHA-256 fingerprint of the current block.
- **Nonce:** Value adjusted during mining.

### Proof of Work

CHAINEX implements a basic Proof of Work mechanism.

During mining, the application repeatedly changes the nonce and recalculates the block hash until the hash satisfies the configured difficulty requirement.

For example, with a difficulty of 3, a valid hash must begin with three leading zeroes.

### Blockchain Integrity

Every block references the hash of the previous block.

If the data inside an existing block is modified, its recalculated hash will no longer match its stored hash. This causes blockchain validation to detect the modification.

---

## Installation & Setup

### Prerequisites

- Java Development Kit (JDK) 17 or compatible newer JDK.
- Apache Maven 3.x.
- Git.
- JavaFX dependencies configured through Maven.

### 1. Clone the Repository

```bash
git clone YOUR_GITHUB_REPOSITORY_URL
```

### 2. Navigate to the Project

```bash
cd JavaBlockchain
```

### 3. Compile the Project

```bash
mvn clean compile
```

### 4. Run the Desktop Application

```bash
mvn javafx:run
```

### 5. Run the Blockchain Self-Test

```bash
mvn compile exec:java "-Dexec.args=--self-test"
```

---

## User Interface

The application uses a minimal dark interface with matte-black surfaces, muted burnt-orange accents, and clear status indicators.

Its desktop interface provides access to the blockchain explorer, block creation, validation, tampering demonstration, and JSON storage operations.

---

## Project Objectives

- Understand the fundamental architecture of blockchain technology.
- Implement cryptographic hashing using SHA-256.
- Demonstrate Proof of Work mining.
- Understand how blocks are linked through cryptographic hashes.
- Implement blockchain integrity validation.
- Demonstrate how tampering can be detected.
- Integrate blockchain operations into a JavaFX desktop application.
- Implement persistent blockchain storage using JSON.

---

## Limitations

CHAINEX is an educational blockchain implementation and is not intended for production cryptocurrency or financial applications.

It demonstrates blockchain fundamentals using a locally managed chain. It does not implement distributed peer-to-peer networking, decentralized consensus, cryptocurrency wallets, or digital transaction signatures.

---

## Future Scope

Potential extensions include:
- Peer-to-peer blockchain networking.
- Digital signatures and transaction verification.
- Wallet management.
- Distributed consensus mechanisms.

These are potential extensions and are not part of the current implementation.

---

## License

No license has currently been specified for this project.
