<template>
  <div class="app-container">
    <!--查询表单-->
    <el-form :inline="true">
      <el-form-item label="关键字">
        <el-input v-model="keyword" placeholder="姓名/手机" />
      </el-form-item>
      <el-button
        type="primary"
        icon="el-icon-search"
        @click="fetchBorrowInfoList()"
      >
        查询
      </el-button>
      <el-button type="default" @click="resetData()">清空</el-button>
    </el-form>

    <!-- 列表 -->
    <el-table :data="borrowInfoList" stripe align="center">
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column
        prop="name"
        label="借款人姓名"
        width="90"
        align="center"
      />
      <el-table-column prop="mobile" label="手机" width="200" align="center" />
      <el-table-column
        prop="amount"
        label="借款金额"
        width="200"
        align="center"
      />
      <el-table-column label="借款期限" width="90" align="center">
        <template slot-scope="scope">{{ scope.row.period }}个月</template>
      </el-table-column>
      <el-table-column
        prop="returnMethod"
        label="还款方式"
        width="150"
        align="center"
      />
      <el-table-column
        prop="moneyUse"
        label="资金用途"
        width="150"
        align="center"
      />
      <el-table-column label="年化利率" width="100" align="center">
        <template slot-scope="scope">
          {{ scope.row.borrowYearRate * 100 }}%
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100" align="center" />

      <el-table-column prop="createTime" label="申请时间" align="center" />

      <el-table-column label="操作" align="center">
        <template slot-scope="scope">
          <el-button type="primary" size="mini">
            <router-link :to="'/core/borrower/info-detail/' + scope.row.id">
              查看
            </router-link>
          </el-button>

          <!-- 因为传递过来的有 BorrowInfoVO 和 BorrowInfo 两个对象，所以需要进行两个判断 -->
          <el-button
            v-if="scope.row.status === '审核中' || scope.row.status === 1"
            type="warning"
            size="mini"
            @click="approvalShow(scope.row)"
          >
            审批
          </el-button>
          <el-button
            v-if="scope.row.status === '审核通过' || scope.row.status === 2"
            type="primary"
            size="mini"
            disabled
          >
            通过
          </el-button>
          <el-button
            v-if="scope.row.status === '审核不通过' || scope.row.status === -1"
            type="danger"
            size="mini"
            disabled
          >
            失败
          </el-button>
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

    <!-- 审批对话框 -->
    <el-dialog title="审批" :visible.sync="dialogVisible" width="490px">
      <el-form label-position="right" label-width="100px">
        <el-form-item label="是否通过">
          <el-radio-group v-model="borrowInfoApproval.status">
            <el-radio :label="2">通过</el-radio>
            <el-radio :label="-1">不通过</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="borrowInfoApproval.status == 2" label="标的名称">
          <el-input v-model="borrowInfoApproval.title" />
        </el-form-item>

        <el-form-item v-if="borrowInfoApproval.status == 2" label="起息日">
          <el-date-picker
            v-model="borrowInfoApproval.lendStartDate"
            type="date"
            placeholder="选择开始时间"
            value-format="yyyy-MM-dd"
          />
        </el-form-item>

        <el-form-item v-if="borrowInfoApproval.status == 2" label="年化收益率">
          <el-input v-model="borrowInfoApproval.lendYearRate">
            <template slot="append">%</template>
          </el-input>
        </el-form-item>

        <el-form-item v-if="borrowInfoApproval.status == 2" label="服务费率">
          <el-input v-model="borrowInfoApproval.serviceRate">
            <template slot="append">%</template>
          </el-input>
        </el-form-item>

        <el-form-item v-if="borrowInfoApproval.status == 2" label="标的描述">
          <el-input v-model="borrowInfoApproval.lendInfo" type="textarea" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="approvalSubmit">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import borrowInfoApi from '@/api/core/borrow-info'

export default {
  data() {
    return {
      total: 0,
      page: 1, // 默认页码
      limit: 10, // 默认查询条数
      keyword: '', // 默认查询条件
      borrowInfoList: [], // 借款信息列表
      dialogVisible: false, // 审批对话框
      borrowInfoApproval: {
        status: 2,
        serviceRate: 5,
        lendYearRate: 0, // 初始化，解决表单中数据修改时无法及时渲染的问题
      }, // 审批对象
    }
  },

  mounted() {
    this.fetchBorrowInfoList()
  },

  methods: {
    // 加载列表数据
    fetchBorrowInfoList() {
      borrowInfoApi
        .getBorrowInfoList(this.page, this.limit, this.keyword)
        .then((response) => {
          this.total = response.data.pageModel.total
          this.borrowInfoList = response.data.pageModel.records
        })
    },

    // 每页展示数量改变后的回调函数
    changePageSize(size) {
      this.limit = size
      this.fetchBorrowInfoList()
    },

    // 页码改变后的回调函数
    changeCurrentPage(page) {
      this.page = page
      this.fetchBorrowInfoList()
    },

    // 重置数据
    resetData() {
      this.keyword = ''
      this.fetchBorrowInfoList()
    },

    // 显示审批框
    approvalShow(row) {
      this.dialogVisible = true
      this.borrowInfoApproval.id = row.id
      this.borrowInfoApproval.lendYearRate = row.borrowYearRate * 100
    },

    // 提交审批信息
    approvalSubmit() {
      borrowInfoApi.approval(this.borrowInfoApproval).then((response) => {
        this.dialogVisible = false;
        this.$message.success(response.message);
        this.fetchBorrowInfoList();
        this.borrowInfoApproval.status = 2;
        this.borrowInfoApproval.serviceRate = 5;
        this.borrowInfoApproval.lendYearRate = 0;
      })
    },
  },
}
</script>
