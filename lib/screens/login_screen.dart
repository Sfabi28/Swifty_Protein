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

  final DatabaseHelper _dbhelper = DatabaseHelper.instance; //prendo l'istanza del db, per ora non serve a nel futuro servira'
  final _prefs =  SharedPreferences.getInstance();

  final LocalAuthentication auth = LocalAuthentication();
  final _authSubject = PublishSubject<void>();
  late StreamSubscription<void> _authSubscription;

  bool _isSnackbarActive = false; // Semaforo per la SnackBar

  @override
  void dispose() {
    _authSubscription.cancel(); // FONDAMENTALE: Cancella la subscription
    _authSubject.close();
    _nameController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

 void _showMessage(String message, {bool isError = false}) {
  if (!mounted) return;
  
  if (_isSnackbarActive) return;

  _isSnackbarActive = true;

  var controller = ScaffoldMessenger.of(context).showSnackBar(
    SnackBar(
      content: Text(message, style: const TextStyle(color: Colors.white)),
      backgroundColor: isError ? Colors.redAccent : Colors.teal,
      duration: const Duration(seconds: 2),
    ),
  );

  controller.closed.then((_) {
    if (mounted) {
      _isSnackbarActive = false;
    }
  });
}

  Future<void> _authenticate() async {
    bool authenticated = false;

    try {
      AppState.ignoreNextResume = true;
      final bool canCheck = await auth.canCheckBiometrics;
      final bool isDeviceSupported = await auth.isDeviceSupported();
      
      final prefs = await _prefs; // Await pulito
      var idToLog = prefs.getString('loggedInUser');

      if (!canCheck || !isDeviceSupported || idToLog == null) {
        AppState.ignoreNextResume = false;
        _showMessage('Biometria non disponibile o utente non trovato', isError: true);
        return; // Il Future finisce qui, exhaustMap si sblocca
      }

      authenticated = await auth.authenticate(
        localizedReason: 'Autenticati per accedere',
        options: const AuthenticationOptions(
          stickyAuth: true,
          biometricOnly: true,
        ),
        authMessages: const <AuthMessages>[
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
      debugPrint("Errore biometria: $e");
      _showMessage('Errore: $e', isError: true);
      AppState.ignoreNextResume = false;
      return;
    }

    if (authenticated && mounted) {
      Navigator.of(context).pushReplacementNamed('/home').then((_) {
        AppState.ignoreNextResume = false;
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
    
    _authSubscription = _authSubject
      .exhaustMap((_) => Stream.fromFuture(_authenticate()))
      .listen(
        (_) {}, 
        onError: (e) {
          debugPrint("Errore durante l'autenticazione: $e");
        }
      );
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
      backgroundColor: Colors.transparent, // Assicurati di avere un contenitore colorato sotto o sembrerà nero
      body: Center(
        child: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
               const SizedBox(height: 20),
               const Text("Login", style: TextStyle(fontSize: 18, color: Color.fromARGB(179, 207, 58, 58))),
               const SizedBox(height: 20),
               SizedBox(
                width: 300,
                child: Column(
                  children: [
                    TextField(controller: _nameController, decoration: const InputDecoration(hintText: "Username", border: OutlineInputBorder())),
                    TextField(controller: _passwordController, decoration: const InputDecoration(hintText: "Password", border: OutlineInputBorder())),
                  ]
                )       
              ),
              TextButton(
                onPressed: _login, // Nota: non serve la lambda () => _login()
                style: TextButton.styleFrom(backgroundColor: Colors.teal),
                child: const Text("Login", style: TextStyle(color: Colors.white)),
              ),
              TextButton(
                onPressed: _signIn,
                style: TextButton.styleFrom(backgroundColor: Colors.teal),
               child: const Text("Registrati", style: TextStyle(color: Colors.white))
              ),
              const SizedBox(height: 30),
              
              // Tasto Biometria collegato al Subject
              IconButton(
                icon: const Icon(Icons.fingerprint, size: 50),
                padding: const EdgeInsets.all(20),
                onPressed: _onAuthenticatePressed, // Triggera l'evento
                tooltip: 'Accedi con Impronta',
              ),
            ],
          ),
        ),
      ),
    );
  }
}