import 'package:flutter/material.dart';
import 'package:local_auth/local_auth.dart';
import 'package:local_auth_android/local_auth_android.dart';
import 'package:local_auth_darwin/local_auth_darwin.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:swifty_protein/models/user_model.dart';
import '../services/db_helper.dart';
import '../services/app_state.dart';
import 'package:rxdart/rxdart.dart';
import 'dart:async';

class LoginScreen extends StatefulWidget { // necessario per gestire input utente e biometria
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {

  bool showFingerprintBtn = false;

  Future<bool> _checkSupport() async {
      final bool canCheck = await auth.canCheckBiometrics;
      final bool isDeviceSupported = await auth.isDeviceSupported();
      
      final prefs = await _prefs; // Await pulito
      var idToLog = prefs.getString('loggedInUser');

      if (!canCheck || !isDeviceSupported) { //se non posso usarla per qualche motivo allora lo dico in una snackbar (toast)
        if (!mounted) return(false);
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

      authenticated = await auth.authenticate(
        localizedReason: 'Autenticati per accedere',
        options: const AuthenticationOptions(
          stickyAuth: true,
          biometricOnly: true,
        ),
        authMessages: const <AuthMessages>[
          AndroidAuthMessages(
            signInTitle: 'Accedi con impronta',
            cancelButton: 'Annulla',
          ),
          IOSAuthMessages(
            cancelButton: 'Annulla',
          ),
        ],
      );
    } catch (e) {
      debugPrint("Errore biometria: $e");
      _showMessage('Errore: $e', isError: true);
      AppState.ignoreNextResume = false;
      return;
    }

    if (authenticated && mounted) { //se autenticazione funziona allora passo alla home, probabilmente da cambiare per dire a quale user accedere
      Navigator.of(context).pushReplacementNamed('/home').then((_) { //TODO login con determinato utente
      AppState.ignoreNextResume = false; // reimposta la flag dopo che la pagina è cambiata
      });
    } else {
      AppState.ignoreNextResume = false;
    }
  }


  final TextEditingController _nameController = TextEditingController();
  final TextEditingController _passwordController = TextEditingController();

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

  void _onAuthenticatePressed() {
    _authSubject.add(null); //quando premo il bottone per autenticazione aggiungo un evento al subject
  }

  Future <void> _login() async { //funzione per login con username e password
    final username = _nameController.text;
    final password = _passwordController.text;

    if (username.isEmpty || password.isEmpty) {
      _showMessage('Inserisci username e password', isError: true);
      return;
    }
    _dbhelper.findUser(username, password).then((user) {
      if (user != null) {
        _prefs.then((prefs) {
          prefs.setString('loggedInUser', user.id.toString()); //salvo l'username dell'utente loggato nelle preferenze condivise
        });
        if (!mounted) {return;}
        Navigator.of(context).pushReplacementNamed('/home');
      } else {
        if (!mounted) {return;}
        _showMessage( 'Credenziali non valide', isError: true);
      }
    });
  }

  Future<void> _signIn() async {
    final username = _nameController.text;
    final password = _passwordController.text;
    if (username.isEmpty || password.isEmpty) {
      _showMessage('Inserisci username e password', isError: true);
      return;
    }
    final user = User(username: username, password: password);
      try{
        _dbhelper.registerUser(user).then((id) {
        _prefs.then((prefs) {
          prefs.setString('loggedInUser', id.toString()); //salvo l'username dell'utente loggato nelle preferenze condivise
        });
        if (!mounted) {return;}
        Navigator.of(context).pushReplacementNamed('/home');
      });
      }catch(e){
        if (!mounted) {return;}
        _showMessage('Errore durante la registrazione', isError: true);
      }
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