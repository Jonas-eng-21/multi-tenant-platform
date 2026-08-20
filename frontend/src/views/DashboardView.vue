<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AdminLayout from '../layouts/AdminLayout.vue'
import Card from '../components/organisms/Card.vue'
import Table from '../components/organisms/Table.vue'
import Badge from '../components/atoms/Badge.vue'
import Avatar from '../components/atoms/Avatar.vue'
import Icon from '../components/atoms/Icon.vue'
import { pessoaService } from '../services/pessoa.service'
import { beneficiarioService } from '../services/beneficiario.service'
import { useToast } from '../composables/useToast'
import type { BeneficiarioResponse } from '../types/api'
import StateFeedback from '../components/molecules/StateFeedback.vue'

const router = useRouter()
const { error: toastError } = useToast()

const loading = ref(true)
const errorMessage = ref<string | null>(null)

const kpisData = ref({
  pessoas: 0,
  beneficiarios: 0,
  ativos: 0,
  inativos: 0
})

const distData = ref({
  total: 0,
  titulares: 0,
  dependentes: 0
})

const recentBeneficiaries = ref<BeneficiarioResponse[]>([])

const kpis = computed(() => [
  { label: 'Pessoas', value: kpisData.value.pessoas.toString(), icon: 'users' as const, color: 'bg-blue-50 text-blue-600' },
  { label: 'Beneficiários', value: kpisData.value.beneficiarios.toString(), icon: 'heart' as const, color: 'bg-purple-50 text-purple-600' },
  { label: 'Beneficiários ativos', value: kpisData.value.ativos.toString(), icon: 'check' as const, color: 'bg-emerald-50 text-success' },
  { label: 'Beneficiários inativos', value: kpisData.value.inativos.toString(), icon: 'close' as const, color: 'bg-red-50 text-error' },
])

const distribution = computed(() => {
  const total = distData.value.total
  if (total === 0) {
    return {
      total: 0,
      titulares: { count: 0, pct: 0 },
      dependentes: { count: 0, pct: 0 },
    }
  }
  
  const pctTitulares = Math.round((distData.value.titulares / total) * 100)
  const pctDependentes = Math.round((distData.value.dependentes / total) * 100)
  
  return {
    total,
    titulares: { count: distData.value.titulares, pct: pctTitulares },
    dependentes: { count: distData.value.dependentes, pct: pctDependentes },
  }
})

const statusLabel: Record<string, string> = {
  ATIVO: 'Ativo',
  INATIVO: 'Inativo',
  SUSPENSO: 'Suspenso',
  CANCELADO: 'Cancelado'
}

const statusVariantMap: Record<string, 'active' | 'inactive' | 'pending' | 'error'> = {
  ATIVO: 'active',
  INATIVO: 'inactive',
  SUSPENSO: 'pending',
  CANCELADO: 'error'
}

const formatDate = (isoString: string) => {
  if (!isoString) return ''
  return new Intl.DateTimeFormat('pt-BR', { day: '2-digit', month: 'short', year: 'numeric' }).format(new Date(isoString))
}

const circumference = computed(() => 2 * Math.PI * 60)
const titularOffset = computed(() => {
  if (distribution.value.total === 0) return circumference.value
  return circumference.value * (1 - distribution.value.titulares.pct / 100)
})

const carregarDados = async () => {
  loading.value = true
  errorMessage.value = null
  try {
    const [
      resPessoas,
      resBenTotal,
      resBenAtivos,
      resBenInativos,
      resBenTitulares,
      resBenDependentes,
      resBenRecentes
    ] = await Promise.all([
      pessoaService.listar({ size: 1 }),
      beneficiarioService.listar({ size: 1 }),
      beneficiarioService.listar({ size: 1, status: 'ATIVO' }),
      beneficiarioService.listar({ size: 1, status: 'INATIVO' }),
      beneficiarioService.listar({ size: 1, tipo: 'TITULAR' }),
      beneficiarioService.listar({ size: 1, tipo: 'DEPENDENTE' }),
      beneficiarioService.listar({ size: 5, sort: 'createdAt,desc' })
    ])
    
    kpisData.value = {
      pessoas: resPessoas.totalElements,
      beneficiarios: resBenTotal.totalElements,
      ativos: resBenAtivos.totalElements,
      inativos: resBenInativos.totalElements
    }
    
    distData.value = {
      total: resBenTotal.totalElements,
      titulares: resBenTitulares.totalElements,
      dependentes: resBenDependentes.totalElements
    }
    
    recentBeneficiaries.value = resBenRecentes.content
    
  } catch (err) {
    toastError('Erro ao carregar dados do dashboard.')
    errorMessage.value = 'Não foi possível carregar o dashboard. Tente novamente mais tarde.'
    console.error(err)
  } finally {
    loading.value = false
  }
}

const handleRetry = () => {
  carregarDados()
}

onMounted(() => {
  carregarDados()
})

const navigateToAll = () => {
  router.push('/beneficiarios')
}
</script>

