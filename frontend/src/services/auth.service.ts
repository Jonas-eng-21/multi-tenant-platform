import api from './api'
import type { LoginRequest, LoginResponse, DecodedJwtPayload } from '../types/api'

class AuthService {
  private readonly TOKEN_KEY = 'auth_token'

  async login(request: LoginRequest, rememberMe: boolean): Promise<LoginResponse> {
    const response = await api.post<LoginResponse>('/api/auth/login', request)
    const token = response.data.token
    
    if (rememberMe) {
      localStorage.setItem(this.TOKEN_KEY, token)
      sessionStorage.removeItem(this.TOKEN_KEY)
    } else {
      sessionStorage.setItem(this.TOKEN_KEY, token)
      localStorage.removeItem(this.TOKEN_KEY)
    }
    
    return response.data
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY)
    sessionStorage.removeItem(this.TOKEN_KEY)
  }

  getToken(): string | null {
    return sessionStorage.getItem(this.TOKEN_KEY) || localStorage.getItem(this.TOKEN_KEY)
  }

  decodeToken(token: string): DecodedJwtPayload | null {
    try {
      const base64Url = token.split('.')[1]
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
      const jsonPayload = decodeURIComponent(
        window.atob(base64).split('').map(function(c) {
          return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
        }).join('')
      )
      return JSON.parse(jsonPayload)
    } catch (e) {
      return null
    }
  }

  isTokenValid(token: string): boolean {
    const decoded = this.decodeToken(token)
    if (!decoded || !decoded.exp) return false
    return (decoded.exp * 1000) > Date.now()
  }
}

export const authService = new AuthService()
