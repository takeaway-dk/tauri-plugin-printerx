<script>
  import { onDestroy } from 'svelte'
  import { addPluginListener, invoke } from '@tauri-apps/api/core'

  let devices = $state([])
  let selectedDevice = $state('')
  let status = $state('')
  let isLoading = $state(false)
  let isConnected = $state(false)
  let hexCommand = $state('1B40')
  let textInput = $state('Test Print')
  let lcdText = $state('Velkommen!')
  let lcdSize = $state(24)
  let lcdBold = $state(false)
  let queryInfoKey = $state('NAME')
  let drawerOpen = $state(null)
  let drawerStatus = $state('Ukendt')
  let isDrawerMonitoring = $state(false)
  let drawerListener = null
  let drawerMonitorId = null

  function formatInvokeError(error) {
    if (typeof error === 'string') {
      try {
        const parsed = JSON.parse(error)
        return JSON.stringify(parsed, null, 2)
      } catch {
        return error
      }
    }

    if (error && typeof error === 'object') {
      try {
        return JSON.stringify(error, null, 2)
      } catch {
        return String(error)
      }
    }

    return String(error)
  }

  // Predefined ESC/POS commands
  const commands = {
    initialize: { hex: '1B40', desc: 'Initialize printer (ESC @)' },
    testPrint: { hex: '1B40546573742050726F7072696E740A0A0A1D5600', desc: 'Print "Test Print" and cut' },
    centerAlign: { hex: '1B6101', desc: 'Center alignment (ESC a 1)' },
    leftAlign: { hex: '1B6100', desc: 'Left alignment (ESC a 0)' },
    boldOn: { hex: '1B4501', desc: 'Bold ON (ESC E 1)' },
    boldOff: { hex: '1B4500', desc: 'Bold OFF (ESC E 0)' },
    feedLine: { hex: '1B6403', desc: 'Feed 3 lines (ESC d 3)' },
    cutPaper: { hex: '1D5600', desc: 'Cut paper (GS V 0)' }
  }

  async function initPrinter() {
    isLoading = true
    status = '⏳ Initialiserer Sunmi printer...'

    try {
      await invoke('plugin:printerx|init_printer')
      status = '✅ Sunmi printer initialiseret og klar!'
      isConnected = true
    } catch (error) {
      const details = formatInvokeError(error)
      status = `❌ Fejl: ${details}\n(Virker kun på Sunmi Android enheder)`
      isConnected = false
    } finally {
      isLoading = false
    }
  }

  const printerInfoKeys = [
    'ID',
    'NAME',
    'VERSION',
    'DISTANCE',
    'CUTTER',
    'HOT',
    'DENSITY',
    'TYPE',
    'PAPER',
    'GRAY'
  ]


  async function scanDevices() {
    isLoading = true
    status = '⏳ Scanner efter USB enheder...'

    try {
      const result = await invoke('plugin:printerx|scan_devices')
      devices = result.devices || []
      status = `✅ Fandt ${devices.length} USB enheder`
    } catch (error) {
      status = `❌ Fejl: ${error}`
      devices = []
    } finally {
      isLoading = false
    }
  }

  async function connectPrinter() {
    if (!selectedDevice) {
      status = '❌ Vælg venligst en printer'
      return
    }

    isLoading = true
    status = `⏳ Forbinder til ${selectedDevice}...`

    try {
      await invoke('plugin:printerx|connect_printer', { address: selectedDevice })
      isConnected = true
      status = `✅ Forbundet til ${selectedDevice}`
    } catch (error) {
      status = `❌ Fejl: ${error}`
      isConnected = false
    } finally {
      isLoading = false
    }
  }

  async function disconnectPrinter() {
    isLoading = true
    status = '⏳ Afbryder forbindelse...'

    try {
      await invoke('plugin:printerx|disconnect_printer')
      isConnected = false
      status = '✅ Forbindelse afbrudt'
    } catch (error) {
      status = `❌ Fejl: ${error}`
    } finally {
      isLoading = false
    }
  }

  async function printText() {
    if (!isConnected) {
      status = '❌ Forbind til en printer først'
      return
    }

    isLoading = true
    status = '⏳ Printer tekst...'

    try {
      await invoke('plugin:printerx|print_line', { text: textInput })
      status = '✅ Tekst printet!'
    } catch (error) {
      status = `❌ Fejl: ${error}`
    } finally {
      isLoading = false
    }
  }

  async function sendCommand() {
    if (!isConnected) {
      status = '❌ Forbind til en printer først'
      return
    }

    if (!hexCommand) {
      status = '❌ Indtast venligst en hex kommando'
      return
    }

    isLoading = true
    status = '⏳ Sender kommando...'

    try {
      await invoke('plugin:printerx|print_esc_pos', { hex: hexCommand })
      status = '✅ Kommando sendt!'
    } catch (error) {
      status = `❌ Fejl: ${error}`
    } finally {
      isLoading = false
    }
  }

  async function printTestReceipt() {
    if (!isConnected) {
      status = '❌ Forbind til en printer først'
      return
    }

    isLoading = true
    status = '⏳ Printer test kvittering...'

    try {
      // Initialize
      await invoke('plugin:printerx|print_esc_pos', { hex: '1B40' })

      // Center align
      await invoke('plugin:printerx|print_esc_pos', { hex: '1B6101' })

      // Bold and double size
      await invoke('plugin:printerx|print_esc_pos', { hex: '1B45011B2101' })

      // Print "TEST KVITTERING"
      const header = 'TEST KVITTERING'
      const headerHex = Array.from(header)
        .map(c => c.charCodeAt(0).toString(16).padStart(2, '0'))
        .join('')
      await invoke('plugin:printerx|print_esc_pos', { hex: headerHex })

      // Normal text + left align
      await invoke('plugin:printerx|print_esc_pos', { hex: '1B45001B21001B6100' })

      // Feed lines
      await invoke('plugin:printerx|print_esc_pos', { hex: '1B6402' })

      // Print timestamp
      const timestamp = new Date().toLocaleString('da-DK')
      const timestampHex = Array.from(timestamp)
        .map(c => c.charCodeAt(0).toString(16).padStart(2, '0'))
        .join('')
      await invoke('plugin:printerx|print_esc_pos', { hex: timestampHex })

      // Feed and cut
      await invoke('plugin:printerx|print_esc_pos', { hex: '1B64041D5600' })

      status = '✅ Test kvittering printet!'
    } catch (error) {
      status = `❌ Fejl: ${error}`
    } finally {
      isLoading = false
    }
  }

  async function printExample() {
    if (!isConnected) {
      status = '❌ Forbind til en printer først'
      return
    }

    isLoading = true
    status = '⏳ Printer eksempel...'

    try {
      await invoke('plugin:printerx|print_line', { text: '--- EKSEMPEL KVITTERING ---' })
      await invoke('plugin:printerx|print_text', { text: 'Sunmi PrinterX', fontSize: 28, align: 'center' })
      await invoke('plugin:printerx|print_line', { text: 'Dato: ' + new Date().toLocaleString('da-DK') })
      await invoke('plugin:printerx|print_line', { text: 'Vare 1            25,00' })
      await invoke('plugin:printerx|print_line', { text: 'Vare 2            10,00' })
      await invoke('plugin:printerx|print_line', { text: '------------------------' })
      await invoke('plugin:printerx|print_line', { text: 'I alt             35,00' })
      await invoke('plugin:printerx|feed_line', { lines: 2 })
      await invoke('plugin:printerx|cut_paper')

      status = '✅ Eksempel printet!'
    } catch (error) {
      const details = formatInvokeError(error)
      status = `❌ Fejl: ${details}`
    } finally {
      isLoading = false
    }
  }

  async function getPrinterStatus() {
    if (!isConnected) {
      status = '❌ Forbind til en printer først'
      return
    }

    isLoading = true
    status = '⏳ Henter printer status...'

    try {
      const result = await invoke('plugin:printerx|get_printer_status')
      status = `✅ Printer status: ${result.status} (code: ${result.code})`
    } catch (error) {
      const details = formatInvokeError(error)
      status = `❌ Fejl: ${details}`
    } finally {
      isLoading = false
    }
  }

  async function getPrinterInfo() {
    if (!isConnected) {
      status = '❌ Forbind til en printer først'
      return
    }

    isLoading = true
    status = `⏳ Henter info (${queryInfoKey})...`

    try {
      const result = await invoke('plugin:printerx|get_printer_info', { info: queryInfoKey })
      status = `✅ Info ${result.info}: ${result.value}`
    } catch (error) {
      const details = formatInvokeError(error)
      status = `❌ Fejl: ${details}`
    } finally {
      isLoading = false
    }
  }


  async function lcdShowText() {
    if (!isConnected) {
      status = '❌ Forbind til en printer først'
      return
    }

    isLoading = true
    status = '⏳ Sender tekst til kunde display...'

    try {
      await invoke('plugin:printerx|lcd_show_text', { text: lcdText, size: lcdSize, bold: lcdBold })
      status = '✅ Tekst sendt til kunde display!'
    } catch (error) {
      const details = formatInvokeError(error)
      status = `❌ Fejl: ${details}`
    } finally {
      isLoading = false
    }
  }

  async function openCashDrawer() {
    if (!isConnected) {
      status = '❌ Forbind til en printer først'
      return
    }

    isLoading = true
    status = '⏳ Åbner kasseskuffe...'

    try {
      await invoke('plugin:printerx|open_cash_drawer')
      status = '✅ Kasseskuffe åbnet!'
      await refreshDrawerStatus(false)
    } catch (error) {
      const details = formatInvokeError(error)
      status = `❌ Fejl: ${details}`
    } finally {
      isLoading = false
    }
  }

  async function refreshDrawerStatus(showMessage = true) {
    if (!isConnected) {
      if (showMessage) {
        status = '❌ Forbind til en printer først'
      }
      return
    }

    try {
      const result = await invoke('plugin:printerx|get_cash_drawer_status')
      if (result && typeof result === 'object' && 'open' in result) {
        drawerOpen = Boolean(result.open)
        const time = new Date().toLocaleTimeString('da-DK')
        drawerStatus = `${drawerOpen ? 'Åben' : 'Lukket'} (${time})`
        if (showMessage) {
          status = `✅ Kasseskuffe: ${drawerStatus}`
        }
      } else if (showMessage) {
        status = '❌ Ugyldigt svar fra kasseskuffe status'
      }
    } catch (error) {
      const details = formatInvokeError(error)
      if (showMessage) {
        status = `❌ Fejl: ${details}`
      }
    }
  }

  function startDrawerMonitor() {
    if (isDrawerMonitoring) {
      return
    }

    isDrawerMonitoring = true
    invoke('plugin:printerx|start_cash_drawer_monitor', { intervalMs: 1000 })
      .then(async () => {
        try {
          drawerListener = await addPluginListener('printerx', 'cash_drawer_status', (payload) => {
            if (payload && payload.success === false) {
              if (payload.error) {
                status = `❌ Fejl: ${payload.error}`
              }
              return
            }

            if (payload && 'open' in payload) {
              drawerOpen = Boolean(payload.open)
              const time = new Date().toLocaleTimeString('da-DK')
              drawerStatus = `${drawerOpen ? 'Åben' : 'Lukket'} (${time})`
            }
          })
        } catch (error) {
          const details = formatInvokeError(error)
          status = `⚠️ Live events fejlede, fallback til polling: ${details}`
          startDrawerPolling()
        }
      })
      .catch((error) => {
        const details = formatInvokeError(error)
        status = `⚠️ Live start fejlede, fallback til polling: ${details}`
        startDrawerPolling()
      })
  }

  function stopDrawerMonitor() {
    invoke('plugin:printerx|stop_cash_drawer_monitor').catch(() => {})
    if (drawerListener) {
      drawerListener.unregister()
      drawerListener = null
    }
    stopDrawerPolling()
    isDrawerMonitoring = false
  }

  onDestroy(() => {
    if (drawerListener) {
      drawerListener.unregister()
    }
    stopDrawerPolling()
    invoke('plugin:printerx|stop_cash_drawer_monitor').catch(() => {})
  })

  function startDrawerPolling() {
    if (drawerMonitorId) {
      return
    }

    refreshDrawerStatus(false)
    drawerMonitorId = setInterval(() => {
      refreshDrawerStatus(false)
    }, 1000)
  }

  function stopDrawerPolling() {
    if (!drawerMonitorId) {
      return
    }

    clearInterval(drawerMonitorId)
    drawerMonitorId = null
  }

  async function getStatus() {
    isLoading = true
    status = '⏳ Henter printer status...'

    try {
      const result = await invoke('plugin:printerx|get_status')
      status = `✅ Status: ${JSON.stringify(result, null, 2)}`
      if (result && typeof result === 'object' && 'ready' in result) {
        isConnected = Boolean(result.ready)
      }
    } catch (error) {
      status = `❌ Fejl: ${error}`
    } finally {
      isLoading = false
    }
  }

  function useCommand(cmd) {
    hexCommand = cmd.hex
    status = `📝 Valgt: ${cmd.desc}`
  }
