<template>
  <div class="block">
    <el-timeline>
      <el-timeline-item v-for="(item,index) of activities" :key="index" :timestamp="item.time" placement="top">
        <el-card>
          <h4>{{ item.header }}</h4>
          <p>{{ item.content }}</p>
        </el-card>
      </el-timeline-item>
    </el-timeline>
  </div>
</template>

<script>
export default {
  props: {
    activities: Array
  },
  data() {
    return { }
  }
}
</script>
