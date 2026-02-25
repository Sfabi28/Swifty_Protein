# Swifty Protein – Issues Checklist

Questo file contiene una divisione pronta da usare come issue list su GitHub.
Ogni blocco può essere copiato in una singola Issue (oppure gestito direttamente qui come backlog).

## Mandatory (da completare al 100% prima dei bonus)

### M1 — Project bootstrap, permessi e base network
- [ ] Configurare progetto Android con Jetpack Compose (struttura app chiara)
- [ ] Aggiungere permesso Internet in `AndroidManifest.xml`
- [ ] Definire layer base API (client, service, result wrapper)
- [ ] Gestire timeout e mapping errori base

**Acceptance criteria**
- [ ] L’app compila e avvia senza crash
- [ ] Le chiamate rete sono centralizzate in un layer dedicato
- [ ] Errori rete mappati in stato UI leggibile

---

### M2 — App icon e Launch Screen non statica (1–2s)
- [ ] Impostare icona tema scientifico/medicale per le densità richieste
- [ ] Implementare launch screen non percepita come loading infinito
- [ ] Garantire visibilità launch screen per almeno 1–2 secondi

**Acceptance criteria**
- [ ] Icona corretta su launcher
- [ ] All’apertura si vede schermata di avvio coerente e professionale
- [ ] Durata launch verificabile (>=1s, <=2s circa)

---

### M3 — Account sicuro e hashing password
- [ ] Implementare creazione account (username univoco + password)
- [ ] Salvare credenziali in modo sicuro (no plain text)
- [ ] Applicare hashing password (Argon2/bcrypt o storage sicuro platform)

**Acceptance criteria**
- [ ] Nessuna password in chiaro in storage/log
- [ ] Username duplicato rifiutato con errore chiaro
- [ ] Login con credenziali corrette funziona

---

### M4 — Login UI con fallback username/password
- [ ] Creare schermata login/signup con validazioni
- [ ] Integrare BiometricPrompt Android
- [ ] Fallback a username/password se biometria assente o fallisce
- [ ] Nascondere/disabilitare opzione biometrica se non supportata
- [ ] Mostrare alert chiaro su errore biometrico

**Acceptance criteria**
- [ ] Biometria disponibile: login biometrico funziona
- [ ] Biometria non disponibile/fallita: fallback manuale disponibile
- [ ] Messaggi di errore comprensibili all’utente

---

### M5 — Enforcement sicurezza: login a ogni foreground
- [ ] Mostrare Login View a ogni avvio app
- [ ] Mostrare Login View al ritorno da background/home
- [ ] Bloccare accesso diretto ai dati senza ri-autenticazione

**Acceptance criteria**
- [ ] Dopo `onResume` sensibile è sempre richiesto login
- [ ] Nessun bypass della schermata login

---

### M6 — Lista ligandi da `ligands.txt`
- [ ] Leggere e parsare `ligands.txt` (un identificatore per riga)
- [ ] Mostrare lista completa in vista scrollabile
- [ ] Gestire dataset ampio senza lag evidente

**Acceptance criteria**
- [ ] Tutti i ligandi del file risultano visibili
- [ ] Scroll fluido e stabile

---

### M7 — Ricerca realtime case-insensitive
- [ ] Aggiungere search bar senza pulsante “cerca”
- [ ] Filtrare in tempo reale durante digitazione
- [ ] Ricerca case-insensitive su ligand identifier

**Acceptance criteria**
- [ ] Lista aggiornata istantaneamente al cambiare query
- [ ] Risultati coerenti con maiuscole/minuscole

---

### M8 — Fetch CIF, loading state e gestione errori
- [ ] Al tap su ligando mostrare loading indicator
- [ ] Fetch da `https://files.rcsb.org/ligands/view/{ligand}.cif`
- [ ] Gestire errori con alert chiari: offline, 404, timeout, parse
- [ ] Nascondere loader sia su successo sia su errore

