import request from '@/utils/request'
export default {
  listByParentId(parentId) {
    return request({
      url: `/admin/core/dict/getDictDataByParentId/${parentId}`,
      method: 'get'
    })
  }
}