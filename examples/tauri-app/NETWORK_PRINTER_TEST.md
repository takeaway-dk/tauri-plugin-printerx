# Network Printer Test Guide

## 🚀 Sådan kører du testen

### 1. Start Development Server

```bash
cd examples/tauri-app
npm install
npm run tauri dev
# eller
npm run tauri android dev
```

### 2. Find din printer's IP adresse

**På Sunmi device:**
- Åbn Settings → Network → Wi-Fi
- Find den tilsluttede netværk
- Notér IP adressen

**På Windows:**
```cmd
ipconfig
```

**På Linux/Mac:**
```bash
ifconfig
# eller
ip addr
```

### 3. Test forbindelsen til printeren

Brug `ping` kommandoen til at verificere at printeren er tilgængelig:

```bash
ping 192.168.1.100
```

### 4. Brug test interfacet

1. Klik på "🔼 Show Network Printer Test" knappen
2. Indtast printer IP adresse (f.eks. `192.168.1.100`)
3. Indtast port (standard: `9100`)
4. Test med en af metoderne:

#### Metode A: Quick Commands
- Klik på "Initialize printer (ESC @)" først
- Prøv derefter andre kommandoer

#### Metode B: Custom Hex Command
- Indtast din hex kommando (f.eks. `1B40`)
- Klik "📤 Send Command"

#### Metode C: Test Receipt
- Klik "🧾 Send Test Receipt"
- Dette sender en komplet test kvittering med:
  - Initialisering
  - Center alignment
  - Bold og double size text
  - "TEST RECEIPT!"
  - Timestamp
  - Paper cut

## 📋 ESC/POS Command Reference

### Grundlæggende Kommandoer

| Kommando | Hex | Beskrivelse |
|----------|-----|-------------|
| Initialize | `1B40` | ESC @ - Reset printer |
| Left align | `1B6100` | ESC a 0 |
| Center align | `1B6101` | ESC a 1 |
| Right align | `1B6102` | ESC a 2 |
| Bold ON | `1B4501` | ESC E 1 |
| Bold OFF | `1B4500` | ESC E 0 |
| Underline ON | `1B2D01` | ESC - 1 |
| Underline OFF | `1B2D00` | ESC - 0 |
| Double size ON | `1B2101` | ESC ! 1 |
| Double size OFF | `1B2100` | ESC ! 0 |
| Feed 1 line | `1B6401` | ESC d 1 |
| Feed 3 lines | `1B6403` | ESC d 3 |
| Cut paper | `1D5600` | GS V 0 |
| Partial cut | `1D5601` | GS V 1 |

### Text Printing

For at printe text skal du konvertere til hex:

**Eksempel: Print "Hello"**
```
H = 48 (hex)
e = 65 (hex)
l = 6C (hex)
l = 6C (hex)
o = 6F (hex)

Hex: 48656C6C6F
```

**JavaScript helper:**
```javascript
function textToHex(text) {
  return Array.from(text)
    .map(c => c.charCodeAt(0).toString(16).padStart(2, '0'))
    .join('');
}

textToHex("Hello") // "48656c6c6f"
```

## 🧪 Test Scenarios

### Test 1: Basic Connection
```javascript
// IP: 192.168.1.100
// Port: 9100
// Hex: 1B40
// Expected: Printer initializes (no visible output)
```

### Test 2: Print Simple Text
```javascript
// Hex: 1B4048656C6C6F0A0A
// Breakdown:
// 1B40 = Initialize
// 48656C6C6F = "Hello"
// 0A0A = Two line feeds
```

### Test 3: Formatted Receipt
```javascript
// Center + Bold + "RECEIPT"
// Hex: 1B61011B45015245434549505421
// Then cut: 1D5600
```

### Test 4: Danish Characters
```javascript
// æ = C3A6
// ø = C3B8
// å = C3A5
// Æ = C386
// Ø = C398
// Å = C385

// "Købmand" = 4BC3B8626D616E64
```

## 🔧 Troubleshooting

### "Network error: Connection refused"
- Verificer at printeren er tændt
- Check at IP adressen er korrekt
- Verificer at port 9100 er åben
- Prøv at ping printeren

### "Network error: Connection timeout"
- Printeren er måske ikke på samme netværk
- Firewall blokerer forbindelsen
- Prøv at øge timeout i koden

### "Print command sent but nothing prints"
- Prøv at sende initialize kommando først (`1B40`)
- Check at hex kommandoen er korrekt formateret
- Verificer at printeren understøtter ESC/POS

### "Invalid hex string"
- Hex strings skal være lige længde (par af tegn)
- Kun 0-9 og A-F er gyldige
- Ingen spaces eller special tegn

## 📱 Test på Android Device

```bash
# Build og installer på device
npm run tauri android dev

# Eller build APK
npm run tauri android build
adb install src-tauri/gen/android/app/build/outputs/apk/debug/app-debug.apk
```

## 🌐 Network Printer Emulator

Hvis du ikke har en fysisk printer, kan du bruge en emulator til test:

**Windows/Linux/Mac:**
```bash
# Installer netcat og lyt på port 9100
nc -l 9100

# Eller med Python
python3 -c "
import socket
s = socket.socket()
s.bind(('0.0.0.0', 9100))
s.listen(1)
print('Listening on port 9100...')
while True:
    conn, addr = s.accept()
    print(f'Connection from {addr}')
    data = conn.recv(1024)
    print(f'Received: {data.hex()}')
    conn.close()
"
```

## ✅ Success Indicators

Når alt virker korrekt:
- ✅ Status viser "Successfully sent command"
- ✅ Printeren laver lyd/bevægelse
- ✅ Kvittering printer ud (hvis ikke kun initialize)
- ✅ Ingen timeout errors

## 📚 Resources

- [ESC/POS Command Reference](https://reference.epson-biz.com/modules/ref_escpos/index.php)
- [Sunmi Developer Docs](https://developer.sunmi.com/)
- [ASCII to Hex Converter](https://www.rapidtables.com/convert/number/ascii-to-hex.html)
