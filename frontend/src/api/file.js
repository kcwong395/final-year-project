import request from '@/utils/request2'

export function uploadFile(data) {
  return request({
    url: '/api/files/uploads',
    method: 'post',
    data
  })
}

export function getFilesUnderTask(id) {
  return request({
    url: `/api/files/${id}`,
    method: 'get'
  })
}

export function deleteFile(id, position) {
  return request({
    url: `/api/files/${id}/${position}`,
    method: 'delete'
  })
}

export function downloadFile(id, position) {
  return request({
    url: `/api/files/downloads/${id}/${position}`,
    method: 'get'
  })
}
