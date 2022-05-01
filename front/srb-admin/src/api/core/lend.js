import request from '@/utils/request'

export default {
  getLendList(page, limit, keyword) {
    return request({
      url: `/admin/core/lend/list/${page}/${limit}`,
      params: {keyword},
      method: 'get'
    })
  },
  getLendDetail(id) {
    return request({
      url: `/admin/core/lend/getLendDetail/${id}`,
      method: 'get'
    })
  },
  makeLoan(id) {
    return request({
      url: `/admin/core/lend/makeLoan/${id}`,
      method: 'get'
    })
  },
}