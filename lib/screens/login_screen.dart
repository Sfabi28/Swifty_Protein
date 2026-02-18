import 'package:flutter/material.dart';
import 'package:local_auth/local_auth.dart';
import 'package:local_auth_android/local_auth_android.dart';
import 'package:local_auth_darwin/local_auth_darwin.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:swifty_protein/models/user_model.dart';
import '../services/db_helper.dart';
import '../services/app_state.dart';

class LoginScreen extends StatefulWidget { // necessario per gestire input utente e biometria
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {

  final DatabaseHelper _dbhelper = DatabaseHelper.instance; //prendo l'istanza del db, per ora non serve a nel futuro servira'
  final prefs =  SharedPreferences.getInstance();

  final LocalAuthentication auth = LocalAuthentication();

  Future<void> _authenticate() async { //funzione per autenticazione con impronta digitale
    bool authenticated = false;
    
    try {
      AppState.ignoreNextResume = true;
      final bool canCheck = await auth.canCheckBiometrics;
      final bool isDeviceSupported = await auth.isDeviceSupported(); //controllo se posso utilizzare la biometri sul dispositivo
      var idToLog = await prefs.then((prefs) => prefs.getString('loggedInUser')); //prendo l'username dell'utente loggato dalle preferenze condivise
      debugPrint('canCheck: $canCheck, isDeviceSupported: $isDeviceSupported, idToLog: $idToLog'); //debug per vedere se posso usare la biometria e quale utente è loggato
      if (!canCheck && !isDeviceSupported || idToLog == null) { //se non posso usarla per qualche motivo allora lo dico in una snackbar (toast)
        AppState.ignoreNextResume = false;
        if (!mounted) return;
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Biometria non disponibile',
              style: TextStyle(color: Colors.white),
            ),
            backgroundColor: Colors.teal,  //colore debug
          ),
        );
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
      Navigator.of(context).pushReplacementNamed('/home').then((_) {
      AppState.ignoreNextResume = false; // reimposta la flag dopo che la pagina è cambiata
      });
    } else {
      AppState.ignoreNextResume = false; // reimposta la flag se non autenticato
    }
  }


  final TextEditingController _nameController = TextEditingController();
  final TextEditingController _passwordController = TextEditingController();

  @override
  void initState() { //quando la finestra viene creata per la prima volta
    super.initState();
  }

  Future <void> _login() async { //funzione per login con username e password
    final username = _nameController.text;
    final password = _passwordController.text;

    if (username.isEmpty || password.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Inserisci username e password', style: TextStyle(color: Colors.white)), backgroundColor: Colors.redAccent),
      );
      return;
    }
    _dbhelper.findUser(username, password).then((user) {
      if (user != null) {
        prefs.then((prefs) {
          prefs.setString('loggedInUser', user.id.toString()); //salvo l'username dell'utente loggato nelle preferenze condivise
        });
        Navigator.of(context).pushReplacementNamed('/home');
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Credenziali non valide', style: TextStyle(color: Colors.white)), backgroundColor: Colors.redAccent),
        );
      }
    });
  }

  Future<void> _sigin() async {
    final username = _nameController.text;
    final password = _passwordController.text;
    if (username.isEmpty || password.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Inserisci username e password', style: TextStyle(color: Colors.white)), backgroundColor: Colors.redAccent),
      );
      return;
    }
    final user = User(username: username, password: password);
      try{
      _dbhelper.registerUser(user).then((id) {
        prefs.then((prefs) {
          prefs.setString('loggedInUser', user.id.toString()); //salvo l'username dell'utente loggato nelle preferenze condivise
        });
        Navigator.of(context).pushReplacementNamed('/home');
      });
      }catch(e){
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Username già esistente', style: TextStyle(color: Colors.white)), backgroundColor: Colors.redAccent),
        );
      }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.transparent,
      body: Center(
        child: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [

              const SizedBox(height: 20),

              Text(
                "Login",
                style: TextStyle(
                  fontSize: 18,
                  color: const Color.fromARGB(179, 207, 58, 58),
                ),
              ),

              const SizedBox(height: 20),

              TextField(
                controller: _nameController,
                decoration: const InputDecoration(
                  hintText: "Username",
                  border: OutlineInputBorder(),
                ),
              ),

              const SizedBox(height: 10),

              TextField(
                controller: _passwordController,
                decoration: const InputDecoration(
                  hintText: "Password",
                  border: OutlineInputBorder(),
                ),
              ),

              TextButton(
                onPressed: () => _login(),
                style: TextButton.styleFrom(
                  backgroundColor: Colors.teal,
                ),
                child: const Text("Login",
                style: TextStyle(color: Colors.white)),
              ),

              TextButton(onPressed: _sigin,
                style: TextButton.styleFrom(
                  backgroundColor: Colors.teal,
                ),
               child: const Text(
                "Registrati", style: TextStyle(color: Colors.white)
               )
              ),

              const SizedBox(height: 30),

              IconButton(
                icon: const Icon(Icons.fingerprint, size: 50),
                padding: const EdgeInsets.all(20),
                onPressed: _authenticate,
                tooltip: 'Accedi con Impronta',
              ),
            ],
          ),
        ),
      ),
    );

  }
}