<template>
  <el-upload
    ref="matrix"
    drag
    :limit="1"
    :auto-upload="false"
    accept=".csv"
    action=""
    :http-request="upload"
    :on-change="(file) => validate(file, 'csv')"
    :on-remove="() => ruleForm[position] = ''"
  >
    <i class="el-icon-upload" />
    <div class="el-upload__text">Please drag the matrix csv file here, or<em> click to upload</em></div>
    <div slot="tip" class="el-upload__tip">Only csv file is allowed</div>
  </el-upload>
</template>

<script>

import { validFileExtension } from '@/utils/validate'
import { uploadFile } from '@/api/file'

export default {
  props: {
    ruleForm: Object,
    position: String,
    taskId: String,
    allowFlag: Boolean
  },
  watch: {
    allowFlag() {
      console.log('watch: ' + this.taskId + ' ' + Date.now())
      this.$refs['matrix'].submit()
      this.$emit('uploaded')
    }
  },
  methods: {
    validate(file, fileExtension) {
      if (validFileExtension(file.raw, fileExtension)) {
        this.ruleForm[this.position] = file.name
      } else {
        this.$refs['matrix'].clearFiles()
      }
    },
    upload(file) {
      const formData = new FormData()
      formData.append('file', file.file)
      formData.append('taskId', this.taskId)
      formData.append('position', this.position.charAt(this.position.length - 1))
      uploadFile(formData).then(() => {
        this.$emit('finished')
      })
    }
  }
}
</script>
