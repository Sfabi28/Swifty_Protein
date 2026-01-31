class User {
  final int? id; // nullable perché quando lo crei non ha ancora un ID dal DB
  final String username;
  final String password;

  const User({
    this.id, 
    required this.username, 
    required this.password
  });

  // metodo per convertire User -> Map (per l'INSERT nel DB)
  Map<String, dynamic> toMap() {
    return {
      'id': id, // se è null, SQLite lo ignora e usa l'autoincrement
      'username': username,
      'password': password,
    };
  }

  // costruttore per convertire Map -> User (per la SELECT dal DB)
  factory User.fromMap(Map<String, dynamic> map) {
    return User(
      id: map['id'],
      username: map['username'],
      password: map['password'],
    );
  }
}