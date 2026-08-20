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
import Badge from '../components/atoms/Badge.vue'
import Select from '../components/atoms/Select.vue'
import { beneficiarioService } from '../services/beneficiario.service'
import type { PageResponse, BeneficiarioResponse, StatusBeneficiario, TipoBeneficiario } from '../types/api'
import ConfirmDialog from '../components/molecules/ConfirmDialog.vue'
import StateFeedback from '../components/molecules/StateFeedback.vue'
import { useToast } from '../composables/useToast'

const { success, error: toastError } = useToast()

const loading = ref(true)
const error = ref<string | null>(null)
const pageData = ref<PageResponse<BeneficiarioResponse>>({
  content: [],
  page: 0,
  size: 10,
  totalElements: 0,
  totalPages: 0,
  first: true,
  last: true
})

const searchTerm = ref('')
const selectedStatus = ref<StatusBeneficiario | ''>('')
const selectedTipo = ref<TipoBeneficiario | ''>('')
const currentPage = ref(1)

const showDeleteModal = ref(false)
const beneficiarioToDelete = ref<BeneficiarioResponse | null>(null)
const deleting = ref(false)

let debounceTimeout: number | undefined

const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const [year, month, day] = dateString.split('-')
  return `${day}/${month}/${year}`
}

const getStatusVariant = (status: StatusBeneficiario) => {
  switch (status) {
    case 'ATIVO': return 'success'
    case 'INATIVO': return 'inactive'
    case 'SUSPENSO': return 'warning'
    case 'CANCELADO': return 'error'
    default: return 'inactive'
  }
}

const getTipoVariant = (tipo: TipoBeneficiario) => {
  return tipo === 'TITULAR' ? 'active' : 'pending'
}

const fetchBeneficiarios = async (page = 1) => {
  loading.value = true
  error.value = null
  
  try {
    const matricula = searchTerm.value.trim() || undefined
    const status = selectedStatus.value || undefined
    const tipo = selectedTipo.value || undefined

    const response = await beneficiarioService.listar({
      page: page - 1, 
      size: 10,
      matricula,
      status,
      tipo
    })
    
    pageData.value = response
    currentPage.value = response.page + 1
  } catch (err: any) {
    console.error('Erro ao buscar beneficiários:', err)
    error.value = 'Ocorreu um erro ao carregar os dados. Tente novamente mais tarde.'
  } finally {
    loading.value = false
  }
}

const handleSearchAndFilter = () => {
  clearTimeout(debounceTimeout)
  debounceTimeout = window.setTimeout(() => {
    fetchBeneficiarios(1) 
  }, 500)
}

const handlePageChange = (page: number) => {
  fetchBeneficiarios(page)
}

const handleRetry = () => {
  fetchBeneficiarios(currentPage.value)
}

const promptDelete = (beneficiario: BeneficiarioResponse) => {
  beneficiarioToDelete.value = beneficiario
  showDeleteModal.value = true
}

const confirmDelete = async () => {
  if (!beneficiarioToDelete.value) return
  
  deleting.value = true
  try {
    await beneficiarioService.excluir(beneficiarioToDelete.value.id)
    success('Beneficiário excluído com sucesso.')
    showDeleteModal.value = false
    
    if (pageData.value.content.length === 1 && currentPage.value > 1) {
      fetchBeneficiarios(currentPage.value - 1)
    } else {
      fetchBeneficiarios(currentPage.value)
    }
    
    beneficiarioToDelete.value = null
  } catch (err: any) {
    console.error('Erro ao excluir beneficiário:', err)
    if (err.response?.status === 404) {
      toastError('Beneficiário não encontrado.')
      showDeleteModal.value = false
      fetchBeneficiarios(currentPage.value)
    } else if (err.response && err.response.status < 500 && err.response.status !== 403) {
      toastError('Não foi possível excluir o beneficiário. Tente novamente.')
    }
  } finally {
    deleting.value = false
  }
}

watch([searchTerm, selectedStatus, selectedTipo], handleSearchAndFilter)

onMounted(() => {
  fetchBeneficiarios()
})
</script>

