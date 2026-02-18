import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:swifty_protein/models/user_model.dart';
import 'package:swifty_protein/services/db_helper.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class Window extends CustomPainter {
  @override
  void paint(Canvas canvas, Size size) {

    final rect = Offset.zero & size;
    final radius = Radius.circular(20.0);
    final rrect = RRect.fromRectAndRadius(rect, radius);
    canvas.clipRRect(rrect);
    final borderPaint = Paint()
      ..color = Colors.white
      ..style = PaintingStyle.stroke
      ..strokeWidth = 14.0
      ..maskFilter = MaskFilter.blur(BlurStyle.normal, 10.0);// aggiunge un'ombra sfocata al bordo

    canvas.drawRRect(rrect, borderPaint);
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}

class _HomeScreenState extends State<HomeScreen> {

  final DatabaseHelper _dbhelper = DatabaseHelper.instance; //prendo l'istanza del db, per ora non serve a nel futuro servira'
  User? _user; //variabile di stato per tenere traccia dell'utente loggato, in attesa di implementare la logica completa per prendere l'utente dal db usando l'id
  final prefs = SharedPreferences.getInstance();
  bool _isLoading = true; //variabile di stato per tenere traccia se stiamo ancora caricando i dati dell'utente

Future<void> _loadUser() async {
    try{
      final prefsIns = await prefs;
      var idToLog = prefsIns.getString('loggedInUser');
      if (idToLog == null){
        setState(() {
          _isLoading = false;
          _user = User(id: null, username: 'Nessun utente loggato', password: '');
        });
        return;
      }
      final user = await _dbhelper.getUserById(idToLog);
      if (mounted){
        setState(() {
          _isLoading = false;
          _user = user;
        });
      }
    } catch(e){
      if (mounted){
        setState(() {
          _isLoading = false;
          _user = User(id: null, username: 'Errore nel caricamento dell\'utente', password: '');
        });
      }
    }
  }
  @override
  void initState() {
    super.initState();
    //TODO aggiungere logica
      _loadUser();
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return Scaffold(
        backgroundColor: const Color.fromARGB(255, 202, 14, 14),
        body: const Center(child: CircularProgressIndicator()),
      );
    }
    if (_user == null) {
      return Scaffold(
        backgroundColor: const Color.fromARGB(255, 202, 14, 14),
        body: const Center(child: Text('Utente non trovato')),
      );
    }
    return  Scaffold(
      backgroundColor: const Color.fromARGB(255, 202, 14, 14), //trasparente per mostrare lo sfondo grigio globale definito nel main.dart
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Text('Benvenuto!'),
            const Text('Questa è la home screen.'),
            Text('Utente loggato: ${_user!.username}'),
            CustomPaint(
              size: const Size(300, 200),
              painter: Window(),
            ),
          ],
        )
        
      ),
    );
  }
}