import 'package:flutter/material.dart';
import '../services/db_helper.dart';

class LoginScreen extends StatefulWidget { // necessario per gestire input utente e biometria
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {

  final DatabaseHelper _dbhelper = DatabaseHelper.instance; //prendo l'istanza del db

  @override
  void initState() {
    super.initState();
    // qui in futuro inizializzeremo la logica per l'autenticazione locale e biometrica
  }

  @override
  Widget build(BuildContext context) {
    return const Scaffold(
      backgroundColor: Colors.transparent, //trasparente per mostrare lo sfondo grigio globale definito nel main.dart
      body: Center(
        child: Text(
          "Login Screen",
          style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
        ),
      ),
    );
  }
}