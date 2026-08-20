<script setup lang="ts">
interface Option {
  value: string | number
  label: string
}

const props = withDefaults(defineProps<{
  modelValue?: string | number
  label?: string
  options: Option[]
  placeholder?: string
  error?: string
  disabled?: boolean
  id?: string
  class?: string
}>(), {
  modelValue: '',
  disabled: false
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string | number): void
}>()

const onChange = (event: Event) => {
  const target = event.target as HTMLSelectElement
  emit('update:modelValue', target.value)
}

const selectId = props.id || `select-${Math.random().toString(36).substr(2, 9)}`
</script>

<template>
  <div class="flex flex-col gap-1 w-full" :class="props.class">
    <label v-if="label" :for="selectId" class="text-sm font-medium text-text-primary">
      {{ label }}
    </label>
    
    <select
      :id="selectId"
      :value="modelValue"
      :disabled="disabled"
      @change="onChange"
      class="block w-full rounded-md border bg-white px-3 py-2 text-sm transition-colors focus:outline-none focus:ring-2 focus:ring-offset-1 disabled:bg-gray-50 disabled:text-inactive disabled:cursor-not-allowed appearance-none"
      :class="[
        error 
          ? 'border-error focus:border-error focus:ring-error text-error' 
          : 'border-border-base focus:border-secondary focus:ring-secondary text-text-primary'
      ]"
    >
      <option v-if="placeholder" value="" disabled selected hidden>{{ placeholder }}</option>
      <option v-for="option in options" :key="option.value" :value="option.value">
        {{ option.label }}
      </option>
    </select>
    
    <span v-if="error" class="text-xs text-error mt-0.5">
      {{ error }}
    </span>
  </div>
</template>

<style scoped>
select {
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 20 20'%3e%3cpath stroke='%234B5563' stroke-linecap='round' stroke-linejoin='round' stroke-width='1.5' d='M6 8l4 4 4-4'/%3e%3c/svg%3e");
  background-position: right 0.5rem center;
  background-repeat: no-repeat;
  background-size: 1.5em 1.5em;
  padding-right: 2.5rem;
}
</style>