<template>
  <AdminLayout>
    <div class="space-y-6 max-w-7xl mx-auto">
      <div>
        <h1 class="text-2xl md:text-3xl font-bold text-text-primary">Visão geral</h1>
        <p class="text-text-secondary text-sm mt-1">Resumo do ambiente</p>
      </div>

      <StateFeedback 
        v-if="loading" 
        type="loading" 
        message="Carregando painel..." 
      />
      
      <StateFeedback 
        v-else-if="errorMessage" 
        type="error" 
        title="Erro no painel" 
        :message="errorMessage" 
        @retry="handleRetry" 
      />

      <template v-else>
        <div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
          <Card v-for="kpi in kpis" :key="kpi.label" class="!p-0">
            <div class="flex items-center gap-4">
              <div class="w-11 h-11 rounded-xl flex items-center justify-center shrink-0" :class="kpi.color">
                <Icon :name="kpi.icon" class="w-5 h-5" />
              </div>
              <div>
                <p class="text-xs text-text-secondary font-medium uppercase tracking-wider">{{ kpi.label }}</p>
                <p class="text-2xl font-bold text-text-primary">{{ kpi.value }}</p>
              </div>
            </div>
          </Card>
        </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-4 lg:gap-6">
        <Card class="lg:col-span-1">
          <template #header>
            <h3 class="font-semibold text-text-primary">Distribuição de Beneficiários</h3>
          </template>

          <div class="flex flex-col items-center gap-6">
            <div class="relative w-40 h-40 sm:w-48 sm:h-48">
              <svg viewBox="0 0 140 140" class="w-full h-full -rotate-90">
                <circle cx="70" cy="70" r="60" fill="none" stroke="#E5E7EB" stroke-width="16" />
                <circle
                  cx="70" cy="70" r="60" fill="none"
                  stroke="#002B2B"
                  stroke-width="16"
                  :stroke-dasharray="circumference"
                  :stroke-dashoffset="titularOffset"
                  stroke-linecap="round"
                />
              </svg>
              <div class="absolute inset-0 flex flex-col items-center justify-center">
                <p class="text-2xl font-bold text-text-primary">{{ distribution.total }}</p>
                <p class="text-xs text-text-secondary">Total</p>
              </div>
            </div>

            <div class="flex gap-6 text-sm w-full justify-center">
              <div class="flex items-center gap-2">
                <span class="w-3 h-3 rounded-full bg-primary shrink-0"></span>
                <span class="text-text-secondary">Titulares</span>
                <span class="font-semibold text-text-primary">{{ distribution.titulares.pct }}%</span>
              </div>
              <div class="flex items-center gap-2">
                <span class="w-3 h-3 rounded-full bg-border-base shrink-0"></span>
                <span class="text-text-secondary">Dependentes</span>
                <span class="font-semibold text-text-primary">{{ distribution.dependentes.pct }}%</span>
              </div>
            </div>

            <div class="grid grid-cols-2 gap-4 w-full">
              <div class="text-center p-3 bg-gray-50 rounded-lg">
                <p class="text-lg font-bold text-text-primary">{{ distribution.titulares.count }}</p>
                <p class="text-xs text-text-secondary">Titulares</p>
              </div>
              <div class="text-center p-3 bg-gray-50 rounded-lg">
                <p class="text-lg font-bold text-text-primary">{{ distribution.dependentes.count }}</p>
                <p class="text-xs text-text-secondary">Dependentes</p>
              </div>
            </div>
          </div>
        </Card>

        <Card class="lg:col-span-2">
          <template #header>
            <div class="flex items-center justify-between">
              <h3 class="font-semibold text-text-primary">Beneficiários recentes</h3>
              <button @click="navigateToAll" class="text-sm text-secondary hover:text-secondary-dark font-medium transition-colors">Ver todos</button>
            </div>
          </template>

          <div v-if="recentBeneficiaries.length === 0" class="py-4">
            <StateFeedback 
              type="empty" 
              icon="heart"
              title="Sem beneficiários"
              message="Nenhum beneficiário recente." 
            />
          </div>
          <template v-else>
            <div class="hidden sm:block -mx-6 -mb-6">
              <Table>
                <template #header>
                  <th>Pessoa</th>
                  <th>Matrícula</th>
                  <th>Tipo</th>
                  <th>Status</th>
                  <th>Data</th>
                </template>
                <tr v-for="b in recentBeneficiaries" :key="b.matricula">
                  <td>
                    <div class="flex items-center gap-3">
                      <Avatar size="sm" :name="b.pessoa.nome" />
                      <span class="font-medium">{{ b.pessoa.nome }}</span>
                    </div>
                  </td>
                  <td class="text-text-secondary font-mono text-xs">{{ b.matricula }}</td>
                  <td class="capitalize">{{ b.tipo.toLowerCase() }}</td>
                  <td><Badge :variant="statusVariantMap[b.status]">{{ statusLabel[b.status] }}</Badge></td>
                  <td class="text-text-secondary">{{ formatDate(b.createdAt) }}</td>
                </tr>
              </Table>
            </div>

            <div class="sm:hidden space-y-3 -mx-2">
              <div
                v-for="b in recentBeneficiaries"
                :key="b.matricula"
                class="flex items-center gap-3 p-3 rounded-lg hover:bg-gray-50 transition-colors"
              >
                <Avatar size="sm" :name="b.pessoa.nome" />
                <div class="flex-1 min-w-0">
                  <div class="flex items-center justify-between gap-2">
                    <p class="text-sm font-medium text-text-primary truncate">{{ b.pessoa.nome }}</p>
                    <Badge :variant="statusVariantMap[b.status]" class="shrink-0">{{ statusLabel[b.status] }}</Badge>
                  </div>
                  <div class="flex items-center gap-2 mt-0.5">
                    <span class="text-xs text-text-secondary font-mono">{{ b.matricula }}</span>
                    <span class="text-xs text-text-secondary">·</span>
                    <span class="text-xs text-text-secondary capitalize">{{ b.tipo.toLowerCase() }}</span>
                    <span class="text-xs text-text-secondary">·</span>
                    <span class="text-xs text-text-secondary">{{ formatDate(b.createdAt) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </template>
        </Card>
      </div>
      </template>
    </div>
  </AdminLayout>
</template>
