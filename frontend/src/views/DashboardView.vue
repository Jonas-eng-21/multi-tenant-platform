<script setup lang="ts">
import { ref, computed } from 'vue'
import AdminLayout from '../layouts/AdminLayout.vue'
import Card from '../components/organisms/Card.vue'
import Table from '../components/organisms/Table.vue'
import Badge from '../components/atoms/Badge.vue'
import Avatar from '../components/atoms/Avatar.vue'
import Icon from '../components/atoms/Icon.vue'

const kpis = [
  { label: 'Pessoas', value: '1.245', icon: 'users' as const, color: 'bg-blue-50 text-blue-600' },
  { label: 'Beneficiários', value: '856', icon: 'heart' as const, color: 'bg-purple-50 text-purple-600' },
  { label: 'Beneficiários ativos', value: '790', icon: 'check' as const, color: 'bg-emerald-50 text-success' },
  { label: 'Beneficiários inativos', value: '66', icon: 'close' as const, color: 'bg-red-50 text-error' },
]

const distribution = {
  total: 856,
  titulares: { count: 599, pct: 70 },
  dependentes: { count: 257, pct: 30 },
}

const recentBeneficiaries = ref([
  { name: 'Carlos Silva', matricula: 'MAT-10293', type: 'Titular', status: 'active' as const, date: '24 Out 2023' },
  { name: 'Ana Oliveira', matricula: 'MAT-10294', type: 'Dependente', status: 'active' as const, date: '24 Out 2023' },
  { name: 'Roberto Mendes', matricula: 'MAT-10295', type: 'Titular', status: 'pending' as const, date: '23 Out 2023' },
  { name: 'Juliana Costa', matricula: 'MAT-10296', type: 'Titular', status: 'inactive' as const, date: '21 Out 2023' },
])

const statusLabel: Record<string, string> = {
  active: 'Ativo',
  pending: 'Pendente',
  inactive: 'Inativo',
}

const circumference = computed(() => 2 * Math.PI * 60)
const titularOffset = computed(() => circumference.value * (1 - distribution.titulares.pct / 100))
</script>

<template>
  <AdminLayout>
    <div class="space-y-6 max-w-7xl mx-auto">
      <div>
        <h1 class="text-2xl md:text-3xl font-bold text-text-primary">Visão geral</h1>
        <p class="text-text-secondary text-sm mt-1">Resumo do ambiente · <span class="font-medium text-primary">Tenant Dev A</span></p>
      </div>

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
              <button class="text-sm text-secondary hover:text-secondary-dark font-medium transition-colors">Ver todos</button>
            </div>
          </template>

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
                    <Avatar size="sm" :name="b.name" />
                    <span class="font-medium">{{ b.name }}</span>
                  </div>
                </td>
                <td class="text-text-secondary font-mono text-xs">{{ b.matricula }}</td>
                <td>{{ b.type }}</td>
                <td><Badge :variant="b.status">{{ statusLabel[b.status] }}</Badge></td>
                <td class="text-text-secondary">{{ b.date }}</td>
              </tr>
            </Table>
          </div>

          <div class="sm:hidden space-y-3 -mx-2">
            <div
              v-for="b in recentBeneficiaries"
              :key="b.matricula"
              class="flex items-center gap-3 p-3 rounded-lg hover:bg-gray-50 transition-colors"
            >
              <Avatar size="sm" :name="b.name" />
              <div class="flex-1 min-w-0">
                <div class="flex items-center justify-between gap-2">
                  <p class="text-sm font-medium text-text-primary truncate">{{ b.name }}</p>
                  <Badge :variant="b.status" class="shrink-0">{{ statusLabel[b.status] }}</Badge>
                </div>
                <div class="flex items-center gap-2 mt-0.5">
                  <span class="text-xs text-text-secondary font-mono">{{ b.matricula }}</span>
                  <span class="text-xs text-text-secondary">·</span>
                  <span class="text-xs text-text-secondary">{{ b.type }}</span>
                  <span class="text-xs text-text-secondary">·</span>
                  <span class="text-xs text-text-secondary">{{ b.date }}</span>
                </div>
              </div>
            </div>
          </div>
        </Card>
      </div>
    </div>
  </AdminLayout>
</template>
