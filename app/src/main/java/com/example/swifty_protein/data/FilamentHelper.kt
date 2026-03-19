package com.example.swifty_protein.data

import android.content.Context
import android.view.Choreographer
import android.view.Surface
import android.view.SurfaceView
import com.google.android.filament.*import com.google.android.filament.android.UiHelper

class FilamentHelper(context: Context, val surfaceView: SurfaceView) {

    companion object {
        init {
            System.loadLibrary("filament-jni")
        }
    }

    private var engine: Engine = Engine.create()
    private var renderer: Renderer = engine.createRenderer()
    private var scene: Scene = engine.createScene()
    private var view: View = engine.createView()
    private var cameraEntity: Int = EntityManager.get().create()
    private var camera: Camera = engine.createCamera(cameraEntity)
    private var swapChain: SwapChain? = null

    private val frameScheduler = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            Choreographer.getInstance().postFrameCallback(this)
            if (swapChain != null) {
                if (renderer.beginFrame(swapChain!!)) {
                    renderer.render(view)
                    renderer.endFrame()
                }
            }
        }
    }

    init {
        view.scene = scene
        view.camera = camera

        // Versione 1.70.0: lo sfondo si imposta sulla View
        // Se .setClearColor non esiste, usiamo il metodo corretto per la 1.70
        // che spesso richiede di creare una skybox colorata o usare le ClearOptions del renderer

        Choreographer.getInstance().postFrameCallback(frameScheduler)
    }

    fun onSurfaceCreated(surface: Surface) {
        swapChain = engine.createSwapChain(surface)
    }

    fun onSizeChanged(width: Int, height: Int) {
        view.viewport = Viewport(0, 0, width, height)
        val aspect = width.toDouble() / height.toDouble()
        camera.setProjection(45.0, aspect, 0.1, 100.0, Camera.Fov.VERTICAL)
    }

    fun onSurfaceDestroyed() {
        Choreographer.getInstance().removeFrameCallback(frameScheduler)
        swapChain?.let {
            engine.destroySwapChain(it)
        }
        swapChain = null
    }

    fun destroy() {
        Choreographer.getInstance().removeFrameCallback(frameScheduler)
        engine.destroyCamera(camera)
        EntityManager.get().destroy(cameraEntity)
        engine.destroyRenderer(renderer)
        engine.destroyScene(scene)
        engine.destroyView(view)
        engine.destroy()
    }
}