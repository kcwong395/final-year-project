<template>
  <el-dialog
    title="Worker Log"
    :visible.sync="boxShow"
    width="70%"
    :before-close="close"
  >
    <el-card>
      <el-table
        :data="activities"
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

    </el-card>
    <span slot="footer" class="dialog-footer">
      <el-button @click="close()">Close</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { getLogsByElfIdWithPages } from '@/api/log'
import Pagination from '@/components/Pagination'
export default {
  components: { Pagination },
  props: {
    show: Boolean,
    elfId: String
  },
  data() {
    return {
      boxShow: false,
      refreshId: null,
      activities: [],
      totalRecords: 0,
      currentPage: 1,
      pageSize: 10
    }
  },
  watch: {
    show() {
      this.boxShow = this.show
      if (this.boxShow) {
        this.fetchData()
        this.refreshId = setInterval(() => {
          if (this.$route.path !== '/management/machine') {
            clearInterval(this.refreshId)
          } else {
            this.fetchData()
          }
        }, 10000)
      } else {
        clearInterval(this.refreshId)
      }
    }
  },
  methods: {
    close() {
      this.$emit('close')
    },
    fetchData() {
      getLogsByElfIdWithPages(this.elfId, this.currentPage, this.pageSize).then(resp => {
        this.activities = resp.data.content
        this.totalRecords = resp.data.totalElements
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
