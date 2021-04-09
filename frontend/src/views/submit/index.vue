<template>
  <div class="app-container">
    <el-form ref="ruleForm" v-loading="loading" :model="ruleForm" :rules="rules" class="ruleForm" label-width="120px">
      <el-form-item label="Application" prop="application">
        <el-select v-model="ruleForm.application" style="width: 25%;" placeholder="Please select the application">
          <el-option label="Matrix Multiplication" value="MatrixMulti" />
          <el-option label="Linear Regression" value="LinearReg" />
        </el-select>
      </el-form-item>

      <el-form-item label="Approach" prop="approach">
        <el-select v-model="ruleForm.approach" style="width: 25%;" placeholder="Please select the approach">
          <el-option label="Normal Approach" value="NormApp" />
          <el-option label="Distributed Approach" value="DistApp" />
          <el-option label="Coded Approach" value="CodeApp" />
        </el-select>
      </el-form-item>

      <el-form-item v-if="ruleForm.approach == 'CodeApp'" label="Algorithm" prop="algorithm">
        <el-select v-model="ruleForm.algorithm" style="width: 25%;" placeholder="Please select the algorithm">
          <el-option label="Reed-Solomon Code (5,3)" value="RSCode_5_3" />
        </el-select>
      </el-form-item>

      <el-form-item v-if="ruleForm.approach == 'DistApp'" label="No. Nodes" prop="n">
        <el-slider v-model="ruleForm.n" show-input :min="1" :max="5" />
      </el-form-item>

      <el-form-item label="MatrixA" prop="matrixA">
        <uploads ref="uploadA" :allowFlag="allowFlagA" :ruleForm="ruleForm" position="matrixA" :taskId="taskId" @finished="countCheck()" />
      </el-form-item>

      <el-form-item label="MatrixB" prop="matrixB">
        <uploads ref="uploadB" :allowFlag="allowFlagB" :ruleForm="ruleForm" position="matrixB" :taskId="taskId" @finished="countCheck()" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="onSubmit('ruleForm')">Summit</el-button>
        <el-button @click="onCancel('ruleForm')">Cancel</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { submitTask } from '@/api/task'
import Uploads from './components/Uploads.vue'

export default {
  components: {
    Uploads
  },
  data() {
    return {
      ruleForm: {
        application: '',
        algorithm: 'NA',
        approach: '',
        matrixA: '',
        matrixB: '',
        n: 1
      },
      rules: {
        application: [{ required: true, message: 'Please select the application', trigger: 'change' }],
        algorithm: [{ required: true, message: 'Please select the algorithm', trigger: 'change' }],
        approach: [{ required: true, message: 'Please select the approach', trigger: 'change' }],
        matrixA: [{ required: true, message: 'Please select the csv file for matrix A', trigger: 'change' }],
        matrixB: [{ required: true, message: 'Please select the csv file for matrix B', trigger: 'change' }],
        n: [{ required: true, message: 'Invalid Number', trigger: 'change' }]
      },
      loading: false,
      taskId: '',
      fileCount: 0,
      allowFlagA: false,
      allowFlagB: false
    }
  },
  methods: {
    onSubmit(formName) {
      this.$refs[formName].validate((valid) => {
        if (!valid) {
          return false
        }

        // if valid
        this.loading = true
        submitTask(this.ruleForm).then(resp => {
          this.taskId = resp.data.id

          // Submit files to backend

          this.allowFlagA = true
          this.allowFlagB = true
          console.log('timeToUpload: ' + this.timeToUpload + ' ' + this.taskId)

          this.$notify({
            title: 'Submission Success',
            message: 'The task is successfully submitted!',
            type: 'success'
          })
        })
      })
    },
    onCancel(formName) {
      this.$refs[formName].resetFields()
      this.$refs['uploadA'].$refs['matrix'].clearFiles()
      this.$refs['uploadB'].$refs['matrix'].clearFiles()
      this.taskId = ''
    },
    countCheck() {
      this.fileCount += 1
      console.log(this.fileCount)
      if (this.fileCount >= 2) {
        this.loading = false
        this.fileCount = 0
        this.allowFlagA = true
        this.allowFlagB = true
        this.onCancel('ruleForm')
        this.$router.push('/management/task')
      }
    }
  }
}
</script>
