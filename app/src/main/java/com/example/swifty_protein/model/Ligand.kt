package com.example.swifty_protein.model

class Atom{
    var id_atoms = ""
    var type = ""
    var x = 0.0
    var y = 0.0
    var z = 0.0
    var charge = 0.0

    var ordinal = 0

    fun returnCoordinates(): Triple<Double, Double, Double>{
        return Triple(x, y, z)
    }

}

class Bond{
    var AtomOneId = ""
    var AtomTwoID = ""
    var order = ""
    var aromatic = false

}

class Ligand {
    var id = ""
    var name = ""
    var type = ""
    var formula = ""
    var synonyms = ""
    var weight = 0

    var raw_cif_data = ""
    var atoms = mutableListOf<Atom>()
    var bonds = mutableListOf<Bond>()

    var smiles = mutableListOf<String>()
    var inchi = mutableListOf<String>()
    var inchikey = mutableListOf<String>()

    fun parseCifData() {
        if (raw_cif_data.isEmpty()) return

        atoms.clear()
        bonds.clear()

        val raw = raw_cif_data.lines()
        for (line in raw) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("_") || trimmed == "loop_") continue

            val splitLine = trimmed.split(Regex("\\s+"))
            when (splitLine.size) {
                18 -> parseAtom(splitLine, xIdx = 9, yIdx = 10, zIdx = 11, ordIdx = 17)
                21 -> parseAtom(splitLine, xIdx = 12, yIdx = 13, zIdx = 14, ordIdx = 20)
                7 -> parseBond(splitLine)
            }
        }
    }

    private fun parseAtom(splitLine: List<String>, xIdx: Int, yIdx: Int, zIdx: Int, ordIdx: Int) {
        try {
            val atom = Atom().apply {
                id_atoms = splitLine[1]
                type = splitLine[3]
                charge = splitLine[4].toDoubleOrNull() ?: 0.0
                x = splitLine[xIdx].toDoubleOrNull() ?: 0.0
                y = splitLine[yIdx].toDoubleOrNull() ?: 0.0
                z = splitLine[zIdx].toDoubleOrNull() ?: 0.0
                ordinal = splitLine[ordIdx].toIntOrNull() ?: 0
            }
            atoms.add(atom)
        } catch (e: Exception) {}
    }

    private fun parseBond(splitLine: List<String>) {
        try {
            val bond = Bond().apply {
                AtomOneId = splitLine[1]
                AtomTwoID = splitLine[2]
                order = splitLine[3]
                aromatic = splitLine[4] == "Y"
            }
            bonds.add(bond)
        } catch (e: Exception) {}
    }

}