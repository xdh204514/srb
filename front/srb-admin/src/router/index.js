import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

/* Layout */
import Layout from '@/layout'

/**
 * Note: sub-menu only appear when route children.length >= 1
 * Detail see: https://panjiachen.github.io/vue-element-admin-site/guide/essentials/router-and-nav.html
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
    hidden: true,
  },

  {
    path: '/404',
    component: () => import('@/views/404'),
    hidden: true,
  },

  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    // 在 src\layout\components\AppMain.vue 中有一个 <router-view/> 来动态显示这些 children
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index'),
        meta: { title: '首页', icon: 'dashboard' },
      },
    ],
  },
  {
    path: '/core/integral-grade',
    component: Layout,
    redirect: '/core/integral-grade/list',
    name: 'coreIntegralGrade',
    meta: { title: '积分等级管理', icon: 'el-icon-s-claim' },
    alwaysShow: true, // 只有一个子节点，也显示出父子节点这种等级关系
    children: [
      {
        path: 'list',
        name: 'coreIntegralGradeList',
        component: () => import('@/views/core/integral-grade/list'),
        meta: { title: '积分等级列表', icon: 'el-icon-notebook-2' },
      },
      {
        path: 'create',
        name: 'coreIntegralGradeCreate',
        component: () => import('@/views/core/integral-grade/form'),
        meta: { title: '新增积分等级', icon: 'el-icon-circle-plus-outline' },
      },
      {
        path: 'edit/:id',
        name: 'coreIntegralGradeEdit',
        component: () => import('@/views/core/integral-grade/form'),
        meta: { title: '编辑积分等级' },
        hidden: true,
      },
    ],
  },
  {
    path: '/core/user-info',
    component: Layout,
    redirect: '/core/user-info/list',
    name: 'coreUserInfo',
    meta: { title: '会员信息管理', icon: 'el-icon-s-custom' },
    alwaysShow: true, // 只有一个子节点，也显示出父子节点这种等级关系
    children: [
      {
        path: 'list',
        name: 'coreUserInfoList',
        component: () => import('@/views/core/user-info/list'),
        meta: { title: '会员信息列表', icon: 'el-icon-s-order' },
      },
    ],
  },

  {
    path: '/core/borrower',
    component: Layout,
    name: 'coreBorrower',
    meta: { title: '借款管理', icon: 'el-icon-s-unfold' },
    redirect: '/core/borrower/list',
    alwaysShow: true,
    children: [
      {
        path: 'list',
        name: 'coreBorrowerList',
        component: () => import('@/views/core/borrower/list'),
        meta: { title: '借款人列表', icon: 'el-icon-s-data' },
      },
      {
        path: 'detail/:id',
        name: 'coreBorrowerDetail',
        component: () => import('@/views/core/borrower/detail'),
        meta: { title: '借款人详情', icon: 'el-icon-s-order' },
        hidden: true,
      },
      {
        path: 'info-list',
        name: 'coreBorrowInfoList',
        component: () => import('@/views/core/borrow-info/list'),
        meta: { title: '借款信息列表', icon: 'el-icon-s-management' },
      },
      {
        path: 'info-detail/:id',
        name: 'coreBorrowInfoDetail',
        component: () => import('@/views/core/borrow-info/detail'),
        meta: { title: '借款信息详情', icon: 'el-icon-s-claim' },
        hidden: true,
      },
    ],
  },

  {
    path: '/core/lend',
    component: Layout,
    name: 'coreLend',
    meta: { title: '标的管理', icon: 'el-icon-s-flag' },
    redirect: '/core/lend/list',
    alwaysShow: true,
    children: [
      {
        path: 'list',
        name: 'coreLendList',
        component: () => import('@/views/core/lend/list'),
        meta: { title: '标的列表', icon: 'el-icon-s-marketing' },
      },
      {
        path: 'detail/:id',
        name: 'coreLendDetail',
        component: () => import('@/views/core/lend/detail'),
        meta: { title: '标的详情' },
        hidden: true,
      },
    ],
  },

  {
    path: '/core/system',
    component: Layout,
    redirect: '/core/system/dict/list',
    name: 'coreSystemSet',
    meta: { title: '系统设置', icon: 'el-icon-setting' },
    alwaysShow: true, // 只有一个子节点，也显示出父子节点这种等级关系
    children: [
      {
        path: 'dict/list',
        name: 'coreDictList',
        component: () => import('@/views/core/dict/list'),
        meta: { title: '数据字典', icon: 'el-icon-notebook-2' },
      },
    ],
  },

  {
    path: '/example',
    component: Layout,
    redirect: '/example/table',
    name: 'Example',
    meta: { title: 'Example', icon: 'el-icon-s-help' },
    children: [
      {
        path: 'table',
        name: 'Table',
        component: () => import('@/views/table/index'),
        meta: { title: 'Table', icon: 'table' },
      },
      {
        path: 'tree',
        name: 'Tree',
        component: () => import('@/views/tree/index'),
        meta: { title: 'Tree', icon: 'tree' },
      },
    ],
  },

  {
    path: '/form',
    component: Layout,
    children: [
      {
        path: 'index',
        name: 'Form',
        component: () => import('@/views/form/index'),
        meta: { title: 'Form', icon: 'form' },
      },
    ],
  },
]

/**
 * asyncRoutes
 * the routes that need to be dynamically loaded based on user roles
 */
