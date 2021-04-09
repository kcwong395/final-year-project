import request from '@/utils/request2'
const moment = require('moment')

export function getLogsWithPage(category, page, size) {
  return request({
    url: `/api/log/${category}/${page}/${size}`,
    method: 'get'
  })
}

export function getLogsByTaskId(taskId) {
  return request({
    url: `/api/log?taskId=${taskId}`,
    method: 'get'
  })
}

export function getLogsByTaskIdWithPages(taskId, page, size) {
  return request({
    url: `/api/log/task/${taskId}/${page}/${size}`,
    method: 'get'
  })
}

export function getLogsByElfId(elfId) {
  return request({
    url: `/api/log?elfId=${elfId}`,
    method: 'get'
  })
}

export function getLogsByElfIdWithPages(elfId, page, size) {
  return request({
    url: `/api/log/elf/${elfId}/${page}/${size}`,
    method: 'get'
  })
}

export function addLog(header, content, type) {
  return request({
    url: `/api/log`,
    method: 'post',
    data: {
      category: 'System',
      header: header,
      content: content,
      time: moment(moment()).format('YYYY-MM-DD HH:mm:ss'),
      type: type
    }
  })
}
