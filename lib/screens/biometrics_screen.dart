import 'package:flutter/material.dart';
import 'package:local_auth/local_auth.dart';
import 'package:local_auth_android/local_auth_android.dart';
import 'package:local_auth_darwin/local_auth_darwin.dart';

class BiometricsScreen extends StatefulWidget { // necessario per gestire biometria
  const BiometricsScreen({super.key});

  @override
  State<BiometricsScreen> createState() => _BiometricsScreenState();
}

class _BiometricsScreenState extends State<BiometricsScreen> {

  final LocalAuthentication auth = LocalAuthentication();

  @override
  void initState() {
    super.initState();
  }

  Future<bool> _canAuthenticate() async {
    final bool canAuthenticateWithBiometrics = await auth.canCheckBiometrics;
    final bool canAuthenticate = canAuthenticateWithBiometrics || await auth.isDeviceSupported();
    return canAuthenticate;
  }

  Future<bool> _authenticate() async {
    final bool didAuthenticate = await auth.authenticate(
      localizedReason: 'Please authenticate to show account balance',
      authMessages: <AuthMessages>[
        AndroidAuthMessages(
          signInTitle: 'Oops! Biometric authentication required!',
          cancelButton: 'No thanks',
        ),
        IOSAuthMessages(cancelButton: 'No thanks'),
      ],
    );
    return didAuthenticate;
  }

  @override
  Widget build(BuildContext context) {
    return const Scaffold(
      backgroundColor: Colors.transparent, //trasparente per mostrare lo sfondo grigio globale definito nel main.dart
      body: Center(
        child: Text(
          "Biometric Screen",
          style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
        ),
      ),
    );
  }
}