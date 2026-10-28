# Mellow: Emergency Dispatch Simulator (CIS 2103 OOP Project)

## 1. Project Overview
**Mellow** is a 2D single-player emergency dispatch desktop game written in pure Java (Swing `Graphics2D` custom rendering, zero external libraries or image/audio files) for **CIS 2103 (Object-Oriented Programming)**.

Set in **San Isidro Valley** after a major earthquake, the player operates the emergency radio dispatch console across **3 real-time shifts**:
1. **Shift 1 — Mainshock Night**: Teaches core prioritization, route calculation, and caller calming.
2. **Shift 2 — Aftershocks**: Scheduled seismic tremors shake the screen, block roads with rubble, and spawn sudden emergencies.
3. **Shift 3 — Recovery Day**: High call volume where responder rotation and fatigue management are critical.

---

## 2. How to Compile & Run

### Option A: Double-Click `run.bat` (Windows)
Simply double-click `run.bat` in the project root folder. It automatically detects your installed JDK, compiles all packages into `out/`, and launches the 1280x720 desktop window.

### Option B: Command Line (Standard JDK)
From the project root directory:
```bash
mkdir out
javac -d out -sourcepath src src/Main.java
java -cp out Main
```

### Automated Self-Verification Mode
To run the automated headless verification suite (tests campaign setup, Dijkstra routing, Auto-Match, Save/Load I/O, and Graphics2D rendering):
```bash
java -cp out Main --verify
```

---

## 3. Controls & New Gameplay Improvements

### Mouse Controls
- **Left Panel (Incoming Call Queue)**: Click any `CallCard` to inspect the caller's transcript, panic level, and required responder types.
- **Center Panel (Tactical District Map)**:
  - Click any pulsing `IncidentMarker` or `ZoneView` to select an active emergency.
  - **Live Route Preview & ETA Badge**: When both an Incident and a Responder are selected, the map draws a live dashed cyan path along the shortest unblocked road route and displays the exact travel ETA in seconds (or warns if all routes are blocked).
  - **Flashing Emergency Strobes**: En-route responders flash red/blue emergency strobes as they travel between districts.
- **Right Panel (Responder Roster)**: Click any responder card (`Med-1`, `Res-1`, `Fir-1`, `Eng-1`, `Moto-1`, etc.) to select them and view their live **Effectiveness Match %** against the selected incident.
- **Bottom Bar (Radio Console)**:
  - **DISPATCH [D]**: Calculates the shortest unblocked path via Dijkstra's algorithm and dispatches the selected unit.
  - **AUTO-MATCH [A]**: Automatically selects the highest-effectiveness available responder with a valid route to the selected incident.
  - **CALM CALL [C]**: Occupies the radio briefly to reduce caller panic (revealing clearer location details) and extend the incident deadline.
  - **BREATHING PAUSE [SPACE]**: Opens the 10-second inhale/exhale pacing minigame to reduce Operator Stress and clear radio static.
  - **REST / WAKE UNIT [R]**: Manually toggle a responder into or out of early recovery rest before they hit 100% exhaustion.
- **Top Bar Quick Controls**:
  - **SPEED: 1x / 2x [Tab]**: Toggle between normal speed and 2x fast-forward during quiet periods.
  - **PAUSE [P]**: Tactical pause that lets you inspect active calls and plan routes calmly.
  - **AUDIO: ON / OFF [M]**: Toggle procedural synthesized VHF radio sound effects (`SoundSynthesizer`).
  - **END SHIFT [F5]**: Immediately conclude the current shift to view the debriefing report.

### Keyboard Shortcuts
| Key | Action |
| :--- | :--- |
| **`N`** | Select Next Most Urgent Call |
| **`A`** | Auto-Match Best Available Responder |
| **`D`** | Dispatch Selected Responder to Selected Incident |
| **`C`** | Calm Selected Caller |
| **`R`** | Manually Rest / Wake Selected Responder |
| **`SPACE`** | Start Breathing Pause (Hold to Inhale, Release to Exhale) |
| **`TAB`** | Toggle 1x / 2x Simulation Speed |
| **`P`** | Toggle Tactical Pause |
| **`M`** | Mute / Unmute Synthesized Radio Audio |
| **`ESC`** | Exit Breathing Pause Early |
| **`F5`** | Skip / Complete Current Shift |

