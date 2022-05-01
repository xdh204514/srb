<template>
  <div class="app-container">
    <!-- 输入表单 -->
    <el-form label-width="120px">
      <el-form-item label="借款额度">
        <el-input-number v-model="integralGrade.borrowAmount" :min="0" />
      </el-form-item>
      <el-form-item label="积分区间开始">
        <el-input-number v-model="integralGrade.integralStart" :min="0" />
      </el-form-item>
      <el-form-item label="积分区间结束">
        <el-input-number v-model="integralGrade.integralEnd" :min="0" />
      </el-form-item>
      <el-form-item>
        <el-button
          :disabled="saveBtnDisabled"
          type="primary"
          @click="saveOrUpdate()"
        >
          保存
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import integralGradeApi from '@/api/core/integral-grade'

export default {
  data() {
    return {
      integralGrade: {}, // 初始化数据
      saveBtnDisabled: false, // 保存按钮是否禁用，防止表单重复提交
    }
  },

  created() {
    // 判断路由中是否有id值，有的话则获取数据
    if (this.$route.params.id) {
      this.fetchDataById(this.$route.params.id)
    }
    // 使用同一个表单页面完成更新和新增的功能，那么核心就在于这个 id 是否存在
  },

  methods: {
    // 根据id查询积分等级
    fetchDataById(id) {
      integralGradeApi.getById(id).then((response) => {
        this.integralGrade = response.data.record
      })
    },

    // 保存或新增积分等级
    saveOrUpdate() {
      this.saveBtnDisabled = true
      // 根据 integralGrade 的 id 是否存在来决定是进行更新还是进行新增
      if (this.integralGrade.id) {
        this.updateData()
      } else {
        this.saveData()
      }
    },

    // 保存积分等级
    saveData() {
      integralGradeApi.save(this.integralGrade).then((response) => {
        this.$message({
          type: 'success',
          message: response.message,
        })
        this.$router.push('/core/integral-grade/list')
      })
    },

    // 更新积分等级
    updateData() {
      integralGradeApi.updateById(this.integralGrade).then((response) => {
        this.$message({
          type: 'success',
          message: response.message,
        })
        this.$router.push('/core/integral-grade/list')
      })
    },
  },
}
</script>

<style scoped></style>
