<script setup lang="ts">
import Modal from '../organisms/Modal.vue'
import Button from '../atoms/Button.vue'

const props = withDefaults(defineProps<{
  modelValue: boolean
  title?: string
  message: string
  confirmText?: string
  cancelText?: string
  loading?: boolean
}>(), {
  title: 'Confirmação',
  confirmText: 'Confirmar',
  cancelText: 'Cancelar',
  loading: false
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'confirm'): void
  (e: 'cancel'): void
}>()

const handleClose = () => {
  if (props.loading) return
  emit('update:modelValue', false)
  emit('cancel')
}

const handleConfirm = () => {
  if (props.loading) return
  emit('confirm')
}
</script>

<template>
  <Modal :is-open="modelValue" :title="title" @close="handleClose">
    <div class="text-text-secondary text-base">
      {{ message }}
    </div>
    
    <template #footer>
      <div class="flex flex-col-reverse sm:flex-row items-center justify-end gap-3 w-full">
        <Button 
          variant="outline" 
          @click="handleClose" 
          :disabled="loading"
          class="w-full sm:w-auto justify-center"
        >
          {{ cancelText }}
        </Button>
        <Button 
          variant="primary" 
          @click="handleConfirm" 
          :disabled="loading"
          class="w-full sm:w-auto justify-center bg-error hover:bg-error/90 focus:ring-error"
        >
          <div class="flex items-center gap-2">
            <div v-if="loading" class="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
            <span>{{ loading ? 'Excluindo...' : confirmText }}</span>
          </div>
        </Button>
      </div>
    </template>
  </Modal>
</template>
