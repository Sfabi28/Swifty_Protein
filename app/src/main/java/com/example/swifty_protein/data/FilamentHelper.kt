package com.example.swifty_protein.data

import android.content.Context
import android.view.Choreographer
import android.view.SurfaceView
import com.example.swifty_protein.model.Ligand
import com.google.android.filament.*
import com.google.android.filament.gltfio.AssetLoader
import com.google.android.filament.gltfio.UbershaderProvider
import com.google.android.filament.utils.ModelViewer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sqrt
import com.google.android.filament.gltfio.ResourceLoader

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
    private val lightEntity = EntityManager.get().create()
    private var isRendering = false

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (isRendering) {
                Choreographer.getInstance().postFrameCallback(this)
                modelViewer.render(frameTimeNanos)
            }
        }
    }

    fun startRendering() {
        if (!isRendering) {
            isRendering = true
            val engine = modelViewer.engine
            val scene = modelViewer.scene

            val skybox = Skybox.Builder()
                .color(0.1f, 0.1f, 0.1f, 1.0f)
                .build(engine)
            scene.skybox = skybox

            LightManager.Builder(LightManager.Type.DIRECTIONAL)
                .color(1.0f, 1.0f, 1.0f)
                .intensity(100000.0f)
                .direction(0.0f, -1.0f, -1.0f)
                .build(engine, lightEntity)
            scene.addEntity(lightEntity)

            loadLigand()
            Choreographer.getInstance().postFrameCallback(frameCallback)
        }
    }

    private fun loadLigand() {
        try {
            val sphereBuffer = readAsset("ball.glb")
            if (ligand.atoms.isEmpty()) return

            val engine = modelViewer.engine

            // 1. L'ANCORA: Facciamo caricare al ModelViewer la sfera per configurare Camera e Touch
            modelViewer.loadModelGlb(sphereBuffer)
            val ghostAsset = modelViewer.asset ?: return

            // Nascondiamo questo atomo originale, ci serviva solo per ingannare la telecamera!
            modelViewer.scene.removeEntities(ghostAsset.entities)

            // 2. LA FABBRICA: Creiamo un nostro loader che non cancella la memoria
            sphereBuffer.rewind() // Riavvolgiamo il file letto prima
            val materialProvider = UbershaderProvider(engine)
            val assetLoader = AssetLoader(engine, materialProvider, EntityManager.get())
            val cloneTemplate = assetLoader.createAsset(sphereBuffer) ?: return
//            assetLoader.loadResources(cloneTemplate)
            // FONDAMENTALE: Qui non chiamiamo la cancellazione dei dati, così possiamo clonare!

            val atoms = ligand.atoms
            val avgX = atoms.map { it.x }.average().toFloat()
            val avgY = atoms.map { it.y }.average().toFloat()
            val avgZ = atoms.map { it.z }.average().toFloat()

            // 3. LA NORMALIZZAZIONE: Adattiamo la molecola alla vista del ModelViewer
            val maxDist = atoms.maxOf {
                val dx = it.x.toFloat() - avgX
                val dy = it.y.toFloat() - avgY
                val dz = it.z.toFloat() - avgZ
                sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toFloat()
            }

            // Il ModelViewer inquadra perfettamente un cubo di grandezza 1.0
            // Rimpiccioliamo tutta la molecola in modo che il raggio massimo sia 0.5
            val normalizeScale = 0.5f / maxDist.coerceAtLeast(0.1f)
            val atomScale = 0.15f * normalizeScale // Dimensione delle singole sfere

            // 4. LA CLONAZIONE: Ora partiamo da 0 e cloniamo TUTTI gli atomi
            for (i in 0 until atoms.size) {
                val atom = atoms[i]

                // Ora questo non restituirà più null!
                val instance = assetLoader.createInstance(cloneTemplate) ?: continue

                val pos = Triple(
                    (atom.x.toFloat() - avgX) * normalizeScale,
                    (atom.y.toFloat() - avgY) * normalizeScale,
                    (atom.z.toFloat() - avgZ) * normalizeScale
                )

                setTransform(instance.root, pos, atomScale)
                modelViewer.scene.addEntities(instance.entities)
            }

            // Nota: Abbiamo eliminato tutto il blocco 'modelViewer.view.camera?.let...'
            // Con la normalizzazione a 0.5f, il ModelViewer vedrà la molecola perfettamente da solo!

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setTransform(entity: Int, pos: Triple<Float, Float, Float>, scale: Float) {
        val tm = modelViewer.engine.transformManager
        val transformMatrix = floatArrayOf(
            scale, 0f, 0f, 0f,
            0f, scale, 0f, 0f,
            0f, 0f, scale, 0f,
            pos.first, pos.second, pos.third, 1f
        )
        tm.setTransform(tm.getInstance(entity), transformMatrix)
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

    fun onTouchEvent(event: android.view.MotionEvent) {
        modelViewer.onTouchEvent(event)
    }

    fun destroy() {
        isRendering = false
        Choreographer.getInstance().removeFrameCallback(frameCallback)
        val engine = modelViewer.engine
        val scene = modelViewer.scene
        scene.skybox?.let {
            engine.destroySkybox(it)
            scene.skybox = null
        }
        engine.destroyEntity(lightEntity)
        EntityManager.get().destroy(lightEntity)
        modelViewer.destroyModel()

    }
}