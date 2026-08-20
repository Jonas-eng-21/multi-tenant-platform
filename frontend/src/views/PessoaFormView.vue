<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AdminLayout from '../layouts/AdminLayout.vue'
import Input from '../components/atoms/Input.vue'
import Button from '../components/atoms/Button.vue'
import Icon from '../components/atoms/Icon.vue'
import { pessoaService } from '../services/pessoa.service'
import { useToast } from '../composables/useToast'

const route = useRoute()
const router = useRouter()
const { success, error: toastError } = useToast()

const isEditMode = computed(() => !!route.params.id)
const pessoaId = computed(() => route.params.id as string)

const loading = ref(false)
const submitting = ref(false)

const form = ref({
  nome: '',
  cpf: '',
  dataNascimento: '',
  email: ''
})

const errors = ref<Record<string, string>>({})

// Máscara e validação de CPF
const formatCpfVisual = (value: string) => {
  const v = value.replace(/\D/g, '').slice(0, 11)
  if (v.length <= 3) return v
  if (v.length <= 6) return `${v.slice(0, 3)}.${v.slice(3)}`
  if (v.length <= 9) return `${v.slice(0, 3)}.${v.slice(3, 6)}.${v.slice(6)}`
  return `${v.slice(0, 3)}.${v.slice(3, 6)}.${v.slice(6, 9)}-${v.slice(9)}`
}

const onCpfInput = (val: string | number) => {
  const strVal = String(val)
  form.value.cpf = formatCpfVisual(strVal)
  if (errors.value.cpf) delete errors.value.cpf
}

const unmaskCpf = (cpf: string) => cpf.replace(/\D/g, '')

const isValidCpf = (cpf: string) => {
  const str = cpf.replace(/\D/g, '')
  if (str.length !== 11) return false
  if (/^(\d)\1+$/.test(str)) return false
  
  let sum = 0
  let remainder
  
  for (let i = 1; i <= 9; i++) sum = sum + parseInt(str.substring(i - 1, i)) * (11 - i)
  remainder = (sum * 10) % 11
  if ((remainder === 10) || (remainder === 11)) remainder = 0
  if (remainder !== parseInt(str.substring(9, 10))) return false
  
  sum = 0
  for (let i = 1; i <= 10; i++) sum = sum + parseInt(str.substring(i - 1, i)) * (12 - i)
  remainder = (sum * 10) % 11
  if ((remainder === 10) || (remainder === 11)) remainder = 0
  if (remainder !== parseInt(str.substring(10, 11))) return false
  
  return true
}

const validateEmail = (email: string) => {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}

const validateForm = () => {
  errors.value = {}
  let valid = true

  if (!form.value.nome.trim()) {
    errors.value.nome = 'O nome é obrigatório'
    valid = false
  }

  const cleanCpf = unmaskCpf(form.value.cpf)
  if (!cleanCpf) {
    errors.value.cpf = 'O CPF é obrigatório'
    valid = false
  } else if (!isValidCpf(cleanCpf)) {
    errors.value.cpf = 'CPF inválido'
    valid = false
  }

  if (!form.value.dataNascimento) {
    errors.value.dataNascimento = 'A data de nascimento é obrigatória'
    valid = false
  }

  if (form.value.email && !validateEmail(form.value.email)) {
    errors.value.email = 'E-mail inválido'
    valid = false
  }

  return valid
}

const loadPessoa = async () => {
  if (!isEditMode.value) return
  
  loading.value = true
  try {
    const data = await pessoaService.obterPorId(pessoaId.value)
    form.value = {
      nome: data.nome,
      cpf: formatCpfVisual(data.cpf),
      dataNascimento: data.dataNascimento,
      email: data.email || ''
    }
  } catch (err: any) {
    console.error(err)
    if (err.response?.status === 404) {
      toastError('Pessoa não encontrada.')
      router.replace('/pessoas')
    } else if (err.response && err.response.status < 500 && err.response.status !== 403) {
      toastError('Erro ao carregar os dados da pessoa.')
    }
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!validateForm()) return

  submitting.value = true
  errors.value = {}

  const payload = {
    nome: form.value.nome.trim(),
    cpf: unmaskCpf(form.value.cpf),
    dataNascimento: form.value.dataNascimento,
    email: form.value.email?.trim() || null
  }

  try {
    if (isEditMode.value) {
      await pessoaService.atualizar(pessoaId.value, payload)
      success('Pessoa atualizada com sucesso.')
    } else {
      await pessoaService.criar(payload)
      success('Pessoa cadastrada com sucesso.')
    }
    router.push('/pessoas')
  } catch (err: any) {
    console.error(err)
    
    if (err.response?.status === 409) {
      toastError('Este CPF já está cadastrado.')
    } else if (err.response?.status === 400 && err.response.data?.fieldErrors) {
      const fieldErrors = err.response.data.fieldErrors
      Object.keys(fieldErrors).forEach(field => {
        errors.value[field] = fieldErrors[field]
      })
      toastError('Verifique os erros nos campos informados.')
    } else if (err.response && err.response.status < 500 && err.response.status !== 403) {
      toastError(isEditMode.value ? 'Não foi possível atualizar a pessoa.' : 'Não foi possível cadastrar a pessoa.')
    }
  } finally {
    submitting.value = false
  }
}

