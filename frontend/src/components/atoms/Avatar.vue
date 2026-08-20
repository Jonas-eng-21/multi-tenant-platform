<script setup lang="ts">
import { computed, ref } from 'vue'

const props = withDefaults(defineProps<{
  src?: string
  name?: string
  size?: 'sm' | 'md' | 'lg'
  class?: string
}>(), {
  size: 'md'
})

const hasError = ref(false)

const initials = computed(() => {
  if (!props.name) return '?'
  const parts = props.name.trim().split(' ')
  if (parts.length >= 2) {
    return `${parts[0][0]}${parts[parts.length - 1][0]}`.toUpperCase()
  }
  return parts[0].substring(0, 2).toUpperCase()
})

const sizeClasses = computed(() => {
  switch (props.size) {
    case 'sm': return 'w-8 h-8 text-xs'
    case 'lg': return 'w-12 h-12 text-base'
    case 'md':
    default: return 'w-10 h-10 text-sm'
  }
})
</script>

<template>
  <div
    class="relative inline-flex items-center justify-center rounded-full overflow-hidden bg-gray-200 text-text-secondary font-medium shrink-0"
    :class="[sizeClasses, props.class]"
  >
    <img
      v-if="src && !hasError"
      :src="src"
      :alt="name || 'Avatar'"
      @error="hasError = true"
      class="w-full h-full object-cover"
    />
    <span v-else>{{ initials }}</span>
  </div>
</template>
