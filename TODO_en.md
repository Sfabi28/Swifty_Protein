# Swifty Protein – Issues Checklist

This file contains a GitHub-ready issue breakdown.
Each block can be copied into a single Issue (or managed directly here as a backlog).

## Mandatory (must be 100% complete before bonuses)

### M1 — Project bootstrap, permissions, and network foundation
- [ ] Configure Android project with Jetpack Compose (clear app structure)
- [ ] Add Internet permission in `AndroidManifest.xml`
- [ ] Define base API layer (client, service, result wrapper)
- [ ] Handle timeouts and base error mapping

**Acceptance criteria**
- [ ] App builds and launches without crashes
- [ ] Network calls are centralized in a dedicated layer
- [ ] Network errors are mapped to readable UI state

---

### M2 — App icon and non-static Launch Screen (1–2s)
- [ ] Set app icon with scientific/medical theme for required densities
- [ ] Implement launch screen that does not feel like infinite loading
- [ ] Ensure launch screen remains visible for at least 1–2 seconds

**Acceptance criteria**
- [ ] Correct icon shown in launcher
- [ ] On app start, launch screen is consistent and professional
- [ ] Launch duration is verifiable (>=1s, around <=2s)

---

### M3 — Secure account system and password hashing
- [ ] Implement account creation (unique username + password)
- [ ] Store credentials securely (no plain text)
- [ ] Apply password hashing (Argon2/bcrypt or secure platform storage)

**Acceptance criteria**
- [ ] No plain-text passwords in storage/logs
- [ ] Duplicate username is rejected with clear error
- [ ] Login with valid credentials works

---

### M4 — Login UI with username/password fallback
- [ ] Create login/signup screens with validations
- [ ] Integrate Android BiometricPrompt
- [ ] Provide username/password fallback if biometrics unavailable or fail
- [ ] Hide/disable biometric option when not supported
- [ ] Show clear alert message on biometric errors

**Acceptance criteria**
- [ ] Biometrics available: biometric login works
- [ ] Biometrics unavailable/failed: manual fallback is available
- [ ] Error messages are understandable for users

---

### M5 — Security enforcement: login on every foreground
- [ ] Show Login View on every app launch
- [ ] Show Login View when returning from background/home
- [ ] Block direct access to sensitive data without re-authentication

**Acceptance criteria**
- [ ] After sensitive `onResume`, login is always required
- [ ] No bypass of the login screen

---

### M6 — Ligand list from `ligands.txt`
- [ ] Read and parse `ligands.txt` (one identifier per line)
- [ ] Display full list in a scrollable view
- [ ] Handle large datasets without noticeable lag

**Acceptance criteria**
- [ ] All ligands from the file are visible
- [ ] Scrolling is smooth and stable

---

### M7 — Real-time case-insensitive search
- [ ] Add a search bar without a “search” button
- [ ] Filter in real time while typing
- [ ] Case-insensitive search on ligand identifiers

**Acceptance criteria**
- [ ] List updates instantly when query changes
- [ ] Results are correct regardless of letter case

---

### M8 — CIF fetch, loading state, and error handling
- [ ] On ligand tap, show loading indicator
- [ ] Fetch from `https://files.rcsb.org/ligands/view/{ligand}.cif`
- [ ] Handle errors with clear alerts: offline, 404, timeout, parse
- [ ] Hide loader on both success and failure

**Acceptance criteria**
- [ ] Loader is visible for the whole loading process
- [ ] Every main error has a user-friendly message

---

### M9 — Async CIF parser and molecular data model
- [ ] Parse `.cif` in background
- [ ] Extract atomic coordinates and bond list
- [ ] Define robust data models (atoms, bonds, spatial relationships)

**Acceptance criteria**
- [ ] Parsing does not block UI
- [ ] Data structures are sufficient for 3D rendering
- [ ] Parsing errors are handled with dedicated UI state

---

### M10 — 3D Ball-and-Stick rendering + CPK colors
- [ ] Implement 3D renderer integrated in app (no Unity/Unreal)
- [ ] Represent atoms as spheres and bonds as cylinders
- [ ] Apply standard CPK colors (C, H, O, N, S, P + others)
- [ ] Set proper initial camera and lighting

**Acceptance criteria**
- [ ] Molecule is readable and visually correct
- [ ] CPK colors are respected
- [ ] Initial camera shows the full molecule

---

### M11 — 3D interactions: rotate/zoom/pan + atom info
- [ ] Drag for smooth rotation
- [ ] Pinch for zoom
- [ ] Two-finger drag for pan
- [ ] Tap atom to show info (element symbol)
- [ ] Tooltip/popup closes on outside tap or another atom tap

**Acceptance criteria**
- [ ] Interactions are responsive (60 FPS target)
- [ ] Atom picking is reliable

---

### M12 — Share screenshot from 3D view
- [ ] Add Share button in Protein View
- [ ] Capture screenshot of current 3D state
- [ ] Open native Android share sheet

**Acceptance criteria**
- [ ] Screenshot can be shared to external apps
- [ ] No crash during export/share

---

### M13 — Mandatory hardening and QA on real device
- [ ] Run end-to-end tests for main flows on real device
- [ ] Verify absence of crashes/freezes/blocking errors
- [ ] Verify login/storage security
- [ ] Verify performance and smooth 3D interactions

**Acceptance criteria**
- [ ] Mandatory scope is complete and stable
- [ ] No missing mandatory requirements

---

## Bonus (only after mandatory is perfect)

### B1 — Multiple visualization models
- [ ] Add real-time model switch control
- [ ] Implement Space-Filling (CPK)
- [ ] Implement Wireframe
- [ ] Implement Stick model

### B2 — Advanced UI and Dark Mode
- [ ] Custom list cells
- [ ] Smooth animations/transitions
- [ ] First-launch onboarding
- [ ] Full dark mode including 3D view (adjusted lighting)

### B3 — Enhanced molecular interactions
- [ ] Highlight atoms of the same element
- [ ] Bond info (type/length)
- [ ] Distance/angle measurement tools
- [ ] Double-tap to center camera

### B4 — Performance and caching
- [ ] Local CIF caching (offline support)
- [ ] Background parsing with progress
- [ ] Lazy loading ligand list
- [ ] Memory optimizations for large molecules

### B5 — Extended export and social
- [ ] Custom share message (ligand/formula/atom count)
- [ ] Multiple export formats (PNG/JPEG)
- [ ] Rotating molecule video recording
- [ ] Favorites system
- [ ] Comparison view (2 molecules)

---

## Suggested milestones
- [ ] **Milestone 1:** M1–M5 (Security/Login foundation)
- [ ] **Milestone 2:** M6–M9 (Data pipeline: list/search/fetch/parse)
- [ ] **Milestone 3:** M10–M12 (3D rendering + interaction + share)
- [ ] **Milestone 4:** M13 (Mandatory stabilization)
- [ ] **Milestone 5:** B1–B5 (Bonus)
