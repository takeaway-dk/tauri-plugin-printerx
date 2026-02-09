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
export interface PrintTextOptions {
    text: string;
    fontSize?: number;
    align?: 'left' | 'center' | 'right';
}
export declare function ping(value: string): Promise<string | null>;
/**
 * Initialize the printer controller and verify Bluetooth availability
 */
export declare function initPrinter(): Promise<void>;
/**
 * Scan for paired Bluetooth devices
 */
export declare function scanDevices(): Promise<ScanDevicesResponse>;
/**
 * Connect to a printer using its Bluetooth MAC address
 * @param address Bluetooth MAC address (e.g., "00:11:22:33:44:55")
 */
export declare function connectPrinter(address: string): Promise<void>;
/**
 * Print formatted text
 * @param text Text to print
 * @param fontSize Font size (default: 24)
 * @param align Text alignment: "left", "center", or "right" (default: "left")
 */
export declare function printText(text: string, fontSize?: number, align?: 'left' | 'center' | 'right'): Promise<void>;
/**
 * Print a single line of text with newline
 * @param text Text to print
 */
export declare function printLine(text: string): Promise<void>;
/**
 * Send raw ESC/POS commands as hex string
 * @param hex Hex string (e.g., "1B40" for initialize)
 */
export declare function printEscPos(hex: string): Promise<void>;
/**
 * Send raw ESC/POS commands to a network printer via TCP/IP
 * @param printerIp IP address of the network printer
 * @param port Port number (default: 9100)
 * @param hex Hex string of ESC/POS commands
 */
export declare function printEscPosNetwork(printerIp: string, port: number, hex: string): Promise<void>;
/**
 * Feed paper by specified number of lines
 * @param lines Number of lines to feed
 */
export declare function feedLine(lines: number): Promise<void>;
/**
 * Send paper cut command to printer
 */
export declare function cutPaper(): Promise<void>;
/**
 * Open the cash drawer
 */
export declare function openCashDrawer(): Promise<void>;
/**
 * Get current printer status
 */
export declare function getStatus(): Promise<PrinterStatus>;
/**
 * Disconnect from the printer
 */
export declare function disconnectPrinter(): Promise<void>;
