<script setup lang="ts">
import { computed } from 'vue'
import Button from '../atoms/Button.vue'
import Icon from '../atoms/Icon.vue'

const props = defineProps<{
  currentPage: number
  totalPages: number
  class?: string
}>()

const emit = defineEmits<{
  (e: 'update:page', page: number): void
}>()

const canGoPrev = computed(() => props.currentPage > 1)
const canGoNext = computed(() => props.currentPage < props.totalPages)

const prev = () => {
  if (canGoPrev.value) emit('update:page', props.currentPage - 1)
}

const next = () => {
  if (canGoNext.value) emit('update:page', props.currentPage + 1)
}
</script>

<template>
  <div class="flex items-center justify-between px-4 py-3 bg-white border-t border-border-base sm:px-6" :class="props.class">
    <div class="flex items-center justify-between flex-1 sm:hidden">
      <Button variant="outline" :disabled="!canGoPrev" @click="prev">Anterior</Button>
      <Button variant="outline" :disabled="!canGoNext" @click="next">Próxima</Button>
    </div>
    
    <div class="hidden sm:flex sm:flex-1 sm:items-center sm:justify-between">
      <div>
        <p class="text-sm text-text-secondary">
          Página <span class="font-medium text-text-primary">{{ currentPage }}</span> de <span class="font-medium text-text-primary">{{ totalPages || 1 }}</span>
        </p>
      </div>
      <div>
        <nav class="inline-flex rounded-md shadow-sm -space-x-px" aria-label="Pagination">
          <button
            @click="prev"
            :disabled="!canGoPrev"
            class="relative inline-flex items-center px-2 py-2 text-sm font-medium text-text-secondary bg-white border border-border-base rounded-l-md hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed focus:z-10 focus:outline-none focus:ring-1 focus:ring-secondary focus:border-secondary"
          >
            <span class="sr-only">Anterior</span>
            <Icon name="chevron-left" class="w-5 h-5" />
          </button>
          <button
            @click="next"
            :disabled="!canGoNext"
            class="relative inline-flex items-center px-2 py-2 text-sm font-medium text-text-secondary bg-white border border-border-base rounded-r-md hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed focus:z-10 focus:outline-none focus:ring-1 focus:ring-secondary focus:border-secondary"
          >
            <span class="sr-only">Próxima</span>
            <Icon name="chevron-right" class="w-5 h-5" />
          </button>
        </nav>
      </div>
    </div>
  </div>
</template>
