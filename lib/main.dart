import 'package:flutter/material.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';
import 'package:flutter/services.dart';
import 'screens/login_screen.dart';
import 'screens/home.dart';
import 'services/app_state.dart';


// chiave globale utilizzata per tornare subito alla schermata di login quando l'app torna dal background
final GlobalKey<NavigatorState> navigatorKey = GlobalKey<NavigatorState>();

void main() async { // entry point dell'applicazione
  WidgetsFlutterBinding.ensureInitialized();
  
  await SystemChrome.setPreferredOrientations([ // lock dello schermo in verticale
    DeviceOrientation.portraitUp,
    DeviceOrientation.portraitDown,
  ]);

  try { // carico le variabili d'ambiente dal file .env
    await dotenv.load(fileName: ".env");
  } catch (e) {
    debugPrint("Errore .env: $e");
  }
  
  runApp(const MyApp()); // avvio l'app
}

class MyApp extends StatefulWidget { // inizializzazione dello StatefulWidget per gestire il ciclo di vita
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

// aggiungo il mixin WidgetsBindingObserver: questo ci permette di "ascoltare" gli eventi di sistema (pausa o chiusura)

class _MyAppState extends State<MyApp> with WidgetsBindingObserver { 
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this); // registra questo widget come osservatore degli eventi
  }

  @override
  void dispose() { // metodo chiamato quando il widget viene distrutto definitivamente
    WidgetsBinding.instance.removeObserver(this); // rimuove l'observer per evitare memory leak
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) { // metodo invocato automaticamente dal sistema al cambio di stato
    // se lo stato è "resumed" (l'utente è tornato sull'app dopo aver premuto Home o cambiato app)
    if (state == AppLifecycleState.resumed) {
      if (AppState.ignoreNextResume) {
        AppState.ignoreNextResume = false;
        return;
      }
      // usa la chiave globale per "resettare" la navigazione e forzare la schermata di Login
      navigatorKey.currentState?.pushAndRemoveUntil(
        MaterialPageRoute(builder: (context) => const LoginScreen()),
        (route) => false, // rimuove tutto lo storico di navigazione precedente
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      navigatorKey: navigatorKey, // collega la chiave globale al navigatore dell'app
      title: 'Swifty Protein',
      debugShowCheckedModeBanner: false,
      
      theme: ThemeData( // configurazione globale dello stile grafico
        useMaterial3: true,
        brightness: Brightness.light,
        scaffoldBackgroundColor: Colors.transparent, //trasparente perché usiamo uno sfondo custom nel builder
        canvasColor: Colors.transparent,
        colorScheme: const ColorScheme.light(
          surface: Colors.transparent,
          primary: Color.fromARGB(255, 255, 255, 255),
        ),
      ),

      builder: (context, child) { // avvolge ogni singola pagina dell'app
        return Stack(
          children: [
            // sfondo grigio fisso che sarà visibile dietro a tutte le pagine
            Container(
              width: double.infinity,
              height: double.infinity,
              color: const Color.fromARGB(255, 255, 255, 255),
            ),
            ?child, // applica ai children solo se esistono
          ],
        );
      },

      initialRoute: '/login',
      routes: {
        '/login': (context) => const LoginScreen(),
        '/home': (context) => const HomeScreen(),
      },
    );
  }
}