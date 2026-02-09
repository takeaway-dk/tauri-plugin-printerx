package com.plugin.printerx

import android.app.Activity
import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.util.Log
import java.util.Timer
import java.util.TimerTask
import app.tauri.annotation.Command
import app.tauri.annotation.InvokeArg
import app.tauri.annotation.TauriPlugin
import app.tauri.plugin.JSObject
import app.tauri.plugin.Plugin
import app.tauri.plugin.Invoke
import com.sunmi.printerx.PrinterSdk
import com.sunmi.printerx.api.PrintResult
import com.sunmi.printerx.enums.Align
import com.sunmi.printerx.enums.PrinterInfo
import com.sunmi.printerx.style.BaseStyle
import com.sunmi.printerx.style.TextStyle
import org.json.JSONArray
import org.json.JSONObject
import java.net.InetSocketAddress
import java.net.Socket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

@InvokeArg
class PingArgs {
    var value: String? = null
}

@InvokeArg
class PrintTextArgs {
    var text: String? = null
    var fontSize: Int? = null
    var align: String? = null
}

@InvokeArg
class PrintEscPosArgs {
    var hex: String? = null
}

@InvokeArg
class PrintEscPosNetworkArgs {
    var printerIp: String? = null
    var port: Int? = null
    var hex: String? = null
}

@InvokeArg
class PrintLineArgs {
    var text: String? = null
}

@InvokeArg
class FeedLineArgs {
    var lines: Int? = null
}

@InvokeArg
class CashDrawerMonitorArgs {
    var intervalMs: Long? = null
}

@InvokeArg
class QueryInfoArgs {
    var info: String? = null
}


@InvokeArg
class LcdTextArgs {
    var text: String? = null
    var size: Int? = null
    var bold: Boolean? = null
}

@TauriPlugin
class PrinterxPlugin(private val activity: Activity) : Plugin(activity), PrinterSdk.PrinterListen {

    private var printer: PrinterSdk.Printer? = null
    private var isInitialized = false
    private val initCallbacks = mutableListOf<(Boolean, String?) -> Unit>()
    private val logTag = "PrinterxPlugin"
    private val drawerStatusEvent = "cash_drawer_status"
    private var drawerMonitorTimer: Timer? = null
    private var lastDrawerOpen: Boolean? = null

    init {
        // SDK initialization will be done when initPrinter is called
    }

    // PrinterSdk.PrinterListen callbacks
    override fun onDefPrinter(printer: PrinterSdk.Printer?) {
        this.printer = printer
        isInitialized = true

        if (printer == null) {
            Log.w(logTag, "Default printer is null")
        } else {
            Log.i(logTag, "Default printer ready")
        }

        // Notify all waiting callbacks
        initCallbacks.forEach { callback ->
            if (printer != null) {
                callback(true, null)
            } else {
                callback(false, "Failed to get default printer from PrinterX SDK")
            }
        }
        initCallbacks.clear()
    }

    override fun onPrinters(printers: MutableList<PrinterSdk.Printer>?) {
        Log.i(logTag, "Printers list count: ${printers?.size ?: 0}")
    }

    @Command
    fun ping(invoke: Invoke) {
        val args = invoke.parseArgs(PingArgs::class.java)
        val ret = JSObject()
        ret.put("value", args.value)
        invoke.resolve(ret)
    }

    @Command
    fun initPrinter(invoke: Invoke) {
        try {
            Log.i(logTag, "initPrinter called")
            if (isInitialized && printer != null) {
                invoke.resolve()
                return
            }

            val sdk = PrinterSdk.getInstance()
            if (sdk == null) {
                val ret = buildErrorResponse(
                    "PrinterSdk instance is null. PrinterX SDK may be unavailable on this device."
                )
                invoke.reject(ret.toString())
                return
            }

            // Register callback for when printer is ready
            initCallbacks.add { success, error ->
                if (success) {
                    invoke.resolve()
                } else {
                    val ret = buildErrorResponse(error ?: "Unknown error")
                    invoke.reject(ret.toString())
                }
            }

            // Initialize SDK and get printer
            Log.i(logTag, "Requesting default printer from PrinterX SDK")
            sdk.getPrinter(activity.applicationContext, this)
        } catch (e: Exception) {
            val ret = buildErrorResponse(e.message ?: "Unknown error", e)
            invoke.reject(ret.toString())
        }
    }

