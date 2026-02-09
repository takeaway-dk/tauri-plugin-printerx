use serde::de::DeserializeOwned;
use std::io::Write;
use std::net::TcpStream;
use tauri::{plugin::PluginApi, AppHandle, Runtime};

use crate::models::*;

pub fn init<R: Runtime, C: DeserializeOwned>(
    app: &AppHandle<R>,
    _api: PluginApi<R, C>,
) -> crate::Result<Printerx<R>> {
    Ok(Printerx(app.clone()))
}

/// Access to the printerx APIs.
pub struct Printerx<R: Runtime>(AppHandle<R>);

impl<R: Runtime> Printerx<R> {
    pub fn ping(&self, payload: PingRequest) -> crate::Result<PingResponse> {
        Ok(PingResponse {
            value: payload.value,
        })
    }

    pub fn init_printer(&self) -> crate::Result<()> {
        Err(crate::Error::String(
            "Printer functionality is only available on mobile platforms".into(),
        ))
    }

    pub fn scan_devices(&self) -> crate::Result<serde_json::Value> {
        Err(crate::Error::String(
            "Printer functionality is only available on mobile platforms".into(),
        ))
    }

    pub fn connect_printer(&self, _address: String) -> crate::Result<()> {
        Err(crate::Error::String(
            "Printer functionality is only available on mobile platforms".into(),
        ))
    }

    pub fn print_text(
        &self,
        _text: String,
        _font_size: Option<i32>,
        _align: Option<String>,
    ) -> crate::Result<()> {
        Err(crate::Error::String(
            "Printer functionality is only available on mobile platforms".into(),
        ))
    }

    pub fn print_line(&self, _text: String) -> crate::Result<()> {
        Err(crate::Error::String(
            "Printer functionality is only available on mobile platforms".into(),
        ))
    }

    pub fn print_esc_pos(&self, _hex: String) -> crate::Result<()> {
        Err(crate::Error::String(
            "Printer functionality is only available on mobile platforms".into(),
        ))
    }

    pub fn print_esc_pos_network(
        &self,
        printer_ip: String,
        port: i32,
        hex: String,
    ) -> crate::Result<()> {
        // Parse hex string to bytes
        let hex_clean = hex.replace(" ", "").replace("\n", "").replace("\r", "");
        let bytes = (0..hex_clean.len())
            .step_by(2)
            .map(|i| u8::from_str_radix(&hex_clean[i..i + 2], 16))
            .collect::<Result<Vec<u8>, _>>()
            .map_err(|e| crate::Error::String(format!("Invalid hex string: {}", e)))?;

        // Connect to printer via TCP
        let address = format!("{}:{}", printer_ip, port);
        let mut stream = TcpStream::connect(&address).map_err(|e| {
            crate::Error::String(format!(
                "Failed to connect to printer at {}: {}",
                address, e
            ))
        })?;

        // Send data to printer
        stream
            .write_all(&bytes)
            .map_err(|e| crate::Error::String(format!("Failed to send data to printer: {}", e)))?;

        stream
            .flush()
            .map_err(|e| crate::Error::String(format!("Failed to flush data to printer: {}", e)))?;

        Ok(())
    }

    pub fn feed_line(&self, _lines: i32) -> crate::Result<()> {
        Err(crate::Error::String(
            "Printer functionality is only available on mobile platforms".into(),
        ))
    }

    pub fn cut_paper(&self) -> crate::Result<()> {
        Err(crate::Error::String(
            "Printer functionality is only available on mobile platforms".into(),
        ))
    }

    pub fn open_cash_drawer(&self) -> crate::Result<()> {
        Err(crate::Error::String(
            "Printer functionality is only available on mobile platforms".into(),
        ))
    }

    pub fn get_status(&self) -> crate::Result<serde_json::Value> {
        Err(crate::Error::String(
            "Printer functionality is only available on mobile platforms".into(),
        ))
    }

    pub fn get_cash_drawer_status(&self) -> crate::Result<serde_json::Value> {
        Err(crate::Error::String(
            "Printer functionality is only available on mobile platforms".into(),
        ))
    }

    pub fn get_printer_status(&self) -> crate::Result<serde_json::Value> {
        Err(crate::Error::String(
            "Printer functionality is only available on mobile platforms".into(),
        ))
    }

    pub fn get_printer_info(&self, _info: String) -> crate::Result<serde_json::Value> {
        Err(crate::Error::String(
            "Printer functionality is only available on mobile platforms".into(),
        ))
    }

    pub fn lcd_show_text(
        &self,
        _text: String,
        _size: Option<i32>,
        _bold: Option<bool>,
    ) -> crate::Result<()> {
        Err(crate::Error::String(
            "Printer functionality is only available on mobile platforms".into(),
        ))
    }

    pub fn start_cash_drawer_monitor(&self, _interval_ms: Option<i64>) -> crate::Result<()> {
        Err(crate::Error::String(
            "Printer functionality is only available on mobile platforms".into(),
        ))
    }

    pub fn stop_cash_drawer_monitor(&self) -> crate::Result<()> {
        Err(crate::Error::String(
            "Printer functionality is only available on mobile platforms".into(),
        ))
    }

    pub fn disconnect_printer(&self) -> crate::Result<()> {
        Err(crate::Error::String(
            "Printer functionality is only available on mobile platforms".into(),
        ))
    }
}