---

## 4. Text UML Class Diagram

```text
+---------------------------------------------------------------------------------------------------+
|                                          <<interface>>                                            |
|  Drawable { +draw(Graphics2D) }          Updatable { +update(double) }                            |
|  Dispatchable { +dispatchTo(...) }       Prioritizable extends Comparable<Prioritizable>          |
|  Saveable { +serializeState(), +deserializeState(String) }    GameListener { +on...() }           |
+---------------------------------------------------------------------------------------------------+
         ^                                         ^                                   ^
         | implements                              | implements                        | implements
+----------------------------------+    +------------------------------------+    +-----------------+
|        model.Incident            |    |          model.Responder           |    |   model.Caller  |
|           (abstract)             |    |             (abstract)             |    |    (abstract)   |
+----------------------------------+    +------------------------------------+    +-----------------+
| - id: int                        |    | - id: int                          |    | - name: String  |
| - type: IncidentType             |    | - callSign: String                 |    | - panic: double |
| - zone: Zone                     |    | - status: ResponderStatus          |    | - mood: Mood    |
| - severity: Severity             |    | - fatigueLevel: double             |    +-----------------+
| - caller: Caller                 |    | - homeZone, currentZone: Zone      |    | +buildDialogue()|
+----------------------------------+    +------------------------------------+    +--------+--------+
| +calculateUrgency(): double (abs)|    | +calculateEffectiveness(Inc) (abs) |             |
| +escalate(): void (abs)          |    | +getHandlingTime(Inc): double (abs)|     +-------+-------+
+----------------+-----------------+    +-----------------+------------------+     |       |       |
                 |                                        |                        v       v       v
   +------+------+------+------+            +------+------+------+------+       Resident Tanod  Elderly
   |      |      |      |      |            |      |      |      |      |        Caller  Caller  Caller
   v      v      v      v      v            v      v      v      v      v
Collapsed Fire Medical Blocked Gas        Medic Rescuer Fire- Engineer Tricycle
Building       Emerg.   Road   Leak                    fighter          Driver

+---------------------------------------------------------------------------------------------------+
|                                      ENGINE & DESIGN PATTERNS                                     |
+---------------------------------------------------------------------------------------------------+
|  engine.GameManager (Singleton)                                                                   |
|   +-- has-a --> model.Barangay (contains 6 Zones + 8 Roads)                                       |
|   +-- has-a --> model.Operator (contains model.StressMeter)                                       |
|   +-- has-a --> engine.DispatchCenter (PriorityQueue<Incident>, List<Responder>, RoutePlanner)    |
|   +-- has-a --> engine.ShiftController, engine.IncidentSpawner, engine.AftershockScheduler        |
|   +-- has-a --> engine.ScoreCalculator, List<GameListener> (Observer Pattern)                     |
|   +-- uses  --> factory.IncidentFactory, factory.ResponderFactory (Factory Pattern)               |
|   +-- uses  --> util.FileHandler, util.GameConfig, util.SoundSynthesizer                          |
+---------------------------------------------------------------------------------------------------+
```

---

## 5. Complete OOP Concept Mapping Table (For Project Paper)

