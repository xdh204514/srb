import request from '@/utils/request'

export default {
  getPageList(page, limit, queryObj) {
    return request({
      url: `/admin/core/userInfo/list/${page}/${limit}`,
      method: 'get',
      params: queryObj
    })
  },

  lock(id, status) {
    return request({
      url: `/admin/core/userInfo/lock/${id}/${status}`,
      method: 'put'
    })
  },

  getuserLoginRecordTopFifty(userId) {
    return request({
      url: `/admin/core/userLoginRecord/getTopFiftyRecords/${userId}`,
      method: 'get'
    })
  }
}