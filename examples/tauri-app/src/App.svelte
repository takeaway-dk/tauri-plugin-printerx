<script>
  import Greet from './lib/Greet.svelte'
  import NetworkPrinterTest from './lib/NetworkPrinterTest.svelte'
  import BluetoothPrinterTest from './lib/BluetoothPrinterTest.svelte'
  import { ping } from 'tauri-plugin-printerx-api'

	let response = $state('')
	let showNetworkPrinterTest = $state(false)
	let showBluetoothPrinterTest = $state(false)

	function updateResponse(returnValue) {
		response += `[${new Date().toLocaleTimeString()}] ` + (typeof returnValue === 'string' ? returnValue : JSON.stringify(returnValue)) + '<br>'
	}

	function _ping() {
		ping("Pong!").then(updateResponse).catch(updateResponse)
	}

	function toggleNetworkPrinterTest() {
		showNetworkPrinterTest = !showNetworkPrinterTest
	}

	function toggleBluetoothPrinterTest() {
		showBluetoothPrinterTest = !showBluetoothPrinterTest
	}
</script>

<main class="container">
  <h1>Welcome to Tauri!</h1>

  <div class="row">
    <a href="https://vite.dev" target="_blank">
      <img src="/vite.svg" class="logo vite" alt="Vite Logo" />
    </a>
    <a href="https://tauri.app" target="_blank">
      <img src="/tauri.svg" class="logo tauri" alt="Tauri Logo" />
    </a>
    <a href="https://svelte.dev" target="_blank">
      <img src="/svelte.svg" class="logo svelte" alt="Svelte Logo" />
    </a>
  </div>

  <p>
    Click on the Tauri, Vite, and Svelte logos to learn more.
  </p>

  <div class="row">
    <Greet />
  </div>

  <div>
    <button onclick="{_ping}">Ping</button>
    <div>{@html response}</div>
  </div>

  <div class="printer-section">
    <button onclick={toggleBluetoothPrinterTest} class="printer-toggle bluetooth">
      {showBluetoothPrinterTest ? '🔽 Skjul' : '🔼 Vis'} Direkte Print på Sunmi (Android)
    </button>

    {#if showBluetoothPrinterTest}
      <BluetoothPrinterTest />
    {/if}
  </div>

  <div class="printer-section">
    <button onclick={toggleNetworkPrinterTest} class="printer-toggle network">
      {showNetworkPrinterTest ? '🔽 Skjul' : '🔼 Vis'} Network Printer Test
    </button>

    {#if showNetworkPrinterTest}
      <NetworkPrinterTest />
    {/if}
  </div>

</main>

<style>
  .logo.vite:hover {
    filter: drop-shadow(0 0 2em #747bff);
  }

  .logo.svelte:hover {
    filter: drop-shadow(0 0 2em #ff3e00);
  }

  .printer-section {
    margin-top: 20px;
    padding-top: 20px;
    border-top: 2px solid #eee;
  }

  .printer-toggle {
    padding: 12px 24px;
    font-size: 16px;
    font-weight: 500;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    transition: background 0.2s;
  }

  .printer-toggle.bluetooth {
    background: #2196f3;
  }

  .printer-toggle.bluetooth:hover {
    background: #1976d2;
  }

  .printer-toggle.network {
    background: #4caf50;
  }

  .printer-toggle.network:hover {
    background: #45a049;
  }
</style>
