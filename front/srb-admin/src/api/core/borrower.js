import request from '@/utils/request'
export default {
  getBorrowerList(page, limit, keyword) {
    return request({
      url: `/admin/core/borrower/list/${page}/${limit}`,
      params: { keyword },
      method: 'get',
    })
  },
  getBorrowerDeatilById(id) {
    return request({
      url: `/admin/core/borrower/detail/${id}`,
      method: 'get',
    })
  },
  approval(borrowerApproval) {
    return request({
      url: '/admin/core/borrower/approval',
      method: 'post',
      data: borrowerApproval,
    })
  },
}
