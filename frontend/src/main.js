import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './assets/styles/styles.css'
import { useToast } from './composables/useToast'

const app = createApp(App)
app.use(router)
app.mount('#app')

// Expose global toast for cross-component usage
const { toast, success, error, info, warn } = useToast()
window.$toast = { toast, success, error, info, warn }
