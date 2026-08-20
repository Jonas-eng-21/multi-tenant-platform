<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import AdminLayout from '../layouts/AdminLayout.vue'
import Button from '../components/atoms/Button.vue'
import SearchInput from '../components/molecules/SearchInput.vue'
import Table from '../components/organisms/Table.vue'
import Pagination from '../components/molecules/Pagination.vue'
import Card from '../components/organisms/Card.vue'
import Avatar from '../components/atoms/Avatar.vue'
import Icon from '../components/atoms/Icon.vue'
import { pessoaService } from '../services/pessoa.service'
import type { PageResponse, PessoaResponse } from '../types/api'
import ConfirmDialog from '../components/molecules/ConfirmDialog.vue'
import StateFeedback from '../components/molecules/StateFeedback.vue'
import { useToast } from '../composables/useToast'

const { success, error: toastError } = useToast()

const showDeleteModal = ref(false)
const pessoaToDelete = ref<PessoaResponse | null>(null)
const deleting = ref(false)

const loading = ref(true)
const error = ref<string | null>(null)
const pageData = ref<PageResponse<PessoaResponse>>({
  content: [],
  page: 0,
  size: 10,
  totalElements: 0,
  totalPages: 0,
  first: true,
  last: true
})

const searchTerm = ref('')
const currentPage = ref(1)

let debounceTimeout: number | undefined

const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const [year, month, day] = dateString.split('-')
  return `${day}/${month}/${year}`
}

const formatCpf = (cpf: string) => {
  if (!cpf) return ''
  const cleanCpf = cpf.replace(/\D/g, '')
  if (cleanCpf.length === 11) {
    return cleanCpf.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4')
  }
  return cpf
}

