package com.example.vpn

import android.content.Context
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.File
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/** Xray via AndroidLibXrayLite (libv2ray) – StartLoop(config, tunFd). */
class XrayEngine(private val vpnService: VpnService) {

    companion object {
        private const val TAG = "XrayEngine"

        fun isAvailable(): Boolean = try {
            Class.forName("libv2ray.Libv2ray")
            true
        } catch (_: Throwable) {
            false
        }
    }

    private var controller: Any? = null
    private var tunPfd: ParcelFileDescriptor? = null

    fun start(context: Context, xrayJson: String, sessionName: String): Boolean {
        if (!isAvailable()) {
            Log.e(TAG, "libv2ray not on classpath")
            return false
        }
        val assetDir = File(context.filesDir, "xray").apply { mkdirs() }
        File(assetDir, "config.json").writeText(xrayJson)

        return try {
            val lib = Class.forName("libv2ray.Libv2ray")
            invokeStaticNamed(lib, listOf("InitCoreEnv", "initCoreEnv"), arrayOf(assetDir.absolutePath, ""))
            Log.i(TAG, "InitCoreEnv OK")

            val pfd = establishTun(sessionName) ?: return false.also { Log.e(TAG, "TUN failed") }
            tunPfd = pfd
            val fd = pfd.fd
            Log.i(TAG, "TUN fd=$fd")

            val cbIface = Class.forName("libv2ray.CoreCallbackHandler")
            val callback = Proxy.newProxyInstance(cbIface.classLoader, arrayOf(cbIface)) { _, method, args ->
                when (method.name) {
                    "Startup", "startup" -> 0.also { Log.i(TAG, "Startup") }
                    "Shutdown", "shutdown" -> 0.also { Log.i(TAG, "Shutdown") }
                    "OnEmitStatus", "onEmitStatus" -> 0.also {
                        Log.i(TAG, "status ${args?.getOrNull(0)}: ${args?.getOrNull(1)}")
                    }
                    else -> 0
                }
            }

            val ctrl = invokeStaticNamed(
                lib, listOf("NewCoreController", "newCoreController"), arrayOf(callback)
            ) ?: return false.also { Log.e(TAG, "NewCoreController null") }
            controller = ctrl

            val result = invokeInstanceNamed(
                ctrl, listOf("StartLoop", "startLoop"), arrayOf(xrayJson, Integer.valueOf(fd))
            )
            if (result is Throwable) throw result
            Log.i(TAG, "StartLoop OK")
            true
        } catch (e: Throwable) {
            Log.e(TAG, "start failed: ${(e.cause ?: e).message}", e.cause ?: e)
            stop()
            false
        }
    }

    fun establishTun(sessionName: String): ParcelFileDescriptor? = try {
        try { tunPfd?.close() } catch (_: Exception) {}
        val b = vpnService.Builder()
            .setSession(sessionName)
            .setMtu(1500)
            .addAddress("10.0.0.2", 32)
            .addRoute("0.0.0.0", 0)
            .addDnsServer("8.8.8.8")
            .addDnsServer("1.1.1.1")
            .setBlocking(false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) b.setMetered(false)
        try { b.addDisallowedApplication(vpnService.packageName) } catch (_: Exception) {}
        b.establish()
    } catch (e: Exception) {
        Log.e(TAG, "establishTun", e)
        null
    }

    fun stop() {
        try {
            controller?.let { invokeInstanceNamed(it, listOf("StopLoop", "stopLoop"), emptyArray()) }
        } catch (e: Exception) {
            Log.w(TAG, "StopLoop: ${e.message}")
        }
        controller = null
        try { tunPfd?.close() } catch (_: Exception) {}
        tunPfd = null
    }

    private fun invokeStaticNamed(clazz: Class<*>, names: List<String>, args: Array<Any?>): Any? {
        for (name in names) {
            for (m in clazz.methods) {
                if (m.name != name || m.parameterTypes.size != args.size) continue
                return try {
                    m.invoke(null, *coerceArgs(m, args))
                } catch (e: Exception) {
                    throw e.cause ?: e
                }
            }
        }
        Log.w(TAG, "static not found: $names")
        return null
    }

    private fun invokeInstanceNamed(target: Any, names: List<String>, args: Array<Any?>): Any? {
        val clazz = target.javaClass
        for (name in names) {
            for (m in clazz.methods) {
                if (m.name != name) continue
                if (args.isEmpty() && m.parameterTypes.isEmpty()) {
                    return m.invoke(target)
                }
                if (m.parameterTypes.size != args.size) continue
                return try {
                    m.invoke(target, *coerceArgs(m, args))
                } catch (e: Exception) {
                    throw e.cause ?: e
                }
            }
        }
        Log.w(TAG, "instance not found: $names")
        return null
    }

    private fun coerceArgs(m: Method, args: Array<Any?>): Array<Any?> {
        val types = m.parameterTypes
        return Array(args.size) { i ->
            val a = args[i]
            val t = types[i]
            when {
                a == null -> null
                t == Integer.TYPE || t == Int::class.javaObjectType -> (a as Number).toInt()
                t == java.lang.Long.TYPE || t == Long::class.javaObjectType -> (a as Number).toLong()
                else -> a
            }
        }
    }
}
