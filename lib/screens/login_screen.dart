import 'package:flutter/material.dart';
import 'package:local_auth/local_auth.dart';
import 'package:local_auth_android/local_auth_android.dart';
import 'package:local_auth_darwin/local_auth_darwin.dart';
import '../services/db_helper.dart';
import '../services/app_state.dart';

class LoginScreen extends StatefulWidget { // necessario per gestire input utente e biometria
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {

  final DatabaseHelper _dbhelper = DatabaseHelper.instance; //TODO login col db

  final LocalAuthentication auth = LocalAuthentication();

  bool showFingerprintBtn = false;

  Future<bool> _checkSupport() async {
      final bool canCheck = await auth.canCheckBiometrics;
      final bool isDeviceSupported = await auth.isDeviceSupported(); //controllo se posso utilizzare la biometri sul dispositivo

      if (!canCheck || !isDeviceSupported) { //se non posso usarla per qualche motivo allora lo dico in una snackbar (toast)
        if (!mounted) return(false);
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Biometria non disponibile su questo dispositivo')),
        );
        return(false);
      }
      return(true);
  }

  Future<void> _authenticate() async { //funzione per autenticazione con impronta digitale
    bool authenticated = false;
    AppState.ignoreNextResume = true;

    try {
      final bool supported = await _checkSupport();
      if (!supported) {
        AppState.ignoreNextResume = false;
        return;
      }

      authenticated = await auth.authenticate( // funzione che autentica l'impronta
        localizedReason: 'Autenticati per accedere',
        options: const AuthenticationOptions(
          stickyAuth: true,
          biometricOnly: true,
        ),
        authMessages: const <AuthMessages>[ //pop-up di autentificazione
          AndroidAuthMessages(
            signInTitle: 'Accesso Biometrico',
            cancelButton: 'Annulla',
          ),
          IOSAuthMessages(
            cancelButton: 'Annulla',
          ),
        ],
      );
    } catch (e) {
      debugPrint("Errore biometria: $e"); //se fallisce allora snackbar
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Errore: $e')),
        );
      }
      AppState.ignoreNextResume = false;
      return;
    }

    if (authenticated && mounted) { //se autenticazione funziona allora passo alla home, probabilmente da cambiare per dire a quale user accedere
      Navigator.of(context).pushReplacementNamed('/home').then((_) { //TODO login con determinato utente
      AppState.ignoreNextResume = false; // reimposta la flag dopo che la pagina è cambiata
      });
    } else {
      AppState.ignoreNextResume = false; // reimposta la flag se non autenticato
    }
  }

  @override
  void initState() { //quando la finestra viene creata per la prima volta
    super.initState();
    _initBiometricSupport();
  }

  Future<void> _initBiometricSupport() async { //controllo iniziale se la biometria e' disponibile sul dispositivo
    bool support = await _checkSupport();
    if (!mounted) return;
    setState(() {
      showFingerprintBtn = support;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.transparent, //trasparente per mostrare lo sfondo grigio globale definito nel main.dart
      body: Align(
        alignment: const Alignment(0, 0.8),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            if (showFingerprintBtn)
              IconButton( //button per impronta digitale
                icon: const Icon(Icons.fingerprint, size: 50),
                onPressed: _authenticate,
                tooltip: 'Accedi con Impronta',
              ),
            const Text(
              "Login Screen",
              style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
            ),
          ],
        ),
      ),
    );
  }
}