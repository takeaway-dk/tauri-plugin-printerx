use tauri::{AppHandle, command, Runtime};

use crate::models::*;
use crate::Result;
use crate::PrinterxExt;

#[command]
pub(crate) async fn ping<R: Runtime>(
    app: AppHandle<R>,
    payload: PingRequest,
) -> Result<PingResponse> {
    app.printerx().ping(payload)
}

#[command]
pub(crate) async fn init_printer<R: Runtime>(
    app: AppHandle<R>,
) -> Result<()> {
    app.printerx().init_printer()
}

#[command]
pub(crate) async fn scan_devices<R: Runtime>(
    app: AppHandle<R>,
) -> Result<serde_json::Value> {
    app.printerx().scan_devices()
}

#[command]
pub(crate) async fn connect_printer<R: Runtime>(
    app: AppHandle<R>,
    address: String,
) -> Result<()> {
    app.printerx().connect_printer(address)
}

#[command]
pub(crate) async fn print_text<R: Runtime>(
    app: AppHandle<R>,
    text: String,
    font_size: Option<i32>,
    align: Option<String>,
) -> Result<()> {
    app.printerx().print_text(text, font_size, align)
}

#[command]
pub(crate) async fn print_line<R: Runtime>(
    app: AppHandle<R>,
    text: String,
) -> Result<()> {
    app.printerx().print_line(text)
}

#[command]
pub(crate) async fn print_esc_pos<R: Runtime>(
    app: AppHandle<R>,
    hex: String,
) -> Result<()> {
    app.printerx().print_esc_pos(hex)
}

#[command]
pub(crate) async fn print_esc_pos_network<R: Runtime>(
    app: AppHandle<R>,
    printer_ip: String,
    port: i32,
    hex: String,
) -> Result<()> {
    app.printerx().print_esc_pos_network(printer_ip, port, hex)
}

#[command]
pub(crate) async fn feed_line<R: Runtime>(
    app: AppHandle<R>,
    lines: i32,
) -> Result<()> {
    app.printerx().feed_line(lines)
}

#[command]
pub(crate) async fn cut_paper<R: Runtime>(
    app: AppHandle<R>,
) -> Result<()> {
    app.printerx().cut_paper()
}

#[command]
pub(crate) async fn open_cash_drawer<R: Runtime>(
    app: AppHandle<R>,
) -> Result<()> {
    app.printerx().open_cash_drawer()
}

#[command]
pub(crate) async fn get_status<R: Runtime>(
    app: AppHandle<R>,
) -> Result<serde_json::Value> {
    app.printerx().get_status()
}

#[command]
pub(crate) async fn get_cash_drawer_status<R: Runtime>(
    app: AppHandle<R>,
) -> Result<serde_json::Value> {
    app.printerx().get_cash_drawer_status()
}

#[command]
pub(crate) async fn get_printer_status<R: Runtime>(
    app: AppHandle<R>,
) -> Result<serde_json::Value> {
    app.printerx().get_printer_status()
}

#[command]
pub(crate) async fn get_printer_info<R: Runtime>(
    app: AppHandle<R>,
    info: String,
) -> Result<serde_json::Value> {
    app.printerx().get_printer_info(info)
}

#[command]
pub(crate) async fn lcd_show_text<R: Runtime>(
    app: AppHandle<R>,
    text: String,
    size: Option<i32>,
    bold: Option<bool>,
) -> Result<()> {
    app.printerx().lcd_show_text(text, size, bold)
}

#[command]
pub(crate) async fn start_cash_drawer_monitor<R: Runtime>(
    app: AppHandle<R>,
    interval_ms: Option<i64>,
) -> Result<()> {
    app.printerx().start_cash_drawer_monitor(interval_ms)
}

#[command]
pub(crate) async fn stop_cash_drawer_monitor<R: Runtime>(
    app: AppHandle<R>,
) -> Result<()> {
    app.printerx().stop_cash_drawer_monitor()
}

#[command]
pub(crate) async fn disconnect_printer<R: Runtime>(
    app: AppHandle<R>,
) -> Result<()> {
    app.printerx().disconnect_printer()
}
