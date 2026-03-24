package com.example.swifty_protein.model

import com.google.android.filament.Camera
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.text.toDouble
import kotlin.times

//class OrbitalCamera {
//    private var orbitTheta = 0f      // angolo orizzontale (azimuth)
//    private var orbitPhi = 0f       // angolo verticale (elevation)
//    private var orbitRadius = 0f     // distanza dal centro
//    private var orbitCenterX = 0f
//    private var orbitCenterY = 0f
//    private var orbitCenterZ = 0f
//    private var lastTouchX = 0f
//    private var lastTouchY = 0f
//    private var lastTouchX2 = 0f
//    private var lastTouchY2 = 0f
//
//    private val CAMERA_DISTANCE_MULT = 1.5f // moltiplicatore distanza camera
//    private val CAMERA_DISTANCE_MIN = 2f  // distanza minima
//    private val ORBIT_SENSITIVITY = 0.3f
//    private val ZOOM_SENSITIVITY = 0.005f
//    private val ZOOM_MIN = 1f
//    private val ZOOM_MAX = 50f
//    init {
//        orbitTheta = 0f      // angolo orizzontale (azimuth)
//        orbitPhi = 45f       // angolo verticale (elevation)
//        orbitRadius = 5f     // distanza dal centro
//        orbitCenterX = 0f
//        orbitCenterY = 0f
//        orbitCenterZ = 0f
//        lastTouchX = 0f
//        lastTouchY = 0f
//        lastTouchX2 = 0f
//        lastTouchY2 = 0f
//    }
//
//    private fun setupCamera(atoms: MutableList<Atom>) {
//        // Centro è ora sempre l'origine
//        orbitCenterX = 0f
//        orbitCenterY = 0f
//        orbitCenterZ = 0f
//
//        // Calcola il raggio come distanza massima dal centro originale
//        val centerX = atoms.map { it.x }.average().toFloat()
//        val centerY = atoms.map { it.y }.average().toFloat()
//        val centerZ = atoms.map { it.z }.average().toFloat()
//
//        val maxDist = atoms.maxOf { atom ->
//            sqrt(
//                (atom.x - centerX).pow(2) +
//                        (atom.y - centerY).pow(2) +
//                        (atom.z - centerZ).pow(2)
//            )
//        }.toFloat()
//
//        orbitRadius = maxDist * CAMERA_DISTANCE_MULT + CAMERA_DISTANCE_MIN
//
//        android.util.Log.d("FILAMENT_CAM", "maxDist=$maxDist orbitRadius=$orbitRadius")
////        updateOrbitalCamera()
//    }
//
//    private fun updateOrbitalCamera() {
//        val radTheta = Math.toRadians(orbitTheta.toDouble())
//        val radPhi = Math.toRadians(orbitPhi.toDouble().coerceIn(-89.0, 89.0))
//
//        val camX = orbitCenterX + orbitRadius * kotlin.math.cos(radPhi) * kotlin.math.sin(radTheta)
//        val camY = orbitCenterY + orbitRadius * kotlin.math.sin(radPhi)
//        val camZ = orbitCenterZ + orbitRadius * kotlin.math.cos(radPhi) * kotlin.math.cos(radTheta)
//
//        val camera = modelViewer.camera  // camera di default ModelViewer
//
//        camera.lookAt(
//            camX.toDouble(), camY.toDouble(), camZ.toDouble(),
//            orbitCenterX.toDouble(), orbitCenterY.toDouble(), orbitCenterZ.toDouble(),
//            0.0, 1.0, 0.0
//        )
//
//        val width = surfaceView.width.takeIf { it > 0 } ?: 1080
//        val height = surfaceView.height.takeIf { it > 0 } ?: 1920
//        val aspect = width.toDouble() / height.toDouble()
//        val near = (orbitRadius * 0.01).coerceAtLeast(0.05)
//        val far = (orbitRadius * 10.0).coerceAtLeast(100.0)
//
//        camera.setProjection(30.0, aspect, near, far, Camera.Fov.VERTICAL)
//    }
//}