<template>
  <AdminLayout>
    <div class="flex flex-col h-full max-w-7xl mx-auto gap-6 w-full">
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 shrink-0">
        <div>
          <h1 class="text-2xl md:text-3xl font-bold text-text-primary">Beneficiários</h1>
          <p class="text-text-secondary text-sm mt-1">
            Gerencie os beneficiários do ambiente atual.
          </p>
        </div>
        <div class="flex items-center gap-3">
          <Button variant="primary" @click="$router.push('/beneficiarios/novo')">
            Novo beneficiário
          </Button>
        </div>
      </div>

      <div class="flex flex-col sm:flex-row gap-3 items-center shrink-0">
        <SearchInput 
          v-model="searchTerm" 
          placeholder="Buscar por matrícula..." 
          class="w-full sm:max-w-xs"
        />
        
        <Select
          v-model="selectedStatus"
          class="w-full sm:w-48"
          :options="[
            { value: '', label: 'Todos os Status' },
            { value: 'ATIVO', label: 'Ativo' },
            { value: 'INATIVO', label: 'Inativo' },
            { value: 'SUSPENSO', label: 'Suspenso' },
            { value: 'CANCELADO', label: 'Cancelado' }
          ]"
        />

        <Select
          v-model="selectedTipo"
          class="w-full sm:w-48"
          :options="[
            { value: '', label: 'Todos os Tipos' },
            { value: 'TITULAR', label: 'Titular' },
            { value: 'DEPENDENTE', label: 'Dependente' }
          ]"
        />
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
        title="Nenhum beneficiário encontrado" 
        :message="(searchTerm || selectedStatus || selectedTipo) ? 'Nenhum beneficiário encontrado para os filtros informados.' : 'Não há beneficiários cadastrados.'"
      />

      <div v-else class="flex-1 flex flex-col min-h-0 gap-4">
        <div class="hidden md:block flex-1 min-h-0">
          <Table class="h-full">
            <template #header>
              <th>Pessoa</th>
              <th>Matrícula</th>
              <th>Tipo</th>
              <th>Status</th>
              <th>Data de adesão</th>
              <th class="text-right">Ações</th>
            </template>
            <template #default>
              <tr v-for="item in pageData.content" :key="item.id">
                <td>
                  <div class="flex items-center gap-3">
                    <Avatar size="sm" :name="item.pessoa.nome" />
                    <div class="flex flex-col min-w-0">
                      <span class="font-medium text-text-primary truncate">{{ item.pessoa.nome }}</span>
                      <span class="text-xs text-text-secondary truncate" v-if="item.pessoa.email">{{ item.pessoa.email }}</span>
                    </div>
                  </div>
                </td>
                <td class="font-mono text-sm text-text-secondary">{{ item.matricula }}</td>
                <td>
                  <Badge :variant="getTipoVariant(item.tipo)">{{ item.tipo }}</Badge>
                </td>
                <td>
                  <Badge :variant="getStatusVariant(item.status)">{{ item.status }}</Badge>
                </td>
                <td class="text-text-secondary">{{ formatDate(item.dataAdesao) }}</td>
                <td>
                  <div class="flex items-center justify-end gap-2">
                    <button class="p-1.5 text-text-secondary hover:text-primary hover:bg-primary/5 rounded-lg transition-colors" title="Visualizar">
                      <Icon name="eye" class="w-4 h-4" />
                    </button>
                    <button @click="$router.push(`/beneficiarios/${item.id}/editar`)" class="p-1.5 text-text-secondary hover:text-primary hover:bg-primary/5 rounded-lg transition-colors" title="Editar">
                      <Icon name="edit" class="w-4 h-4" />
                    </button>
                    <button @click="promptDelete(item)" class="p-1.5 text-text-secondary hover:text-error hover:bg-error/5 rounded-lg transition-colors" title="Excluir">
                      <Icon name="trash-2" class="w-4 h-4" />
                    </button>
                  </div>
                </td>
              </tr>
            </template>
          </Table>
        </div>

        <div class="md:hidden flex-1 min-h-0 overflow-y-auto space-y-3">
          <Card v-for="item in pageData.content" :key="item.id" class="p-4 shrink-0">
            <div class="flex items-start justify-between gap-3 mb-3">
              <div class="flex items-center gap-3 min-w-0">
                <Avatar size="md" :name="item.pessoa.nome" class="shrink-0" />
                <div class="min-w-0">
                  <p class="font-medium text-text-primary truncate">{{ item.pessoa.nome }}</p>
                  <p class="text-sm font-mono text-text-secondary mt-0.5">{{ item.matricula }}</p>
                </div>
              </div>
            </div>
            
            <div class="flex flex-wrap gap-2 pt-3 border-t border-border-base">
              <Badge :variant="getTipoVariant(item.tipo)">{{ item.tipo }}</Badge>
              <Badge :variant="getStatusVariant(item.status)">{{ item.status }}</Badge>
            </div>
            
            <div class="flex items-center justify-end gap-1 mt-4 pt-3 border-t border-border-base">
              <Button variant="outline" class="!px-3 !py-1.5 flex-1 justify-center">Visualizar</Button>
              <Button variant="outline" class="!px-3 !py-1.5 flex-1 justify-center" @click="$router.push(`/beneficiarios/${item.id}/editar`)">Editar</Button>
              <Button variant="outline" class="!px-3 !py-1.5 justify-center text-error border-error/30 hover:bg-error/5" @click="promptDelete(item)">
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
      title="Excluir beneficiário?"
      :message="`Tem certeza de que deseja excluir o beneficiário da matrícula ${beneficiarioToDelete?.matricula}? Esta ação não poderá ser desfeita.`"
      confirm-text="Excluir"
      :loading="deleting"
      @confirm="confirmDelete"
    />
  </AdminLayout>
</template>
