package com.homeflix.tv.data.remote.stremio

import android.content.Context
import android.graphics.Bitmap
import android.net.wifi.WifiManager
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AddonWebServer"
private const val PORT = 8765

/**
 * On-device web server for managing Stremio addons from a phone.
 * Tap "Manage on phone" in settings → TV shows QR code → scan from phone →
 * manage addons in the browser on the same Wi-Fi network.
 *
 * Uses NanoHTTPD for the lightweight web server and ZXing for QR generation.
 */
@Singleton
class AddonWebServer @Inject constructor(
    private val addonManager: AddonManager
) {

    private var server: AddonNanoServer? = null
    private var serverUrl: String? = null

    /**
     * Start the web server and return the URL for the QR code.
     */
    fun start(context: Context): String? {
        return try {
            val ip = getDeviceIp(context) ?: return null
            val url = "http://$ip:$PORT"

            server = AddonNanoServer(PORT, addonManager)
            server?.start()

            serverUrl = url
            Log.d(TAG, "Addon web server started at $url")
            url
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start addon web server: ${e.message}", e)
            null
        }
    }

    /**
     * Stop the web server.
     */
    fun stop() {
        server?.stop()
        server = null
        serverUrl = null
        Log.d(TAG, "Addon web server stopped")
    }

    /**
     * Generate a QR code bitmap for the server URL.
     */
    fun generateQrCode(size: Int = 512): Bitmap? {
        val url = serverUrl ?: return null
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(url, BarcodeFormat.QR_CODE, size, size)
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
                }
            }
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error generating QR code: ${e.message}", e)
            null
        }
    }

    val isRunning: Boolean get() = server?.isAlive == true

    private fun getDeviceIp(context: Context): String? {
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ip = wifiManager.connectionInfo.ipAddress
            if (ip == 0) return null
            String.format(
                "%d.%d.%d.%d",
                ip and 0xff, ip shr 8 and 0xff,
                ip shr 16 and 0xff, ip shr 24 and 0xff
            )
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * NanoHTTPD server that serves a simple addon management page.
 */
private class AddonNanoServer(
    port: Int,
    private val addonManager: AddonManager
) : NanoHTTPD(port) {

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri
        val method = session.method

        return when {
            // Main page — show installed addons + add form
            uri == "/" || uri == "" -> serveMainPage()

            // Add addon
            method == Method.POST && uri == "/add" -> {
                val params = mutableMapOf<String, String>()
                session.parseBody(params)
                val postData = session.parameters["url"]?.firstOrNull()
                    ?: params["postData"] ?: ""
                handleAddAddon(postData)
            }

            // Remove addon
            method == Method.POST && uri.startsWith("/remove/") -> {
                val addonId = uri.removePrefix("/remove/")
                handleRemoveAddon(addonId)
            }

            else -> newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not Found")
        }
    }

    private fun serveMainPage(): Response {
        val addons = runBlocking { addonManager.getInstalledAddons().first() }

        val addonRows = addons.joinToString("") { addon ->
            """
            <tr>
                <td>${addon.name}</td>
                <td>${addon.version}</td>
                <td>${addon.addonType.name}</td>
                <td>${if (addon.isEnabled) "✅" else "❌"}</td>
                <td>
                    ${if (!addon.isBuiltIn) """<form method="post" action="/remove/${addon.id}" style="display:inline"><button type="submit">Remove</button></form>""" else "<em>Built-in</em>"}
                </td>
            </tr>
            """.trimIndent()
        }

        val html = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>HomeFlixTV - Addon Manager</title>
            <style>
                body { font-family: -apple-system, sans-serif; background: #141414; color: #fff; padding: 20px; }
                h1 { color: #E50914; }
                table { width: 100%; border-collapse: collapse; margin: 20px 0; }
                th, td { padding: 12px; text-align: left; border-bottom: 1px solid #333; }
                th { background: #222; }
                input[type=text] { width: 70%; padding: 10px; border-radius: 4px; border: 1px solid #555; background: #222; color: #fff; }
                button { padding: 10px 20px; background: #E50914; color: #fff; border: none; border-radius: 4px; cursor: pointer; }
                button:hover { background: #f40612; }
                .add-form { margin: 20px 0; }
            </style>
        </head>
        <body>
            <h1>🎬 HomeFlixTV Addon Manager</h1>
            <p>Manage your Stremio addons from your phone.</p>
            
            <div class="add-form">
                <h2>Install Addon</h2>
                <form method="post" action="/add">
                    <input type="text" name="url" placeholder="Paste addon manifest URL..." />
                    <button type="submit">Install</button>
                </form>
            </div>
            
            <h2>Installed Addons</h2>
            <table>
                <thead><tr><th>Name</th><th>Version</th><th>Type</th><th>Enabled</th><th>Actions</th></tr></thead>
                <tbody>$addonRows</tbody>
            </table>
        </body>
        </html>
        """.trimIndent()

        return newFixedLengthResponse(Response.Status.OK, "text/html", html)
    }

    private fun handleAddAddon(url: String): Response {
        if (url.isBlank()) {
            return redirect("/")
        }
        runBlocking { addonManager.installAddon(url) }
        return redirect("/")
    }

    private fun handleRemoveAddon(addonId: String): Response {
        runBlocking { addonManager.removeAddon(addonId) }
        return redirect("/")
    }

    private fun redirect(location: String): Response {
        val response = newFixedLengthResponse(Response.Status.REDIRECT, "text/html", "")
        response.addHeader("Location", location)
        return response
    }
}
