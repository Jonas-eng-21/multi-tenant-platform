<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'
import Icon from '../components/atoms/Icon.vue'
import Avatar from '../components/atoms/Avatar.vue'
import NavItem from '../components/molecules/NavItem.vue'

const route = useRoute()
const router = useRouter()
const { user, logout } = useAuth()
const drawerOpen = ref(false)

const handleLogout = () => {
  logout()
  router.push('/login')
}

const navItems = [
  { label: 'Dashboard', icon: 'dashboard' as const, to: '/dashboard' },
  { label: 'Pessoas', icon: 'users' as const, to: '/pessoas' },
  { label: 'Beneficiários', icon: 'heart' as const, to: '/beneficiarios' },
]

const toggleDrawer = () => {
  drawerOpen.value = !drawerOpen.value
}

const closeDrawer = () => {
  drawerOpen.value = false
}

const handleEscape = (e: KeyboardEvent) => {
  if (e.key === 'Escape' && drawerOpen.value) closeDrawer()
}

watch(() => route.path, () => closeDrawer())

onMounted(() => document.addEventListener('keydown', handleEscape))
onUnmounted(() => document.removeEventListener('keydown', handleEscape))
</script>

<template>
  <div class="h-screen bg-bg-base flex overflow-hidden">
    <aside class="hidden md:flex md:flex-col md:w-[280px] md:shrink-0 bg-primary h-screen sticky top-0">
      <div class="px-6 py-6 border-b border-white/10 shrink-0">
        <div class="flex items-center gap-3">
          <div class="w-9 h-9 rounded-lg bg-secondary flex items-center justify-center">
            <Icon name="heart" class="w-5 h-5 text-primary" />
          </div>
          <div>
            <h1 class="text-white font-semibold text-base leading-tight">Health Admin</h1>
            <p class="text-white/50 text-xs">Enterprise</p>
          </div>
        </div>
      </div>

      <nav class="flex-1 px-4 py-4 space-y-1 overflow-y-auto">
        <NavItem
          v-for="item in navItems"
          :key="item.to"
          :label="item.label"
          :icon="item.icon"
          :to="item.to"
        />
      </nav>

      <div class="px-4 py-4 border-t border-white/10 shrink-0">
        <div class="flex items-center gap-3 px-3 py-2">
          <Avatar size="sm" :name="user?.username || 'Usuário'" />
          <div class="flex-1 min-w-0">
            <p class="text-sm font-medium text-white truncate">{{ user?.username || 'Usuário' }}</p>
            <p class="text-xs text-white/50 truncate">Admin</p>
          </div>
        </div>
        <button @click="handleLogout" class="flex items-center w-full px-3 py-2 mt-1 text-sm text-white/60 hover:text-white hover:bg-white/5 rounded-lg transition-colors">
          <Icon name="log-out" class="w-5 h-5 mr-3 shrink-0" />
          Sair
        </button>
      </div>
    </aside>

    <Teleport to="body">
      <div
        v-if="drawerOpen"
        class="fixed inset-0 bg-black/50 z-40 md:hidden"
        @click="closeDrawer"
      ></div>
    </Teleport>

    <Teleport to="body">
      <aside
        class="fixed top-0 left-0 bottom-0 w-[280px] bg-primary z-50 md:hidden flex flex-col transition-transform duration-300"
        :class="drawerOpen ? 'translate-x-0' : '-translate-x-full'"
      >
        <div class="px-6 py-4 border-b border-white/10 flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="w-9 h-9 rounded-lg bg-secondary flex items-center justify-center">
              <Icon name="heart" class="w-5 h-5 text-primary" />
            </div>
            <div>
              <h1 class="text-white font-semibold text-base leading-tight">Health Admin</h1>
              <p class="text-white/50 text-xs">Enterprise</p>
            </div>
          </div>
          <button @click="closeDrawer" class="text-white/60 hover:text-white p-1" aria-label="Fechar menu">
            <Icon name="close" class="w-5 h-5" />
          </button>
        </div>

        <nav class="flex-1 px-4 py-4 space-y-1 overflow-y-auto">
          <NavItem
            v-for="item in navItems"
            :key="item.to"
            :label="item.label"
            :icon="item.icon"
            :to="item.to"
          />
        </nav>

        <div class="px-4 py-4 border-t border-white/10">
          <div class="flex items-center gap-3 px-3 py-2">
            <Avatar size="sm" :name="user?.username || 'Usuário'" />
            <div class="flex-1 min-w-0">
              <p class="text-sm font-medium text-white truncate">{{ user?.username || 'Usuário' }}</p>
              <p class="text-xs text-white/50 truncate">Admin</p>
            </div>
          </div>
          <button @click="handleLogout" class="flex items-center w-full px-3 py-2 mt-1 text-sm text-white/60 hover:text-white hover:bg-white/5 rounded-lg transition-colors">
            <Icon name="log-out" class="w-5 h-5 mr-3 shrink-0" />
            Sair
          </button>
        </div>
      </aside>
    </Teleport>

    <div class="flex-1 flex flex-col min-w-0 w-full h-screen">
      <header class="md:hidden sticky top-0 z-30 bg-surface border-b border-border-base px-4 py-3 flex items-center justify-between shrink-0">
        <button @click="toggleDrawer" class="p-1.5 text-text-secondary hover:text-text-primary rounded-lg" aria-label="Abrir menu">
          <Icon name="menu" class="w-6 h-6" />
        </button>
        <span class="text-sm font-medium text-text-primary truncate mx-3">{{ user?.tenantId ? user.tenantId.substring(0, 8) : '' }}</span>
        <Avatar size="sm" :name="user?.username || 'Usuário'" />
      </header>

      <main class="flex-1 flex flex-col min-h-0 p-4 md:p-6 lg:p-8 pb-24 md:pb-8 overflow-y-auto overflow-x-hidden">
        <slot></slot>
      </main>
      <nav class="md:hidden fixed bottom-0 left-0 right-0 z-30 bg-surface border-t border-border-base flex items-center justify-around px-2 py-1.5 safe-area-bottom">
        <router-link
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          class="flex flex-col items-center gap-0.5 px-2 py-1.5 rounded-lg text-xs font-medium transition-colors min-w-[56px]"
          :class="route.path === item.to
            ? 'text-primary'
            : 'text-text-secondary hover:text-text-primary'"
        >
          <Icon :name="item.icon" class="w-5 h-5" />
          <span>{{ item.label }}</span>
        </router-link>
      </nav>
    </div>
  </div>
</template>
