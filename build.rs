const COMMANDS: &[&str] = &[
    "ping",
    "init_printer",
    "scan_devices",
    "connect_printer",
    "print_text",
    "print_line",
    "print_esc_pos",
    "print_esc_pos_network",
    "feed_line",
    "cut_paper",
    "open_cash_drawer",
    "get_status",
    "disconnect_printer"
];

fn main() {
  tauri_plugin::Builder::new(COMMANDS)
    .android_path("android")
    .ios_path("ios")
    .build();
}
