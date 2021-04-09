import request from '@/utils/request2'

export function getShoeMakerWithPage(page, size) {
  return request({
    url: `/api/sm/${page}/${size}`,
    method: 'get'
  })
}
