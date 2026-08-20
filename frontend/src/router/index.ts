import { createRouter, createWebHistory } from 'vue-router'
import { useAuth } from '../composables/useAuth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/dashboard'
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('../views/DashboardView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/beneficiarios',
      name: 'beneficiarios',
      component: () => import('../views/BeneficiariosView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/beneficiarios/novo',
      name: 'novo-beneficiario',
      component: () => import('../views/BeneficiarioFormView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/beneficiarios/:id/editar',
      name: 'editar-beneficiario',
      component: () => import('../views/BeneficiarioFormView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/pessoas',
      name: 'pessoas',
      component: () => import('../views/PessoasView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/pessoas/nova',
      name: 'pessoas-nova',
      component: () => import('../views/PessoaFormView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/pessoas/:id/editar',
      name: 'pessoas-editar',
      component: () => import('../views/PessoaFormView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('../views/NotFoundView.vue'),
      meta: { requiresAuth: false } 
    }
  ]
})

router.beforeEach((to, _from, next) => {
  const auth = useAuth()
  
  if (to.meta.requiresAuth && !auth.isAuthenticated.value) {
    next({ name: 'login' })
  } else if (to.name === 'login' && auth.isAuthenticated.value) {
    next({ name: 'dashboard' })
  } else {
    next()
  }
})

export default router
