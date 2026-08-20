<script setup lang="ts">
import { onMounted, onUnmounted, watch } from 'vue'
import Icon from '../atoms/Icon.vue'

const props = defineProps<{
  isOpen: boolean
  title?: string
}>()

const emit = defineEmits<{
  (e: 'close'): void
}>()

const handleEscape = (e: KeyboardEvent) => {
  if (e.key === 'Escape' && props.isOpen) {
    emit('close')
  }
}

watch(() => props.isOpen, (value) => {
  if (value) {
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = ''
  }
})

onMounted(() => {
  document.addEventListener('keydown', handleEscape)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleEscape)
  document.body.style.overflow = ''
})
</script>

<template>
  <Teleport to="body">
    <div v-if="isOpen" class="fixed inset-0 z-50 flex items-center justify-center overflow-y-auto overflow-x-hidden bg-black/50 p-4" @click.self="emit('close')">
      <div class="relative w-full max-w-lg bg-surface rounded-2xl shadow-xl border border-border-base flex flex-col max-h-[90vh]" role="dialog" aria-modal="true" :aria-label="title">
        <div class="flex items-center justify-between px-6 py-4 border-b border-border-base shrink-0">
          <h3 class="text-lg font-semibold text-text-primary">
            {{ title }}
          </h3>
          <button @click="emit('close')" class="text-text-secondary hover:text-text-primary focus:outline-none focus:ring-2 focus:ring-secondary rounded-md" aria-label="Fechar modal">
            <Icon name="close" class="w-5 h-5" />
          </button>
        </div>
        
        <div class="p-6 overflow-y-auto">
          <slot></slot>
        </div>
        
        <div v-if="$slots.footer" class="px-6 py-4 border-t border-border-base bg-gray-50 shrink-0 rounded-b-2xl">
          <slot name="footer"></slot>
        </div>
      </div>
    </div>
  </Teleport>
</template>
