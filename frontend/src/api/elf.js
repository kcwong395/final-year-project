import request from '@/utils/request2'
import axios from 'axios'

export function getElvesWithPage(page, size) {
  return request({
    url: `/api/elf/${page}/${size}`,
    method: 'get'
  })
}

export function createElf() {
  return request({
    url: '/api/elf/create',
    method: 'post'
  })
}

export function deleteElf(elfId) {
  return request({
    url: `/api/elf/delete/${elfId}`,
    method: 'post'
  })
}

export function getElfLocation() {
  return axios.create({
    url: 'https://ipapi.co/json/', // url = base url + request url
    timeout: 5000 // request timeout
  })
}
