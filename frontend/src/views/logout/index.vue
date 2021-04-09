<template>
    <span>Logout</span>
</template>

<script>
export default {
  beforeMount() {
    this.logout()
  },
  methods: {
    async logout() {
      await this.$store.dispatch('user/logout')
      this.$router.push('/login')
    }
  }
}
</script>
