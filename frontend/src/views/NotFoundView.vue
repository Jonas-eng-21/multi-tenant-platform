<script setup lang="ts">
import { useRouter } from 'vue-router'
import Button from '../components/atoms/Button.vue'
import Icon from '../components/atoms/Icon.vue'

const router = useRouter()
</script>

<template>
  <div class="min-h-screen bg-bg-base flex flex-col items-center justify-center px-4 py-12 text-center">
    <div class="max-w-md w-full space-y-8">
      <div class="flex justify-center">
        <svg viewBox="0 0 320 200" class="w-64 sm:w-80 h-auto" fill="none" xmlns="http://www.w3.org/2000/svg">
          <rect x="20" y="40" width="280" height="120" rx="16" fill="#F7FAFA" stroke="#E5E7EB" stroke-width="1"/>
          
          <text x="70" y="130" font-size="72" font-weight="700" fill="#002B2B" font-family="Inter, sans-serif" text-anchor="middle">4</text>
          
          <circle cx="160" cy="105" r="38" fill="none" stroke="#00CCCC" stroke-width="5" />
          <circle cx="160" cy="105" r="24" fill="#002B2B" opacity="0.08" />
          <circle cx="160" cy="95" r="8" fill="#002B2B" opacity="0.15" />
          <path d="M150 110 Q160 120 170 110" stroke="#002B2B" stroke-width="2.5" fill="none" stroke-linecap="round" opacity="0.2"/>
          
          <text x="250" y="130" font-size="72" font-weight="700" fill="#002B2B" font-family="Inter, sans-serif" text-anchor="middle">4</text>
          
          <circle cx="40" cy="50" r="3" fill="#00CCCC" opacity="0.4" />
          <circle cx="280" cy="55" r="2.5" fill="#00CCCC" opacity="0.3" />
          <circle cx="55" cy="160" r="2" fill="#002B2B" opacity="0.15" />
          <circle cx="265" cy="155" r="3" fill="#002B2B" opacity="0.1" />
        </svg>
      </div>

      <div class="space-y-3">
        <h1 class="text-2xl sm:text-3xl font-bold text-text-primary">Página não encontrada</h1>
        <p class="text-text-secondary text-sm sm:text-base leading-relaxed">
          Desculpe, o link que você seguiu pode estar quebrado ou a página pode ter sido removida.
        </p>
      </div>

      <div class="flex flex-col sm:flex-row items-center justify-center gap-3 pt-2">
        <Button variant="primary" @click="router.push('/dashboard')" class="w-full sm:w-auto">
          <template #icon-left>
            <Icon name="arrow-left" class="w-4 h-4" />
          </template>
          Voltar ao Dashboard
        </Button>
        <Button variant="outline" class="w-full sm:w-auto">
          <template #icon-left>
            <Icon name="headphones" class="w-4 h-4" />
          </template>
          Suporte Técnico
        </Button>
      </div>

      <p class="text-xs text-text-secondary/60 pt-6">Health Admin · Enterprise</p>
    </div>
  </div>
</template>
