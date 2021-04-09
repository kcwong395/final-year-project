/**
 * Created by PanJiaChen on 16/11/18.
 */

/**
 * @param {string} path
 * @returns {Boolean}
 */
export function isExternal(path) {
  return /^(https?:|mailto:|tel:)/.test(path)
}

/**
 * @param {string} str
 * @returns {Boolean}
 */
export function validUsername(str) {
  const valid_map = ['admin', 'editor']
  return valid_map.indexOf(str.trim()) >= 0
}

/**
 * Created by Martin Wong on 20/01/21.
 */
import { Message } from 'element-ui'

export function validFileExtension(file, fileExtension) {
  if (fileExtension === 'csv') {
    const isCsv = file.type === 'application/vnd.ms-excel'
    if (!isCsv) {
      Message({
        message: 'Only csv file is allowed!',
        type: 'error'
      })
    }
    return isCsv
  } else {
    return false
  }
}
