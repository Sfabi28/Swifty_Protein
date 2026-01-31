import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';

class DatabaseHelper {
  static final DatabaseHelper instance = DatabaseHelper._init(); //unica istanza del db che useremo in tutta l'app

  DatabaseHelper._init(); //costruttore

  static Database? _database;

  Future<Database> get database async { // getter del database
    // se esiste già, ritornalo subito
    if (_database != null) return _database!;

    // se non esiste viene inizializzato (apri il file e crea tabelle)
    _database = await _initDB('swifty_protein.db');
    return _database!;
  }

  Future<Database> _initDB(String filePath) async { // inizializzazione del database
    // trova il percorso sicuro dove Android salva i dati
    final dbPath = await getDatabasesPath();
    final path = join(dbPath, filePath);

    // apre il database. Se è la prima volta lo crea
    return await openDatabase(path, version: 1, onCreate: _createDB);
  }

  Future _createDB(Database db, int version) async { // prima creazione del database
    const userTable = '''
      CREATE TABLE users (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        username TEXT UNIQUE,
        password TEXT
      )
    ''';
    
    await db.execute(userTable);
  }

  Future<int> registerUser(String username, String password) async { // aggiunta di un utente al db
    final db = await instance.database;
    
    final data = {
      'username': username,
      'password': password, 
    };

    return await db.insert('users', data);
  }
}