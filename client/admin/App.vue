<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script>
export default {
  name: 'AdminApp'
};
</script>

<style>
html,
body {
  height: 100%;
  margin: 0;
}

#app {
  min-height: 100vh;
  height: 100%;
  display: flex;
  flex-direction: column;
}
</style>