    @Command
    fun scanDevices(invoke: Invoke) {
        try {
            Log.i(logTag, "scanDevices called")
            val usbManager = activity.getSystemService(Context.USB_SERVICE) as UsbManager
            val devices = JSONArray()

            for (device in usbManager.deviceList.values) {
                val deviceObj = JSONObject()
                deviceObj.put("name", device.deviceName)
                deviceObj.put("vendorId", String.format("0x%04X", device.vendorId))
                deviceObj.put("productId", String.format("0x%04X", device.productId))
                deviceObj.put("deviceClass", device.deviceClass)
                devices.put(deviceObj)
            }

            val ret = JSObject()
            ret.put("success", true)
            ret.put("devices", devices)
            invoke.resolve(ret)
        } catch (e: Exception) {
            val ret = JSObject()
            ret.put("success", false)
            ret.put("error", e.message ?: "Unknown error")
            invoke.reject(ret.toString())
        }
    }

    @Command
    fun connectPrinter(invoke: Invoke) {
        try {
            Log.i(logTag, "connectPrinter called")
            if (printer == null) {
                throw Exception("Printer not initialized. Call initPrinter first")
            }

            // Sunmi PrinterX automatically connects to the default printer
            // when getPrinter is called, so we just verify it's ready
            invoke.resolve()
        } catch (e: Exception) {
            val ret = JSObject()
            ret.put("success", false)
            ret.put("error", e.message ?: "Unknown error")
            invoke.reject(ret.toString())
        }
    }

    @Command
    fun printText(invoke: Invoke) {
        try {
            Log.i(logTag, "printText called")
            if (printer == null) {
                throw Exception("Printer not initialized")
            }

            val args = invoke.parseArgs(PrintTextArgs::class.java)
            val text = args.text ?: throw Exception("Text is required")
            val fontSize = args.fontSize ?: 24
            val alignStr = args.align ?: "left"

            Log.i(logTag, "printText args: length=${text.length}, fontSize=$fontSize, align=$alignStr")

            val align = when(alignStr.lowercase()) {
                "center" -> Align.CENTER
                "right" -> Align.RIGHT
                else -> Align.LEFT
            }

            val canvas = printer!!.canvasApi()

            canvas.initCanvas(
                BaseStyle.getStyle()
                    .setWidth(48 * 8)
                    .setHeight(40 * 8)
            )

            canvas.renderText(
                text,
                TextStyle.getStyle()
                    .setAlign(align)
                    .setTextSize(fontSize)
                    .setPosX(10)
                    .setPosY(10)
            )

            canvas.printCanvas(1, object : PrintResult() {
                override fun onResult(code: Int, msg: String?) {
                    if (code == 0) {
                        invoke.resolve()
                    } else {
                        val ret = JSObject()
                        ret.put("success", false)
                        ret.put("error", "Print failed: $msg (code: $code)")
                        invoke.reject(ret.toString())
                    }
                }
            })
        } catch (e: Exception) {
            val ret = JSObject()
            ret.put("success", false)
            ret.put("error", e.message ?: "Unknown error")
            invoke.reject(ret.toString())
        }
    }

    @Command
    fun printLine(invoke: Invoke) {
        try {
            Log.i(logTag, "printLine called")
            if (printer == null) {
                throw Exception("Printer not initialized")
            }

            val args = invoke.parseArgs(PrintLineArgs::class.java)
            val text = args.text ?: ""

            Log.i(logTag, "printLine args: length=${text.length}")

            val canvas = printer!!.canvasApi()

            canvas.initCanvas(
                BaseStyle.getStyle()
                    .setWidth(48 * 8)
                    .setHeight(40 * 8)
            )

            canvas.renderText(
                text,
                TextStyle.getStyle()
                    .setAlign(Align.LEFT)
                    .setTextSize(24)
                    .setPosX(10)
                    .setPosY(10)
            )

            canvas.printCanvas(1, object : PrintResult() {
                override fun onResult(code: Int, msg: String?) {
                    if (code == 0) {
                        invoke.resolve()
                    } else {
                        val ret = JSObject()
                        ret.put("success", false)
                        ret.put("error", "Print failed: $msg (code: $code)")
                        invoke.reject(ret.toString())
                    }
                }
            })
        } catch (e: Exception) {
            val ret = JSObject()
            ret.put("success", false)
            ret.put("error", e.message ?: "Unknown error")
            invoke.reject(ret.toString())
        }
    }

