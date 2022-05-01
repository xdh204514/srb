<template>
  <div class="app-container">
    <!-- 表格 -->
    <el-table :data="list" border stripe>
      <el-table-column type="index" width="50" />
      <el-table-column prop="borrowAmount" label="借款额度" />
      <el-table-column prop="integralStart" label="积分区间开始" />
      <el-table-column prop="integralEnd" label="积分区间结束" />
      <el-table-column label="操作" width="200" align="center">
        <template slot-scope="scope">
          <router-link
            :to="'/core/integral-grade/edit/' + scope.row.id"
            style="margin-right: 5px"
          >
            <el-button type="primary" size="mini" icon="el-icon-edit">
              修改
            </el-button>
          </router-link>
          <el-button
            type="danger"
            size="mini"
            icon="el-icon-delete"
            @click="removeById(scope.row.id)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import integralGradeApi from '@/api/core/integral-grade'

export default {
  data() {
    return {
      list: [],
    }
  },

  created() {
    this.fetchData()
  },

  methods: {
    // 获取积分等级列表
    fetchData() {
      integralGradeApi.list().then((response) => {
        this.list = response.data.records
      })
    },

    // 删除积分等级
    removeById(id) {
      this.$confirm('是否删除该积分等级?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      })
        .then(() => {
          integralGradeApi.removeById(id).then((response) => {
            this.$message.success(response.message)
            this.fetchData()
          })
        })
        .catch(() => {})
    },
  },
}
</script>

<style scoped></style>
