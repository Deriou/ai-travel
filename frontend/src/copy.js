import { ElMessage } from 'element-plus'

// 把一条路线整理成便于粘贴到其他应用的纯文本。
export function formatRoute(route) {
  let text = `目的地：${route.destination}（${route.days}天）\n游玩偏好：${route.preference}\n\n【路线安排】\n${route.routeContent}`
  if (route.tipsContent) {
    text += `\n\n【出行小贴士】\n${route.tipsContent}`
  }
  return text
}

// 复制到剪贴板；两种方式都不可用时提示手动选择文本。
export async function copyText(text) {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制，可粘贴到其他应用')
    return
  } catch {
    // 部分浏览器或非 https 地址不允许使用 clipboard，改用下面的传统方式。
  }

  // 传统方式：放入一个看不见的文本框，选中后执行复制命令。
  const textarea = document.createElement('textarea')
  textarea.value = text
  textarea.style.position = 'fixed'
  textarea.style.opacity = '0'
  document.body.appendChild(textarea)
  textarea.select()
  const ok = document.execCommand('copy')
  document.body.removeChild(textarea)

  if (ok) {
    ElMessage.success('已复制，可粘贴到其他应用')
  } else {
    ElMessage.warning('复制失败，请手动选择文本复制')
  }
}
