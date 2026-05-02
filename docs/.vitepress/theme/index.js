import DefaultTheme from 'vitepress/theme'
import './custom.css'

// Add lightbox keyframes
const style = document.createElement('style')
style.textContent = `
  @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
  @keyframes fadeOut { from { opacity: 1; } to { opacity: 0; } }
  @keyframes scaleIn { from { transform: scale(0.9); opacity: 0; } to { transform: scale(1); opacity: 1; } }
`
document.head.appendChild(style)

// Image lightbox for desktop with blur backdrop
if (window.matchMedia('(min-width: 768px)').matches) {
  document.addEventListener('click', (e) => {
    const img = e.target.closest('.vp-doc img')
    if (!img) return

    const overlay = document.createElement('div')
    overlay.style.cssText = `
      position: fixed;
      inset: 0;
      background: rgba(255, 255, 255, 0.1);
      backdrop-filter: blur(12px);
      -webkit-backdrop-filter: blur(12px);
      z-index: 9999;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: zoom-out;
      animation: fadeIn 0.2s ease;
    `

    const imgEl = document.createElement('img')
    imgEl.src = img.src
    imgEl.style.cssText = `
      max-width: 90vw;
      max-height: 90vh;
      border-radius: 8px;
      box-shadow: 0 8px 32px rgba(0,0,0,0.4);
      animation: scaleIn 0.2s ease;
    `

    overlay.appendChild(imgEl)
    document.body.appendChild(overlay)
    document.body.style.overflow = 'hidden'

    overlay.addEventListener('click', () => {
      overlay.style.animation = 'fadeOut 0.2s ease forwards'
      setTimeout(() => {
        overlay.remove()
        document.body.style.overflow = ''
      }, 200)
    })
  })
}

export default {
  extends: DefaultTheme
}