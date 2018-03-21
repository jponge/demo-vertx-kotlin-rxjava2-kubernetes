<template>
  <div class="container">
    <div class="columns">
      <div class="column">
        <h1 class="title is-1">📢 #zlack</h1>
        <div class="field is-grouped">
          <p class="control">
            <input class="input is-danger" type="text" placeholder="Your name">
          </p>
          <p class="control is-expanded">
            <input class="input" type="text" placeholder="Express yourself!">
          </p>
          <p class="control">
            <a class="button is-primary">
              Send
            </a>
          </p>
          <p class="control">
            <a class="button is-black">
              Reload
            </a>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ChatBox'
}
</script>


<style scoped>
.container {
  margin-top: 1em;
}
</style>