    @Command
    fun printEscPos(invoke: Invoke) {
        try {
            Log.i(logTag, "printEscPos called")
            if (printer == null) {
                throw Exception("Printer not initialized")
            }

            val args = invoke.parseArgs(PrintEscPosArgs::class.java)
            val hex = args.hex ?: throw Exception("Hex string is required")

            Log.i(logTag, "printEscPos args: hexLength=${hex.length}")

            val bytes = hexToBytes("1C43FF$hex")
            val cmdApi = printer!!.commandApi()

            cmdApi.sendEscCommand(bytes)

            invoke.resolve()
        } catch (e: Exception) {
            val ret = JSObject()
            ret.put("success", false)
            ret.put("error", e.message ?: "Unknown error")
            invoke.reject(ret.toString())
        }
    }

    @Command
    fun printEscPosNetwork(invoke: Invoke) {
        try {
            Log.i(logTag, "printEscPosNetwork called")
            val args = invoke.parseArgs(PrintEscPosNetworkArgs::class.java)
            val printerIp = args.printerIp ?: throw Exception("Printer IP is required")
            val port = args.port ?: 9100
            val hex = args.hex ?: throw Exception("Hex string is required")

            Log.i(logTag, "printEscPosNetwork args: ip=$printerIp, port=$port, hexLength=${hex.length}")

            // Run network operation in background thread
            Thread {
                try {
                    val socket = Socket()
                    socket.connect(InetSocketAddress(printerIp, port), 5000) // 5 second timeout

                    val outputStream = socket.getOutputStream()

                    // Send ESC @ (initialize printer)
                    val initCommand = byteArrayOf(0x1B, 0x40)
                    outputStream.write(initCommand)

                    // Send the actual command with prefix
                    val bytes = hexToBytes("1C43FF$hex")
                    outputStream.write(bytes)

                    outputStream.flush()
                    socket.close()

                    invoke.resolve()
                } catch (e: Exception) {
                    val ret = JSObject()
                    ret.put("success", false)
                    ret.put("error", "Network error: ${e.message}")
                    invoke.reject(ret.toString())
                }
            }.start()
        } catch (e: Exception) {
            val ret = JSObject()
            ret.put("success", false)
            ret.put("error", e.message ?: "Unknown error")
            invoke.reject(ret.toString())
        }
    }

    @Command
    fun feedLine(invoke: Invoke) {
        try {
            Log.i(logTag, "feedLine called")
            if (printer == null) {
                throw Exception("Printer not initialized")
            }

            val args = invoke.parseArgs(FeedLineArgs::class.java)
            val lines = args.lines ?: 1

            Log.i(logTag, "feedLine args: lines=$lines")

            // ESC/POS command for line feed: ESC d n
            val escPos = "1B64" + String.format("%02X", lines)
            val bytes = hexToBytes(escPos)

            printer!!.commandApi().sendEscCommand(bytes)

            invoke.resolve()
        } catch (e: Exception) {
            val ret = JSObject()
            ret.put("success", false)
            ret.put("error", e.message ?: "Unknown error")
            invoke.reject(ret.toString())
        }
    }

    @Command
    fun cutPaper(invoke: Invoke) {
        try {
            Log.i(logTag, "cutPaper called")
            if (printer == null) {
                throw Exception("Printer not initialized")
            }

            // ESC/POS command for full cut: GS V 0
            val bytes = hexToBytes("1D5600")
            printer!!.commandApi().sendEscCommand(bytes)

            invoke.resolve()
        } catch (e: Exception) {
            val ret = JSObject()
            ret.put("success", false)
            ret.put("error", e.message ?: "Unknown error")
            invoke.reject(ret.toString())
        }
    }

