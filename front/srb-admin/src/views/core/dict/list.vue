<template>
  <div class="app-container">
    <div style="margin-bottom: 10px">
      <el-button
        @click="dialogVisible = true"
        type="primary"
        size="mini"
        icon="el-icon-download"
      >
        导入Excel
      </el-button>
      <el-button
        @click="exportExcelData"
        type="primary"
        size="mini"
        icon="el-icon-upload2"
      >
        导出Excel
      </el-button>
    </div>

    <el-table
      :data="dictDataList"
      border
      row-key="id"
      lazy
      :load="load"
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
    >
      <el-table-column label="名称" align="left" prop="name" />
      <el-table-column label="编码" prop="dictCode" />
      <el-table-column label="值" align="left" prop="value" />
    </el-table>

    <el-dialog
      class="dialog-class"
      title="数据字典导入"
      :visible.sync="dialogVisible"
      width="25%"
    >
      <el-upload
        class="upload-demo"
        drag
        :auto-upload="true"
        :multiple="false"
        :limit="1"
        :on-exceed="fileUploadExceed"
        :on-success="fileUploadSuccess"
        :on-error="fileUploadError"
        :action="BASE_API + '/admin/core/dict/import'"
        name="file"
        accept="application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
      >
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">
          将文件拖到此处，或
          <em>点击上传</em>
        </div>
      </el-upload>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" size="mini" @click="dialogVisible = false">
          取消
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import dictApi from '@/api/core/dict'

export default {
  data() {
    return {
      dialogVisible: false,
      BASE_API: process.env.VUE_APP_BASE_API, //获取后端接口地址
      dictDataList: [],
    }
  },

  created() {
    this.fetchData()
  },

  methods: {
    // 上传多于一个文件时
    fileUploadExceed() {
      this.$message.warning('只能选取一个文件')
    },

    // 上传成功回调
    fileUploadSuccess(response) {
      if (response.code === 0) {
        this.$message.success('数据导入成功')
        this.dialogVisible = false
        // 优化步骤，在成功将 Excel 上传到数据库后，需要从数据库中获取数据加载到页面
        this.fetchData()
      } else {
        this.$message.error(response.message)
      }
    },

    // 上传失败回调
    fileUploadError(error) {
      this.$message.error('数据导入失败')
    },

    // 导出 excel 文件
    exportExcelData() {
      window.location.href = this.BASE_API + '/admin/core/dict/export'
    },

    // 获取节点列表
    fetchData() {
      dictApi.listByParentId(1).then((response) => {
        this.dictDataList = response.data.records
      })
    },

    // 延迟加载获取子节点
    load(row, treeNode, resolve) {
      dictApi.listByParentId(row.id).then((response) => {
        // 负责将子节点数据展示在展开的列表中
        resolve(response.data.records)
      })
    },
  },
}
</script>
