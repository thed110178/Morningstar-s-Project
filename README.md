# Soul Knight & POBG Top-Down Roguelite Shooter

A 2D Top-Down Roguelite Shooter game built with **Pure Java 21** and **Java Swing / Graphics2D**, inspired by **Soul Knight** and **POBG (PlayerOmmni's Battlegrounds)**.

---

## 🎮 Key Features
* **Full 360° Mouse Aiming & WASD Movement**: Responsive controls with smooth rotation and crosshair targeting.
* **Diverse Arsenal**: Pistols, Shotguns, Assault Rifles with unique recoil, spread, and projectile velocity.
* **Roguelite Mechanics**: Level up perks (choose 1 of 3 random upgrade cards), procedural dungeon/waves, and permadeath run progression.
* **Solid Software Architecture**:
  * **Multithreaded Game Loop**: Fixed 60 FPS update tick with decoupled rendering and delta time.
  * **Design Patterns**: State Pattern, Factory Pattern, Strategy Pattern, Observer Pattern.
  * **Performance Optimization**: Spatial Partitioning Grid for collision detection ($O(N)$ efficiency).
  * **File I/O**: High score persistence and save/load system.
* **Unit Testing**: Complete test coverage using JUnit 5.

---

## 🛠️ Requirements & Tech Stack
* **Java**: JDK 21+
* **Build Tool**: Apache Maven 3.8+
* **Framework**: Java Swing / Graphics2D (Zero external game engine dependencies)
* **Testing**: JUnit 5 (Jupiter)

---

## 🚀 How to Build and Run

### 1. Compile the Project
```bash
mvn clean compile
```

### 2. Run the Game
```bash
mvn exec:java
```

Or package as a standalone executable JAR:
```bash
mvn clean package
java -jar target/soul-knight-pobg-shooter-1.0.0-SNAPSHOT-jar-with-dependencies.jar
```

### 3. Run Unit Tests
```bash
mvn test
```

---

## 🕹️ Controls
* **W, A, S, D**: Move Player (Up, Left, Down, Right)
* **Mouse Movement**: 360° Weapon Aiming & Crosshair
* **Left Mouse Button**: Shoot
* **R**: Reload current weapon
* **Space**: Dash / Dodge Roll
* **1, 2, Q**: Switch Weapons
* **Esc**: Pause Game / Open Options Menu