const goBack = () => {
  router.push('/pessoas')
}

onMounted(() => {
  loadPessoa()
})
</script>

<template>
  <AdminLayout>
    <div class="space-y-6 max-w-4xl mx-auto">
      
      <!-- Breadcrumb and Header (Desktop mainly) -->
      <div class="flex flex-col sm:flex-row sm:items-center gap-4">
        <button @click="goBack" class="sm:hidden self-start p-2 -ml-2 text-text-secondary hover:text-text-primary rounded-lg">
          <Icon name="arrow-left" class="w-6 h-6" />
        </button>
        <div>
          <nav class="hidden sm:flex text-sm text-text-secondary mb-1" aria-label="Breadcrumb">
            <ol class="inline-flex items-center space-x-1 md:space-x-2">
              <li class="inline-flex items-center">
                <router-link to="/pessoas" class="hover:text-primary transition-colors">Pessoas</router-link>
              </li>
              <li>
                <div class="flex items-center">
                  <Icon name="chevron-right" class="w-4 h-4 mx-1" />
                  <span class="text-text-primary">{{ isEditMode ? 'Editar pessoa' : 'Nova pessoa' }}</span>
                </div>
              </li>
            </ol>
          </nav>
          <h1 class="text-2xl md:text-3xl font-bold text-text-primary">
            {{ isEditMode ? 'Editar pessoa' : (isEditMode ? 'Novo Cadastro' : 'Nova pessoa') }}
          </h1>
          <p class="text-text-secondary text-sm mt-1">
            Preencha os dados abaixo para {{ isEditMode ? 'atualizar o cadastro' : 'cadastrar uma nova pessoa' }}.
          </p>
        </div>
      </div>

      <!-- Loading Skeleton (Edit Mode) -->
      <div v-if="loading" class="bg-white border border-border-base rounded-xl p-6 shadow-sm animate-pulse">
        <div class="h-6 bg-gray-200 rounded w-1/4 mb-6"></div>
        <div class="space-y-4">
          <div class="h-10 bg-gray-200 rounded w-full"></div>
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div class="h-10 bg-gray-200 rounded w-full"></div>
            <div class="h-10 bg-gray-200 rounded w-full"></div>
          </div>
          <div class="h-10 bg-gray-200 rounded w-full"></div>
        </div>
      </div>

      <!-- Form Card -->
      <div v-else class="bg-white border border-border-base rounded-xl overflow-hidden shadow-sm">
        <form @submit.prevent="handleSubmit">
          <div class="p-4 sm:p-6 space-y-6">
            <div>
              <h3 class="text-lg font-medium text-text-primary mb-4 border-b border-border-base pb-2">Informações Pessoais</h3>
              
              <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 sm:gap-6">
                <div class="sm:col-span-2">
                  <Input 
                    v-model="form.nome" 
                    label="Nome completo" 
                    placeholder="Ex: João da Silva"
                    :error="errors.nome"
                    :disabled="submitting"
                    @input="errors.nome = ''"
                  />
                </div>
                
                <div>
                  <Input 
                    :model-value="form.cpf" 
                    @update:model-value="onCpfInput"
                    label="CPF" 
                    placeholder="000.000.000-00"
                    :error="errors.cpf"
                    :disabled="submitting"
                  />
                </div>
                
                <div>
                  <Input 
                    v-model="form.dataNascimento" 
                    type="date"
                    label="Data de nascimento" 
                    :error="errors.dataNascimento"
                    :disabled="submitting"
                    @input="errors.dataNascimento = ''"
                  />
                </div>

                <div class="sm:col-span-2">
                  <Input 
                    v-model="form.email" 
                    type="email"
                    label="E-mail (opcional)" 
                    placeholder="Ex: joao@email.com"
                    :error="errors.email"
                    :disabled="submitting"
                    @input="errors.email = ''"
                  />
                </div>
              </div>
            </div>
          </div>
          
          <div class="bg-gray-50 px-4 py-4 sm:px-6 border-t border-border-base flex flex-col-reverse sm:flex-row items-center justify-end gap-3">
            <Button 
              type="button" 
              variant="outline" 
              class="w-full sm:w-auto justify-center"
              @click="goBack"
              :disabled="submitting"
            >
              Cancelar
            </Button>
            <Button 
              type="submit" 
              variant="primary" 
              class="w-full sm:w-auto justify-center"
              :loading="submitting"
            >
              {{ isEditMode ? 'Salvar alterações' : 'Salvar pessoa' }}
            </Button>
          </div>
        </form>
      </div>

    </div>
  </AdminLayout>
</template>
