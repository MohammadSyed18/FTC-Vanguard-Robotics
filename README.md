# Vanguard autonomous and mechanism tools

Java components for inspecting autonomous geometry and testing sensor-driven mechanism sequencing without an FTC hardware connection.

```sh
make test
```

| Component | Purpose |
| --- | --- |
| [BezierPath](src/main/java/dev/vanguard/BezierPath.java) | Cubic position/derivative, tangent heading, sampled length, bounded nearest-progress search |
| [RouteDiagnostics](src/main/java/dev/vanguard/RouteDiagnostics.java) | Monotonic route progress and cross-track error from timestamped observations |
| [DebouncedSwitch](src/main/java/dev/vanguard/DebouncedSwitch.java) | Stable touch-sensor state and one rising edge per press |
| [MechanismSequencer](src/main/java/dev/vanguard/MechanismSequencer.java) | Shooter presets, spin-up gate, 50-tick indexing, pusher/retraction timing, index timeout, disarm reset |

The mechanism state machine returns motor, servo, and encoder-target requests; it does not access a hardware map. Holding the feed button does not repeatedly fire. A debounced touch input gates feeding, index motion must reach its target before pushing, and a timeout enters a fault state that requires disarming. Four shooter power presets and bounded turret commands are represented explicitly.

Path projection uses coarse sampling followed by local refinement. It is an approximation and should not be treated as a globally exact projection for self-intersecting curves. The geometry and diagnostic classes are independent of Pedro Pathing so they can run in host tests.

These modules require motor/servo adapters, robot-specific tuning, and physical safety checks before use on hardware. No competition performance is implied by the host tests.
