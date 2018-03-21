<template>
  <div id="app">
    <chat-box/>
  </div>
</template>

<script>
import ChatBox from './components/ChatBox.vue'

export default {
  name: 'app',
  components: {
    'chat-box': ChatBox
  }
}
</script>

<style>

</style>
