<script setup lang="ts">
const props = defineProps<{
  emptyMessage?: string
  isEmpty?: boolean
}>()
</script>

<template>
  <div class="overflow-x-auto w-full border border-border-base sm:rounded-lg">
    <table class="w-full text-sm text-left text-text-primary bg-white">
      <thead class="text-xs text-text-secondary uppercase bg-gray-50 border-b border-border-base">
        <tr>
          <slot name="header"></slot>
        </tr>
      </thead>
      <tbody>
        <tr v-if="isEmpty" class="bg-white">
          <td colspan="100%" class="px-6 py-8 text-center text-text-secondary">
            <slot name="empty">
              {{ emptyMessage || 'Nenhum registro encontrado.' }}
            </slot>
          </td>
        </tr>
        <slot v-else></slot>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
@reference "../../style.css";

:deep(th) {
  @apply px-6 py-3 font-medium;
}
:deep(td) {
  @apply px-6 py-4 border-b border-border-base;
}
:deep(tr:last-child td) {
  @apply border-b-0;
}
:deep(tr:hover:not(thead tr)) {
  @apply bg-gray-50;
}
</style>
