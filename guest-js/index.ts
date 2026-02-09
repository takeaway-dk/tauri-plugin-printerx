import { invoke } from '@tauri-apps/api/core'

export interface PrinterDevice {
  name: string;
  address: string;
}

export interface ScanDevicesResponse {
  success: boolean;
  devices: PrinterDevice[];
}

export interface PrinterStatus {
  success: boolean;
  connected: boolean;
  status: string;
}

export interface QueryStatusResponse {
  success: boolean;
  status: string;
  code: number;
}

export interface QueryInfoResponse {
  success: boolean;
  info: string;
  value: string;
}


export interface CashDrawerStatus {
  success: boolean;
  open: boolean;
}

export interface CashDrawerStatusEvent extends CashDrawerStatus {
  changed?: boolean;
  ts?: number;
  error?: string;
}

export interface PrintTextOptions {
  text: string;
  fontSize?: number;
  align?: 'left' | 'center' | 'right';
}

export async function ping(value: string): Promise<string | null> {
  return await invoke<{value?: string}>('plugin:printerx|ping', {
    payload: {
      value,
    },
  }).then((r) => (r.value ? r.value : null));
}

/**
 * Initialize the printer controller and verify Bluetooth availability
 */
export async function initPrinter(): Promise<void> {
  await invoke('plugin:printerx|init_printer');
}

/**
 * Scan for paired Bluetooth devices
 */
export async function scanDevices(): Promise<ScanDevicesResponse> {
  return await invoke<ScanDevicesResponse>('plugin:printerx|scan_devices');
}

/**
 * Connect to a printer using its Bluetooth MAC address
 * @param address Bluetooth MAC address (e.g., "00:11:22:33:44:55")
 */
export async function connectPrinter(address: string): Promise<void> {
  await invoke('plugin:printerx|connect_printer', { address });
}

/**
 * Print formatted text
 * @param text Text to print
 * @param fontSize Font size (default: 24)
 * @param align Text alignment: "left", "center", or "right" (default: "left")
 */
export async function printText(text: string, fontSize?: number, align?: 'left' | 'center' | 'right'): Promise<void> {
  await invoke('plugin:printerx|print_text', { text, fontSize, align });
}

/**
 * Print a single line of text with newline
 * @param text Text to print
 */
export async function printLine(text: string): Promise<void> {
  await invoke('plugin:printerx|print_line', { text });
}

/**
 * Send raw ESC/POS commands as hex string
 * @param hex Hex string (e.g., "1B40" for initialize)
 */
export async function printEscPos(hex: string): Promise<void> {
  await invoke('plugin:printerx|print_esc_pos', { hex });
}

/**
 * Send raw ESC/POS commands to a network printer via TCP/IP
 * @param printerIp IP address of the network printer
 * @param port Port number (default: 9100)
 * @param hex Hex string of ESC/POS commands
 */
export async function printEscPosNetwork(printerIp: string, port: number, hex: string): Promise<void> {
  await invoke('plugin:printerx|print_esc_pos_network', { printerIp, port, hex });
}

/**
 * Feed paper by specified number of lines
 * @param lines Number of lines to feed
 */
export async function feedLine(lines: number): Promise<void> {
  await invoke('plugin:printerx|feed_line', { lines });
}

/**
 * Send paper cut command to printer
 */
export async function cutPaper(): Promise<void> {
  await invoke('plugin:printerx|cut_paper');
}

/**
 * Open the cash drawer
 */
export async function openCashDrawer(): Promise<void> {
  await invoke('plugin:printerx|open_cash_drawer');
}

/**
 * Get current printer status
 */
export async function getStatus(): Promise<PrinterStatus> {
  return await invoke<PrinterStatus>('plugin:printerx|get_status');
}

/**
 * Get printer status from QueryApi
 */
export async function getPrinterStatus(): Promise<QueryStatusResponse> {
  return await invoke<QueryStatusResponse>('plugin:printerx|get_printer_status');
}

/**
 * Get printer info from QueryApi
 */
export async function getPrinterInfo(info: string): Promise<QueryInfoResponse> {
  return await invoke<QueryInfoResponse>('plugin:printerx|get_printer_info', { info });
}


/**
 * Get current cash drawer status
 */
export async function getCashDrawerStatus(): Promise<CashDrawerStatus> {
  return await invoke<CashDrawerStatus>('plugin:printerx|get_cash_drawer_status');
}

/**
 * Start cash drawer monitor (native event stream)
 */
export async function startCashDrawerMonitor(intervalMs?: number): Promise<void> {
  await invoke('plugin:printerx|start_cash_drawer_monitor', { intervalMs });
}

/**
 * Stop cash drawer monitor (native event stream)
 */
export async function stopCashDrawerMonitor(): Promise<void> {
  await invoke('plugin:printerx|stop_cash_drawer_monitor');
}

/**
 * Show text on customer display (LCD)
 */
export async function lcdShowText(text: string, size?: number, bold?: boolean): Promise<void> {
  await invoke('plugin:printerx|lcd_show_text', { text, size, bold });
}

/**
 * Disconnect from the printer
 */
export async function disconnectPrinter(): Promise<void> {
  await invoke('plugin:printerx|disconnect_printer');
}
