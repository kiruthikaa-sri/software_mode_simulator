# Real-Time System Control Plane & Chaos Telemetry Suite

A high-concurrency desktop simulator built in pure Java that models dynamic runtime configuration, operational guardrails, chaos engineering fault injection, and live telemetry observability.

---

## Key Features

- **Environment & Feature Flag Management:** Switch between runtime modes (`Stable`, `Experimental`, `Debug`, and dynamic custom profiles) while managing granular system feature flags.
- **Operational Safety Guardrails:** Dynamic rule verification engine preventing unsafe administrative operations in production environments.
- **Chaos Engineering & Resilience Engine:** Background fault injection thread (Chaos Monkey) simulating node drops (`HTTP 500`), throttling (`HTTP 429`), and latency spikes.
- **Live Observability Dashboard:** Real-time progress gauges tracking CPU load, memory dump allocation, and request latency.
- **Audit Logging & State Replay:** Command-pattern-driven bidirectional undo/redo stacks.
- **Profile Persistence:** Import and export active runtime configurations to disk.

---

## Technical Highlights & Architecture

| Concept | Implementation Details |
| :--- | :--- |
| **Design Patterns** | **Observer Pattern** for decoupled UI updates; **Command Pattern** for bidirectional undo/redo stacks. |
| **Multithreading** | **Daemon Worker Thread** (`TrafficSimulator`) executing asynchronous request cycles with volatile flags and synchronization. |
| **Data Structures** | **Circular Ring Buffer** (`LogBuffer`) for fixed-memory log streaming ($O(1)$ appends) and dual `ArrayDeque` stacks. |
| **Persistence** | Flat-file configuration serialization using Java `BufferedReader` and `BufferedWriter`. |

---

## Getting Started

### Prerequisites
- Java Development Kit (JDK 17 or higher recommended)

### Compile & Run

1. Clone the repository:
   ```bash
   git clone https://github.com/https://github.com/kiruthikaa-sri/software_mode_simulator.git
   cd software_mode_simulator