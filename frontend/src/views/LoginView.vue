<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'
import type { ApiErrorResponse } from '../types/api'
import axios from 'axios'
import Icon from '../components/atoms/Icon.vue'
import Input from '../components/atoms/Input.vue'
import Button from '../components/atoms/Button.vue'

const router = useRouter()
const { login } = useAuth()

const tenantId = ref('')
const username = ref('')
const password = ref('')
const rememberMe = ref(false)
const showPassword = ref(false)

const isLoading = ref(false)
const errorMessage = ref('')

const handleLogin = async () => {
  if (!tenantId.value || !username.value || !password.value) {
    errorMessage.value = 'Preencha todos os campos obrigatórios.'
    return
  }

  isLoading.value = true
  errorMessage.value = ''

  try {
    await login({
      tenantId: tenantId.value,
      username: username.value,
      password: password.value
    }, rememberMe.value)
    
    router.push('/dashboard')
  } catch (error: any) {
    if (axios.isAxiosError(error) && error.response) {
      if (error.response.status === 401) {
        errorMessage.value = 'Credenciais inválidas ou ambiente não encontrado.'
      } else {
        const data = error.response.data as ApiErrorResponse
        errorMessage.value = data.message || 'Ocorreu um erro ao realizar o login.'
      }
    } else {
      errorMessage.value = 'Não foi possível conectar ao servidor. Tente novamente.'
    }
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-bg-base flex flex-col md:flex-row">
    <div class="relative bg-primary overflow-hidden flex flex-col justify-between shrink-0 md:w-1/2 lg:w-[45%] h-[30vh] md:h-auto">
      <svg class="absolute inset-0 w-full h-full object-cover opacity-20" viewBox="0 0 100 100" preserveAspectRatio="none">
        <polygon points="0,100 100,0 100,100" fill="#004D4D" />
        <circle cx="20" cy="20" r="40" fill="#00CCCC" opacity="0.3" />
        <circle cx="80" cy="90" r="30" fill="#004D4D" opacity="0.6" />
      </svg>
      
      <div class="relative z-10 p-6 md:p-12 h-full flex flex-col justify-center md:justify-start">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-xl bg-secondary flex items-center justify-center shadow-lg">
            <Icon name="shield" class="w-6 h-6 text-primary" />
          </div>
          <div>
            <h1 class="text-white font-bold text-xl leading-tight tracking-tight">Health Admin</h1>
            <p class="text-white/60 text-xs font-medium uppercase tracking-widest mt-0.5">Enterprise</p>
          </div>
        </div>
        
        <div class="hidden md:block mt-24 max-w-md">
          <h2 class="text-3xl lg:text-4xl font-bold text-white leading-tight">
            Gestão inteligente de beneficiários em um único ambiente.
          </h2>
          <p class="mt-6 text-lg text-white/80 leading-relaxed">
            Plataforma corporativa de alta performance para administração de saúde integrada.
          </p>
        </div>
      </div>
      
      <div class="hidden md:block relative z-10 p-6 md:p-12 text-white/50 text-sm">
        &copy; 2026 Health Admin. Todos os direitos reservados.
      </div>
    </div>
    <div class="flex-1 flex flex-col justify-center px-4 md:px-8 lg:px-16 relative -mt-8 md:mt-0 z-20 pb-8">
      <div class="w-full max-w-md mx-auto">
        <div class="bg-surface rounded-2xl shadow-xl shadow-black/5 p-6 sm:p-8 border border-border-base">
          <div class="mb-8">
            <h3 class="text-2xl font-bold text-text-primary">Acesse sua conta</h3>
            <p class="text-text-secondary mt-2 text-sm">Insira suas credenciais para entrar na plataforma.</p>
          </div>

          <form @submit.prevent="handleLogin" class="space-y-5">
            <div v-if="errorMessage" class="p-4 bg-red-50 border border-red-100 rounded-xl flex gap-3 text-error">
              <Icon name="close" class="w-5 h-5 shrink-0 mt-0.5" />
              <p class="text-sm font-medium">{{ errorMessage }}</p>
            </div>
            <div class="space-y-1.5">
              <label for="tenant" class="block text-sm font-medium text-text-primary">Tenant</label>
              <div class="relative">
                <div class="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-text-secondary">
                  <Icon name="building" class="w-5 h-5" />
                </div>
                <Input
                  id="tenant"
                  v-model="tenantId"
                  type="text"
                  placeholder="Identificação do ambiente"
                  class="pl-11 block w-full"
                  :disabled="isLoading"
                />
              </div>
            </div>
            <div class="space-y-1.5">
              <label for="username" class="block text-sm font-medium text-text-primary">Usuário</label>
              <div class="relative">
                <div class="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-text-secondary">
                  <Icon name="user" class="w-5 h-5" />
                </div>
                <Input
                  id="username"
                  v-model="username"
                  type="text"
                  placeholder="Seu e-mail ou login"
                  class="pl-11 block w-full"
                  :disabled="isLoading"
                />
              </div>
            </div>
            <div class="space-y-1.5">
              <label for="password" class="block text-sm font-medium text-text-primary">Senha</label>
              <div class="relative">
                <div class="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-text-secondary">
                  <Icon name="lock" class="w-5 h-5" />
                </div>
                <Input
                  id="password"
                  v-model="password"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="Sua senha"
                  class="pl-11 pr-11 block w-full"
                  :disabled="isLoading"
                />
                <button
                  type="button"
                  @click="showPassword = !showPassword"
                  class="absolute inset-y-0 right-0 pr-3.5 flex items-center text-text-secondary hover:text-text-primary transition-colors"
                  tabindex="-1"
                >
                  <Icon :name="showPassword ? 'eye-off' : 'eye'" class="w-5 h-5" />
                </button>
              </div>
            </div>
            <div class="flex items-center">
              <input
                id="remember"
                v-model="rememberMe"
                type="checkbox"
                class="w-4 h-4 text-primary bg-surface border-border-base rounded focus:ring-primary focus:ring-2"
                :disabled="isLoading"
              />
              <label for="remember" class="ml-2.5 block text-sm text-text-secondary">
                Lembrar-me neste dispositivo
              </label>
            </div>
            <Button
              type="submit"
              variant="primary"
              class="w-full justify-center mt-2"
              :disabled="isLoading"
            >
              {{ isLoading ? 'Entrando...' : 'Entrar na plataforma' }}
            </Button>
          </form>
        </div>
        <div class="mt-8 text-center md:hidden">
          <button class="text-sm text-text-secondary font-medium flex items-center justify-center gap-2 w-full hover:text-primary transition-colors">
            <Icon name="headphones" class="w-4 h-4" />
            Suporte Técnico
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
