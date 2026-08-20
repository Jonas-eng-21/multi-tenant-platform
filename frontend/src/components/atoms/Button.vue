<script setup lang="ts">
import { computed } from 'vue'

type ButtonVariant = 'primary' | 'secondary' | 'outline' | 'destructive'

const props = withDefaults(defineProps<{
  variant?: ButtonVariant
  disabled?: boolean
  loading?: boolean
  type?: 'button' | 'submit' | 'reset'
  class?: string
}>(), {
  variant: 'primary',
  disabled: false,
  loading: false,
  type: 'button'
})

const variantClasses = computed(() => {
  switch (props.variant) {
    case 'primary':
      return 'bg-primary text-white hover:bg-primary-dark focus:ring-secondary'
    case 'secondary':
      return 'bg-gray-100 text-text-primary hover:bg-gray-200 focus:ring-secondary'
    case 'outline':
      return 'bg-transparent border border-border-base text-text-primary hover:bg-gray-50 focus:ring-secondary'
    case 'destructive':
      return 'bg-error text-white hover:bg-error-dark focus:ring-error'
    default:
      return ''
  }
})
</script>

<template>
  <button
    :type="type"
    :disabled="disabled || loading"
    class="inline-flex items-center justify-center gap-2 px-4 py-2 text-sm font-medium transition-colors rounded-md focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
    :class="[variantClasses, props.class]"
  >
    <svg v-if="loading" class="w-4 h-4 mr-2 animate-spin" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
      <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
    </svg>
    <slot name="icon-left" v-if="!loading"></slot>
    <slot></slot>
    <slot name="icon-right"></slot>
  </button>
</template>
