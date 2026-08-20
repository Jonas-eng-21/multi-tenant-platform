<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import AdminLayout from '../layouts/AdminLayout.vue'
import Input from '../components/atoms/Input.vue'
import Select from '../components/atoms/Select.vue'
import Button from '../components/atoms/Button.vue'
import Icon from '../components/atoms/Icon.vue'
import Avatar from '../components/atoms/Avatar.vue'
import { beneficiarioService } from '../services/beneficiario.service'
import { pessoaService } from '../services/pessoa.service'
import type { PessoaResponse } from '../types/api'
import { useToast } from '../composables/useToast'

const router = useRouter()
const route = useRoute()
const { success, error: toastError } = useToast()

const isEditMode = computed(() => !!route.params.id)
const beneficiarioId = computed(() => route.params.id as string)

const loading = ref(false)
const submitting = ref(false)
const loadingPessoas = ref(false)

const form = ref({
  matricula: '',
  dataAdesao: '',
  tipo: '',
  status: ''
})

const errors = ref<Record<string, string>>({})

const searchPessoaText = ref('')
const pessoaSelecionada = ref<PessoaResponse | null>(null)
const pessoasResult = ref<PessoaResponse[]>([])
const showDropdown = ref(false)
let debounceTimeout: number | undefined

const formatCpf = (cpf: string) => {
  if (!cpf) return ''
  const cleanCpf = cpf.replace(/\D/g, '')
  if (cleanCpf.length === 11) {
    return cleanCpf.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4')
  }
  return cpf
}

const loadBeneficiario = async () => {
  if (!isEditMode.value) return
  
  loading.value = true
  try {
    const data = await beneficiarioService.obterPorId(beneficiarioId.value)
    
    form.value = {
      matricula: data.matricula,
      dataAdesao: data.dataAdesao,
      tipo: data.tipo,
      status: data.status
    }
    
    pessoaSelecionada.value = data.pessoa
    
  } catch (err: any) {
    console.error(err)
    if (err.response?.status === 404) {
      toastError('Beneficiário não encontrado.')
      router.replace('/beneficiarios')
    } else if (err.response && err.response.status < 500 && err.response.status !== 403) {
      toastError('Erro ao carregar os dados do beneficiário.')
    }
  } finally {
    loading.value = false
  }
}

const fetchPessoas = async (term: string) => {
  if (!term.trim()) {
    pessoasResult.value = []
    showDropdown.value = false
    return
  }
  
  loadingPessoas.value = true
  try {
    let nome: string | undefined = undefined
    let cpf: string | undefined = undefined
    
    const isCpf = /^[\d.-]+$/.test(term)
    if (isCpf) {
      cpf = term.replace(/\D/g, '')
    } else {
      nome = term
    }

    const response = await pessoaService.listar({
      page: 0, 
      size: 5,
      nome,
      cpf
    })
    
    pessoasResult.value = response.content
    showDropdown.value = true
  } catch (err) {
    console.error('Erro ao buscar pessoas:', err)
  } finally {
    loadingPessoas.value = false
  }
}

const onSearchInput = () => {
  clearTimeout(debounceTimeout)
  if (searchPessoaText.value.trim().length > 0) {
    showDropdown.value = true
    debounceTimeout = window.setTimeout(() => {
      fetchPessoas(searchPessoaText.value) 
    }, 500)
  } else {
    pessoasResult.value = []
    showDropdown.value = false
  }
}

const selectPessoa = (pessoa: PessoaResponse) => {
  pessoaSelecionada.value = pessoa
  searchPessoaText.value = ''
  showDropdown.value = false
  if (errors.value.pessoaId) delete errors.value.pessoaId
}

const clearPessoa = () => {
  pessoaSelecionada.value = null
}

const handleClickOutside = (e: MouseEvent) => {
  const target = e.target as HTMLElement
  if (!target.closest('.pessoa-search-container')) {
    showDropdown.value = false
  }
}

const validateForm = () => {
  errors.value = {}
  let valid = true

  if (!isEditMode.value && !pessoaSelecionada.value) {
    errors.value.pessoaId = 'Por favor, selecione uma pessoa'
    valid = false
  }

  if (!form.value.matricula.trim()) {
    errors.value.matricula = 'A matrícula é obrigatória'
    valid = false
  }

  if (!form.value.dataAdesao) {
    errors.value.dataAdesao = 'A data de adesão é obrigatória'
    valid = false
  }

  if (!form.value.tipo) {
    errors.value.tipo = 'O tipo é obrigatório'
    valid = false
  }

  if (!form.value.status) {
    errors.value.status = 'O status é obrigatório'
    valid = false
  }

  return valid
}

