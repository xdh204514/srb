<template>
  <div class="app-container">
    <!--查询表单-->
    <el-form :inline="true" class="demo-form-inline">
      <el-form-item label="手机号">
        <el-input v-model="queryObj.mobile" placeholder="手机号" />
      </el-form-item>

      <el-form-item label="用户类型" class="left">
        <el-select v-model="queryObj.userType" placeholder="请选择" clearable>
          <el-option label="投资人" value="1" />
          <el-option label="借款人" value="2" />
        </el-select>
      </el-form-item>

      <el-form-item label="用户状态" class="left">
        <el-select v-model="queryObj.status" placeholder="请选择" clearable>
          <el-option label="正常" value="1" />
          <el-option label="锁定" value="0" />
        </el-select>
      </el-form-item>

      <el-button
        class="left"
        type="primary"
        icon="el-icon-search"
        @click="fetchData()"
      >
        查询
      </el-button>
      <el-button class="left" type="default" @click="resetData()">
        清空
      </el-button>
    </el-form>

    <!-- 列表 -->
    <el-table :data="userInfoList" border stripe>
      <el-table-column label="#" width="50" align="center">
        <template slot-scope="scope">
          {{ (page - 1) * limit + scope.$index + 1 }}
        </template>
      </el-table-column>

      <el-table-column label="用户类型" width="100" align="center">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.userType === 1" type="success" size="mini">
            投资人
          </el-tag>
          <el-tag
            v-else-if="scope.row.userType === 2"
            type="warning"
            size="mini"
          >
            借款人
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="mobile" label="手机号" align="center" />
      <el-table-column prop="name" label="用户姓名" align="center" />
      <el-table-column prop="idCard" label="身份证号" width="200" align="center" />
      <el-table-column prop="email" label="邮箱" align="center" />
      <el-table-column label="头像" align="center">
        <template slot-scope="scope">
          <el-avatar :src="scope.row.headImg"></el-avatar>
        </template>
      </el-table-column>
      <el-table-column prop="integral" label="用户积分" align="center" />
      <el-table-column
        prop="createTime"
        label="注册时间"
        width="200"
        align="center"
      />
      <el-table-column label="绑定状态" width="90" align="center">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.bindStatus === 0" type="warning" size="mini">
            未绑定
          </el-tag>
          <el-tag
            v-else-if="scope.row.bindStatus === 1"
            type="success"
            size="mini"
          >
            已绑定
          </el-tag>
          <el-tag v-else type="danger" size="mini">绑定失败</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="用户状态" width="90" align="center">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === 0" type="danger" size="mini">
            锁定
          </el-tag>
          <el-tag v-else type="success" size="mini">正常</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200">
        <template slot-scope="scope">
          <el-button
            v-if="scope.row.status == 1"
            type="primary"
            size="mini"
            @click="lock(scope.row.id, 0)"
          >
            锁定
          </el-button>
          <el-button
            v-else
            type="danger"
            size="mini"
            @click="lock(scope.row.id, 1)"
          >
            解锁
          </el-button>
          <el-button
            type="primary"
            size="mini"
            @click="showLoginRecord(scope.row.id, scope.row.name)"
          >
            登录日志
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 用户登录日志 -->
    <el-dialog :title="whoRecord" :visible.sync="dialogTableVisible">
      <el-table :data="loginRecordList" border stripe max-height="500">
        <el-table-column type="index" />
        <el-table-column prop="ip" label="IP" />
        <el-table-column prop="createTime" label="登录时间" />
      </el-table>
    </el-dialog>

    <!-- 分页组件 -->
    <el-pagination
      :current-page="page"
      :total="total"
      :page-size="limit"
      :page-sizes="[10, 20]"
      style="padding: 30px 0"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="changePageSize"
      @current-change="changeCurrentPage"
    />
  </div>
</template>
<script>
import userInfoApi from '@/api/core/user-info'
export default {
  data() {
    return {
      total: 0,
      page: 1, // 默认页码
      limit: 10, // 默认查询条数
      queryObj: {}, // 默认查询条件
      userInfoList: [], // 会员信息列表
      loginRecordList: [], // 会员日志列表
      dialogTableVisible: false, // 是否显示日志列表框
      whoRecord: '',
    }
  },

  created() {
    // 当页面加载时获取数据
    this.fetchData()
  },

  methods: {
    // 获取会员信息列表
    fetchData() {
      userInfoApi
        .getPageList(this.page, this.limit, this.queryObj)
        .then((response) => {
          this.userInfoList = response.data.pageModel.records
          this.total = response.data.pageModel.total
        })
    },

    // 改变每页记录数，重新获取会员信息列表
    changePageSize(size) {
      this.limit = size
      this.fetchData()
    },

    // 改变页码，重新获取会员信息列表
    changeCurrentPage(page) {
      this.page = page
      this.fetchData()
    },

    // 重置表单
    resetData() {
      this.queryObj = {}
      this.fetchData()
    },

    // 锁定状态
    lock(id, status) {
      userInfoApi.lock(id, status).then((response) => {
        this.$message.success(response.message)
        this.fetchData()
      })
    },

    // 显示登录日志
    showLoginRecord(id, name) {
      // 先发送请求获取日志
      userInfoApi.getuserLoginRecordTopFifty(id).then((response) => {
        this.loginRecordList = response.data.records
      })

      this.whoRecord = name + '的登入日志'

      // 然后打开对话框
      this.dialogTableVisible = true
    },
  },
}
</script>
<style scoped>
.left {
  margin-left: 40px;
}
</style>
>
