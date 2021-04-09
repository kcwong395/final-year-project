<template>
  <div class="app-container">
    <el-table
      v-loading="loading"
      :data="list"
      border
      fit
      highlight-current-row
      style="width: 100%;"
      empty-text="No Tasks Created"
    >
      <el-table-column type="index" align="center" width="50" />

      <el-table-column align="center" label="Task Id" width="320">
        <template slot-scope="{ row }">
          <span>{{ row.id }}</span>
        </template>
      </el-table-column>

      <el-table-column class-name="status-col" align="center" label="Status" width="120">
        <template slot-scope="{ row }">
          <el-tag :type="row.status | statusFilter">
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column width="250px" align="center" label="Create Time">
        <template slot-scope="{ row }">
          <span>{{ row.createTime | parseTime("YYYY-MM-DD HH:mm:ss") }}</span>
        </template>
      </el-table-column>

      <el-table-column width="250px" align="center" label="End Time">
        <template slot-scope="{ row }">
          <span>{{ row.endTime | parseTime("YYYY-MM-DD HH:mm:ss") }}</span>
        </template>
      </el-table-column>

      <el-table-column width="150px" align="center" label="Duration (ms)">
        <template slot-scope="{ row }">
          <span>{{ row.duration }}</span>
        </template>
      </el-table-column>

      <el-table-column align="center" label="Details" fixed="right">
        <template slot-scope="{ row }">
          <el-button type="primary" size="small" @click="() => showDetails(row.id)">
            View
          </el-button>
          <el-button :disabled="row.status !== 'Completed'" type="success" size="small" @click="() => downloadCsv(row.id, 'R')">
            Download
          </el-button>
          <el-button :disabled="row.status !== 'Started' && row.status !== 'Paused'" type="warning" size="small" @click="() => showDetails(row.id)">
            <span v-if="row.status === 'Started'">Pause</span><span v-else>Resume</span>
          </el-button>
          <el-button :disabled="row.status !== 'Started' && row.status !== 'Paused'" type="danger" size="small" @click="() => showDetails(row.id)">
            Cancel
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <task-details :taskId="taskId" :show="show" @close="close" />

    <pagination align="center" :currentPage="currentPage" :pageSize="pageSize" :total="totalTasks" @updateSize="updateSize" @updatePage="updatePage" />

  </div>
</template>

<script>
import { getTasksWithPage } from '@/api/task'
import { downloadFile } from '@/api/file'
import Pagination from '@/components/Pagination'
import TaskDetails from '@/views/task/components/TaskDetails'

const moment = require('moment')

export default {
  components: { Pagination, TaskDetails },
  filters: {
    statusFilter(status) {
      const statusMap = {
        Created: 'primary',
        Ready: 'primary',
        Started: 'primary',
        Error: 'danger',
        Paused: 'warning',
        Canceled: 'info',
        Completed: 'success'
      }
      return statusMap[status]
    },
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
      totalTasks: 0,
      currentPage: 1,
      pageSize: 10,
      loading: true,
      show: false,
      taskId: '',
      refreshId: null
    }
  },
  mounted() {
    this.fetchData(this.currentPage, this.pageSize)
    this.refreshId = setInterval(() => {
      if (this.$route.path !== '/management/task') {
        clearInterval(this.refreshId)
      } else {
        this.fetchData(this.currentPage, this.pageSize)
      }
    }, 10000)
  },
  methods: {
    fetchData(page, size) {
      this.loading = true
      getTasksWithPage(page, size).then(resp => {
        this.list = resp.data.content
        this.totalTasks = resp.data.totalElements
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
    showDetails(id) {
      this.show = true
      this.taskId = id
    },
    close() {
      this.show = false
      this.taskId = ''
    },
    downloadCsv(id, position) {
      downloadFile(id, position).then(resp => {
        var arr = resp.data
        // var byteArray = new Uint8Array(arr)
        var a = window.document.createElement('a')
        a.href = window.URL.createObjectURL(new Blob([arr], { type: 'application/octet-stream' }))
        a.download = 'Result.csv'

        // Append anchor to body.
        document.body.appendChild(a)
        a.click()

        // Remove anchor from body
        document.body.removeChild(a)
      })
    }
  }
}
</script>
