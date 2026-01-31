# Swifty Protein - Stato del Progetto

## ✅ Fatto (Completed)

### 1. Configurazione e Architettura
- [x] Inizializzazione progetto Flutter.
- [x] Blocco orientamento schermo (solo Portrait).
- [x] Struttura cartelle organizzata (`screens`, `services`, `models`).

### 2. Ciclo di Vita e Navigazione (Main)
- [x] Implementazione `WidgetsBindingObserver` nel Main.
- [x] Gestione sicurezza: l'app torna forzatamente alla Login Screen se messa in background (Resumed state).
- [x] Setup della `navigatorKey` globale per gestire il routing fuori dal contesto UI.

### 3. Backend Locale (Database)
- [x] Integrazione pacchetti `sqflite` e `path`.
- [x] Creazione `DatabaseHelper` con pattern Singleton.
- [x] Implementazione query SQL `CREATE TABLE users`.
- [x] Creazione Model `User` con metodi `toMap` e `fromMap`.
- [x] Implementazione funzione di **Registrazione** (INSERT).
- [x] Implementazione funzione di **Login** (SELECT con verifica credenziali).

---

## 📝 Da Fare (To Do)

### 1. Login Screen (UI & Logic)
- [ ] Creare i `TextEditingController` per username e password.
- [ ] Implementare l'interfaccia grafica (TextFields e Bottoni).
- [ ] Collegare i bottoni alle funzioni del Database (Login/Register).
- [ ] Gestire i messaggi di errore (es. "Utente non trovato", "Password errata").
- [ ] Implementare **Autenticazione Biometrica** (FaceID/TouchID) usando `local_auth`.

### 2. Lista Proteine (Ligands List)
- [ ] Creare la schermata della lista post-login.
- [ ] Caricare la lista dei ligandi dal file `ligands.json` (o API fornita).
- [ ] Implementare la **Barra di Ricerca** per filtrare i ligandi.
- [ ] Gestire il tap su un elemento per navigare al dettaglio.

### 3. Visualizzazione 3D (Protein Detail)
- [ ] Creare schermata di dettaglio.
- [ ] Recuperare il file `.pdb` della proteina selezionata.
- [ ] Implementare il visualizzatore 3D (plugin grafico).
- [ ] Aggiungere funzionalità di condivisione dello screenshot della proteina.

### 4. Bonus
- [ ] Seconda visuale delle proteine

### 5. Rifiniture
- [ ] Styling avanzato (Tema scuro/chiaro coerente).
- [ ] Pulizia del codice e rimozione debug print.