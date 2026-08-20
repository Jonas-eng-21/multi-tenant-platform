<script setup lang="ts">
import Icon from '../atoms/Icon.vue'
import type { IconName } from '../atoms/Icon.vue'
import Button from '../atoms/Button.vue'

withDefaults(defineProps<{
  type: 'loading' | 'empty' | 'error'
  title?: string
  message?: string
  icon?: IconName
}>(), {
  title: '',
  message: ''
})

defineEmits(['retry'])
</script>

<template>
  <div v-if="type === 'loading'" class="flex flex-col items-center justify-center py-20 space-y-3">
    <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
    <span v-if="message" class="text-text-secondary text-sm font-medium">{{ message }}</span>
  </div>

  <div v-else-if="type === 'error'" class="flex flex-col items-center justify-center py-20 bg-white border border-border-base rounded-xl text-center px-4">
    <div class="w-12 h-12 bg-red-50 text-error rounded-full flex items-center justify-center mb-4">
      <Icon :name="icon || 'close'" class="w-6 h-6" />
    </div>
    <h3 class="text-lg font-medium text-text-primary mb-2">{{ title || 'Erro ao carregar os dados' }}</h3>
    <p class="text-text-secondary max-w-md mb-6">{{ message || 'Ocorreu um erro inesperado.' }}</p>
    <slot name="action">
      <Button variant="outline" @click="$emit('retry')">Tentar novamente</Button>
    </slot>
  </div>

  <div v-else-if="type === 'empty'" class="flex flex-col items-center justify-center py-20 bg-white border border-border-base rounded-xl text-center px-4">
    <div class="w-12 h-12 bg-gray-50 text-text-secondary rounded-full flex items-center justify-center mb-4">
      <Icon :name="icon || 'users'" class="w-6 h-6" />
    </div>
    <h3 class="text-lg font-medium text-text-primary mb-2">{{ title || 'Nenhum registro encontrado' }}</h3>
    <p v-if="message" class="text-text-secondary max-w-md mb-6">{{ message }}</p>
    <slot name="action"></slot>
  </div>
</template>
