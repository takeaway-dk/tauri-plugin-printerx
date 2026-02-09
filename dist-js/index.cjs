'use strict';

var core = require('@tauri-apps/api/core');

async function ping(value) {
    return await core.invoke('plugin:printerx|ping', {
        payload: {
            value,
        },
    }).then((r) => (r.value ? r.value : null));
}
/**
 * Initialize the printer controller and verify Bluetooth availability
 */
async function initPrinter() {
    await core.invoke('plugin:printerx|init_printer');
}
/**
 * Scan for paired Bluetooth devices
 */
async function scanDevices() {
    return await core.invoke('plugin:printerx|scan_devices');
}
/**
 * Connect to a printer using its Bluetooth MAC address
 * @param address Bluetooth MAC address (e.g., "00:11:22:33:44:55")
 */
async function connectPrinter(address) {
    await core.invoke('plugin:printerx|connect_printer', { address });
}
/**
 * Print formatted text
 * @param text Text to print
 * @param fontSize Font size (default: 24)
 * @param align Text alignment: "left", "center", or "right" (default: "left")
 */
async function printText(text, fontSize, align) {
    await core.invoke('plugin:printerx|print_text', { text, fontSize, align });
}
/**
 * Print a single line of text with newline
 * @param text Text to print
 */
async function printLine(text) {
    await core.invoke('plugin:printerx|print_line', { text });
}
/**
 * Send raw ESC/POS commands as hex string
 * @param hex Hex string (e.g., "1B40" for initialize)
 */
async function printEscPos(hex) {
    await core.invoke('plugin:printerx|print_esc_pos', { hex });
}
/**
 * Send raw ESC/POS commands to a network printer via TCP/IP
 * @param printerIp IP address of the network printer
 * @param port Port number (default: 9100)
 * @param hex Hex string of ESC/POS commands
 */
async function printEscPosNetwork(printerIp, port, hex) {
    await core.invoke('plugin:printerx|print_esc_pos_network', { printer_ip: printerIp, port, hex });
}
/**
 * Feed paper by specified number of lines
 * @param lines Number of lines to feed
 */
async function feedLine(lines) {
    await core.invoke('plugin:printerx|feed_line', { lines });
}
/**
 * Send paper cut command to printer
 */
async function cutPaper() {
    await core.invoke('plugin:printerx|cut_paper');
}
/**
 * Open the cash drawer
 */
async function openCashDrawer() {
    await core.invoke('plugin:printerx|open_cash_drawer');
}
/**
 * Get current printer status
 */
async function getStatus() {
    return await core.invoke('plugin:printerx|get_status');
}
/**
 * Disconnect from the printer
 */
async function disconnectPrinter() {
    await core.invoke('plugin:printerx|disconnect_printer');
}

exports.connectPrinter = connectPrinter;
exports.cutPaper = cutPaper;
exports.disconnectPrinter = disconnectPrinter;
exports.feedLine = feedLine;
exports.getStatus = getStatus;
exports.initPrinter = initPrinter;
exports.openCashDrawer = openCashDrawer;
exports.ping = ping;
exports.printEscPos = printEscPos;
exports.printEscPosNetwork = printEscPosNetwork;
exports.printLine = printLine;
exports.printText = printText;
exports.scanDevices = scanDevices;
