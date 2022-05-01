<template>
  <div id="app">
    <!-- 路由出口：通过在 router 中的配置文件，将 path 和 component 映射，通过分析 path 显示 component -->
    <router-view />
  </div>
</template>

<script>
export default {
  name: 'App'
}
</script>
