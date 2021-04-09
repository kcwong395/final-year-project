<template>
  <div class="app-container">
    <el-card v-loading="loading">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="Master" name="master">
          <el-row :gutter="20">
            <el-col :span="8" v-for="master in masters" :key="master.id">
              <el-card>
                <div slot="header" class="clearfix">
                  <el-col :span="12"><strong>{{ master.name }}</strong></el-col>
                  <el-tag style="float: right" :type="master.status | statusFilter">
                    {{ master.status }}
                  </el-tag>
                </div>
                <p class="text">Ip Address: {{ master.ip }}</p>
                <p class="text">Port: {{ master.port }}</p>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>
        <el-tab-pane label="Worker" name="worker">
          <el-button type="primary" plain @click="createElf()">New</el-button>
          <el-row :gutter="20">
            <el-col :span="8" v-for="elf in elves" :key="elf.id">
              <el-card>
                <div slot="header" class="clearfix">
                  <el-col :span="12"><strong>{{ elf.name }}</strong></el-col>
                  <el-tag style="float: right" :type="elf.status | statusFilter">
                    {{ elf.status }}
                  </el-tag>
                </div>
                <p class="text">Id: {{ elf.id }}</p>
                <p class="text">Ip Address: {{ elf.ip }}</p>
                <p class="text">Port: {{ elf.port }}</p>
                <el-button size="small" type="info" plain @click="() => showLogs(elf.id)">Log</el-button>
                <el-button :disabled="elf.status !== 'Active' && elf.status !== 'Busy'" size="small" type="danger" plain @click="deleteElf(elf.id)">Shutdown</el-button>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>
      </el-tabs>

      <log :elfId="elfId" :show="show" @close="close" />
      <pagination v-if="activeTab === 'master'" align="center" :currentPage="currentPage" :pageSize="pageSize" :total="totalShoeMaker" @updateSize="updateSize" @updatePage="updatePage" />
      <pagination v-if="activeTab === 'worker'" align="center" :currentPage="currentPage" :pageSize="pageSize" :total="totalElves" @updateSize="updateSize" @updatePage="updatePage" />
    </el-card>
  </div>
</template>

<script>
import { getElvesWithPage } from '@/api/elf'
import { getShoeMakerWithPage } from '@/api/shoe-maker'
import { createElf } from '@/api/elf'
import { deleteElf } from '@/api/elf'
import Pagination from '@/components/Pagination'
import Log from '@/views/machine/components/log'

export default {
  components: { Pagination, Log },
  filters: {
    statusFilter(status) {
      const statusMap = {
        Initialized: 'primary',
        Active: 'success',
        Busy: 'warning',
        Disconnected: 'danger',
        Shutdown: 'danger'
      }
      return statusMap[status]
    }
  },
  data() {
    return {
      masters: [],
      elves: [],
      activeTab: 'master',
      loading: false,
      totalShoeMaker: 0,
      totalElves: 0,
      currentPage: 1,
      pageSize: 10,
      refreshId: null,
      elfId: '',
      show: false
    }
  },
  mounted() {
    this.fetchData(this.currentPage, this.pageSize)
    this.refreshId = setInterval(() => {
      if (this.$route.path !== '/management/machine') {
        clearInterval(this.refreshId)
      } else {
        this.fetchData(this.currentPage, this.pageSize)
      }
    }, 10000)
  },
  watch: {
    activeTab() {
      this.fetchData(this.currentPage, this.pageSize)
    }
  },
  methods: {
    fetchData(page, size) {
      // this.loading = true
      if (this.activeTab === 'worker') {
        getElvesWithPage(page, size).then(resp => {
          this.elves = resp.data.content
          this.totalElves = resp.data.totalElements
          // this.loading = false
        })
      } else {
        getShoeMakerWithPage(page, size).then(resp => {
          this.masters = resp.data.content
          this.totalShoeMaker = resp.data.totalElements
          // this.loading = false
        })
      }
    },
    updateSize(size) {
      this.pageSize = size
      this.fetchData(1, this.pageSize)
    },
    updatePage(page) {
      this.currentPage = page
      this.fetchData(page, this.pageSize)
    },
    createElf() {
      this.loading = true
      let tmp = 0
      for (let i = 0; i < this.totalElves; i++) {
        if (this.elves[i].status === 'Active') {
          tmp += 1
        }
      }
      createElf()
      const intervalId = setInterval(() => {
        let totalActive = 0
        for (let i = 0; i < this.totalElves; i++) {
          if (this.elves[i].status === 'Active') {
            totalActive += 1
          }
        }
        this.loading = (tmp === totalActive)
        if (!this.loading) {
          clearInterval(intervalId)
        }
      }, 10000)
    },
    deleteElf(elfId) {
      this.loading = true
      deleteElf(elfId)
      const intervalId = setInterval(() => {
        for (let i = 0; i < this.totalElves; i++) {
          if (this.elves[i].id === elfId) {
            this.loading = !(this.elves[i].status === 'Disconnected')
            if (!this.loading) {
              clearInterval(intervalId)
            }
          }
        }
      }, 10000)
    },
    showLogs(id) {
      this.show = true
      this.elfId = id
    },
    close() {
      this.show = false
      this.elfId = ''
    }
  }
}
</script>

<style scoped>
  .el-col {
    margin: 8px 0px
  }
  .text {
    font-size: 13px;
    color: #999;
  }
</style>
