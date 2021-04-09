import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

/* Layout */
import Layout from '@/layout'

/**
 * Note: sub-menu only appear when route children.length >= 1
 *
 * hidden: true                   if set true, item will not show in the sidebar(default is false)
 * alwaysShow: true               if set true, will always show the root menu
 *                                if not set alwaysShow, when item has more than one children route,
 *                                it will becomes nested mode, otherwise not show the root menu
 * redirect: noRedirect           if set noRedirect will no redirect in the breadcrumb
 * name:'router-name'             the name is used by <keep-alive> (must set!!!)
 * meta : {
    roles: ['admin','editor']    control the page roles (you can set multiple roles)
    title: 'title'               the name show in sidebar and breadcrumb (recommend set)
    icon: 'svg-name'/'el-icon-x' the icon show in the sidebar
    breadcrumb: false            if set false, the item will hidden in breadcrumb(default is true)
    activeMenu: '/example/list'  if set path, the sidebar will highlight the path you set
  }
 */

/**
 * constantRoutes
 * a base page that does not have permission requirements
 * all roles can be accessed
 */
export const constantRoutes = [
  {
    path: '/login',
    component: () => import('@/views/login/index'),
    hidden: true
  },

  {
    path: '/404',
    component: () => import('@/views/404'),
    hidden: true
  },

  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [{
      path: 'dashboard',
      name: 'Dashboard',
      component: () => import('@/views/dashboard/index'),
      meta: { title: 'Dashboard', icon: 'dashboard' }
    }]
  },

  {
    path: '/profile',
    component: Layout,
    redirect: '/profile/index',
    children: [
      {
        path: 'index',
        component: () => import('@/views/profile/index'),
        name: 'Profile',
        meta: { title: 'Profile', icon: 'user', noCache: true }
      }
    ]
  },

  {
    path: '/submit',
    component: Layout,
    children: [
      {
        path: 'index',
        name: 'Submit',
        component: () => import('@/views/submit/index'),
        meta: { title: 'Submit Task', icon: 'form' }
      }
    ]
  }
]

export const asyncRoutes = [
  {
    path: '/management',
    component: Layout,
    redirect: '/management/machine',
    name: 'Management',
    meta: { title: 'Management', icon: 'el-icon-s-management' },
    children: [
      {
        path: 'machine',
        name: 'Machine',
        component: () => import('@/views/machine/index'),
        meta: { title: 'Machine', icon: 'el-icon-cpu', roles: ['admin'] }
      },
      {
        path: 'task',
        name: 'Task',
        component: () => import('@/views/task/index'),
        meta: { title: 'Task', icon: 'el-icon-files' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/user/index'),
        meta: { title: 'User', icon: 'peoples', roles: ['admin'] }
      }
    ]
  },

  {
    path: '/log',
    component: Layout,
    children: [
      {
        path: 'index',
        name: 'Log',
        component: () => import('@/views/log/index'),
        meta: { title: 'Log', icon: 'el-icon-monitor', roles: ['admin'] }
      }
    ]
  },

  {
    path: '/logout',
    component: () => import('@/views/logout/index'),
    children: [
      {
        path: 'index',
        name: 'Logout',
        meta: { title: 'Logout', icon: 'el-icon-switch-button' }
      }
    ]
  },
  // 404 page must be placed at the end !!!
  { path: '*', redirect: '/404', hidden: true }
]

const createRouter = () => new Router({
  // mode: 'history', // require service support
  scrollBehavior: () => ({ y: 0 }),
  routes: constantRoutes
})

const router = createRouter()

// Detail see: https://github.com/vuejs/vue-router/issues/1234#issuecomment-357941465
export function resetRouter() {
  const newRouter = createRouter()
  router.matcher = newRouter.matcher // reset router
}

export default router
