<script setup lang="ts">
import { useToast } from '../../composables/useToast'
import Icon from './Icon.vue'

const { toasts, removeToast } = useToast()
</script>

<template>
  <div class="fixed top-4 right-4 z-50 flex flex-col gap-2 max-w-sm w-full pointer-events-none">
    <TransitionGroup 
      enter-active-class="transition duration-300 ease-out"
      enter-from-class="transform translate-x-full opacity-0"
      enter-to-class="transform translate-x-0 opacity-100"
      leave-active-class="transition duration-200 ease-in"
      leave-from-class="transform translate-x-0 opacity-100"
      leave-to-class="transform translate-x-full opacity-0"
    >
      <div 
        v-for="toast in toasts" 
        :key="toast.id"
        class="flex items-start p-4 rounded-lg shadow-lg bg-white border border-border-base pointer-events-auto"
      >
        <div class="flex-shrink-0">
          <Icon v-if="toast.type === 'SUCCESS'" name="check" class="h-5 w-5 text-green-500" />
          <Icon v-else-if="toast.type === 'ERROR'" name="close" class="h-5 w-5 text-red-500" />
          <div v-else-if="toast.type === 'LOADING'" class="h-5 w-5 rounded-full border-2 border-primary border-t-transparent animate-spin"></div>
        </div>
        
        <div class="ml-3 w-0 flex-1 pt-0.5">
          <p class="text-sm font-medium text-text-primary">
            {{ toast.message }}
          </p>
        </div>
        
        <div class="ml-4 flex-shrink-0 flex" v-if="toast.type !== 'LOADING'">
          <button 
            @click="removeToast(toast.id)"
            class="bg-white rounded-md inline-flex text-text-secondary hover:text-text-primary focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary"
          >
            <span class="sr-only">Fechar</span>
            <Icon name="close" class="h-4 w-4" />
          </button>
        </div>
      </div>
    </TransitionGroup>
  </div>
</template>
