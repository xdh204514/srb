<template>
  <div class="app-container">
    <!--查询表单-->
    <el-form :inline="true">
      <el-form-item label="关键字">
        <el-input v-model="keyword" placeholder="标的名称/标的说明" />
      </el-form-item>
      <el-button type="primary" icon="el-icon-search" @click="fetchLendList()">
        查询
      </el-button>
      <el-button type="default" @click="resetData()">清空</el-button>
    </el-form>
    <!-- 列表 -->
    <el-table :data="lendList" stripe align="center">
      <el-table-column type="index" label="序号" width="50" align="center" />
      <el-table-column
        prop="lendNo"
        label="标的编号"
        width="200"
        align="center"
      />
      <el-table-column
        prop="title"
        label="标的名称"
        width="140"
        align="center"
      />
      <el-table-column prop="amount" label="标的金额" width="80" align="center" />
      <el-table-column prop="period" label="投资期数" align="center" />
      <el-table-column label="年化利率" align="center">
        <template slot-scope="scope">
          {{ scope.row.lendYearRate * 100 }}%
        </template>
      </el-table-column>
      <el-table-column prop="investAmount" label="已投金额" align="center" />
      <el-table-column prop="investNum" label="投资人数" align="center" />
      <el-table-column
        prop="publishDate"
        label="发布时间"
        width="180"
        align="center"
      />
      <el-table-column
        prop="lendStartDate"
        label="开始日期"
        width="140"
        align="center"
      />
      <el-table-column
        prop="lendEndDate"
        label="结束日期"
        width="140"
        align="center"
      />
      <el-table-column
        prop="param.returnMethod"
        label="还款方式"
        align="center"
      />
      <el-table-column prop="param.status" label="状态" align="center" />
      <el-table-column label="操作" width="200"  align="center">
        <template slot-scope="scope">
          <el-button type="primary" size="mini">
            <router-link :to="'/core/lend/detail/' + scope.row.id">
              查看
            </router-link>
          </el-button>

          <el-button
            v-if="scope.row.status == 1"
            type="warning"
            size="mini"
            @click="makeLoan(scope.row.id)"
          >
            待放款
          </el-button>
          <el-button
            v-if="scope.row.status == 2"
            type="warning"
            size="mini"
            disabled
          >
            已放款
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <el-pagination
      :current-page="page"
      :total="total"
      :page-size="limit"
      :page-sizes="[1, 10, 20]"
      style="padding: 30px 0"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="changePageSize"
      @current-change="changeCurrentPage"
    />
  </div>
</template>
<script>
import lendApi from '@/api/core/lend'

export default {
  data() {
    return {
      total: 0,
      page: 1, // 默认页码
      limit: 10, // 默认查询条数
      keyword: '', // 默认查询条件
      lendList: [], // 标的列表
    }
  },

  mounted() {
    this.fetchLendList()
  },

  methods: {
    // 加载列表数据
    fetchLendList() {
      lendApi
        .getLendList(this.page, this.limit, this.keyword)
        .then((response) => {
          this.total = response.data.pageModel.total
          this.lendList = response.data.pageModel.records
        })
    },

    // 每页展示数量改变后的回调函数
    changePageSize(size) {
      this.limit = size
      this.fetchLendList()
    },

    // 页码改变后的回调函数
    changeCurrentPage(page) {
      this.page = page
      this.fetchLendList()
    },

    // 重置数据
    resetData() {
      this.keyword = ''
      this.fetchLendList()
    },

    makeLoan(id) {
      this.$confirm('确定放款吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      })
        .then(() => {
          return lendApi.makeLoan(id)
        })
        .then((response) => {
          // 放款成功则重新获取数据列表
          this.fetchLendList()
          this.$message({
            type: 'success',
            message: response.message,
          })
        })
        .catch((error) => {
          console.log('取消', error)
          if (error === 'cancel') {
            this.$message({
              type: 'info',
              message: '已取消放款',
            })
          }
        })
    },
  },
}
</script>
