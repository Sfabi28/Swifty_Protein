package com.example.swifty_protein.data

import com.example.swifty_protein.model.Ligand

class LigandRepository(private val dbHelper: DbHelper) {

    suspend fun getLigandData(id: String): Resource<Ligand> {
        // 1. Controlla il Database
        val localLigand = dbHelper.getLigand(id)
        if (localLigand != null) {
            return Resource.Success(localLigand)
        }

        // 2. Se non c'è, chiama l'API
        val apiResponse = ApiClient.safeApiCall {
            ApiClient.rcsbService.getLigandCif(id)
        }

        // 3. Gestisci la risposta dell'API
        return when (apiResponse) {
            is Resource.Success -> {
                val parsedLigand = parseCifData(id, apiResponse.data)
                // Salviamo nel DB per la prossima volta
                dbHelper.saveLigand(parsedLigand, parsedLigand.raw_cif_data)
                Resource.Success(parsedLigand)
            }
            is Resource.Error -> Resource.Error(apiResponse.message)
            is Resource.Loading -> Resource.Loading
        }
    }

    private fun parseCifData(id: String, rawData: String): Ligand {
        val ligand = Ligand().apply { 
            this.id = id
            this.smiles = mutableListOf()
            this.inchi = mutableListOf()
            this.inchikey = mutableListOf()
        }
        
        val structuralBuilder = StringBuilder()
        var currentLoop = ""

        rawData.lines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty()) return@forEach

            when {
                trimmed == "loop_" || trimmed == "#" -> currentLoop = ""
                trimmed.startsWith("_chem_comp.name") -> 
                    ligand.name = line.substringAfter("_chem_comp.name").trim()
                trimmed.startsWith("_chem_comp.formula ") -> 
                    ligand.formula = line.substringAfter("_chem_comp.formula").trim().removeSurrounding("'")
                
                // Riconoscimento loop
                trimmed.startsWith("_chem_comp_atom.") -> currentLoop = "ATOM"
                trimmed.startsWith("_chem_comp_bond.") -> currentLoop = "BOND"
                trimmed.startsWith("_pdbx_chem_comp_descriptor.") -> currentLoop = "DESC"

                // Metadati descrittori (SMILES/InChI)
                currentLoop == "DESC" && !trimmed.startsWith("_") -> {
                    val valClean = if (line.contains("\"")) line.substringAfter("\"").substringBeforeLast("\"") else trimmed.split(" ").last()
                    when {
                        trimmed.contains("SMILES") -> (ligand.smiles as MutableList).add(valClean)
                        trimmed.contains("InChI ") -> (ligand.inchi as MutableList).add(valClean)
                        trimmed.contains("InChIKey") -> (ligand.inchikey as MutableList).add(valClean)
                    }
                }
                
                // Atomi e Legami (Raw Data)
                (currentLoop == "ATOM" || currentLoop == "BOND") -> {
                    structuralBuilder.append(line).append("\n")
                }
                trimmed.startsWith("_chem_comp_atom.") || trimmed.startsWith("_chem_comp_bond.") -> {
                     structuralBuilder.append(line).append("\n")
                }
            }
        }
        ligand.raw_cif_data = structuralBuilder.toString()
        return ligand
    }
}
