<template>
  <div class="app-container">
    <el-table
      v-loading="loading"
      :data="list"
      :row-class-name="tableRowClassName"
      border
      fit
      highlight-current-row
      style="width: 100%;"
      empty-text="No Log Found"
    >
      <el-table-column width="250px" align="center" label="Header">
        <template slot-scope="{ row }">
          <span>{{ row.header }}</span>
        </template>
      </el-table-column>

      <el-table-column width="250px" align="center" label="Time">
        <template slot-scope="{ row }">
          <span>{{ row.time | parseTime("YYYY-MM-DD HH:mm:ss") }}</span>
        </template>
      </el-table-column>

      <el-table-column align="center" label="Content">
        <template slot-scope="{ row }">
          <span>{{ row.content }}</span>
        </template>
      </el-table-column>

    </el-table>

    <pagination align="center" :currentPage="currentPage" :pageSize="pageSize" :total="totalRecords" @updateSize="updateSize" @updatePage="updatePage" />

  </div>
</template>

<script>
import { getLogsWithPage } from '@/api/log'

import Pagination from '@/components/Pagination'

const moment = require('moment')

export default {
  components: { Pagination },
  filters: {
    parseTime(time, format) {
      if (!time) {
        return 'N/A'
      }
      return moment(time).format(format)
    }
  },
  data() {
    return {
      list: null,
      totalRecords: 0,
      currentPage: 1,
      pageSize: 10,
      loading: true,
      refreshId: null
    }
  },
  mounted() {
    this.fetchData(this.currentPage, this.pageSize)
    this.refreshId = setInterval(() => {
      if (this.$route.path !== '/log') {
        clearInterval(this.refreshId)
      } else {
        this.fetchData(this.currentPage, this.pageSize)
      }
    }, 10000)
  },
  methods: {
    fetchData(page, size) {
      this.loading = true
      getLogsWithPage('System', page, size).then(resp => {
        this.list = resp.data.content
        this.totalRecords = resp.data.totalElements
        this.loading = false
      })
    },
    updateSize(size) {
      this.pageSize = size
      this.fetchData(1, this.pageSize)
    },
    updatePage(page) {
      this.currentPage = page
      this.fetchData(page, this.pageSize)
    },
    tableRowClassName({ row, index }) {
      return row.type.toLowerCase().concat('-row')
    }
  }
}
</script>

<style>
  .el-table .danger-row {
    background: #FEF0F0;
  }

  .el-table .success-row {
    background: #F0F9EB
  }

  .el-table .info-row {
    background: #F4F4F5
  }

  .el-table .warning-row {
    background: #FDF6EC
  }

  .el-table .normal-row {
    background: #ECF5FF
  }
</style>
