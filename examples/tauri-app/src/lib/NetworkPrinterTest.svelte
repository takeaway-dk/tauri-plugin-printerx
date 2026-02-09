<script>
  import { invoke } from '@tauri-apps/api/core'

  let printerIp = $state('192.168.1.100')
  let port = $state(9100)
  let hexCommand = $state('1B40')
  let status = $state('')
  let isLoading = $state(false)

  // Predefined ESC/POS commands
  const commands = {
    initialize: { hex: '1B40', desc: 'Initialize printer (ESC @)' },
    testPrint: { hex: '1B40546573742050726F7072696E740A0A0A1D5600', desc: 'Print "Test Print" and cut' },
    centerAlign: { hex: '1B6101', desc: 'Center alignment (ESC a 1)' },
    leftAlign: { hex: '1B6100', desc: 'Left alignment (ESC a 0)' },
    boldOn: { hex: '1B4501', desc: 'Bold ON (ESC E 1)' },
    boldOff: { hex: '1B4500', desc: 'Bold OFF (ESC E 0)' },
    doubleSizeOn: { hex: '1B2101', desc: 'Double size ON (ESC ! 1)' },
    doubleSizeOff: { hex: '1B2100', desc: 'Double size OFF (ESC ! 0)' },
    feedLine: { hex: '1B6403', desc: 'Feed 3 lines (ESC d 3)' },
    cutPaper: { hex: '1D5600', desc: 'Cut paper (GS V 0)' }
  }

  async function sendCommand() {
    if (!printerIp) {
      status = '❌ Please enter printer IP address'
      return
    }

    if (!hexCommand) {
      status = '❌ Please enter hex command'
      return
    }

    isLoading = true
    status = `⏳ Sending to ${printerIp}:${port}...`

    try {
      await invoke('plugin:printerx|print_esc_pos_network', {
        printerIp,
        port,
        hex: hexCommand
      })

      status = `✅ Successfully sent command to ${printerIp}:${port}`
    } catch (error) {
      status = `❌ Error: ${error}`
    } finally {
      isLoading = false
    }
  }

  async function sendTestReceipt() {
    isLoading = true
    status = '⏳ Sending test receipt...'

    try {
      // Initialize printer
      await invoke('plugin:printerx|print_esc_pos_network', {
        printerIp, port, hex: '1B40'
      })

      // Center align
      await invoke('plugin:printerx|print_esc_pos_network', {
        printerIp, port, hex: '1B6101'
      })

      // Bold and double size
      await invoke('plugin:printerx|print_esc_pos_network', {
        printerIp, port, hex: '1B45011B2101'
      })

      // Print "TEST RECEIPT"
      await invoke('plugin:printerx|print_esc_pos_network', {
        printerIp, port,
        hex: '54455354205245434549505421' // "TEST RECEIPT!"
      })

      // Normal text
      await invoke('plugin:printerx|print_esc_pos_network', {
        printerIp, port, hex: '1B45001B21001B6100'
      })

      // Feed lines
      await invoke('plugin:printerx|print_esc_pos_network', {
        printerIp, port, hex: '1B6402'
      })

      // Print timestamp
      const timestamp = new Date().toISOString()
      const timestampHex = Array.from(timestamp)
        .map(c => c.charCodeAt(0).toString(16).padStart(2, '0'))
        .join('')

      await invoke('plugin:printerx|print_esc_pos_network', {
        printerIp, port, hex: timestampHex
      })

      // Feed and cut
      await invoke('plugin:printerx|print_esc_pos_network', {
        printerIp, port, hex: '1B64041D5600'
      })

      status = '✅ Test receipt sent successfully!'
    } catch (error) {
      status = `❌ Error: ${error}`
    } finally {
      isLoading = false
    }
  }

  function useCommand(cmd) {
    hexCommand = cmd.hex
    status = `📝 Selected: ${cmd.desc}`
  }
</script>

<div class="printer-test">
  <h2>🖨️ Network Printer Test</h2>

  <div class="form-group">
    <label>
      Printer IP Address:
      <input
        type="text"
        bind:value={printerIp}
        placeholder="192.168.1.100"
        disabled={isLoading}
      />
    </label>
  </div>

  <div class="form-group">
    <label>
      Port:
      <input
        type="number"
        bind:value={port}
        placeholder="9100"
        disabled={isLoading}
      />
    </label>
  </div>

  <div class="form-group">
    <label>
      Hex Command:
      <input
        type="text"
        bind:value={hexCommand}
        placeholder="1B40"
        disabled={isLoading}
      />
    </label>
  </div>

  <div class="button-group">
    <button onclick={sendCommand} disabled={isLoading}>
      📤 Send Command
    </button>
    <button onclick={sendTestReceipt} disabled={isLoading} class="primary">
      🧾 Send Test Receipt
    </button>
  </div>

  <div class="commands">
    <h3>Quick Commands:</h3>
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
      <li>Standard ESC/POS printer port er 9100</li>
      <li>Commands er automatisk prefixed med "1C43FF"</li>
      <li>Hex strings skal være uden spaces (f.eks. "1B40" ikke "1B 40")</li>
      <li>Test med "Initialize" kommandoen først</li>
    </ul>
  </div>
</div>

<style>
  .printer-test {
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
  }

  .form-group {
    margin-bottom: 15px;
  }

  label {
    display: block;
    margin-bottom: 5px;
    font-weight: 500;
  }

  input {
    width: 100%;
    padding: 8px 12px;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 14px;
  }

  input:disabled {
    background-color: #f5f5f5;
    cursor: not-allowed;
  }

  .button-group {
    display: flex;
    gap: 10px;
    margin: 20px 0;
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

  .commands {
    margin: 20px 0;
    padding: 15px;
    background: #f9f9f9;
    border-radius: 4px;
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
