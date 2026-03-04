<template>
  <div class="app">
    <nav v-if="isAuthenticated" class="navbar">
      <div class="nav-container">
        <div class="nav-brand">JWT Auth</div>
        <button class="hamburger" @click="toggleMenu" :class="{ active: menuOpen }" aria-label="Toggle navigation">
          <span></span>
          <span></span>
          <span></span>
        </button>
        <ul class="nav-menu" :class="{ active: menuOpen }">
          <li><button @click="navigateTo('home')" class="nav-link">Home</button></li>
          <li v-if="isAdmin"><button @click="navigateTo('users')" class="nav-link">Users</button></li>
          <li><button @click="navigateTo('profile')" class="nav-link">Profile</button></li>
          <li><button @click="handleLogout" class="nav-link logout">Logout</button></li>
        </ul>
      </div>
    </nav>

    <div class="content">
      <Login v-if="currentPage === 'login'" @navigate="goToPage" />
      <Register v-else-if="currentPage === 'register'" @navigate="goToPage" />
      <Home v-else-if="currentPage === 'home'" />
      <Users v-else-if="currentPage === 'users'" />
      <Profile v-else-if="currentPage === 'profile'" @navigate="goToPage" />
      <NotFound v-else />
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { router } from './router'
import { authService } from './services/authService'
import Login from './pages/Login.vue'
import Register from './pages/Register.vue'
import Home from './pages/Home.vue'
import Users from './pages/Users.vue'
import Profile from './pages/Profile.vue'
import NotFound from './pages/NotFound.vue'

export default {
  components: {
    Login,
    Register,
    Home,
    Users,
    Profile,
    NotFound
  },
  setup() {
    const currentPage = ref('login')
    const menuOpen = ref(false)
    const authState = ref(0)

    const isAuthenticated = computed(() => {
      authState.value
      return authService.isAuthenticated()
    })
    const isAdmin = computed(() => {
      authState.value
      return authService.isAdmin()
    })

    const refreshAuth = () => {
      authState.value += 1
    }

    const updatePage = () => {
      currentPage.value = router.getCurrentRoute()
      if (authService.isAuthenticated() && !authService.isAdmin()) {
        if (currentPage.value === 'users') {
          router.navigate('profile')
          currentPage.value = 'profile'
        }
      }
      menuOpen.value = false
    }

    const normalizeRoute = (pageName) => {
      if (authService.isAuthenticated() && !authService.isAdmin() && pageName === 'users') {
        return 'profile'
      }
      return pageName
    }

    const goToPage = (pageName) => {
      const target = normalizeRoute(pageName)
      router.navigate(target)
      currentPage.value = target
      menuOpen.value = false
    }

    const navigateTo = (pageName) => {
      const target = normalizeRoute(pageName)
      router.navigate(target)
      currentPage.value = target
      menuOpen.value = false
    }

    const toggleMenu = () => {
      menuOpen.value = !menuOpen.value
    }

    const handleLogout = async () => {
      await authService.logout()
      router.navigate('login')
      currentPage.value = 'login'
      menuOpen.value = false
    }

    const handleUnauthorized = () => {
      router.navigate('login')
      currentPage.value = 'login'
      menuOpen.value = false
    }

    onMounted(async () => {
      router.subscribe(updatePage)
      window.addEventListener('auth-changed', refreshAuth)
      window.addEventListener('unauthorized', handleUnauthorized)
      
      // Initialize authentication state by fetching profile
      // This checks if user has a valid JWT cookie
      const isLoggedIn = await authService.initialize()
      
      if (isLoggedIn) {
        router.navigate('home')
      } else {
        router.navigate('login')
      }
      updatePage()
    })

    onBeforeUnmount(() => {
      window.removeEventListener('auth-changed', refreshAuth)
      window.removeEventListener('unauthorized', handleUnauthorized)
    })

    return {
      currentPage,
      goToPage,
      navigateTo,
      toggleMenu,
      handleLogout,
      menuOpen,
      isAuthenticated,
      isAdmin
    }
  }
}
</script>

<style scoped>
.app {
  min-height: 100vh;
  background: radial-gradient(circle at 10% 10%, rgba(255, 239, 213, 0.9), transparent 45%),
    radial-gradient(circle at 90% 20%, rgba(255, 213, 186, 0.8), transparent 40%),
    linear-gradient(135deg, #f7f3e9 0%, #f0d8b8 50%, #efc2a2 100%);
}

.navbar {
  background: rgba(51, 51, 51, 0.95);
  padding: 0;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
  position: relative;
}

.nav-brand {
  color: white;
  font-size: 24px;
  font-weight: bold;
  letter-spacing: 1px;
}

.hamburger {
  display: flex;
  flex-direction: column;
  background: none;
  border: none;
  cursor: pointer;
  padding: 8px;
}

.hamburger span {
  width: 25px;
  height: 3px;
  background: white;
  margin: 5px 0;
  transition: 0.3s;
  display: block;
}

.hamburger.active span:nth-child(1) {
  transform: rotate(-45deg) translate(-8px, 8px);
}

.hamburger.active span:nth-child(2) {
  opacity: 0;
}

.hamburger.active span:nth-child(3) {
  transform: rotate(45deg) translate(-7px, -6px);
}

.nav-menu {
  display: flex;
  list-style: none;
  margin: 0;
  padding: 0;
  gap: 0;
  position: absolute;
  right: 20px;
  top: 60px;
  flex-direction: column;
  background-color: rgba(34, 34, 34, 0.98);
  width: 220px;
  border-radius: 8px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.25);
  opacity: 0;
  transform: translateY(-8px);
  pointer-events: none;
  transition: opacity 0.2s, transform 0.2s;
}

.nav-menu.active {
  opacity: 1;
  transform: translateY(0);
  pointer-events: auto;
}

.nav-menu li {
  padding: 12px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.nav-menu li:last-child {
  border-bottom: none;
}

.nav-link {
  color: white;
  background: none;
  border: none;
  font-size: 16px;
  cursor: pointer;
  padding: 8px 0;
  transition: color 0.3s;
}

.nav-link:hover {
  color: #667eea;
}

.nav-link.logout {
  color: #ff6b6b;
}

.nav-link.logout:hover {
  color: #ff8787;
}

.content {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 60px);
  padding: 20px;
}

/* Mobile responsive */
@media (max-width: 768px) {
  .content {
    min-height: auto;
    padding: 20px;
    margin-top: 10px;
  }

  .nav-menu {
    width: 100%;
    right: 0;
    border-radius: 0;
  }
}
</style>
