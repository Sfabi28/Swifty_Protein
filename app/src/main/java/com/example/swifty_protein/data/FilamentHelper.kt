package com.example.swifty_protein.data

import android.content.Context
import android.view.Choreographer
import android.view.SurfaceView
import androidx.core.text.color
import com.example.swifty_protein.model.Atom
import com.example.swifty_protein.model.Ligand
import com.google.android.filament.*
import com.google.android.filament.gltfio.AssetLoader
import com.google.android.filament.gltfio.UbershaderProvider
import com.google.android.filament.utils.ModelViewer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sqrt
import com.google.android.filament.gltfio.ResourceLoader
import com.google.android.filament.utils.Mat4
import com.google.android.filament.utils.Float3
import kotlin.math.pow

class FilamentHelper(val context: Context, val surfaceView: SurfaceView, val ligand: Ligand) {


    companion object {
        init {
            System.loadLibrary("filament-jni")
            System.loadLibrary("gltfio-jni")
            System.loadLibrary("filamat-jni")
            System.loadLibrary("filament-utils-jni")
        }
    }

    val modelViewer = ModelViewer(surfaceView)
    private var customCamera: Camera? = null

    private val lightEntity = EntityManager.get().create()
    private var isRendering = false

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (isRendering) {
                Choreographer.getInstance().postFrameCallback(this)
                updateOrbitalCamera()
//                setupFixedCamera(30.0)
                if (cameraUpdateNeeded) {
//                    updateOrbitalCamera()
                    cameraUpdateNeeded = false
                }
                modelViewer.render(frameTimeNanos)
            }
        }
    }

    fun startRendering() {
        if (!isRendering) {
            isRendering = true
            val engine = modelViewer.engine
            val scene = modelViewer.scene

            val cameraEntity = EntityManager.get().create()
            customCamera = engine.createCamera(cameraEntity)
            modelViewer.view.camera = customCamera

            val skybox = Skybox.Builder()
                .color(0.1f, 0.1f, 0.1f, 1.0f)
                .build(engine)
            scene.skybox = skybox

            // SOLO IBL, nessuna luce direzionale
            val ibl = IndirectLight.Builder()
                .intensity(100000.0f)
                .build(engine)
            scene.indirectLight = ibl

            loadLigand(engine, scene)
            Choreographer.getInstance().postFrameCallback(frameCallback)
        }
    }



    private var atomMap = mutableMapOf<Int, Atom>()
    private var assetLoader: AssetLoader? = null
    private var resourceLoader: ResourceLoader? = null

    // Variabili di controllo facilmente modificabili
    private val ATOM_SCALE = 0.3f        // grandezza delle sfere
    private var rootEntity: Int = 0
    var cameraDistance = 0.0

    private fun setUpAtom(atoms: MutableList<Atom>, engine: Engine, scene: Scene) {
        android.util.Log.d("FILAMENT_ATOMS", "numero atomi: ${atoms.size}")

        // Calcola il centro PRIMA di posizionare gli atomi
        val centerX = atoms.map { it.x }.average().toFloat()
        val centerY = atoms.map { it.y }.average().toFloat()
        val centerZ = atoms.map { it.z }.average().toFloat()

        var minX = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var minY = Float.MAX_VALUE
        var maxY = Float.MIN_VALUE
        var minZ = Float.MAX_VALUE
        var maxZ = Float.MIN_VALUE

        val assetLoader = AssetLoader(engine, UbershaderProvider(engine), EntityManager.get())
        val resourceLoader = ResourceLoader(engine)
        val tm = engine.transformManager

        rootEntity = EntityManager.get().create()
        tm.create(rootEntity)
        scene.addEntity(rootEntity)

        for (atom in atoms) {
            val atomAsset = readAsset("ball.glb")
            val filamentAsset = assetLoader.createAsset(atomAsset) ?: continue
            resourceLoader.loadResources(filamentAsset)
            val color = atom.returnColor()
            scene.addEntities(filamentAsset.entities)

            filamentAsset.entities.forEach { entity ->
                applyColorToEntity(engine, entity, color)
            }

            val instance = tm.getInstance(filamentAsset.root)

            // Sottrai il centro → ogni atomo è relativo all'origine
            val x = (atom.x - centerX).toFloat()
            val y = (atom.y - centerY).toFloat()
            val z = (atom.z - centerZ).toFloat()

            if (x - ATOM_SCALE < minX) minX = x - ATOM_SCALE
            if (x + ATOM_SCALE > maxX) maxX = x + ATOM_SCALE
            if (y - ATOM_SCALE < minY) minY = y - ATOM_SCALE
            if (y + ATOM_SCALE > maxY) maxY = y + ATOM_SCALE
            if (z - ATOM_SCALE < minZ) minZ = z - ATOM_SCALE
            if (z + ATOM_SCALE > maxZ) maxZ = z + ATOM_SCALE

            val transform = floatArrayOf(
                ATOM_SCALE, 0f, 0f, 0f,
                0f, ATOM_SCALE, 0f, 0f,
                0f, 0f, ATOM_SCALE, 0f,
                x, y, z, 1f
            )
            tm.setTransform(instance, transform)
            tm.setParent(instance, tm.getInstance(rootEntity))
            atomMap[filamentAsset.root] = atom
        }
        cameraDistance =  calculateOptimalDistance(minX, maxX, minY, maxY, minZ, maxZ)
        orbitRadius = cameraDistance.toFloat()
    }

    private fun applyColorToEntity(engine: Engine, entity: Int, color: FloatArray) {
        val rm = engine.renderableManager
        val instance = rm.getInstance(entity)
        if (instance != 0) {
            val materialInstance = rm.getMaterialInstanceAt(instance, 0)
            materialInstance.setParameter("baseColorFactor", color[0], color[1], color[2], 1.0f)
            materialInstance.setParameter("metallicFactor", 0.0f)
            materialInstance.setParameter("roughnessFactor", 1.0f)
            materialInstance.setParameter("emissiveFactor", color[0], color[1], color[2], 0.75f)

            rm.setCastShadows(instance, false)
            rm.setReceiveShadows(instance, false)
        }
    }

    private fun calculateOptimalDistance(
        minX: Float, maxX: Float,
        minY: Float, maxY: Float,
        minZ: Float, maxZ: Float
    ): Double {
        val width = (maxX - minX)
        val height = (maxY - minY)
        val depth = (maxZ - minZ)

        // Troviamo la dimensione massima dell'oggetto (diagonale o lato maggiore)
        val maxDimension = maxOf(width, height).toDouble()

        // FOV verticale impostato in setupFixedCamera è 45 gradi
        val fovInRadians = Math.toRadians(45.0)

        // Calcoliamo la distanza necessaria affinché l'altezza (o larghezza)
        // dell'oggetto rientri nel FOV.
        // Formula: distanza = (dimensione / 2) / tan(fov / 2)
        val distance = (maxDimension / 2.0) / Math.tan(fovInRadians / 2.0)

        // Aggiungi un margine di sicurezza (es. 20%) e tieni conto della profondità dell'oggetto
        return (distance + (depth / 2.0)) * 1.2
    }

    private fun updateOrbitalCamera() {
        val width = surfaceView.width.takeIf { it > 0 } ?: return
        val height = surfaceView.height.takeIf { it > 0 } ?: return

        val radTheta = Math.toRadians(orbitTheta.toDouble())
        val radPhi = Math.toRadians(orbitPhi.toDouble().coerceIn(-89.0, 89.0))

        val camX = orbitRadius * kotlin.math.cos(radPhi) * kotlin.math.sin(radTheta)
        val camY = orbitRadius * kotlin.math.sin(radPhi)
        val camZ = orbitRadius * kotlin.math.cos(radPhi) * kotlin.math.cos(radTheta)

        val camera = customCamera ?: return

        camera.lookAt(camX, camY, camZ, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0)

        val aspect = width.toDouble() / height.toDouble()
        val near = (orbitRadius * 0.01).coerceAtLeast(0.1)
        val far = orbitRadius * 10.0
        camera.setProjection(45.0, aspect, near, far, Camera.Fov.VERTICAL)
    }

    fun onTouchEvent(event: android.view.MotionEvent) {
        when (event.actionMasked) {
            android.view.MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
            }
            android.view.MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.pointerCount == 2) {
                    lastTouchX = event.getX(0); lastTouchY = event.getY(0)
                    lastTouchX2 = event.getX(1); lastTouchY2 = event.getY(1)
                }
            }
            android.view.MotionEvent.ACTION_MOVE -> {
                when (event.pointerCount) {
                    1 -> handleOrbit(event)
                    2 -> handleZoom(event)
                }
            }
        }
    }

    private fun handleOrbit(event: android.view.MotionEvent) {
        val dx = event.x - lastTouchX
        val dy = event.y - lastTouchY
        orbitTheta -= dx * ORBIT_SENSITIVITY
        orbitPhi += dy * ORBIT_SENSITIVITY
        orbitPhi = orbitPhi.coerceIn(-89f, 89f)
        lastTouchX = event.x
        lastTouchY = event.y
        // NO updateOrbitalCamera() qui
    }

    private fun handleZoom(event: android.view.MotionEvent) {
        val newX1 = event.getX(0); val newY1 = event.getY(0)
        val newX2 = event.getX(1); val newY2 = event.getY(1)

        val oldDist = sqrt(((lastTouchX2 - lastTouchX).pow(2) + (lastTouchY2 - lastTouchY).pow(2)).toDouble()).toFloat()
        val newDist = sqrt(((newX2 - newX1).pow(2) + (newY2 - newY1).pow(2)).toDouble()).toFloat()

        if (oldDist > 0) {
            orbitRadius -= (newDist - oldDist) * ZOOM_SENSITIVITY * orbitRadius
            orbitRadius = orbitRadius.coerceIn(ZOOM_MIN, ZOOM_MAX)
        }

        lastTouchX = newX1; lastTouchY = newY1
        lastTouchX2 = newX2; lastTouchY2 = newY2
        // NO updateOrbitalCamera() qui
    }


    private var orbitTheta = 0f      // angolo orizzontale (azimuth)
    private var orbitPhi = 45f       // angolo verticale (elevation)
    private var orbitRadius = 5f     // distanza dal centro
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var lastTouchX2 = 0f
    private var lastTouchY2 = 0f

    // Sensibilità
    private val ORBIT_SENSITIVITY = 0.3f
    private val ZOOM_SENSITIVITY = 0.005f
    private val ZOOM_MIN = 1f
    private val ZOOM_MAX = 50f

    private fun loadLigand(engine: Engine, scene: Scene) {
        try {
            setUpAtom(ligand.atoms, engine, scene)
            setUpBond(ligand, engine, scene)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setUpBond(ligand: Ligand, engine: Engine, scene: Scene) {
        val centerX = ligand.atoms.map { it.x }.average().toFloat()
        val centerY = ligand.atoms.map { it.y }.average().toFloat()
        val centerZ = ligand.atoms.map { it.z }.average().toFloat()

        val assetLoader = AssetLoader(engine, UbershaderProvider(engine), EntityManager.get())
        val resourceLoader = ResourceLoader(engine)
        val tm = engine.transformManager

        for (bond in ligand.bonds) {
            val a1 = bond.AtomOne ?: continue
            val a2 = bond.AtomTwo ?: continue

            val x1 = (a1.x - centerX).toFloat()
            val y1 = (a1.y - centerY).toFloat()
            val z1 = (a1.z - centerZ).toFloat()
            val x2 = (a2.x - centerX).toFloat()
            val y2 = (a2.y - centerY).toFloat()
            val z2 = (a2.z - centerZ).toFloat()

            val length = bond.getLength().toFloat()
            if (length < 0.001f) continue

            // Calcola il numero di cilindri e i loro offset laterali
            val bondCount = when (bond.order) {
                "SING" -> 1
                "DOUB" -> 2
                "TRIP" -> 3
                else -> 1
            }
            val offsetDistance = 0.08f // distanza laterale tra i cilindri

            // Calcola asse perpendicolare al bond per l'offset
            val dx = x2 - x1; val dy = y2 - y1; val dz = z2 - z1
            val yAxis = floatArrayOf(dx / length, dy / length, dz / length)
            val worldUp = if (Math.abs(yAxis[1]) < 0.99f) floatArrayOf(0f, 1f, 0f) else floatArrayOf(1f, 0f, 0f)
            val offsetAxis = cross(worldUp, yAxis).normalized() // asse perpendicolare

            // Offset per ogni cilindro:
            // singolo  → [0]         (centrato)
            // doppio   → [-0.5, +0.5] (due cilindri separati)
            // triplo   → [-1, 0, +1]  (tre cilindri)
            val offsets = when (bondCount) {
                1 -> listOf(0f)
                2 -> listOf(1f, -1f)
                3 -> listOf(-1f, 0f, 1f)
                else -> listOf(0f)
            }

            for (offset in offsets) {
                val ox = offsetAxis[0] * offsetDistance * offset
                val oy = offsetAxis[1] * offsetDistance * offset
                val oz = offsetAxis[2] * offsetDistance * offset

                val bondAsset = readAsset("bond.glb")
                val filamentAsset = assetLoader.createAsset(bondAsset) ?: continue
                resourceLoader.loadResources(filamentAsset)
                scene.addEntities(filamentAsset.entities)
                filamentAsset.entities.forEach { entity ->
                    applyBondStyle(engine, entity)
                }

                val instance = tm.getInstance(filamentAsset.root)
                tm.setTransform(instance, buildBondTransform(
                    x1 + ox, y1 + oy, z1 + oz,
                    x2 + ox, y2 + oy, z2 + oz,
                    length
                ))
                tm.setParent(instance, tm.getInstance(rootEntity))
            }
        }
    }

    private fun buildBondTransform(
        x1: Float, y1: Float, z1: Float,
        x2: Float, y2: Float, z2: Float,
        length: Float
    ): FloatArray {
        val dx = x2 - x1
        val dy = y2 - y1
        val dz = z2 - z1

        val yAxis = floatArrayOf(dx / length, dy / length, dz / length)
        val worldUp = if (Math.abs(yAxis[1]) < 0.99f) floatArrayOf(0f, 1f, 0f) else floatArrayOf(1f, 0f, 0f)
        val xAxis = cross(worldUp, yAxis).normalized()
        val zAxis = cross(xAxis, yAxis).normalized()

        val cx = (x1 + x2) / 2f
        val cy = (y1 + y2) / 2f
        val cz = (z1 + z2) / 2f

        // Sottrai il raggio degli atomi da entrambe le estremità
        val atomRadius = ATOM_SCALE  // 0.3f
        val adjustedLength = (length - atomRadius * 2f).coerceAtLeast(0.01f)

        val scaleXZ = 0.1f
        return floatArrayOf(
            xAxis[0] * scaleXZ,          xAxis[1] * scaleXZ,          xAxis[2] * scaleXZ,          0f,
            yAxis[0] * adjustedLength,   yAxis[1] * adjustedLength,   yAxis[2] * adjustedLength,   0f,
            zAxis[0] * scaleXZ,          zAxis[1] * scaleXZ,          zAxis[2] * scaleXZ,          0f,
            cx, cy, cz, 1f
        )
    }

    private fun applyBondStyle(engine: Engine, entity: Int) {
        val rm = engine.renderableManager
        val instance = rm.getInstance(entity)
        if (instance != 0) {
            val materialInstance = rm.getMaterialInstanceAt(instance, 0)
            // Colore grigio per i bond
            materialInstance.setParameter("baseColorFactor", 0.7f, 0.7f, 0.7f, 1.0f)
            materialInstance.setParameter("metallicFactor", 0.0f)
            materialInstance.setParameter("roughnessFactor", 1.0f)
            materialInstance.setParameter("emissiveFactor", 0.7f, 0.7f, 0.7f, 0.75f)
            rm.setCastShadows(instance, false)
            rm.setReceiveShadows(instance, false)
        }
    }

    // Utility
    private fun cross(a: FloatArray, b: FloatArray): FloatArray {
        return floatArrayOf(
            a[1]*b[2] - a[2]*b[1],
            a[2]*b[0] - a[0]*b[2],
            a[0]*b[1] - a[1]*b[0]
        )
    }

    private fun FloatArray.normalized(): FloatArray {
        val len = sqrt((this[0]*this[0] + this[1]*this[1] + this[2]*this[2]).toDouble()).toFloat()
        return if (len > 0.0001f) floatArrayOf(this[0]/len, this[1]/len, this[2]/len) else this
    }

    private fun readAsset(assetName: String): ByteBuffer {
        context.assets.open(assetName).use { input ->
            val bytes = input.readBytes()
            val buffer = ByteBuffer.allocateDirect(bytes.size)
            buffer.order(ByteOrder.nativeOrder())
            buffer.put(bytes)
            buffer.rewind()
            return buffer
        }
    }

    private var cameraUpdateNeeded = false

    fun destroy() {
        isRendering = false
        Choreographer.getInstance().removeFrameCallback(frameCallback)

        val engine = modelViewer.engine
        assetLoader?.destroy()
        resourceLoader?.destroy()
        // Distruggi la luce
        engine.destroyEntity(lightEntity)
        EntityManager.get().destroy(lightEntity)

        // Distruggi gli atomi nella mappa
        atomMap.keys.forEach { instance ->
            engine.destroyEntity(instance)
            EntityManager.get().destroy(instance)
        }
        atomMap.clear()

        // Distruggi skybox
        modelViewer.scene.skybox?.let { engine.destroySkybox(it) }

        // Distruggi il motore
        engine.destroy()
    }
}