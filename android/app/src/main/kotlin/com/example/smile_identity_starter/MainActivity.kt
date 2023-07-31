package com.example.smile_identity_starter

import android.content.Intent
import android.os.Bundle
import com.example.smile_identity.SmileIdentityPlugin
import io.flutter.embedding.android.FlutterFragmentActivity
import io.flutter.embedding.engine.FlutterEngine

class MainActivity: FlutterFragmentActivity() {
    private val smileIdentityPlugin = SmileIdentityPlugin()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        smileIdentityPlugin.initializeActivity(this)
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        smileIdentityPlugin.registerChannel(flutterEngine.dartExecutor.binaryMessenger)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        smileIdentityPlugin.onActivityResult(requestCode, resultCode)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        smileIdentityPlugin.deRegisterChannel()
    }
}
