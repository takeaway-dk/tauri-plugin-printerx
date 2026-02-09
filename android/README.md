# Android Setup for Sunmi PrinterX Plugin

## AAR File

The `printerx-1.0.17.aar` file has been placed in the `android/libs/` directory.

The AAR is automatically included by the build.gradle.kts configuration:
```kotlin
implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
```

## Sunmi PrinterX SDK

This plugin uses the official Sunmi PrinterX SDK which provides:
- Canvas-based printing API
- ESC/POS command API
- Cash drawer API
- Automatic printer detection and connection

### Key Components

- **PrinterSdk** - Main SDK singleton (`PrinterSdk.getInstance()`)
- **Printer** - Printer instance obtained via callback
- **CanvasApi** - For text and graphics rendering
- **CommandApi** - For raw ESC/POS commands
- **CashDrawerApi** - For cash drawer control

## Required Permissions

No special permissions are required for built-in Sunmi printers. The AndroidManifest.xml is minimal.

## Implementation

The plugin is fully implemented using the Sunmi PrinterX SDK:

```kotlin
// SDK Initialization
PrinterSdk.getInstance()?.getPrinter(context, printerListener)

// Canvas Printing
val canvas = printer.canvasApi()
canvas.initCanvas(BaseStyle.getStyle().setWidth(384).setHeight(320))
canvas.renderText("Hello", TextStyle.getStyle().setAlign(Align.CENTER))
canvas.printCanvas(1, printResultCallback)

// ESC/POS Commands
printer.commandApi().sendEscCommand(byteArray)

// Cash Drawer
printer.cashDrawerApi().open(printResultCallback)
```

## Building

The plugin will be built automatically when you build your Tauri Android app:

```bash
npm run tauri android build
```

## Testing

You can test the plugin in the example app:

```bash
cd examples/tauri-app
npm run tauri android dev
```

## Available Commands

The following commands are exposed to the Tauri app:

### Basic Commands
- `ping(value: string)` - Test command that echoes back the value
- `init_printer()` - Initialize the Sunmi PrinterX SDK and get default printer
- `get_status()` - Get current printer initialization status

### Device Management
- `scan_devices()` - Scan for USB devices, returns array with vendorId, productId, etc.
- `connect_printer()` - Verify printer is ready (connection is automatic)
- `disconnect_printer()` - Managed automatically by SDK

### Printing Commands
- `print_text(text: string, fontSize?: number, align?: string)` - Print formatted text using Canvas API
  - `fontSize`: Font size in pixels (default: 24)
  - `align`: Text alignment - "left", "center", or "right" (default: "left")
- `print_line(text: string)` - Print a line of text
- `print_esc_pos(hex: string)` - Send raw ESC/POS commands to connected printer
- `print_esc_pos_network(printerIp: string, port: number, hex: string)` - Send ESC/POS commands to network printer via TCP/IP
- `feed_line(lines: number)` - Feed paper (sends ESC d n command)
- `cut_paper()` - Cut paper (sends GS V 0 command)
- `open_cash_drawer()` - Open cash drawer

## Canvas Printing Details

The plugin uses the Sunmi Canvas API for text rendering:

```kotlin
// Initialize canvas with paper width
canvas.initCanvas(
    BaseStyle.getStyle()
        .setWidth(48 * 8)   // 58mm = 384 dots
        .setHeight(40 * 8)  // Height in dots
)

// Render text with styling
canvas.renderText(
    "Your text here",
    TextStyle.getStyle()
        .setAlign(Align.CENTER)    // LEFT, CENTER, RIGHT
        .setTextSize(24)            // Font size
        .setPosX(10)                // X position
        .setPosY(10)                // Y position
        .enableBold(true)           // Optional bold
)

// Print the canvas
canvas.printCanvas(1, printResultCallback)
```

## ESC/POS Commands

The plugin automatically prefixes hex commands with `1C43FF` before sending:

```kotlin
// Your input: "1B40"
// Sent to printer: "1C43FF1B40"
```

Common ESC/POS commands:
- `1B40` - Initialize printer (ESC @)
- `1B64nn` - Feed n lines (ESC d n)
- `1D5600` - Full cut (GS V 0)
- `1D5601` - Partial cut (GS V 1)

## Network Printer Support

The plugin includes TCP/IP network printer support for printing to remote printers:

```kotlin
// Connect to network printer and send ESC/POS commands
val socket = Socket()
socket.connect(InetSocketAddress(printerIp, port), 5000)
val outputStream = socket.getOutputStream()

// Initialize printer
outputStream.write(byteArrayOf(0x1B, 0x40))

// Send command with prefix
val bytes = hexToBytes("1C43FF$hex")
outputStream.write(bytes)
outputStream.flush()
socket.close()
```

**Features:**
- 5-second connection timeout
- Automatic ESC @ initialization
- Support for standard ESC/POS network printers (port 9100)
- Background thread execution to avoid blocking

## Testing on Sunmi Devices

This plugin is designed for Sunmi commercial devices such as:
- Sunmi T2
- Sunmi V2 Pro
- Sunmi T2 Lite
- Sunmi T2s
- Any Sunmi device with built-in or USB printer support

**Network printing** works on any Android device with network access.
