<template>
  <el-dialog
    title="Task Details"
    :visible.sync="boxShow"
    width="70%"
    :before-close="close"
  >
    <el-card>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="Summary" name="summary">
          <el-row :gutter="20">
            <el-col :span="8"><h3>TaskId: {{ task.id }}</h3></el-col>
            <el-tag :span="8" style="margin: 10px 0px" :type="task.status | statusFilter">
              {{ task.status }}
            </el-tag>
          </el-row>
          <el-collapse>
            <el-collapse-item title='Task Metadata'>
              <el-row>{{ task.application | applicationFilter }}: {{ task.approach | approachFilter }} <span v-if="task.approach==='CodeApp'">with {{ task.algorithm }}</span></el-row>
              <el-row>Start at: {{ task.createTime | parseTime("YYYY-MM-DD HH:mm:ss") }}<span v-if="task.endTime"> - End at: {{ task.endTime | parseTime("YYYY-MM-DD HH:mm:ss") }}</span></el-row>
              <el-row>Duration: <span v-if="task.duration">{{ task.duration }} (ms)</span><span v-else>Not Yet Completed</span></el-row>
              <el-row>Batch Progress: {{ task.batch }} / {{ task.totalBatch }}</el-row>
              <el-progress :percentage="task.batch / task.totalBatch * 100" :status="task.status | progressFilter"></el-progress>
            </el-collapse-item>
            <el-collapse-item title='Files Management'>
              <el-table :data="files" border fit highlight-current-row style="width: 100%;" empty-text="No Files Found">
                <el-table-column width="250px" align="center" label="File Name">
                  <template slot-scope="{ row }">
                    <span>{{ row.fileName }}</span>
                  </template>
                </el-table-column>

                <el-table-column width="250px" align="center" label="Create Time">
                  <template slot-scope="{ row }">
                    <span>{{ row.uploadTime | parseTime("YYYY-MM-DD HH:mm:ss") }}</span>
                  </template>
                </el-table-column>

                <el-table-column width="250px" align="center" label="File Size (byte)">
                  <template slot-scope="{ row }">
                    <span>{{ row.fileSize }}</span>
                  </template>
                </el-table-column>

                <el-table-column width="250px" align="center" label="Description">
                  <template slot-scope="{ row }">
                    <span>{{ row.position | descriptionFilter }}</span>
                  </template>
                </el-table-column>

                <el-table-column align="center" label="Operations" fixed="right">
                  <template slot-scope="{ row }">
                    <el-button :disabled="task.status !== 'Completed' && row.position === 'R'" type="primary" size="small" @click="() => downloadCsv(task.id, row.position, row.fileName)">
                      Download
                    </el-button>
                    <el-button :disabled="task.status !== 'Completed' && row.position === 'R'" type="danger" size="small" @click="() => deleteFile(task.id, row.position)">
                      Delete
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-collapse-item>
            <el-collapse-item title='Elves On Duty'>
              <el-row v-for="(item, index) in task.elvesOnDuty" :key="index">{{ index + 1 }}: {{ item }}</el-row>
            </el-collapse-item>
          </el-collapse>
        </el-tab-pane>
        <el-tab-pane label="Task Log" name="taskLog">
          <!--<timeline :activities="activities" />-->
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
        </el-tab-pane>
      </el-tabs>
    </el-card>
    <span slot="footer" class="dialog-footer">
      <el-button @click="close()">Close</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { getTaskById } from '@/api/task'
import { getLogsByTaskIdWithPages } from '@/api/log'
import { getFilesUnderTask, downloadFile, deleteFile } from '@/api/file'

import Pagination from '@/components/Pagination'
const moment = require('moment')

export default {
  components: { Pagination },
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
    applicationFilter(application) {
      const applicationMap = {
        MatrixMulti: 'Matrix Multiplication',
        LinearReg: 'Linear Regression'
      }
      return applicationMap[application]
    },
    approachFilter(approach) {
      const approachMap = {
        NormApp: 'Normal Approach',
        DistApp: 'Distributed Approach',
        CodeApp: 'Encoding Approach'
      }
      return approachMap[approach]
    },
    progressFilter(progress) {
      const progressMap = {
        Completed: 'success',
        Error: 'exception',
        Created: 'primary',
        Started: 'primary',
        Paused: 'warning',
        Canceled: 'warning'
      }
      return progressMap[progress]
    },
    descriptionFilter(description) {
      const descriptionMap = {
        A: 'Left Matrix',
        B: 'Right Matrix',
        R: 'Result Matrix'
      }
      return descriptionMap[description]
    },
    parseTime(time, format) {
      if (!time) {
        return 'N/A'
      }
      return moment(time).format(format)
    }
  },
  props: {
    show: Boolean,
    taskId: String
  },
  data() {
    return {
      task: [],
      files: [],
      activities: [],
      activeTab: 'summary',
      boxShow: false,
      refreshId: null,
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
          if (this.$route.path !== '/management/task') {
            clearInterval(this.refreshId)
          } else {
            this.fetchData()
          }
        }, 10000)
      } else {
        clearInterval(this.refreshId)
      }
    },
    activeTab() {
      this.fetchData()
    }
  },
  methods: {
    close() {
      this.$emit('close')
    },
    fetchData() {
      if (this.activeTab === 'summary') {
        getTaskById(this.taskId).then(resp => {
          this.task = resp.data
        })
        getFilesUnderTask(this.taskId).then(resp => {
          this.files = resp.data
        })
      } else {
        getLogsByTaskIdWithPages(this.taskId, this.currentPage, this.pageSize).then(resp => {
          this.activities = resp.data.content
          this.totalRecords = resp.data.totalElements
        })
      }
    },
    downloadCsv(id, position, fileName) {
      downloadFile(id, position).then(resp => {
        var arr = resp.data
        // var byteArray = new Uint8Array(arr)
        var a = window.document.createElement('a')
        a.href = window.URL.createObjectURL(new Blob([arr], { type: 'application/octet-stream' }))
        a.download = fileName

        // Append anchor to body.
        document.body.appendChild(a)
        a.click()

        // Remove anchor from body
        document.body.removeChild(a)
      })
    },
    deleteFile(id, position) {
      deleteFile(id, position)
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
  .el-row {
    margin: 8px 0px
  }
  .text {
    font-size: 16px;
    color: #999;
  }
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
