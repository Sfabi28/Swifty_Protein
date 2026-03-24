package com.example.swifty_protein.model

import android.graphics.Color

val atomColors = mapOf(
        "H"  to Color.WHITE,
        "C"  to Color.BLACK,
        "N"  to Color.rgb(48, 80, 248),
        "O"  to Color.RED,
        "F"  to Color.GREEN,
        "CL" to Color.GREEN,
        "BR" to Color.rgb(165, 42, 42),
        "I"  to Color.rgb(160, 32, 240),
        "HE" to Color.CYAN,
        "P"  to Color.rgb(255, 165, 0),
        "S"  to Color.YELLOW,
        "B"  to Color.rgb(255, 170, 119),
        "LI" to Color.rgb(119, 0, 255),
        "NA" to Color.rgb(119, 0, 255),
        "K"  to Color.rgb(119, 0, 255),
        "FE" to Color.rgb(255, 140, 0),
        "MG" to Color.rgb(0, 128, 0),
        "CA" to Color.GRAY,
        "ZN" to Color.rgb(125, 128, 176),
        "CU" to Color.rgb(200, 128, 51)
)

class Atom{
    constructor()
    constructor(id_atoms: String, type: String, x: Double, y: Double, z: Double, charge: Double, ordinal: Int)
    {
        this.id_atoms = id_atoms
        this.type = type
        this.x = x
        this.y = y
        this.z = z
        this.charge = charge
        this.ordinal = ordinal
    }
    var id_atoms = ""
    var type = ""
    var x = 0.0
    var y = 0.0
    var z = 0.0
    var charge = 0.0

    var ordinal = 0

    fun returnColor(): FloatArray {
        val element = type.takeWhile { it.isLetter() }.uppercase()
        val color = atomColors[element] ?: Color.MAGENTA
        return floatArrayOf(
            Color.red(color) / 255f,
            Color.green(color) / 255f,
            Color.blue(color) / 255f,
            1.0f
        )
    }

}

class Bond{
    var AtomOne: Atom? = null
    var AtomTwo: Atom? = null
    var order = ""
    var aromatic = false

    fun getLength(): Double {
        if (AtomOne == null || AtomTwo == null) throw Exception("Atoms not set")
        val dx = AtomOne!!.x - AtomTwo!!.x
        val dy = AtomOne!!.y - AtomTwo!!.y
        val dz = AtomOne!!.z - AtomTwo!!.z
        return kotlin.math.sqrt(dx * dx + dy * dy + dz * dz)
    }

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

    private fun getAtom(id: String): Atom? {
        return atoms.find { it.id_atoms == id }
    }

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

                AtomOne = getAtom(splitLine[1])
                AtomTwo = getAtom(splitLine[2])
                order = splitLine[3]
                aromatic = splitLine[4] == "Y"
            }
            bonds.add(bond)
        } catch (e: Exception) {}
    }

}