    @Command
    fun openCashDrawer(invoke: Invoke) {
        try {
            Log.i(logTag, "openCashDrawer called")
            if (printer == null) {
                throw Exception("Printer not initialized")
            }

            val drawerApi = printer!!.cashDrawerApi()
            drawerApi.open(object : PrintResult() {
                override fun onResult(code: Int, msg: String?) {
                    if (code == 0) {
                        invoke.resolve()
                    } else {
                        val ret = JSObject()
                        ret.put("success", false)
                        ret.put("error", "Failed to open drawer: $msg (code: $code)")
                        invoke.reject(ret.toString())
                    }
                }
            })
        } catch (e: Exception) {
            val ret = JSObject()
            ret.put("success", false)
            ret.put("error", e.message ?: "Unknown error")
            invoke.reject(ret.toString())
        }
    }

    @Command
    fun getCashDrawerStatus(invoke: Invoke) {
        try {
            Log.i(logTag, "getCashDrawerStatus called")
            if (printer == null) {
                throw Exception("Printer not initialized")
            }

            val drawerApi = printer!!.cashDrawerApi()
            val isOpen = drawerApi.isOpen()

            val ret = JSObject()
            ret.put("success", true)
            ret.put("open", isOpen)
            invoke.resolve(ret)
        } catch (e: Exception) {
            val ret = JSObject()
            ret.put("success", false)
            ret.put("error", e.message ?: "Unknown error")
            invoke.reject(ret.toString())
        }
    }

