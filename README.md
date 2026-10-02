Cập nhật mới nhất vào ngày: 02/10/2026

# Project DontKnow - An Unplanned Top-Down Roguelite Shooter

A 2D Top-Down Roguelite Shooter built with **Pure Java 21 LTS** and **Java Swing / Graphics2D**, inspired by the room-clearing dungeon mechanics of **Soul Knight** and the intense 360° tactical combat of **POBG (PlayerOmmni's Battlegrounds)**.

---

## 🎮 Key Features & Highlights
* **Full 360° Omnidirectional Aiming & WASD Locomotion**:
  * Real-time cursor tracking via trigonometric angle calculation ($\theta = \text{atan2}(\Delta y, \Delta x)$).
  * Responsive 8-directional movement with zero typematic repeat delay.
* **Smooth 2D Camera Tracking**:
  * Camera with Linear Interpolation (**Lerp**) smooth follow to eliminate visual stutter during rapid directional shifts.
  * Rigid world boundary clamping within large map coordinates ($3840 \times 2160$ px).
* **Multithreaded Game Loop (60 FPS / 60 UPS)**:
  * Decoupled rendering and physics calculation running on a dedicated worker thread (`Game-Loop-Worker`).
  * Fixed Timestep simulation with real-time FPS and UPS diagnostic tracking.
* **Architectural Excellence & Design Patterns (SOLID)**:
  * **State Pattern**: `GameStateManager` for smooth, decoupled transitions between `MenuState`, `PlayingState`, `PauseState`, `UpgradeState`, and `GameOverState`.
  * **Double Buffering & Anti-Aliasing**: Flawless, tear-free rendering with high-quality font and geometry hints.
* **Comprehensive Documentation & Unit Testing**:
  * Complete code documentation for every class in [`Docs/`](file:///c:/Users/THE%20DUONG/My%20Project/GroupProject/Docs/).
  * Automated testing suite using **JUnit 5 (Jupiter)**.

---

## 🛠️ Tech Stack & Requirements
* **Language**: Java 21 LTS (Oracle OpenJDK 21+)
* **Build System**: Apache Maven 3.8+
* **GUI / Graphics**: Java Swing & Java 2D Graphics (`Graphics2D`, Double-Buffered `JPanel`)
* **Testing Framework**: JUnit 5 (Jupiter 5.10.2)
* **Version Control**: Git / GitHub

---

## 📂 Project Directory Structure

```text
GroupProject/
├── pom.xml                                // Maven build script (Java 21, JUnit 5)
├── README.md                              // Project overview and instructions
├── .agents/                               // Workspace rules and guidelines
├── Docs/                                  // Detailed project documentation
│   ├── Project_Summary.md                 // Architecture and gameplay summary
│   ├── Feature Category/                  // Class & module technical documentation
│   │   ├── Main.md                        // Entry point documentation
│   │   ├── Core/                          // Engine, Loop, Window, Camera docs
│   │   ├── Input/                         // Keyboard and Mouse listener docs
│   │   └── State/                         // Game State Pattern docs
│   ├── PlanForNextFeature/                // Feature roadmaps
│   └── Update/                            // Daily change logs (yy-mm-dd_xx.md)
└── src/
    ├── main/java/com/groupproject/game/   // Application source code
    │   ├── Main.java                      // Application bootstrap (EDT safe)
    │   ├── core/                          // Config, Loop, Window, Panel, Camera
    │   ├── input/                         // Keyboard and Mouse event handlers
    │   └── state/                         // State Pattern classes
    └── test/java/com/groupproject/game/   // JUnit 5 test classes
```

---

## 🚀 How to Build, Run & Test

### 1. Compile the Project
```bash
mvn clean compile
```

### 2. Run the Game
To run immediately via Maven:
```bash
mvn exec:java
```

Or build a standalone executable fat JAR:
```bash
mvn clean package
java -jar target/soul-knight-pobg-shooter-1.0.0-SNAPSHOT-jar-with-dependencies.jar
```

### 3. Run Automated Tests
```bash
mvn test
```

---

## 🕹️ Controls & Keybindings

| Key / Input | Action |
|---|---|
| **W, A, S, D** | Move Operative (Up, Left, Down, Right - 8 Directions) |
| **Mouse Movement** | 360° Weapon Aiming & Crosshair Positioning |
| **Left Mouse Button** | Fire Equipped Weapon (Single click or Automatic hold) |
| **R** | Reload Weapon Magazine |
| **1, 2, Q** | Cycle / Swap Primary & Secondary Firearms |
| **Spacebar** | Tactical Dash / Dodge Roll (Brief i-frame invulnerability) |
| **Esc** | Pause Game / Return to Menu |
| **Enter / Click** | Confirm Menu Selection |

---

## 👥 Team Collaboration & Contributions

| Member | Technical Area | Key Responsibilities |
|---|---|---|
| **Lead / Architecture** | Core Engine & Loop | Threading (`GameLoop`), Windowing, 2D Camera (`Lerp`), State Management |
| **Gameplay & Physics** | Entities & Combat | Player locomotion, 360° firearm logic, projectile simulation, Hitbox collision |
| **AI & Level Design** | Enemies & Dungeon | Enemy behavioral states, pathfinding, procedural room generator |
| **Progression & UI/IO** | Roguelite & Persistence | 3-Card perk upgrade system, HUD, Leaderboard file I/O, JUnit testing |
