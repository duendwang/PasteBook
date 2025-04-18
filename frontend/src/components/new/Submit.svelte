<script lang="ts">
  import { goto } from "$app/navigation";
  import { expire, wrap, writableTitle } from "$lib/stores";
  import { writableContent } from "$lib/stores";
  import { onMount } from "svelte";

  onMount(() => {
    writableTitle.subscribe(() => {
      allFieldsFilled();
    });

    writableContent.subscribe(() => {
      allFieldsFilled();
    });
  });

  let alreadyUploading = false;

  function onClick() {
    if ($writableTitle === "") {
      let title = document.getElementById("title") as HTMLInputElement;
      title.focus();
      shakeElement(title, 500);

      return;
    }

    if (!allFieldsFilled()) {
      return;
    }

    if (alreadyUploading) {
      return;
    }

    if ($writableTitle === "") {
      $writableTitle = "Untitled";
    }

    let submit = document.getElementsByClassName("submit")[0] as HTMLElement;
    submit.classList.add("loading");

    alreadyUploading = true;
    const xhr = new XMLHttpRequest();

    let domain = window.location.host;

    if (domain.includes("localhost")) {
      xhr.open("POST", `http://localhost/api/upload`);
    } else {
      if (domain.match(/192\.168\.\d+\.\d+/)) {
        domain = domain.replace(/:\d+/, "");

        xhr.open("POST", `http://${domain}/api/upload`);
      } else {
        xhr.open("POST", `https://${domain}/api/upload`);
      }
    }

    xhr.setRequestHeader("Content-Type", "plain/text");
    xhr.setRequestHeader("access-control-allow-methods", "POST");
    xhr.setRequestHeader("title", $writableTitle);
    xhr.setRequestHeader("wrap", String($wrap));
    xhr.setRequestHeader("expires", String($expire));

    xhr.send($writableContent);
    xhr.responseType = "text";
    xhr.onload = function () {
      if (xhr.status !== 200) {
        submit.classList.remove("loading");
        submit.innerText = "ERROR: " + xhr.status;
        alreadyUploading = false;

        setTimeout(() => {
          submit.innerText = "SUBMIT";
        }, 3000);
        return;
      }

      goto("/p/" + xhr.response);
    };
  }

  function allFieldsFilled() {
    let ready = $writableContent !== "";
    let submitButton = document.querySelector(".submit") as HTMLElement;

    if (!submitButton) {
      return false;
    }

    if (ready) {
      submitButton.classList.add("ready");
      submitButton.classList.remove("not-ready");
    } else {
      submitButton.classList.remove("ready");
      submitButton.classList.add("not-ready");
    }

    return ready;
  }

  const shakeElement = (element: HTMLElement, duration: number) => {
    const startTime = Date.now();
    const shakeInterval = 1000 / 60;

    const shake = () => {
      const elapsedTime = Date.now() - startTime;
      const progress = elapsedTime / duration;
      const fullShakes = Math.floor(progress / 0.25);

      if (fullShakes % 2 === 0) {
        element.style.transform = "translateX(-15px)";
      } else {
        element.style.transform = "translateX(15px)";
      }

      if (progress < 1) {
        setTimeout(shake, shakeInterval);
      } else {
        element.style.transform = "translateX(0)";
      }
    };

    shake();
  };
</script>

<container>
  <button class="submit" on:click={onClick}>SUBMIT</button>
</container>

<style lang="scss">
  container {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    height: 50px;
    padding-bottom: 10px;
    transition: transform 0.5s ease;

    &:active {
      transform: scale(0.96);
    }

    .submit {
      margin: 0;
      width: 200px;
      height: 50px;
      border-radius: 17px;
      font-family: Gabarito, sans-serif;
      font-size: 20px;
      font-weight: 800;
      transition:
        opacity 0.5s,
        transform 0.5s,
        background-color 0.5s;
      background-color: #eeeeee;
      color: black;
      text-decoration: none;
      border: 1px solid #c9c9c9;

      animation: fadeIn 0.5s forwards;

      :global(.dark-mode) & {
        background-color: #1a1a1a;
        border: 1px solid #333;
        color: white;
      }

      &:global(.loading) {
        animation: blink 3s infinite;
        color: #999999;
      }

      &:hover:global(.loading) {
        cursor: not-allowed;
      }

      &:hover:not(.loading) {
        background-color: #cfcfcf;

        :global(.dark-mode) & {
          background-color: #333;
        }

        cursor: pointer;
      }

      &:active {
        transform: scale(0.96);
      }
    }

    :global(.dark-mode) & {
      color: white;
    }
  }

  @keyframes blink {
    0% {
      opacity: 0.5;
      transform: scale(1);
    }
    50% {
      opacity: 1;
      transform: scale(1.02);
    }
    100% {
      opacity: 0.5;
      transform: scale(1);
    }
  }

  @keyframes fadeIn {
    from {
      opacity: 0;
    }

    to {
      opacity: 1;
    }
  }
</style>
