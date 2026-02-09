# Tauri Plugin PrinterX

A Tauri plugin for Sunmi thermal printer support on Android using the Sunmi PrinterX SDK (printerx-1.0.17.aar).

## Features

- Initialize and connect to Sunmi thermal printers (built-in or USB)
- Network printer support via TCP/IP
- Scan for USB printer devices
- Print text with formatting options (font size, alignment)
- Send raw ESC/POS commands (local and network)
- Feed paper and cut commands
- Cash drawer control
- Cash drawer status + live events
- QueryApi: printer status + info
- LCD customer display
- Full TypeScript type support

## Installation

Add the plugin to your Tauri project:

```bash
npm install tauri-plugin-printerx
# or
yarn add tauri-plugin-printerx
```

In your Rust code (`src-tauri/src/main.rs` or `src-tauri/src/lib.rs`):

```rust
fn main() {
    tauri::Builder::default()
        .plugin(tauri_plugin_printerx::init())
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
```

## Usage

### TypeScript/JavaScript

```typescript
import { addPluginListener, invoke } from '@tauri-apps/api/core';

// Initialize printer (connects to default Sunmi printer)
await invoke('plugin:printerx|init_printer');

// Scan for USB devices
const devices = await invoke('plugin:printerx|scan_devices');
console.log(devices);

// Connect to printer (for USB printers use address from scan_devices)
// For built-in Sunmi printers, init_printer is usually enough
await invoke('plugin:printerx|connect_printer', { address: '00:11:22:33:44:55' });

// Print text with formatting
await invoke('plugin:printerx|print_text', {
  text: 'Hello, World!',
  fontSize: 32,
  align: 'center'
});

// Print a line
await invoke('plugin:printerx|print_line', {
  text: 'Simple line of text'
});

// Send raw ESC/POS commands (hex format)
await invoke('plugin:printerx|print_esc_pos', {
  hex: '1B40'  // ESC @ (initialize printer)
});

// Send ESC/POS commands to network printer
await invoke('plugin:printerx|print_esc_pos_network', {
  printerIp: '192.168.1.100',
  port: 9100,
  hex: '1B40'
});

// Feed paper
await invoke('plugin:printerx|feed_line', { lines: 3 });

// Cut paper
await invoke('plugin:printerx|cut_paper');

// Open cash drawer
await invoke('plugin:printerx|open_cash_drawer');

// Cash drawer status
const drawer = await invoke('plugin:printerx|get_cash_drawer_status');
console.log(drawer);

// Start live cash drawer events
await invoke('plugin:printerx|start_cash_drawer_monitor', { intervalMs: 1000 });
const listener = await addPluginListener('printerx', 'cash_drawer_status', (payload) => {
  console.log(payload);
});

// Stop live events
await invoke('plugin:printerx|stop_cash_drawer_monitor');
await listener.unregister();

// Get status
const status = await invoke('plugin:printerx|get_status');
console.log(status);

// QueryApi status + info
const printerStatus = await invoke('plugin:printerx|get_printer_status');
const printerName = await invoke('plugin:printerx|get_printer_info', { info: 'NAME' });
console.log(printerStatus, printerName);

// Customer display (LCD)
await invoke('plugin:printerx|lcd_show_text', { text: 'Velkommen!', size: 24, bold: false });

// Disconnect (managed automatically by SDK)
await invoke('plugin:printerx|disconnect_printer');
```

## API Reference

### `init_printer()`
Initialize the Sunmi PrinterX SDK and get the default printer (built-in or USB).

**Returns:** `Promise<void>`

**Errors:**
- Failed to initialize SDK
- No printer available

---

### `scan_devices()`
Scan for USB printer devices connected to the device.

**Returns:** `Promise<{ devices: Array<{ name: string, vendorId: string, productId: string, deviceClass: number }> }>`

---

### `connect_printer(address: string)`
Verify printer is ready. Connection is managed automatically by the Sunmi SDK.

**Returns:** `Promise<void>`

---

### `print_text(text: string, fontSize?: number, align?: string)`
Print formatted text.