| Required OOP Concept | Package & Class | Exact Method / Field / Implementation Details |
| :--- | :--- | :--- |
| **Classes & Objects** | All 8 packages (`46` classes total) | Instantiated across `Barangay`, `Zone`, `Road`, `Operator`, `Incident`, `Responder`, and `Drawable` UI components. |
| **Constructor Overloading** | `model.Zone`, `model.Road`, `model.Operator`, `model.StressMeter`, `model.Incident`, `model.Responder` & all subclasses | Multiple constructors per class providing default vs. custom parameters (e.g., `Road(int, String, Zone, Zone, double)` and `Road(..., boolean, double)`). |
| **Encapsulation & Validation** | `model.StressMeter`, `model.Caller`, `model.Responder`, `model.Road` | All fields are `private`; getters/setters clamp ranges (`Math.max`/`Math.min` for `0..100` panic, stress, and fatigue) and reject `null`/blank inputs. |
| **Inheritance** | `model.Incident` -> 5 subclasses<br>`model.Responder` -> 5 subclasses<br>`model.Caller` -> 3 subclasses | `CollapsedBuilding`, `Fire`, `MedicalEmergency`, `BlockedRoad`, `GasLeak` extend `Incident`; `Medic`, `Rescuer`, `Firefighter`, `Engineer`, `TricycleDriver` extend `Responder`. |
| **Abstraction** | `model.Incident`, `model.Responder`, `model.Caller` | Abstract base classes defining abstract contracts (`calculateUrgency()`, `escalate()`, `calculateEffectiveness()`, `getHandlingTime()`, `buildDialogueLine()`). |
| **Polymorphism (Method Overriding)** | `model.Incident` subclasses & `model.Responder` subclasses | Each incident overrides `calculateUrgency()` and `escalate()`; each responder overrides `calculateEffectiveness(Incident)` and `getHandlingTime(Incident)`. |
| **Polymorphism (Method Overloading)** | `engine.DispatchCenter`, `model.Operator`, `engine.RoutePlanner` | `DispatchCenter.dispatch(Responder, Incident, Operator)`, `dispatch(..., boolean)`, `dispatch(int, int, Operator)`; `Operator.calmCaller(Incident)` and `calmCaller(Incident, double)`. |
| **Interfaces** | `interfaces.Dispatchable`, `Prioritizable`, `Drawable`, `Updatable`, `Saveable`, `GameListener` | `Prioritizable extends Comparable<Prioritizable>`; all 12 visual classes implement `Drawable` and `Updatable`; core models implement `Saveable`. |
| **Composition & Aggregation** | `model.Barangay`, `engine.DispatchCenter`, `model.Operator` | `Barangay` owns `Zone` and `Road` lists; `DispatchCenter` aggregates `Responder` roster and `Incident` queue; `Operator` is composed with `StressMeter`. |
| **Enums** | `enums.IncidentType`, `ResponderType`, `ResponderStatus`, `Severity`, `Mood`, `GameStage`, `IncidentStatus`, `Difficulty` | Rich enums with private fields, constructors, and helper methods (`Severity.escalate()`, `Mood.fromPanicLevel(double)`, `Difficulty.next()`). |
| **Static Members & Constants** | `util.GameConfig`, `util.SoundSynthesizer`, `model.Incident.nextId`, `model.Responder.nextId` | Static configuration constants, difficulty multipliers, procedural audio synthesizer, and auto-incrementing static ID counters. |
| **Custom Checked Exceptions** | `exception.ResponderUnavailableException`, `InvalidDispatchException`, `NoRouteException` | Thrown in `DispatchCenter.dispatch(...)` and `RoutePlanner.findShortestPath(...)`; caught in `GameManager.dispatchSelected()` to report clear radio feedback. |
| **Collections & Generics** | `PriorityQueue<Incident>`, `ArrayList<Responder>`, `HashMap<Integer, List<Road>>`, `Queue<AftershockEvent>` | Used for urgency-sorted call queues, responder rosters, adjacency lists in `Barangay`, and FIFO seismic queues in `AftershockScheduler`. |
| **Shortest-Path Algorithm** | `engine.RoutePlanner.findShortestPath(Zone, Zone, boolean)` | Full **Dijkstra's shortest-path algorithm** over `Barangay`'s road adjacency graph, respecting blocked roads and `TricycleDriver` narrow-alley bypass weights. |
| **Singleton Pattern** | `engine.GameManager.getInstance()` | Thread-safe single instance coordinating all shifts, models, and UI events. |
| **Factory Pattern** | `factory.IncidentFactory`, `factory.ResponderFactory` | Encapsulates polymorphic creation of `Incident`, `Caller`, and `Responder` subclasses per shift. |
| **Observer Pattern** | `interfaces.GameListener`, `engine.GameManager`, `ui.GamePanel` | `GameManager` notifies registered `GameListener` observers when incidents spawn, aftershocks hit, stress changes, or shifts transition. |
| **File I/O** | `util.FileHandler` | Saves and loads campaign state (`rescue_radio_save.txt`) and persistent high score (`rescue_radio_highscore.txt`) using `BufferedReader`/`BufferedWriter`. |
