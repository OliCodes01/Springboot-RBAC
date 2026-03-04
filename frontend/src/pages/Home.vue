<template>
  <div class="home-shell">
    <section class="hero">
      <div class="hero-copy">
        <h1>Welcome back<span v-if="profile.firstName">, {{ profile.firstName }}</span>.</h1>
        <div class="hero-actions">
          <button class="primary" @click="goTo('profile')">Edit Profile</button>
          <button v-if="isAdmin" class="ghost" @click="goTo('users')">Browse Users</button>
        </div>
      </div>
      <div class="hero-panel">
        <div class="panel-header">
        </div>
        <div class="panel-grid">
          <div class="panel-card" v-if="isAdmin">
            <p class="panel-label">Total Users</p>
            <p class="panel-value">{{ loading ? '-' : usersCount }}</p>
          </div>
          <div class="panel-card">
            <p class="panel-label">Profile Status</p>
            <p class="panel-value">{{ profileCompleteness }}</p>
          </div>
          
        </div>
        <p v-if="errorMessage" class="panel-error">{{ errorMessage }}</p>
      </div>
    </section>

    
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { authService } from '../services/authService'
import { router } from '../router'

export default {
  setup() {
    const profile = ref({})
    const usersCount = ref(0)
    const loading = ref(true)
    const errorMessage = ref('')
    const isAdmin = computed(() => authService.isAdmin())

    const loadDashboard = async () => {
      loading.value = true
      errorMessage.value = ''
      try {
        const profileData = await authService.getProfile()
        profile.value = profileData
        // Only admins can see the users list count.
        if (isAdmin.value) {
          const users = await authService.getAllUsers()
          usersCount.value = Array.isArray(users) ? users.length : 0
        } else {
          usersCount.value = 0
        }
      } catch (error) {
        errorMessage.value = 'Unable to load dashboard data'
      } finally {
        loading.value = false
      }
    }

    const completenessPercent = computed(() => {
      const fields = ['email', 'firstName', 'lastName', 'phoneNumber', 'bio']
      const filled = fields.filter(field => profile.value?.[field])
      return Math.round((filled.length / fields.length) * 100)
    })

    const profileCompleteness = computed(() => {
      if (loading.value) {
        return '-'
      }
      const percent = completenessPercent.value
      return percent >= 80
        ? `Strong (${percent}%)`
        : `In progress (${percent}%)`
    })

    const goTo = (pageName) => {
      router.navigate(pageName)
    }

    onMounted(() => {
      loadDashboard()
    })

    return {
      profile,
      usersCount,
      loading,
      errorMessage,
      isAdmin,
      completenessPercent,
      profileCompleteness,
      goTo
    }
  }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;600&family=Playfair+Display:wght@600&display=swap');

.home-shell {
  width: 100%;
  max-width: 1200px;
  padding: 30px 20px 60px;
  font-family: 'Space Grotesk', sans-serif;
  color: #2b2b2b;
}

.hero {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(0, 0.9fr);
  gap: 30px;
  background: rgba(255, 255, 255, 0.85);
  border-radius: 18px;
  padding: 30px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.12);
  backdrop-filter: blur(8px);
}

.hero-copy h1 {
  font-family: 'Playfair Display', serif;
  font-size: 36px;
  margin: 10px 0 12px;
}

.eyebrow {
  text-transform: uppercase;
  letter-spacing: 2px;
  font-size: 12px;
  color: #a35a3a;
  margin: 0;
}

.subtitle {
  font-size: 16px;
  line-height: 1.6;
  color: #4b4b4b;
  margin-bottom: 20px;
}

.hero-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.hero-actions button {
  border-radius: 999px;
  padding: 12px 22px;
  border: none;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
}

.primary {
  background: #2f6f66;
  color: white;
}

.ghost {
  background: transparent;
  border: 1px solid #2f6f66;
  color: #2f6f66;
}

.hero-panel {
  background: #fdf7f2;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  border: 1px solid rgba(163, 90, 58, 0.2);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  font-weight: 600;
  color: #6a4b3c;
}

.status {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 999px;
  background: #f3e5dc;
}

.status.ok {
  background: #e2f1ea;
  color: #2f6f66;
}

.panel-grid {
  display: grid;
  gap: 12px;
}

.panel-card {
  background: white;
  border-radius: 12px;
  padding: 12px 14px;
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.panel-label {
  margin: 0;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 1.5px;
  color: #9b6c55;
}

.panel-value {
  margin: 6px 0 0;
  font-size: 18px;
  font-weight: 600;
}

.panel-error {
  color: #b0442d;
  font-size: 13px;
  margin: 0;
}

.feature-grid {
  margin-top: 30px;
  display: grid;
  gap: 20px;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
}

.feature {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(0, 0, 0, 0.04);
}

.feature h3 {
  margin-top: 0;
  font-size: 18px;
  color: #2f6f66;
}

.feature ul {
  padding-left: 18px;
  margin: 10px 0 0;
  color: #4b4b4b;
}

.progress {
  height: 10px;
  background: #f0e1d6;
  border-radius: 999px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #2f6f66, #6aa99a);
  border-radius: 999px;
  transition: width 0.3s;
}

.progress-caption {
  margin-top: 10px;
  font-size: 13px;
  color: #6a4b3c;
}

@media (max-width: 900px) {
  .hero {
    grid-template-columns: 1fr;
  }
}
</style>