</script>

<div class="bluetooth-test">
  <h2>🖨️ Direkte Print på Sunmi</h2>

  <div class="connection-section">
    <h3>Sunmi Printer Forbindelse</h3>

    <div class="button-group">
      <button onclick={initPrinter} disabled={isLoading}>
        🔧 Initialiser Sunmi Printer
      </button>
      <button onclick={scanDevices} disabled={isLoading}>
        🔍 Scan USB Enheder
      </button>
      <button onclick={getStatus} disabled={isLoading || !isConnected}>
        📊 Hent Status
      </button>
    </div>

    {#if devices.length > 0}
      <div class="form-group">
        <label>
          Vælg Printer:
          <select bind:value={selectedDevice} disabled={isLoading || isConnected}>
            <option value="">-- Vælg en enhed --</option>
            {#each devices as device}
              <option value={device.address}>
                {device.name} ({device.address})
              </option>
            {/each}
          </select>
        </label>
      </div>
    {/if}

    <div class="button-group">
      <button
        onclick={connectPrinter}
        disabled={isLoading || !selectedDevice || isConnected}
        class="primary"
      >
        🔗 Forbind
      </button>
      <button
        onclick={disconnectPrinter}
        disabled={isLoading || !isConnected}
        class="danger"
      >
        ⛔ Afbryd
      </button>
    </div>

    {#if isConnected}
      <div class="connection-status">
        ✅ Forbundet til Sunmi printer - Klar til print
      </div>
    {/if}
  </div>

  <div class="print-section">
    <h3>Print Funktioner</h3>

    <div class="form-group">
      <label>
        Tekst til print:
        <input
          type="text"
          bind:value={textInput}
          placeholder="Indtast tekst"
          disabled={isLoading || !isConnected}
        />
      </label>
    </div>

    <div class="button-group">
      <button onclick={printText} disabled={isLoading || !isConnected}>
        📝 Print Tekst
      </button>
      <button onclick={printTestReceipt} disabled={isLoading || !isConnected} class="primary">
        🧾 Print Test Kvittering
      </button>
      <button onclick={printExample} disabled={isLoading || !isConnected}>
        ✅ Print Eksempel
      </button>
      <button onclick={openCashDrawer} disabled={isLoading || !isConnected}>
        💰 Åbn Kasseskuffe
      </button>
    </div>

    <div class="form-group">
      <label>
        Hex Kommando:
        <input
          type="text"
          bind:value={hexCommand}
          placeholder="1B40"
          disabled={isLoading || !isConnected}
        />
      </label>
    </div>

    <button onclick={sendCommand} disabled={isLoading || !isConnected}>
      📤 Send Hex Kommando
    </button>

    <div class="drawer-section">
      <h3>Kasseskuffe</h3>
      <div class="button-group">
        <button onclick={openCashDrawer} disabled={isLoading || !isConnected}>
          💰 Åbn Kasseskuffe
        </button>
        <button onclick={() => refreshDrawerStatus(true)} disabled={isLoading || !isConnected}>
          🔄 Hent Status
        </button>
        {#if isDrawerMonitoring}
          <button onclick={stopDrawerMonitor} disabled={isLoading} class="danger">
            ⏹ Stop Live Status
          </button>
        {:else}
          <button onclick={startDrawerMonitor} disabled={isLoading || !isConnected} class="primary">
            ▶️ Start Live Status
          </button>
        {/if}
      </div>

      <div class="drawer-status">
        <span
          class="drawer-indicator"
          class:open={drawerOpen === true}
          class:closed={drawerOpen === false}
          class:unknown={drawerOpen === null}
        ></span>
        <span class="drawer-label">Status: {drawerStatus}</span>
        {#if isDrawerMonitoring}
          <span class="drawer-live">LIVE</span>
        {/if}
      </div>
    </div>
  </div>

  <div class="query-section">
    <h3>QueryApi</h3>

    <div class="button-group">
      <button onclick={getPrinterStatus} disabled={isLoading || !isConnected} class="primary">
        📊 Hent Printer Status
      </button>
    </div>

    <div class="form-group">
      <label>
        Printer Info:
        <select bind:value={queryInfoKey} disabled={isLoading || !isConnected}>
          {#each printerInfoKeys as key}
            <option value={key}>{key}</option>
          {/each}
        </select>
      </label>
    </div>
    <button onclick={getPrinterInfo} disabled={isLoading || !isConnected}>
      🔎 Hent Info
    </button>

  </div>

  <div class="lcd-section">
    <h3>Kunde Display (LCD)</h3>

    <div class="form-group">
      <label>
        Tekst:
        <input
          type="text"
          bind:value={lcdText}
          placeholder="Velkommen!"
          disabled={isLoading || !isConnected}
        />
      </label>
    </div>

    <div class="form-group">
      <label>
        Tekst størrelse:
        <input
          type="number"
          min="8"
          max="48"
          bind:value={lcdSize}
          disabled={isLoading || !isConnected}
        />
      </label>
    </div>

    <div class="form-group">
      <label>
        <input type="checkbox" bind:checked={lcdBold} disabled={isLoading || !isConnected} />
        Fed tekst
      </label>
    </div>

    <button onclick={lcdShowText} disabled={isLoading || !isConnected} class="primary">
      🖥️ Vis tekst på display
    </button>
  </div>

  <div class="commands">
    <h3>Hurtig Kommandoer:</h3>
    <div class="command-grid">
      {#each Object.entries(commands) as [key, cmd]}
        <button
          onclick={() => useCommand(cmd)}
          class="command-btn"
          disabled={isLoading}
          title={cmd.desc}
        >
          {cmd.desc}
        </button>
      {/each}
    </div>
  </div>

  {#if status}
    <div class="status" class:error={status.includes('❌')} class:success={status.includes('✅')}>
      {status}
    </div>
  {/if}

  <div class="help">
    <h3>💡 Tips:</h3>
    <ul>
      <li>Denne funktion virker <strong>KUN på Sunmi Android enheder</strong></li>
      <li>Bruger Sunmi PrinterX SDK til direkte kommunikation</li>
      <li>Start med at initialisere printeren (finder automatisk Sunmi printer)</li>
      <li>Scan viser USB enheder (valgfrit)</li>
      <li>PrinterX håndterer forbindelse automatisk</li>
      <li>Kommandoer prefixes automatisk med "1C43FF" (Sunmi format)</li>
    </ul>
  </div>
</div>

<style>
  .bluetooth-test {
    max-width: 800px;
    margin: 0 auto;
    padding: 20px;
  }

  h2 {
    margin-bottom: 20px;
    color: #333;
  }

  h3 {
    margin-top: 20px;
    margin-bottom: 10px;
    font-size: 1.1em;
    color: #555;
  }

  .connection-section,
  .print-section,
  .query-section,
  .lcd-section {
    margin-bottom: 30px;
    padding: 20px;
    background: #f5f5f5;
    border-radius: 8px;
  }

  .form-group {
    margin-bottom: 15px;
  }

  label {
    display: block;
    margin-bottom: 5px;
    font-weight: 500;
  }

  input,
  select {
    width: 100%;
    padding: 8px 12px;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 14px;
  }

  input:disabled,
  select:disabled {
    background-color: #e9e9e9;
    cursor: not-allowed;
  }

  .button-group {
    display: flex;
    gap: 10px;
    margin: 15px 0;
    flex-wrap: wrap;
  }

  button {
    padding: 10px 20px;
    border: 1px solid #ddd;
    border-radius: 4px;
    background: white;
    cursor: pointer;
    font-size: 14px;
    transition: all 0.2s;
  }

  button:hover:not(:disabled) {
    background: #f0f0f0;
  }

  button:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  button.primary {
    background: #0066cc;
    color: white;
    border-color: #0066cc;
  }

  button.primary:hover:not(:disabled) {
    background: #0052a3;
  }

  button.danger {
    background: #dc3545;
    color: white;
    border-color: #dc3545;
  }

  button.danger:hover:not(:disabled) {
    background: #c82333;
  }

  .connection-status {
    padding: 12px;
    background: #d4edda;
    border: 1px solid #c3e6cb;
    border-radius: 4px;
    color: #155724;
    font-weight: 500;
    margin-top: 10px;
  }

  .commands {
    margin: 20px 0;
    padding: 15px;
    background: #f9f9f9;
    border-radius: 4px;
  }

  .drawer-section {
    margin-top: 20px;
    padding: 15px;
    background: #f9f9f9;
    border-radius: 4px;
  }

  .drawer-status {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-top: 10px;
    font-weight: 500;
    color: #333;
  }

  .drawer-indicator {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    background: #9e9e9e;
    box-shadow: 0 0 0 2px rgba(0, 0, 0, 0.05);
  }

  .drawer-indicator.open {
    background: #4caf50;
    box-shadow: 0 0 0 2px rgba(76, 175, 80, 0.2);
  }

  .drawer-indicator.closed {
    background: #f44336;
    box-shadow: 0 0 0 2px rgba(244, 67, 54, 0.2);
  }

  .drawer-indicator.unknown {
    background: #9e9e9e;
  }

  .drawer-live {
    font-size: 12px;
    color: #1565c0;
    background: #e3f2fd;
    border-radius: 12px;
    padding: 2px 8px;
    letter-spacing: 0.5px;
  }

  .command-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 8px;
    margin-top: 10px;
  }

  .command-btn {
    padding: 8px 12px;
    font-size: 12px;
    text-align: left;
    background: white;
  }

  .status {
    padding: 15px;
    margin: 20px 0;
    border-radius: 4px;
    background: #e3f2fd;
    border-left: 4px solid #2196f3;
    white-space: pre-wrap;
    word-wrap: break-word;
  }

  .status.error {
    background: #ffebee;
    border-left-color: #f44336;
  }

  .status.success {
    background: #e8f5e9;
    border-left-color: #4caf50;
  }

  .help {
    margin-top: 30px;
    padding: 15px;
    background: #fff3cd;
    border-radius: 4px;
    border-left: 4px solid #ffc107;
  }

  .help ul {
    margin: 10px 0 0 20px;
  }

  .help li {
    margin-bottom: 5px;
  }
</style>
