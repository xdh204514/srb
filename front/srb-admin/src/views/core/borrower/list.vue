<template>
  <div class="app-container">
    <!--查询表单-->
    <el-form :inline="true">
      <el-form-item label="关键字">
        <el-input v-model="keyword" placeholder="姓名/手机/身份证" />
      </el-form-item>
      <el-button
        type="primary"
        icon="el-icon-search"
        @click="fetchBorrowerList()"
      >
        查询
      </el-button>
      <el-button type="default" @click="resetData()">清空</el-button>
    </el-form>

    <!-- 列表 -->
    <el-table :data="borrowerList" stripe align="center">
      <el-table-column label="序号" width="70" align="center">
        <template slot-scope="scope">
          {{ (page - 1) * limit + scope.$index + 1 }}
        </template>
      </el-table-column>

      <el-table-column prop="name" label="姓名" width="100" align="center" />
      <el-table-column prop="mobile" label="手机" width="200" align="center" />
      <el-table-column prop="idCard" label="身份证号" align="center" />
      <el-table-column label="性别" width="80" align="center">
        <template slot-scope="scope">
          {{ scope.row.sex === 1 ? '男' : '女' }}
        </template>
      </el-table-column>

      <el-table-column prop="age" label="年龄" width="80" align="center" />

      <el-table-column label="是否结婚" width="120" align="center">
        <template slot-scope="scope">
          {{ scope.row.marry ? '是' : '否' }}
        </template>
      </el-table-column>

      <el-table-column label="状态" width="100" align="center">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === 0" type="info" size="mini">
            未认证
          </el-tag>
          <el-tag v-if="scope.row.status === 1" type="warning" size="mini">
            认证中
          </el-tag>
          <el-tag v-if="scope.row.status === 2" type="success" size="mini">
            认证通过
          </el-tag>
          <el-tag v-if="scope.row.status === -1" type="danger" size="mini">
            认证失败
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="createTime" label="申请时间" align="center" />
      <el-table-column prop="updateTime" label="更新时间" align="center" />

      <el-table-column label="操作" width="200" align="center">
        <template slot-scope="scope">
          <router-link :to="'/core/borrower/detail/' + scope.row.id">
            <el-button v-if="scope.row.status === 1" type="warning" size="mini">
              审批
            </el-button>
            <el-button v-else type="primary" size="mini">查看</el-button>
          </router-link>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <el-pagination
      :current-page="page"
      :total="total"
      :page-size="limit"
      :page-sizes="[2, 10, 20]"
      style="padding: 30px 0"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="changePageSize"
      @current-change="changeCurrentPage"
    />
  </div>
</template>

<script>
import borrowerApi from '@/api/core/borrower'

export default {
  data() {
    return {
      total: 0,
      page: 1, // 默认页码
      limit: 10, // 默认查询条数
      keyword: '', // 默认查询条件
      borrowerList: [], // 借款人列表
    }
  },

  created() {
    this.fetchBorrowerList()
  },

  methods: {
    // 获取借款人列表
    fetchBorrowerList() {
      borrowerApi
        .getBorrowerList(this.page, this.limit, this.keyword)
        .then((response) => {
          this.total = response.data.pageModel.total
          this.borrowerList = response.data.pageModel.records
        })
    },

    // 每页展示数量改变后的回调函数
    changePageSize(size) {
      this.limit = size
      this.fetchBorrowerList()
    },

    // 页码改变后的回调函数
    changeCurrentPage(page) {
      this.page = page
      this.fetchBorrowerList()
    },

    // 查询条件重置
    resetData() {
      this.keyword = ''
      this.fetchBorrowerList()
    },
  },
}
</script>