export const asyncRoutes = [
  {
    path: '/nested',
    component: Layout,
    redirect: '/nested/menu1',
    name: 'Nested',
    meta: {
      title: 'Nested',
      icon: 'nested',
    },
    children: [
      {
        path: 'menu1',
        component: () => import('@/views/nested/menu1/index'), // Parent router-view
        name: 'Menu1',
        meta: { title: 'Menu1' },
        children: [
          {
            path: 'menu1-1',
            component: () => import('@/views/nested/menu1/menu1-1'),
            name: 'Menu1-1',
            meta: { title: 'Menu1-1' },
          },
          {
            path: 'menu1-2',
            component: () => import('@/views/nested/menu1/menu1-2'),
            name: 'Menu1-2',
            meta: { title: 'Menu1-2' },
            children: [
              {
                path: 'menu1-2-1',
                component: () =>
                  import('@/views/nested/menu1/menu1-2/menu1-2-1'),
                name: 'Menu1-2-1',
                meta: { title: 'Menu1-2-1' },
              },
              {
                path: 'menu1-2-2',
                component: () =>
                  import('@/views/nested/menu1/menu1-2/menu1-2-2'),
                name: 'Menu1-2-2',
                meta: { title: 'Menu1-2-2' },
              },
            ],
          },
          {
            path: 'menu1-3',
            component: () => import('@/views/nested/menu1/menu1-3'),
            name: 'Menu1-3',
            meta: { title: 'Menu1-3' },
          },
        ],
      },
      {
        path: 'menu2',
        component: () => import('@/views/nested/menu2/index'),
        meta: { title: 'menu2' },
      },
    ],
  },

  {
    path: 'external-link',
    component: Layout,
    children: [
      {
        path: 'https://panjiachen.github.io/vue-element-admin-site/#/',
        meta: { title: 'External Link', icon: 'link' },
      },
    ],
  },

  // 404 page must be placed at the end !!!
  { path: '*', redirect: '/404', hidden: true },
]

const createRouter = () =>
  new Router({
    // mode: 'history', // require service support
    scrollBehavior: () => ({ y: 0 }),
    routes: constantRoutes,
  })

const router = createRouter()

// Detail see: https://github.com/vuejs/vue-router/issues/1234#issuecomment-357941465
export function resetRouter() {
  const newRouter = createRouter()
  router.matcher = newRouter.matcher // reset router
}

export default router