**Parameters:**
- `text` - Text to print
- `fontSize` - Font size (default: 24)
- `align` - Text alignment: "left", "center", or "right" (default: "left")

**Returns:** `Promise<void>`

---

### `print_line(text: string)`
Print a single line of text with newline.

**Parameters:**
- `text` - Text to print

**Returns:** `Promise<void>`

---

### `print_esc_pos(hex: string)`
Send raw ESC/POS commands as hex string to the connected Sunmi printer.

**Parameters:**
- `hex` - Hex string without prefix (e.g., "1B40" for ESC @)

**Returns:** `Promise<void>`

**Note:** The command is automatically prefixed with "1C43FF" before sending.

---

### `print_esc_pos_network(printerIp: string, port: number, hex: string)`
Send raw ESC/POS commands to a network printer via TCP/IP.

**Parameters:**
- `printerIp` - IP address of the network printer (e.g., "192.168.1.100")
- `port` - Port number (typically 9100 for ESC/POS printers)
- `hex` - Hex string of ESC/POS commands

**Returns:** `Promise<void>`

**Note:**
- Automatically sends ESC @ (initialize) before the command
- Uses a 5-second connection timeout
- The command is automatically prefixed with "1C43FF" before sending

---

### `feed_line(lines: number)`
Feed paper by specified number of lines.

**Parameters:**
- `lines` - Number of lines to feed

**Returns:** `Promise<void>`

---

### `cut_paper()`
Send paper cut command to printer (ESC/POS: GS V 0).

**Returns:** `Promise<void>`

---

### `open_cash_drawer()`
Open the connected cash drawer.

**Returns:** `Promise<void>`

---

### `get_cash_drawer_status()`
Get current cash drawer status.

**Returns:** `Promise<{ success: boolean, open: boolean }>`

---

### `start_cash_drawer_monitor(intervalMs?: number)`
Start live cash drawer status events.

**Returns:** `Promise<void>`

**Event:** `cash_drawer_status` payload:
- `success: boolean`
- `open?: boolean`
- `changed?: boolean`
- `ts?: number`
- `error?: string`

---

### `stop_cash_drawer_monitor()`
Stop live cash drawer status events.

**Returns:** `Promise<void>`

---

### `get_status()`
Get current printer status.

**Returns:** `Promise<{ success: boolean, initialized: boolean, ready: boolean }>`

---

### `get_printer_status()`
QueryApi: get printer status and code.

**Returns:** `Promise<{ success: boolean, status: string, code: number }>`

---

### `get_printer_info(info: string)`
QueryApi: get printer info string.

**Allowed values:** `ID`, `NAME`, `VERSION`, `DISTANCE`, `CUTTER`, `HOT`, `DENSITY`, `TYPE`, `PAPER`, `GRAY`

**Returns:** `Promise<{ success: boolean, info: string, value: string }>`

---

### `lcd_show_text(text: string, size?: number, bold?: boolean)`
Show text on the customer display (LCD).

**Returns:** `Promise<void>`

---

### `disconnect_printer()`
Disconnect from the printer. Connection lifecycle is managed automatically by the Sunmi SDK.

**Returns:** `Promise<void>`

## Permissions / Capabilities

No extra Android runtime permissions are required for built-in Sunmi printers.

Tauri capabilities required for the new commands (example):

```json
{
  "permissions": [
    "printerx:allow-get-cash-drawer-status",
    "printerx:allow-start-cash-drawer-monitor",
    "printerx:allow-stop-cash-drawer-monitor",
    "printerx:allow-register-listener",
    "printerx:allow-remove-listener",
    "printerx:allow-get-printer-status",
    "printerx:allow-get-printer-info",
    "printerx:allow-lcd-show-text"
  ]
}
```

## Platform Support

- ✅ Android (Sunmi devices with built-in or USB printers)
- ❌ iOS (not applicable - Sunmi is Android-only)
- ❌ Desktop (returns error messages)

## Supported Devices

This plugin is designed for Sunmi commercial Android devices with:
- Built-in thermal printers
- USB thermal printers
- Cash drawer support

## Development

See [android/README.md](android/README.md) for Android-specific development instructions.

## License

MIT