const handleSubmit = async () => {
  if (!validateForm() || (!isEditMode.value && !pessoaSelecionada.value)) return

  submitting.value = true
  errors.value = {}

  try {
    if (isEditMode.value) {
      const updatePayload = {
        matricula: form.value.matricula.trim(),
        dataAdesao: form.value.dataAdesao,
        tipo: form.value.tipo as any,
        status: form.value.status as any
      }
      
      await beneficiarioService.atualizar(beneficiarioId.value, updatePayload)
      success('Beneficiário atualizado com sucesso.')
    } else {
      const createPayload = {
        pessoaId: pessoaSelecionada.value!.id,
        matricula: form.value.matricula.trim(),
        dataAdesao: form.value.dataAdesao,
        tipo: form.value.tipo as any,
        status: form.value.status as any
      }
      
      await beneficiarioService.criar(createPayload)
      success('Beneficiário cadastrado com sucesso.')
    }
    router.push('/beneficiarios')
  } catch (err: any) {
    console.error(err)
    
    if (err.response?.status === 409) {
      toastError('Esta matrícula já está em uso neste ambiente.')
      errors.value.matricula = 'Matrícula duplicada'
    } else if (err.response?.status === 400 && err.response.data?.fieldErrors) {
      const fieldErrors = err.response.data.fieldErrors
      Object.keys(fieldErrors).forEach(field => {
        errors.value[field] = fieldErrors[field]
      })
      toastError('Verifique os erros nos campos informados.')
    } else if (err.response && err.response.status < 500 && err.response.status !== 403) {
      toastError(isEditMode.value ? 'Não foi possível atualizar o beneficiário.' : 'Não foi possível cadastrar o beneficiário.')
    }
  } finally {
    submitting.value = false
  }
}

