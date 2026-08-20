<script setup lang="ts">
import Input from '../atoms/Input.vue'
import Icon from '../atoms/Icon.vue'

const props = withDefaults(defineProps<{
  modelValue?: string
  placeholder?: string
  disabled?: boolean
  class?: string
}>(), {
  modelValue: '',
  placeholder: 'Pesquisar...',
  disabled: false
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const onUpdate = (val: string | number) => {
  emit('update:modelValue', String(val))
}
</script>

<template>
  <Input
    type="search"
    :model-value="modelValue"
    :placeholder="placeholder"
    :disabled="disabled"
    :class="props.class"
    @update:model-value="onUpdate"
  >
    <template #icon>
      <Icon name="search" class="w-4 h-4" />
    </template>
  </Input>
</template>
