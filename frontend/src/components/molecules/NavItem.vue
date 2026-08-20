<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import Icon from '../atoms/Icon.vue'
import type { IconName } from '../atoms/Icon.vue'

const props = defineProps<{
  label: string
  icon?: IconName
  to?: string
  active?: boolean
  class?: string
}>()

const route = useRoute()

const isActive = computed(() => {
  if (props.active !== undefined) return props.active
  if (props.to) return route.path === props.to
  return false
})
</script>

<template>
  <router-link
    v-if="to"
    :to="to"
    class="flex items-center w-full px-3 py-2.5 text-sm font-medium transition-colors rounded-lg group focus:outline-none focus:ring-2 focus:ring-secondary focus:ring-offset-1"
    :class="[
      isActive 
        ? 'bg-white/10 text-white' 
        : 'text-white/60 hover:bg-white/5 hover:text-white',
      props.class
    ]"
  >
    <Icon 
      v-if="icon" 
      :name="icon" 
      class="w-5 h-5 mr-3 shrink-0" 
      :class="isActive ? 'text-secondary' : 'text-white/50 group-hover:text-white/80'"
    />
    {{ label }}
  </router-link>

  <button
    v-else
    class="flex items-center w-full px-3 py-2.5 text-sm font-medium transition-colors rounded-lg group focus:outline-none focus:ring-2 focus:ring-secondary focus:ring-offset-1"
    :class="[
      isActive 
        ? 'bg-white/10 text-white' 
        : 'text-white/60 hover:bg-white/5 hover:text-white',
      props.class
    ]"
  >
    <Icon 
      v-if="icon" 
      :name="icon" 
      class="w-5 h-5 mr-3 shrink-0" 
      :class="isActive ? 'text-secondary' : 'text-white/50 group-hover:text-white/80'"
    />
    {{ label }}
  </button>
</template>