const goBack = () => {
  router.push('/beneficiarios')
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  loadBeneficiario()
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <AdminLayout>
    <div class="space-y-6 max-w-4xl mx-auto">
      
      <div class="flex flex-col sm:flex-row sm:items-center gap-4">
        <button @click="goBack" class="sm:hidden self-start p-2 -ml-2 text-text-secondary hover:text-text-primary rounded-lg">
          <Icon name="arrow-left" class="w-6 h-6" />
        </button>
        <div>
          <nav class="hidden sm:flex text-sm text-text-secondary mb-1" aria-label="Breadcrumb">
            <ol class="inline-flex items-center space-x-1 md:space-x-2">
              <li class="inline-flex items-center">
                <router-link to="/beneficiarios" class="hover:text-primary transition-colors">Beneficiários</router-link>
              </li>
              <li>
                <div class="flex items-center">
                  <Icon name="chevron-right" class="w-4 h-4 mx-1" />
                  <span class="text-text-primary">{{ isEditMode ? 'Editar beneficiário' : 'Novo beneficiário' }}</span>
                </div>
              </li>
            </ol>
          </nav>
          <h1 class="text-2xl md:text-3xl font-bold text-text-primary">
            {{ isEditMode ? 'Editar beneficiário' : 'Novo beneficiário' }}
          </h1>
          <p class="text-text-secondary text-sm mt-1">
            {{ isEditMode ? 'Atualize os dados do beneficiário abaixo.' : 'Preencha os dados abaixo para vincular uma pessoa como beneficiário.' }}
          </p>
        </div>
      </div>

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

      <div v-else class="bg-white border border-border-base rounded-xl overflow-hidden shadow-sm">
        <form @submit.prevent="handleSubmit">
          <div class="p-4 sm:p-6 space-y-8">
            
            <div>
              <h3 class="text-lg font-medium text-text-primary mb-4 border-b border-border-base pb-2">Vínculo Pessoal</h3>
              
              <div v-if="!pessoaSelecionada" class="pessoa-search-container relative max-w-xl">
                <Input 
                  v-model="searchPessoaText" 
                  label="Buscar Pessoa" 
                  placeholder="Digite nome ou CPF..."
                  :error="errors.pessoaId"
                  :disabled="submitting"
                  @input="onSearchInput"
                  @focus="onSearchInput"
                />
                
                <div v-if="showDropdown && searchPessoaText" class="absolute z-10 w-full mt-1 bg-white border border-border-base rounded-lg shadow-lg overflow-hidden">
                  <div v-if="loadingPessoas" class="p-4 text-center text-sm text-text-secondary">
                    Buscando...
                  </div>
                  <div v-else-if="pessoasResult.length === 0" class="p-4 text-center text-sm text-text-secondary">
                    Nenhuma pessoa encontrada.
                  </div>
                  <ul v-else class="max-h-60 overflow-y-auto">
                    <li 
                      v-for="pessoa in pessoasResult" 
                      :key="pessoa.id"
                      @click="selectPessoa(pessoa)"
                      class="p-3 hover:bg-gray-50 cursor-pointer border-b border-gray-100 last:border-0 flex items-center gap-3 transition-colors"
                    >
                      <Avatar size="sm" :name="pessoa.nome" class="shrink-0" />
                      <div class="flex-1 min-w-0">
                        <p class="text-sm font-medium text-text-primary truncate">{{ pessoa.nome }}</p>
                        <p class="text-xs text-text-secondary truncate">CPF: {{ formatCpf(pessoa.cpf) }}</p>
                      </div>
                    </li>
                  </ul>
                </div>
              </div>

              <!-- Box da Pessoa Selecionada -->
              <div v-else class="max-w-xl bg-blue-50 border border-blue-100 rounded-lg p-4 flex items-start justify-between gap-4">
                <div class="flex items-center gap-3 min-w-0">
                  <Avatar size="md" :name="pessoaSelecionada.nome" class="shrink-0" />
                  <div class="min-w-0">
                    <p class="text-sm text-blue-600 font-medium mb-0.5">Pessoa Selecionada</p>
                    <p class="font-semibold text-text-primary truncate">{{ pessoaSelecionada.nome }}</p>
                    <p class="text-sm text-text-secondary mt-0.5">CPF: {{ formatCpf(pessoaSelecionada.cpf) }}</p>
                  </div>
                </div>
                <button 
                  v-if="!isEditMode"
                  type="button" 
                  @click="clearPessoa"
                  :disabled="submitting"
                  class="text-blue-500 hover:text-blue-700 p-1 rounded-md hover:bg-blue-100 transition-colors shrink-0 disabled:opacity-50"
                  title="Trocar pessoa"
                >
                  <Icon name="close" class="w-5 h-5" />
                </button>
              </div>
            </div>

            <div>
              <h3 class="text-lg font-medium text-text-primary mb-4 border-b border-border-base pb-2">Dados do Beneficiário</h3>
              
              <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 sm:gap-6">
                <div>
                  <Input 
                    v-model="form.matricula" 
                    label="Matrícula" 
                    placeholder="Ex: MAT-12345"
                    :error="errors.matricula"
                    :disabled="submitting"
                    @input="errors.matricula = ''"
                  />
                </div>
                
                <div>
                  <Input 
                    v-model="form.dataAdesao" 
                    type="date"
                    label="Data de adesão" 
                    :error="errors.dataAdesao"
                    :disabled="submitting"
                    @input="errors.dataAdesao = ''"
                  />
                </div>

                <div>
                  <Select
                    v-model="form.tipo"
                    label="Tipo de Beneficiário"
                    placeholder="Selecione..."
                    :options="[
                      { value: 'TITULAR', label: 'Titular' },
                      { value: 'DEPENDENTE', label: 'Dependente' }
                    ]"
                    :error="errors.tipo"
                    :disabled="submitting"
                    @update:model-value="errors.tipo = ''"
                  />
                </div>

                <div>
                  <Select
                    v-model="form.status"
                    label="Status"
                    placeholder="Selecione..."
                    :options="[
                      { value: 'ATIVO', label: 'Ativo' },
                      { value: 'INATIVO', label: 'Inativo' },
                      { value: 'SUSPENSO', label: 'Suspenso' },
                      { value: 'CANCELADO', label: 'Cancelado' }
                    ]"
                    :error="errors.status"
                    :disabled="submitting"
                    @update:model-value="errors.status = ''"
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
              {{ isEditMode ? 'Salvar alterações' : 'Salvar beneficiário' }}
            </Button>
          </div>
        </form>
      </div>

    </div>
  </AdminLayout>
</template>
