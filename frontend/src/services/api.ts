import axios from 'axios'
import { useToast } from '../composables/useToast'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080'
})

api.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('auth_token') || localStorage.getItem('auth_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && !error.config.url?.includes('/api/auth/login')) {
      sessionStorage.removeItem('auth_token')
      localStorage.removeItem('auth_token')
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    } else {
      const { error: toastError } = useToast()
      
      if (!error.response) {
        toastError('Não foi possível conectar ao servidor. Verifique sua conexão.')
      } else if (error.response.status >= 500) {
        toastError('Ocorreu um erro inesperado no servidor. Tente novamente mais tarde.')
      } else if (error.response.status === 403) {
        toastError('Você não tem permissão para acessar este recurso.')
      }
    }
    
    return Promise.reject(error)
  }
)

export default api
