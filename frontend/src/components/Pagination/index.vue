<template>
  <el-pagination
    layout="total, sizes, prev, pager, next, jumper"
    :current-page="currentPage"
    :total="total"
    :page-sizes="pageSizes"
    :page-size="pageSize"
    @size-change="handleSizeChange"
    @current-change="handlePageChange"
  />
</template>

<script>
export default {
  props: {
    total: Number,
    currentPage: Number,
    pageSize: Number
  },
  data() {
    return {
      pageSizes: [10, 50, 100]
    }
  },
  methods: {
    handleSizeChange(size) {
      this.$emit('updateSize', size)
    },
    handlePageChange(page) {
      this.$emit('updatePage', page)
    }
  }
}
</script>
