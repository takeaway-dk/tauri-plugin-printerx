use serde::de::DeserializeOwned;
use tauri::{
    plugin::{PluginApi, PluginHandle},
    AppHandle, Runtime,
};

use crate::models::*;

#[cfg(target_os = "ios")]
tauri::ios_plugin_binding!(init_plugin_printerx);

// initializes the Kotlin or Swift plugin classes
pub fn init<R: Runtime, C: DeserializeOwned>(
    _app: &AppHandle<R>,
    api: PluginApi<R, C>,
) -> crate::Result<Printerx<R>> {
    #[cfg(target_os = "android")]
    let handle = api.register_android_plugin("com.plugin.printerx", "PrinterxPlugin")?;
    #[cfg(target_os = "ios")]
    let handle = api.register_ios_plugin(init_plugin_printerx)?;
    Ok(Printerx(handle))
}

/// Access to the printerx APIs.
pub struct Printerx<R: Runtime>(PluginHandle<R>);

impl<R: Runtime> Printerx<R> {
    pub fn ping(&self, payload: PingRequest) -> crate::Result<PingResponse> {
        self.0
            .run_mobile_plugin("ping", payload)
            .map_err(Into::into)
    }

    pub fn init_printer(&self) -> crate::Result<()> {
        self.0
            .run_mobile_plugin("initPrinter", ())
            .map_err(Into::into)
    }

    pub fn scan_devices(&self) -> crate::Result<serde_json::Value> {
        self.0
            .run_mobile_plugin("scanDevices", ())
            .map_err(Into::into)
    }

    pub fn connect_printer(&self, address: String) -> crate::Result<()> {
        self.0
            .run_mobile_plugin("connectPrinter", serde_json::json!({ "address": address }))
            .map_err(Into::into)
    }

    pub fn print_text(
        &self,
        text: String,
        font_size: Option<i32>,
        align: Option<String>,
    ) -> crate::Result<()> {
        self.0
            .run_mobile_plugin(
                "printText",
                serde_json::json!({
                  "text": text,
                  "fontSize": font_size,
                  "align": align
                }),
            )
            .map_err(Into::into)
    }

    pub fn print_line(&self, text: String) -> crate::Result<()> {
        self.0
            .run_mobile_plugin("printLine", serde_json::json!({ "text": text }))
            .map_err(Into::into)
    }

    pub fn print_esc_pos(&self, hex: String) -> crate::Result<()> {
        self.0
            .run_mobile_plugin("printEscPos", serde_json::json!({ "hex": hex }))
            .map_err(Into::into)
    }

    pub fn print_esc_pos_network(
        &self,
        printer_ip: String,
        port: i32,
        hex: String,
    ) -> crate::Result<()> {
        self.0
            .run_mobile_plugin(
                "printEscPosNetwork",
                serde_json::json!({
                  "printerIp": printer_ip,
                  "port": port,
                  "hex": hex
                }),
            )
            .map_err(Into::into)
    }

    pub fn feed_line(&self, lines: i32) -> crate::Result<()> {
        self.0
            .run_mobile_plugin("feedLine", serde_json::json!({ "lines": lines }))
            .map_err(Into::into)
    }

    pub fn cut_paper(&self) -> crate::Result<()> {
        self.0.run_mobile_plugin("cutPaper", ()).map_err(Into::into)
    }

    pub fn open_cash_drawer(&self) -> crate::Result<()> {
        self.0
            .run_mobile_plugin("openCashDrawer", ())
            .map_err(Into::into)
    }

    pub fn get_status(&self) -> crate::Result<serde_json::Value> {
        self.0
            .run_mobile_plugin("getStatus", ())
            .map_err(Into::into)
    }

    pub fn get_cash_drawer_status(&self) -> crate::Result<serde_json::Value> {
        self.0
            .run_mobile_plugin("getCashDrawerStatus", ())
            .map_err(Into::into)
    }

    pub fn get_printer_status(&self) -> crate::Result<serde_json::Value> {
        self.0
            .run_mobile_plugin("getPrinterStatus", ())
            .map_err(Into::into)
    }

    pub fn get_printer_info(&self, info: String) -> crate::Result<serde_json::Value> {
        self.0
            .run_mobile_plugin("getPrinterInfo", serde_json::json!({ "info": info }))
            .map_err(Into::into)
    }

    pub fn lcd_show_text(
        &self,
        text: String,
        size: Option<i32>,
        bold: Option<bool>,
    ) -> crate::Result<()> {
        self.0
            .run_mobile_plugin(
                "lcdShowText",
                serde_json::json!({
                    "text": text,
                    "size": size,
                    "bold": bold
                }),
            )
            .map_err(Into::into)
    }

    pub fn start_cash_drawer_monitor(&self, interval_ms: Option<i64>) -> crate::Result<()> {
        self.0
            .run_mobile_plugin(
                "startCashDrawerMonitor",
                serde_json::json!({ "intervalMs": interval_ms }),
            )
            .map_err(Into::into)
    }

    pub fn stop_cash_drawer_monitor(&self) -> crate::Result<()> {
        self.0
            .run_mobile_plugin("stopCashDrawerMonitor", ())
            .map_err(Into::into)
    }

    pub fn disconnect_printer(&self) -> crate::Result<()> {
        self.0
            .run_mobile_plugin("disconnectPrinter", ())
            .map_err(Into::into)
    }
}
