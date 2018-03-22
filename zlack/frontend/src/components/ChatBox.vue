<template>
  <div class="container">
    <h1 class="title is-1">📢 #zlack</h1>

    <div class="field is-grouped">
      <p class="control">
        <input class="input" v-model="author" v-bind:class="{ 'is-danger' : authorEmpty }" type="text" placeholder="Your name" autofocus>
      </p>
    </div>
    <div class="field is-grouped">
      <p class="control is-expanded">
        <input class="input" v-model="content" v-on:keyup.enter="send" type="text" placeholder="Express yourself!">
      </p>
      <p class="control">
        <a class="button is-primary" v-on:click="send">
          Send
        </a>
      </p>
      <p class="control">
        <a class="button is-black" v-on:click="fetchAllMessages">
          Reload
        </a>
      </p>
    </div>

    <div>
      <p class="chat-line" v-for="message in messages" :key="message.id">
        <span class="has-text-grey-light">{{ new Date(message.timestamp).toLocaleTimeString() }}</span>
        <span class="has-text-weight-semibold">{{ message.author }}:</span>
        <span>{{ message.content }}</span>
      </p>
    </div>

  </div>
</template>

<script>
import EventBusClient from 'vertx3-eventbus-client'

export default {
  name: 'ChatBox',
  data () {
    return {
      author: "",
      content: "",
      messages: [],
      eventBus: new EventBusClient('/eventbus')
    }
  },
  mounted () {
    this.eventBus.enableReconnect(true)
    this.eventBus.onopen = () => {
      this.fetchAllMessages()
      this.eventBus.registerHandler('events.new-message', (err, msg) => {
        this.messages.unshift(msg.body)
      })
    }
  },
  computed: {
    authorEmpty () {
      return this.author.trim().length == 0
    },
    contentEmpty () {
      return this.content.trim().length == 0
    }
  },
  methods: {
    fetchAllMessages: function () {
      this.eventBus.send('messages.get-all', {}, (err, reply) => {
        if (err !== null) {
          console.log(`Could not get all messages: ${JSON.stringify(err)}`)
          return
        }
        const messages = reply.body.messages
        messages.reverse()
        this.messages = messages
      })
    },
    send: function () {
      if (this.authorEmpty || this.contentEmpty) {
        return
      }
      this.eventBus.send('messages.store', {
        author: this.author,
        content: this.content
      })
      this.content = ""
    }
  }
}
</script>

<style scoped>
.container {
  margin: 1em;
}
.chat-line > span {
  padding-right: 0.33em;
}
</style>