**Acceptance criteria**
- [ ] Loader visibile per tutta la durata richiesta
- [ ] Ogni errore principale ha messaggio user-friendly

---

### M9 — Parser CIF asincrono e data model molecolare
- [ ] Parsare file `.cif` in background
- [ ] Estrarre coordinate atomiche ed elenco legami
- [ ] Definire data model robusto (atomi, legami, relazioni spaziali)

**Acceptance criteria**
- [ ] Parsing non blocca UI
- [ ] Strutture dati sufficienti per rendering 3D
- [ ] Errori parsing gestiti con stato UI dedicato

---

### M10 — Rendering 3D Ball-and-Stick + CPK
- [ ] Implementare renderer 3D integrato app (no Unity/Unreal)
- [ ] Rappresentare atomi come sfere e legami come cilindri
- [ ] Applicare colori CPK standard (C, H, O, N, S, P + altri)
- [ ] Impostare camera iniziale e lighting adeguati

**Acceptance criteria**
- [ ] Molecola leggibile e corretta visivamente
- [ ] Colori CPK rispettati
- [ ] Camera iniziale mostra intera molecola

---

### M11 — Interazioni 3D: rotate/zoom/pan + atom info
- [ ] Drag per rotazione fluida
- [ ] Pinch per zoom
- [ ] Two-finger drag per pan
- [ ] Tap su atomo mostra info (simbolo elemento)
- [ ] Tooltip/popup si chiude al tap altrove o su altro atomo

**Acceptance criteria**
- [ ] Interazioni reattive (target 60 FPS)
- [ ] Atom picking affidabile

---

### M12 — Share screenshot della vista 3D
- [ ] Aggiungere pulsante Share nella Protein View
- [ ] Catturare screenshot dello stato corrente 3D
- [ ] Aprire share sheet nativo Android

**Acceptance criteria**
- [ ] Screenshot condivisibile su app esterne
- [ ] Nessun crash durante export/share

---

### M13 — Hardening mandatory e QA su device reale
- [ ] Test end-to-end flussi principali su device reale
- [ ] Verificare assenza crash/freeze/errori bloccanti
- [ ] Verificare sicurezza login e storage
- [ ] Verificare performance e fluidità interazioni 3D

**Acceptance criteria**
- [ ] Mandatory completo e stabile
- [ ] Nessun requisito mandatory mancante

---

## Bonus (solo dopo mandatory perfetto)

### B1 — Multiple visualization models
- [ ] Aggiungere switch modello in tempo reale
- [ ] Implementare Space-Filling (CPK)
- [ ] Implementare Wireframe
- [ ] Implementare Stick model

### B2 — Advanced UI e Dark Mode
- [ ] Celle lista custom
- [ ] Animazioni/transizioni fluide
- [ ] Onboarding primo avvio
- [ ] Dark mode completa inclusa vista 3D (luci adattate)

### B3 — Enhanced molecular interactions
- [ ] Highlight atomi per stesso elemento
- [ ] Info legami (tipo/lunghezza)
- [ ] Strumenti misura distanze/angoli
- [ ] Double-tap per center camera

### B4 — Performance & caching
- [ ] Cache locale file CIF (supporto offline)
- [ ] Parsing in background con progress
- [ ] Lazy loading lista ligandi
- [ ] Ottimizzazioni memoria per molecole grandi

### B5 — Extended export & social
- [ ] Messaggio share personalizzato (ligando/formula/atomi)
- [ ] Export multipli (PNG/JPEG)
- [ ] Registrazione video rotazione
- [ ] Sistema preferiti
- [ ] Comparison view (2 molecole)

---

## Milestone suggerite
- [ ] **Milestone 1:** M1–M5 (Security/Login foundation)
- [ ] **Milestone 2:** M6–M9 (Data pipeline: list/search/fetch/parse)
- [ ] **Milestone 3:** M10–M12 (3D rendering + interaction + share)
- [ ] **Milestone 4:** M13 (stabilizzazione mandatory)
- [ ] **Milestone 5:** B1–B5 (bonus)
