import { ref } from 'vue'

export type ToastType = 'SUCCESS' | 'ERROR' | 'LOADING'

export interface ToastMessage {
  id: string
  type: ToastType
  message: string
  duration?: number // ms
}

const toasts = ref<ToastMessage[]>([])

export function useToast() {
  const addToast = (toast: Omit<ToastMessage, 'id'>) => {
    const id = Math.random().toString(36).substring(2, 9)
    const newToast = { ...toast, id }
    toasts.value.push(newToast)

    if (toast.type !== 'LOADING') {
      const duration = toast.duration || (toast.type === 'SUCCESS' ? 3000 : 5000)
      setTimeout(() => {
        removeToast(id)
      }, duration)
    }

    return id
  }

  const success = (message: string, duration?: number) => {
    return addToast({ type: 'SUCCESS', message, duration })
  }

  const error = (message: string, duration?: number) => {
    return addToast({ type: 'ERROR', message, duration })
  }

  const loading = (message: string) => {
    return addToast({ type: 'LOADING', message })
  }

  const removeToast = (id: string) => {
    const index = toasts.value.findIndex(t => t.id === id)
    if (index > -1) {
      toasts.value.splice(index, 1)
    }
  }

  return {
    toasts,
    addToast,
    success,
    error,
    loading,
    removeToast
  }
}
