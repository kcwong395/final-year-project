import request from '@/utils/request2'
import { addLog } from '@/api/log'

export function submitTask(data) {
  // TODO: Change user to user id
  console.log(addLog('[Task Created]', 'user', 'Normal'))
  return request({
    url: '/api/task',
    method: 'post',
    data
  })
}

export function getTasksWithPage(page, size) {
  return request({
    url: `/api/task/${page}/${size}`,
    method: 'get'
  })
}

export function getTaskById(taskId) {
  return request({
    url: `/api/task/${taskId}`,
    method: 'get'
  })
}