    @Command
    fun startCashDrawerMonitor(invoke: Invoke) {
        try {
            Log.i(logTag, "startCashDrawerMonitor called")
            if (printer == null) {
                throw Exception("Printer not initialized")
            }

            val args = invoke.parseArgs(CashDrawerMonitorArgs::class.java)
            val intervalMs = args.intervalMs ?: 1000L

            stopCashDrawerMonitorInternal()
            drawerMonitorTimer = Timer("CashDrawerMonitor", true)
            drawerMonitorTimer?.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    try {
                        val drawerApi = printer!!.cashDrawerApi()
                        val isOpen = drawerApi.isOpen()
                        val changed = lastDrawerOpen == null || lastDrawerOpen != isOpen
                        lastDrawerOpen = isOpen

                        val payload = JSObject()
                        payload.put("success", true)
                        payload.put("open", isOpen)
                        payload.put("changed", changed)
                        payload.put("ts", System.currentTimeMillis())
                        trigger(drawerStatusEvent, payload)
                    } catch (e: Exception) {
                        val payload = JSObject()
                        payload.put("success", false)
                        payload.put("error", e.message ?: "Unknown error")
                        payload.put("ts", System.currentTimeMillis())
                        trigger(drawerStatusEvent, payload)
                    }
                }
            }, 0L, intervalMs)

            invoke.resolve()
        } catch (e: Exception) {
            val ret = JSObject()
            ret.put("success", false)
            ret.put("error", e.message ?: "Unknown error")
            invoke.reject(ret.toString())
        }
    }

    @Command
    fun stopCashDrawerMonitor(invoke: Invoke) {
        try {
            Log.i(logTag, "stopCashDrawerMonitor called")
            stopCashDrawerMonitorInternal()
            invoke.resolve()
        } catch (e: Exception) {
            val ret = JSObject()
            ret.put("success", false)
            ret.put("error", e.message ?: "Unknown error")
            invoke.reject(ret.toString())
        }
    }

    @Command
    fun getStatus(invoke: Invoke) {
        try {
            Log.i(logTag, "getStatus called")
            val ret = JSObject()
            ret.put("success", true)
            ret.put("initialized", isInitialized)
            ret.put("ready", printer != null)
            invoke.resolve(ret)
        } catch (e: Exception) {
            val ret = JSObject()
            ret.put("success", false)
            ret.put("error", e.message ?: "Unknown error")
            invoke.reject(ret.toString())
        }
    }

    @Command
    fun getPrinterStatus(invoke: Invoke) {
        try {
            Log.i(logTag, "getPrinterStatus called")
            if (printer == null) {
                throw Exception("Printer not initialized")
            }

            val status = printer!!.queryApi().getStatus()
            val ret = JSObject()
            ret.put("success", true)
            ret.put("status", status.name)
            ret.put("code", status.code)
            invoke.resolve(ret)
        } catch (e: Exception) {
            val ret = JSObject()
            ret.put("success", false)
            ret.put("error", e.message ?: "Unknown error")
            invoke.reject(ret.toString())
        }
    }

    @Command
    fun getPrinterInfo(invoke: Invoke) {
        try {
            Log.i(logTag, "getPrinterInfo called")
            if (printer == null) {
                throw Exception("Printer not initialized")
            }

            val args = invoke.parseArgs(QueryInfoArgs::class.java)
            val key = args.info ?: throw Exception("info is required")
            val infoEnum = PrinterInfo.findInfo(key)
            if (infoEnum == null || infoEnum == PrinterInfo.NULL) {
                val allowed = PrinterInfo.values()
                    .filter { it != PrinterInfo.NULL }
                    .joinToString(",") { it.name }
                throw Exception("Unknown info: $key. Allowed: $allowed")
            }

            val value = printer!!.queryApi().getInfo(infoEnum)
            val ret = JSObject()
            ret.put("success", true)
            ret.put("info", infoEnum.name)
            ret.put("value", value)
            invoke.resolve(ret)
        } catch (e: Exception) {
            val ret = JSObject()
            ret.put("success", false)
            ret.put("error", e.message ?: "Unknown error")
            invoke.reject(ret.toString())
        }
    }


    @Command
    fun lcdShowText(invoke: Invoke) {
        try {
            Log.i(logTag, "lcdShowText called")
            if (printer == null) {
                throw Exception("Printer not initialized")
            }

            val args = invoke.parseArgs(LcdTextArgs::class.java)
            val text = args.text ?: ""
            val size = args.size ?: 24
            val bold = args.bold ?: false

            printer!!.lcdApi().showText(text, size, bold)
            invoke.resolve()
        } catch (e: Exception) {
            val ret = JSObject()
            ret.put("success", false)
            ret.put("error", e.message ?: "Unknown error")
            invoke.reject(ret.toString())
        }
    }

    @Command
    fun disconnectPrinter(invoke: Invoke) {
        try {
            // Sunmi PrinterX handles connection lifecycle automatically
            invoke.resolve()
        } catch (e: Exception) {
            val ret = JSObject()
            ret.put("success", false)
            ret.put("error", e.message ?: "Unknown error")
            invoke.reject(ret.toString())
        }
    }

    private fun hexToBytes(hex: String): ByteArray {
        val len = hex.length
        if (len % 2 != 0) {
            throw IllegalArgumentException("Hex string must have even length")
        }

        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(hex[i], 16) shl 4) + Character.digit(hex[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

    private fun buildErrorResponse(message: String, cause: Throwable? = null): JSObject {
        val ret = JSObject()
        ret.put("success", false)
        ret.put("error", message)

        if (cause != null) {
            val causeMessage = cause.message ?: "Unknown cause"
            ret.put("cause", "${cause.javaClass.name}: $causeMessage")
        }

        val device = JSObject()
        device.put("manufacturer", Build.MANUFACTURER)
        device.put("brand", Build.BRAND)
        device.put("model", Build.MODEL)
        device.put("device", Build.DEVICE)
        device.put("product", Build.PRODUCT)
        device.put("sdkInt", Build.VERSION.SDK_INT)
        ret.put("device", device)

        ret.put("printerSdkAvailable", PrinterSdk.getInstance() != null)
        ret.put("initialized", isInitialized)
        ret.put("ready", printer != null)

        return ret
    }

    private fun stopCashDrawerMonitorInternal() {
        drawerMonitorTimer?.cancel()
        drawerMonitorTimer = null
        lastDrawerOpen = null
    }
}
