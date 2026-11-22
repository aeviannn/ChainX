# Java Blockchain Learning Project

A small, beginner-friendly console project based on [Creating Your First Blockchain with Java](https://medium.com/programmers-blockchain/create-simple-blockchain-java-tutorial-from-scratch-6eeed3cb03fa). It keeps the tutorial's main ideas—blocks, SHA-256 hashes, previous-hash links, validation, and proof of work—and adds a menu, JSON save/load, and a tampering demonstration.

This is an educational, single-computer example. It has no peer-to-peer network, consensus between computers, real cryptocurrency, transaction signatures, or production security.

## Requirements

- JDK 17 or newer
- IntelliJ IDEA or Visual Studio Code with Java support
- Maven, or the IDE's bundled Maven, for the Maven commands below

No extra Java libraries are required.

If Java is not installed, install a Windows JDK such as [Eclipse Temurin 17](https://adoptium.net/temurin/releases/?os=windows&arch=x64&package=jdk&version=17) (choose the Windows x64 MSI). To use Maven from PowerShell, follow the [official Maven Windows installation guide](https://maven.apache.org/guides/getting-started/windows-prerequisites.html): extract Maven and add its `bin` folder to your Windows `PATH`. Close and reopen PowerShell, then confirm setup:

```powershell
java -version
javac -version
mvn -version
```

If you use IntelliJ IDEA, it can import the `pom.xml` and run Maven goals through its Maven tool window, so a separate command-line Maven setup may not be needed.

## Open the project

1. Extract or copy the `JavaBlockchain` folder to a convenient location.
2. In IntelliJ IDEA, choose **File → Open** and select this folder. Accept the Maven project import prompt.
3. In VS Code, open this folder and install the **Extension Pack for Java** if prompted. Let the Java language server import the Maven project.
4. Open `src/main/java/edu/college/blockchain/NoobChain.java` and run its `main` method. The IDE will compile the project and start the console menu.

## Run from Windows 11 Terminal

Open PowerShell in the `JavaBlockchain` folder and run:

```powershell
mvn compile exec:java
```

The default mining difficulty is 3 leading zeroes. To try a different value, pass the program argument after `-Dexec.args` (difficulty is limited to 0–6 so the demo stays manageable):

```powershell
mvn compile exec:java "-Dexec.args=--difficulty=2"
```

The program saves to `data\blockchain.json` when you add a block, use **Save now**, or exit. The next launch loads that file automatically. To use a different save path:

```powershell
mvn compile exec:java "-Dexec.args=--data=data\class-demo.json"
```

## Verify the project

Run the built-in smoke checks for hashing, mining, validation, JSON persistence, and tampering:

```powershell
mvn compile exec:java "-Dexec.args=--self-test"
```

Expected result:

```text
All self-checks passed: SHA-256, mining, validation, JSON persistence, tamper detection.
```

## Console menu

- **Add and mine a block** stores a text record and searches for a nonce whose hash has the configured number of leading zeroes.
- **Display the chain** shows every block's data, timestamp, nonce, hash, and previous hash.
- **Validate the chain** recalculates hashes and checks block order, links, and proof of work.
- **Demonstrate tampering** changes one block's data while keeping its old hash, so validation reports the change and broken links.
- **Reload saved chain** reads the JSON file again. Invalid saved data is loaded for inspection and reported by validation.
- **Start a new chain** replaces the current chain with a freshly mined genesis block.

## How the code fits together

| File | Purpose |
| --- | --- |
| `Block.java` | Holds one record, its timestamp, nonce, hash, and link to its predecessor. It also performs proof-of-work mining. |
| `StringUtil.java` | Computes SHA-256 using Java's built-in cryptography API. |
| `BlockchainManager.java` | Creates the genesis block, mines new blocks, resets the chain, and supports the tampering lesson. |
| `BlockchainValidator.java` | Verifies each block's hash, order, previous-hash link, and mining difficulty. |
| `BlockchainJsonStore.java` | Saves and loads blocks in a readable JSON file without external libraries. |
| `NoobChain.java` | Starts the program and provides the interactive menu. |
| `BlockchainSelfTest.java` | Runs quick smoke checks with the `--self-test` argument. |

## Concepts to explain in a presentation

1. A block's hash is recalculated from its index, timestamp, previous hash, nonce, and data.
2. Each block stores the previous block's hash, linking records into a chain.
3. Mining changes the nonce until the hash starts with the selected number of zeroes. Higher difficulty usually means more guesses.
4. Validation recalculates hashes and checks links. Editing old data leaves a mismatch, so tampering becomes visible.
5. This local demo does not make records impossible to change; it demonstrates how changes can be detected. A real distributed blockchain needs network participants and a consensus protocol.
