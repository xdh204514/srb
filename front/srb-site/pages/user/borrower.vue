<template>
  <div class="personal-main">
    <div class="personal-pay">
      <h3><i>借款人信息认证</i></h3>

      <el-steps :active="active" finish-status="success" style="margin: 40px">
        <el-step title="填写借款人信息"></el-step>
        <el-step title="提交平台审核"></el-step>
        <el-step title="等待认证结果"></el-step>
      </el-steps>

      <!-- 第一步显示内容 -->
      <div v-if="active === 0" class="user-borrower">
        <h6>个人基本信息</h6>
        <el-form label-width="120px">
          <el-form-item label="年龄">
            <el-col :span="5">
              <el-input v-model="borrower.age" />
            </el-col>
          </el-form-item>

          <el-form-item label="性别">
            <el-select v-model="borrower.sex">
              <el-option :value="1" :label="'男'" />
              <el-option :value="0" :label="'女'" />
            </el-select>
          </el-form-item>
          <el-form-item label="婚否">
            <el-select v-model="borrower.marry">
              <el-option :value="true" :label="'是'" />
              <el-option :value="false" :label="'否'" />
            </el-select>
          </el-form-item>
          <el-form-item label="学历">
            <el-select v-model="borrower.education">
              <el-option
                v-for="item in educationList"
                :key="item.value"
                :label="item.name"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="行业">
            <el-select v-model="borrower.industry">
              <el-option
                v-for="item in industryList"
                :key="item.value"
                :label="item.name"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="月收入">
            <el-select v-model="borrower.income">
              <el-option
                v-for="item in incomeList"
                :key="item.value"
                :label="item.name"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="还款来源">
            <el-select v-model="borrower.returnSource">
              <el-option
                v-for="item in returnSourceList"
                :key="item.value"
                :label="item.name"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-form>
        <h6>联系人信息</h6>
        <el-form label-width="120px">
          <el-form-item label="联系人姓名">
            <el-col :span="5">
              <el-input v-model="borrower.contactsName" />
            </el-col>
          </el-form-item>
          <el-form-item label="联系人手机">
            <el-col :span="5">
              <el-input v-model="borrower.contactsMobile" />
            </el-col>
          </el-form-item>
          <el-form-item label="联系人关系">
            <el-select v-model="borrower.contactsRelation">
              <el-option
                v-for="item in contactsRelationList"
                :key="item.value"
                :label="item.name"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-form>

        <h6>身份认证信息</h6>
        <el-form label-width="120px">
          <el-form-item label="身份证人像面" class="idCard">
            <el-upload
              class="idCard1"
              :on-success="onUploadSuccessIdCard1"
              :on-remove="onUploadRemove"
              :on-exceed="onExceed"
              :multiple="false"
              :action="uploadUrl"
              :data="{ module: 'idCard1' }"
              :limit="1"
              list-type="picture-card"
            >
              <i class="el-icon-plus"></i>
            </el-upload>
          </el-form-item>
          <el-form-item label="身份证国徽面">
            <el-upload
              class="idCard1"
              :on-success="onUploadSuccessIdCard2"
              :on-remove="onUploadRemove"
              :on-exceed="onExceed"
              :multiple="false"
              :action="uploadUrl"
              :data="{ module: 'idCard2' }"
              :limit="1"
              list-type="picture-card"
            >
              <i class="el-icon-plus"></i>
            </el-upload>
          </el-form-item>
        </el-form>

        <h6>其他信息</h6>
        <el-form label-width="120px">
          <el-form-item label="房产信息">
            <el-upload
              :on-success="onUploadSuccessHouse"
              :on-remove="onUploadRemove"
              :multiple="false"
              :action="uploadUrl"
              :data="{ module: 'house' }"
              list-type="picture-card"
            >
              <i class="el-icon-plus"></i>
            </el-upload>
          </el-form-item>
          <el-form-item label="车辆信息">
            <el-upload
              :on-success="onUploadSuccessCar"
              :on-remove="onUploadRemove"
              :multiple="false"
              :action="uploadUrl"
              :data="{ module: 'car' }"
              list-type="picture-card"
            >
              <i class="el-icon-plus"></i>
            </el-upload>
          </el-form-item>
        </el-form>

        <el-form label-width="120px">
          <el-form-item
            style="display: flex;margin-left: 121px;justify-content: flex-end;padding-right: 100px;"
          >
            <el-button
              type="primary"
              :disabled="submitBtnDisabled"
              @click="save"
            >
              提交
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 第二步显示内容 -->
      <div v-if="active === 1">
        <div style="margin-top:40px;">
          <el-alert
            title="您的认证申请已成功提交，请耐心等待"
            type="warning"
            show-icon
            :closable="false"
          >
            我们将在2小时内完成审核，审核时间为周一至周五8:00至20:00。
          </el-alert>
        </div>
      </div>

      <!-- 第三步显示内容 -->
      <div v-if="active === 3">
        <div style="margin-top:40px;">
          <el-alert
            v-if="borrowerStatus === 2"
            title="您的认证审批已通过"
            type="success"
            show-icon
            :closable="false"
          >
          </el-alert>

          <NuxtLink to="/user/apply" v-if="borrowerStatus === 2">
            <el-button style="margin-top:20px;" type="success">
              我要借款
            </el-button>
          </NuxtLink>

          <el-alert
            v-if="borrowerStatus === -1"
            title="您的认证审批未通过"
            type="error"
            show-icon
            :closable="false"
          >
          </el-alert>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    let BASE_API = process.env.BASE_API
    return {
      active: null,
      submitBtnDisabled: false,
      borrowerStatus: null,
      // 借款人信息
      borrower: {
        borrowerAttachList: [],
      },
      // 学历列表
      educationList: [],
      // 行业列表
      industryList: [],
      // 月收入列表
      incomeList: [],
      // 还款来源列表
      returnSourceList: [],
      // 联系人关系
      contactsRelationList: [],
      // 文件上传地址
      uploadUrl: BASE_API + '/api/oss/file/upload',
    }
  },

  mounted() {
    this.getUserInfo()
  },

  methods: {
    // 获取当前登录用户的认证状态
    getUserInfo() {
      this.$axios
        .$get('/api/core/borrower/auth/getBorrowerStatus')
        .then((response) => {
          this.borrowerStatus = response.data.borrowerStatus
          if (this.borrowerStatus === 0) {
            //未认证
            this.active = 0
            //获取下拉列表
            this.initSelectionData()
          } else if (this.borrowerStatus === 1) {
            //认证中
            this.active = 1
          } else if (this.borrowerStatus === 2) {
            //认证成功
            this.active = 3
          } else if (this.borrowerStatus === -1) {
            //认证失败
            this.active = 3
          }
        })
    },

    // 获取下拉列表中的数据字典
    initSelectionData() {
      //学历列表
      this.$axios
        .$get('/api/core/dict/getDictByDictCode/education')
        .then((response) => {
          this.educationList = response.data.records
        })

      //行业列表
      this.$axios
        .$get('/api/core/dict/getDictByDictCode/industry')
        .then((response) => {
          this.industryList = response.data.records
        })

      //收入列表
      this.$axios
        .$get('/api/core/dict/getDictByDictCode/income')
        .then((response) => {
          this.incomeList = response.data.records
        })

      //还款来源列表
      this.$axios
        .$get('/api/core/dict/getDictByDictCode/returnSource')
        .then((response) => {
          this.returnSourceList = response.data.records
        })

      //联系人关系列表
      this.$axios
        .$get('/api/core/dict/getDictByDictCode/relation')
        .then((response) => {
          this.contactsRelationList = response.data.records
        })
    },

    onUploadSuccessIdCard1(response, file) {
      this.onUploadSuccess(response, file, 'idCard1')
    },

    onUploadSuccessIdCard2(response, file) {
      this.onUploadSuccess(response, file, 'idCard2')
    },

    onUploadSuccessHouse(response, file) {
      this.onUploadSuccess(response, file, 'house')
    },

    onUploadSuccessCar(response, file) {
      this.onUploadSuccess(response, file, 'car')
    },

    /**
     * 上传成功后回调
     */
    onUploadSuccess(response, file, type) {
      if (response.code !== 0) {
        this.$message.error(response.message)
        return
      }
      // 填充上传文件列表
      this.borrower.borrowerAttachList.push({
        imageName: file.name,
        imageUrl: response.data.url,
        imageType: type,
      })
    },

    onUploadRemove(file) {
      this.$axios
        .$delete('/api/oss/file/remove?url=' + file.response.data.url)
        .then((response) => {
          console.log('删除阿里云中的存储的文件')
          if (response.code != 0) {
            this.$message.error(response.message)
            return
          } else {
            // 删除 borrower.borrowerAttachList 中的文件
            this.$message.success(response.message)
            this.borrower.borrowerAttachList = this.borrower.borrowerAttachList.filter(
              (item) => {
                return item.imageUrl != file.response.data.url
              }
            )
          }
        })
    },

    onExceed() {
      this.$message.warning('只能上传一张身份证照片')
    },

    save() {
      this.submitBtnDisabled = true
      this.$axios
        .$post('/api/core/borrower/auth/save', this.borrower)
        .then((response) => {
          this.active = 1
        })
    },
  },
}
</script>

<style>
.idCard1 .el-upload--picture-card {
  width: 240px !important;
}

.idCard1 .el-upload-list--picture-card .el-upload-list__item {
  width: 240px !important;
}

.el-step__head.is-success {
  color: #28a7e1 !important;
  border-color: #28a7e1 !important;
}
.el-step__title.is-success {
  color: #28a7e1 !important;
}
.el-step__description.is-success {
  color: #28a7e1 !important;
}
.el-step__icon-inner.is-status.el-icon-check {
  color: #28a7e1 !important;
}
</style>