const fetchPessoas = async (page = 1) => {
  loading.value = true
  error.value = null
  
  try {
    let nome: string | undefined = undefined
    let cpf: string | undefined = undefined
    
    if (searchTerm.value.trim()) {
      const term = searchTerm.value.trim()
      const isCpf = /^[\d.-]+$/.test(term)
      if (isCpf) {
        cpf = term.replace(/\D/g, '')
      } else {
        nome = term
      }
    }

    const response = await pessoaService.listar({
      page: page - 1, 
      size: 10,
      nome,
      cpf
    })
    
    pageData.value = response
    currentPage.value = response.page + 1
  } catch (err: any) {
    console.error('Erro ao buscar pessoas:', err)
    error.value = 'Ocorreu um erro ao carregar os dados. Tente novamente mais tarde.'
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  clearTimeout(debounceTimeout)
  debounceTimeout = window.setTimeout(() => {
    fetchPessoas(1) 
  }, 500)
}

const handlePageChange = (page: number) => {
  fetchPessoas(page)
}

const handleRetry = () => {
  fetchPessoas(currentPage.value)
}

const promptDelete = (pessoa: PessoaResponse) => {
  pessoaToDelete.value = pessoa
  showDeleteModal.value = true
}

const confirmDelete = async () => {
  if (!pessoaToDelete.value) return
  
  deleting.value = true
  try {
    await pessoaService.excluir(pessoaToDelete.value.id)
    success('Pessoa excluída com sucesso.')
    showDeleteModal.value = false
    
    if (pageData.value.content.length === 1 && currentPage.value > 1) {
      fetchPessoas(currentPage.value - 1)
    } else {
      fetchPessoas(currentPage.value)
    }
    
    pessoaToDelete.value = null
  } catch (err: any) {
    console.error('Erro ao excluir pessoa:', err)
    if (err.response?.status === 409) {
      toastError('Não é possível excluir esta pessoa pois ela possui beneficiários vinculados.')
    } else if (err.response?.status === 404) {
      toastError('Pessoa não encontrada.')
      showDeleteModal.value = false
      fetchPessoas(currentPage.value)
    } else if (err.response && err.response.status < 500 && err.response.status !== 403) {
      toastError('Não foi possível excluir a pessoa. Tente novamente.')
    }
  } finally {
    deleting.value = false
  }
}

watch(searchTerm, handleSearch)

onMounted(() => {
  fetchPessoas()
})
</script>

<template>
  <AdminLayout>
    <div class="flex flex-col h-full max-w-7xl mx-auto gap-6 w-full">
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 shrink-0">
        <div>
          <h1 class="text-2xl md:text-3xl font-bold text-text-primary">Pessoas</h1>
          <p class="text-text-secondary text-sm mt-1">Gerencie o cadastro global de pessoas.</p>
        </div>
        <div class="flex items-center gap-3">
          <Button variant="outline" class="hidden sm:flex">
            Exportar
          </Button>
          <Button variant="primary" @click="$router.push('/pessoas/nova')">
            Nova pessoa
          </Button>
        </div>
      </div>

      <div class="flex flex-col sm:flex-row gap-3 shrink-0">
        <SearchInput 
          v-model="searchTerm" 
          placeholder="Buscar por nome, CPF ou email..." 
          class="w-full sm:max-w-md"
        />
        <Button variant="outline" class="sm:w-auto w-full justify-center">
          Filtros
        </Button>
      </div>

      <StateFeedback 
        v-if="loading" 
        type="loading" 
      />

      <StateFeedback 
        v-else-if="error" 
        type="error" 
        title="Erro ao carregar os dados" 
        :message="error" 
        @retry="handleRetry" 
      />

      <StateFeedback 
        v-else-if="pageData.content.length === 0" 
        type="empty" 
        icon="users"
        title="Nenhuma pessoa encontrada" 
        :message="searchTerm ? 'Nenhum resultado para a busca atual. Tente outros termos.' : 'O cadastro de pessoas está vazio. Comece adicionando uma nova pessoa.'"
      >
        <template #action>
          <Button variant="primary" v-if="!searchTerm" @click="$router.push('/pessoas/nova')">Nova pessoa</Button>
        </template>
      </StateFeedback>

      <div v-else class="flex-1 flex flex-col min-h-0 gap-4">
        <div class="hidden sm:block flex-1 min-h-0">
          <Table class="h-full">
            <template #header>
              <th>Nome</th>
              <th>CPF</th>
              <th>E-mail</th>
              <th>Data de nascimento</th>
              <th class="text-right">Ações</th>
            </template>
            <template #default>
              <tr v-for="pessoa in pageData.content" :key="pessoa.id">
                <td>
                  <div class="flex items-center gap-3">
                    <Avatar size="sm" :name="pessoa.nome" />
                    <span class="font-medium text-text-primary">{{ pessoa.nome }}</span>
                  </div>
                </td>
                <td class="text-text-secondary font-mono text-sm">{{ formatCpf(pessoa.cpf) }}</td>
                <td class="text-text-secondary">{{ pessoa.email || '-' }}</td>
                <td class="text-text-secondary">{{ formatDate(pessoa.dataNascimento) }}</td>
                <td>
                  <div class="flex items-center justify-end gap-2">
                    <button class="p-1.5 text-text-secondary hover:text-primary hover:bg-primary/5 rounded-lg transition-colors" title="Visualizar">
                      <Icon name="eye" class="w-4 h-4" />
                    </button>
                    <button @click="$router.push(`/pessoas/${pessoa.id}/editar`)" class="p-1.5 text-text-secondary hover:text-primary hover:bg-primary/5 rounded-lg transition-colors" title="Editar">
                      <Icon name="edit" class="w-4 h-4" />
                    </button>
                    <button @click="promptDelete(pessoa)" class="p-1.5 text-text-secondary hover:text-error hover:bg-error/5 rounded-lg transition-colors" title="Excluir">
                      <Icon name="trash-2" class="w-4 h-4" />
                    </button>
                  </div>
                </td>
              </tr>
            </template>
          </Table>
        </div>

        <div class="sm:hidden flex-1 min-h-0 overflow-y-auto space-y-3">
          <Card v-for="pessoa in pageData.content" :key="pessoa.id" class="p-4 shrink-0">
            <div class="flex items-start justify-between gap-3 mb-3">
              <div class="flex items-center gap-3 min-w-0">
                <Avatar size="md" :name="pessoa.nome" class="shrink-0" />
                <div class="min-w-0">
                  <p class="font-medium text-text-primary truncate">{{ pessoa.nome }}</p>
                  <p class="text-sm font-mono text-text-secondary mt-0.5">{{ formatCpf(pessoa.cpf) }}</p>
                </div>
              </div>
            </div>
            
            <div class="space-y-1.5 pt-3 border-t border-border-base">
              <div class="flex items-center gap-2 text-sm text-text-secondary truncate">
                <span class="w-4 h-4 flex items-center justify-center opacity-70">@</span>
                <span class="truncate">{{ pessoa.email || 'Não informado' }}</span>
              </div>
            </div>
            
            <div class="flex items-center justify-end gap-1 mt-4 pt-3 border-t border-border-base">
              <Button variant="outline" class="!px-3 !py-1.5 flex-1 justify-center">Visualizar</Button>
              <Button variant="outline" class="!px-3 !py-1.5 flex-1 justify-center" @click="$router.push(`/pessoas/${pessoa.id}/editar`)">Editar</Button>
              <Button variant="outline" class="!px-3 !py-1.5 justify-center text-error border-error/30 hover:bg-error/5" @click="promptDelete(pessoa)">
                <Icon name="trash-2" class="w-4 h-4" />
              </Button>
            </div>
          </Card>
        </div>

        <Pagination 
          class="shrink-0"
          :current-page="currentPage"
          :total-pages="pageData.totalPages"
          @update:page="handlePageChange"
        />
      </div>
    </div>

    <ConfirmDialog
      v-model="showDeleteModal"
      title="Excluir pessoa?"
      :message="`Tem certeza de que deseja excluir ${pessoaToDelete?.nome}? Esta ação não poderá ser desfeita.`"
      confirm-text="Excluir"
      :loading="deleting"
      @confirm="confirmDelete"
    />
  </AdminLayout>
</template>
