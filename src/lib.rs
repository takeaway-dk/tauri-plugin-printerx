use tauri::{
  plugin::{Builder, TauriPlugin},
  Manager, Runtime,
};

pub use models::*;

#[cfg(desktop)]
mod desktop;
#[cfg(mobile)]
mod mobile;

mod commands;
mod error;
mod models;

pub use error::{Error, Result};

#[cfg(desktop)]
use desktop::Printerx;
#[cfg(mobile)]
use mobile::Printerx;

/// Extensions to [`tauri::App`], [`tauri::AppHandle`] and [`tauri::Window`] to access the printerx APIs.
pub trait PrinterxExt<R: Runtime> {
  fn printerx(&self) -> &Printerx<R>;
}

impl<R: Runtime, T: Manager<R>> crate::PrinterxExt<R> for T {
  fn printerx(&self) -> &Printerx<R> {
    self.state::<Printerx<R>>().inner()
  }
}

/// Initializes the plugin.
pub fn init<R: Runtime>() -> TauriPlugin<R> {
  Builder::new("printerx")
    .invoke_handler(tauri::generate_handler![
      commands::ping,
      commands::init_printer,
      commands::scan_devices,
      commands::connect_printer,
      commands::print_text,
      commands::print_line,
      commands::print_esc_pos,
      commands::print_esc_pos_network,
      commands::feed_line,
      commands::cut_paper,
      commands::open_cash_drawer,
      commands::get_cash_drawer_status,
      commands::get_printer_status,
      commands::get_printer_info,
      commands::lcd_show_text,
      commands::start_cash_drawer_monitor,
      commands::stop_cash_drawer_monitor,
      commands::get_status,
      commands::disconnect_printer
    ])
    .setup(|app, api| {
      #[cfg(mobile)]
      let printerx = mobile::init(app, api)?;
      #[cfg(desktop)]
      let printerx = desktop::init(app, api)?;
      app.manage(printerx);
      Ok(())
    })
    .build()
}
