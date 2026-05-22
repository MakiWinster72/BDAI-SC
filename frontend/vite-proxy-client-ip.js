/** Vite dev proxy：从入站连接解析真实客户端 IP（跳过 127.0.0.1 等本机回环） */

function stripIpv6Mapped(ip) {
  return ip.replace(/^::ffff:/i, '')
}

function normalizeIp(raw) {
  if (raw == null) return null
  let ip = String(raw).trim()
  if (!ip || ip.toLowerCase() === 'unknown') return null
  ip = stripIpv6Mapped(ip)
  if (ip.startsWith('[') && ip.endsWith(']')) {
    ip = ip.slice(1, -1)
  }
  return ip || null
}

function isLoopback(ip) {
  const v = normalizeIp(ip)
  if (!v) return true
  if (v === '::1' || v === '0:0:0:0:0:0:0:1') return true
  if (v.startsWith('127.')) return true
  return false
}

/** @param {import('http').IncomingMessage} req */
export function resolveClientIpFromReq(req) {
  const candidates = []

  const xff = req.headers['x-forwarded-for']
  if (xff) {
    candidates.push(...String(xff).split(','))
  }
  const realIp = req.headers['x-real-ip']
  if (realIp) {
    candidates.push(String(realIp))
  }
  const forwarded = req.headers['forwarded']
  if (forwarded) {
    for (const segment of String(forwarded).split(',')) {
      const match = /for=(?:"?)([^;,"\s]+)/i.exec(segment.trim())
      if (match) {
        candidates.push(match[1])
      }
    }
  }
  const sock = req.socket?.remoteAddress || req.connection?.remoteAddress
  if (sock) {
    candidates.push(String(sock))
  }

  for (const raw of candidates) {
    const ip = normalizeIp(raw)
    if (ip && !isLoopback(ip)) {
      return ip
    }
  }
  return null
}

/** @param {import('http').ClientRequest} proxyReq @param {import('http').IncomingMessage} req */
export function applyProxyClientIpHeaders(proxyReq, req) {
  const ip = resolveClientIpFromReq(req)
  if (!ip) {
    return
  }
  // 覆盖反代可能写入的 127.0.0.1，避免后端只看到本机
  proxyReq.removeHeader('x-forwarded-for')
  proxyReq.removeHeader('x-real-ip')
  proxyReq.setHeader('X-Forwarded-For', ip)
  proxyReq.setHeader('X-Real-IP', ip)
}
