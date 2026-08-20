<script setup lang="ts">
const props = withDefaults(defineProps<{
  modelValue?: string | number
  label?: string
  placeholder?: string
  error?: string
  disabled?: boolean
  type?: string
  id?: string
  class?: string
}>(), {
  modelValue: '',
  type: 'text',
  disabled: false
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string | number): void
}>()

const onInput = (event: Event) => {
  const target = event.target as HTMLInputElement
  emit('update:modelValue', target.value)
}

const inputId = props.id || `input-${Math.random().toString(36).substr(2, 9)}`
</script>

<template>
  <div class="flex flex-col gap-1 w-full" :class="props.class">
    <label v-if="label" :for="inputId" class="text-sm font-medium text-text-primary">
      {{ label }}
    </label>
    
    <div class="relative">
      <div v-if="$slots.icon" class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-text-secondary">
        <slot name="icon"></slot>
      </div>
      
      <input
        :id="inputId"
        :type="type"
        :value="modelValue"
        :placeholder="placeholder"
        :disabled="disabled"
        @input="onInput"
        class="block w-full rounded-md border text-sm transition-colors focus:outline-none focus:ring-2 focus:ring-offset-1 disabled:bg-gray-50 disabled:text-inactive disabled:cursor-not-allowed"
        :class="[
          error 
            ? 'border-error focus:border-error focus:ring-error text-error placeholder:text-error/60' 
            : 'border-border-base focus:border-secondary focus:ring-secondary text-text-primary placeholder:text-text-secondary/60',
          $slots.icon ? 'pl-10' : 'pl-3',
          'pr-3 py-2',
          type === 'search' ? 'rounded-full' : 'rounded-md'
        ]"
      />
    </div>
    
    <span v-if="error" class="text-xs text-error mt-0.5">
      {{ error }}
    </span>
  </div>
</template>
