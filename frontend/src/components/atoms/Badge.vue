<script setup lang="ts">
import { computed } from 'vue'

type BadgeVariant = 'active' | 'success' | 'pending' | 'warning' | 'inactive' | 'error'

const props = withDefaults(defineProps<{
  variant?: BadgeVariant
  class?: string
}>(), {
  variant: 'inactive'
})

const variantClasses = computed(() => {
  switch (props.variant) {
    case 'active':
    case 'success':
      return 'bg-green-100 text-success'
    case 'pending':
    case 'warning':
      return 'bg-yellow-100 text-warning'
    case 'error':
      return 'bg-red-100 text-error'
    case 'inactive':
    default:
      return 'bg-gray-100 text-inactive'
  }
})
</script>

<template>
  <span
    class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium"
    :class="[variantClasses, props.class]"
  >
    <slot></slot>
  </span>
</template>
