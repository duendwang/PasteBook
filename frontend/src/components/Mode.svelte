<script>
  import { onMount } from "svelte";

  export let darkMode = false;

  onMount(() => {
    if (localStorage.getItem("dark-mode") === null) {
      localStorage.setItem("dark-mode", "true");
    }

    darkMode = localStorage.getItem("dark-mode") === "true";

    if (darkMode) {
      document.body.classList.add("dark-mode");
      document.body.style.background = "#000000";
    } else {
      document.body.classList.remove("dark-mode");
      document.body.style.backgroundColor = "#ffffff";
    }
  });

  function toggleStyle() {
    darkMode = !darkMode;

    if (darkMode) {
      document.body.classList.add("dark-mode");
      localStorage.setItem("dark-mode", "true");
      document.body.style.background = "#000000";
    } else {
      document.body.classList.remove("dark-mode");
      localStorage.setItem("dark-mode", "false");
      document.body.style.backgroundColor = "#ffffff";
    }
  }
</script>

<container>
  {#if darkMode}
    <button class="style" on:click={toggleStyle}>LIGHT</button>
  {:else}
    <button class="style" on:click={toggleStyle}>DARK</button>
  {/if}
</container>

<style lang="scss">
  container {
    transition:
      background 0.5s ease,
      color 0.5s ease,
      transform 0.5s ease,
      bottom 0.5s ease;

    .style {
      all: unset;
      transition:
        background 0.5s ease,
        color 0.5s ease,
        transform 0.5s ease,
        bottom 0.5s ease;

      background: none;
      border: none;
      font: inherit;
      cursor: pointer;
      outline: inherit;

      position: fixed;
      margin: 0;
      right: 30px;
      bottom: 0;

      background: #000000;
      color: #fff;

      :global(body.dark-mode) & {
        color: black;
        background: white;
      }

      padding: 4px 8px;
      border-top-left-radius: 5px;
      border-top-right-radius: 5px;
      font-size: 12px;
      font-weight: bold;
      z-index: 9999;
      font-family: Gabarito, sans-serif;
      text-decoration: none;
      opacity: 0;

      animation: flyIn ease 0.7s forwards;
      animation-delay: 0.3s;
      animation-iteration-count: 1;

      &:hover {
        cursor: pointer;
        bottom: -2px;
      }
    }
  }

  @keyframes flyIn {
    0% {
      transform: translateY(90%);
      opacity: 0;
    }
    100% {
      transform: translateY(0%);
      opacity: 1;
    }
  }
</style>
