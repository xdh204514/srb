import request from '@/utils/request'

export default {
  getBorrowInfoList(page, limit, keyword) {
    return request({
      url: `/admin/core/borrowInfo/list/${page}/${limit}`,
      params: { keyword },
      method: 'get'
    })
  },
  getBorrowInfoDetail(id) {
    return request({
      url: `/admin/core/borrowInfo/getBorrowInfoDetail/${id}`,
      method: 'get'
    })
  },
  approval(borrowInfoApproval) {
    return request({
      url: '/admin/core/borrowInfo/approval',
      method: 'post',
      data: borrowInfoApproval
    })
  },
}