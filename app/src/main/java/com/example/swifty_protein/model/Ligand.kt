package com.example.swifty_protein.model

class Atom{
    var id_atoms = ""
    var type = ""
    var x = 0.0
    var y = 0.0
    var z = 0.0
    var charge = 0.0

    fun getId(): String{
        return id_atoms
    }

    fun getType(): String{
        return type
    }
    fun returnCoordinates(): Triple<Double, Double, Double>{
        return Triple(x, y, z)
    }

    fun getCharge(): Double{
        return charge
    }
}

class Bond{
    var AtomOne = Atom()
    var AtomTwo = Atom()
    var order = ""
    var aromatic = Boolean

    fun getAtomOne(): Atom{
        return AtomOne
    }

    fun getAtomTwo(): Atom{
        return AtomTwo
    }

    fun getOrder(): String{
        return order
    }

    fun getAromatic(): Boolean.Companion{
        return aromatic
    }
}

class Ligand {
    var ligands = ""
    var name = ""
    var type = ""
    var formula = ""
    var synonyms = ""
    var weight = 0

    var atoms = listOf<Atom>()
    var bonds = listOf<Bond>()

    var smiles = listOf<String>()
    var inchi = listOf<String>()
    var inchikey = listOf<String>()

}