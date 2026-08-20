import { ref } from 'vue'
import { authService } from '../services/auth.service'
import type { LoginRequest } from '../types/api'

const isAuthenticated = ref(false)
const isInitialized = ref(false)
const user = ref<{ username: string; tenantId: string } | null>(null)

export function useAuth() {
  const initialize = () => {
    const token = authService.getToken()
    if (token && authService.isTokenValid(token)) {
      const decoded = authService.decodeToken(token)
      if (decoded) {
        isAuthenticated.value = true
        user.value = {
          username: decoded.sub,
          tenantId: decoded.tenant_id
        }
      } else {
        authService.logout()
      }
    } else {
      authService.logout()
    }
    isInitialized.value = true
  }

  const login = async (request: LoginRequest, rememberMe: boolean = false) => {
    try {
      const response = await authService.login(request, rememberMe)
      const decoded = authService.decodeToken(response.token)
      if (decoded) {
        isAuthenticated.value = true
        user.value = {
          username: decoded.sub,
          tenantId: decoded.tenant_id
        }
      }
      return true
    } catch (error) {
      throw error
    }
  }

  const logout = () => {
    authService.logout()
    isAuthenticated.value = false
    user.value = null
  }

  return {
    isAuthenticated,
    isInitialized,
    user,
    initialize,
    login,
    logout
  }
}
