<script setup lang="ts">
const props = defineProps<{
  class?: string
}>()
</script>

<template>
  <div class="bg-surface rounded-2xl border border-border-base shadow-sm overflow-hidden" :class="props.class">
    <div v-if="$slots.header" class="px-6 py-4 border-b border-border-base">
      <slot name="header"></slot>
    </div>
    
    <div class="p-6">
      <slot></slot>
    </div>
    
    <div v-if="$slots.footer" class="px-6 py-4 bg-gray-50 border-t border-border-base">
      <slot name="footer"></slot>
    </div>
  </div>
</template>
