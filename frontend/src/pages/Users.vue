<template>
  <div class="users-container">
    <div class="users-card">
      <h1>All Users</h1>
      
      <div v-if="errorMessage" class="error">{{ errorMessage }}</div>
      <div v-if="loading" class="loading">Loading users...</div>

      <div v-else class="users-list">
        <div v-if="users.length === 0" class="no-users">No users found</div>
        <div v-else class="user-card" v-for="user in users" :key="user.id">
          <div class="user-header">
            <div class="user-avatar">{{ getInitials(user) }}</div>
            <div class="user-main-info">
              <h3>{{ user.firstName }} {{ user.lastName }}</h3>
              <p class="username">@{{ user.username }}</p>
            </div>
          </div>
          <div class="user-details" v-if="editingUserId !== user.id">
            <div class="detail-row">
              <span class="label">Email:</span>
              <span class="value">{{ user.email }}</span>
            </div>
            <div class="detail-row" v-if="user.phoneNumber">
              <span class="label">Phone:</span>
              <span class="value">{{ user.phoneNumber }}</span>
            </div>
            <div class="detail-row" v-if="user.bio">
              <span class="label">Bio:</span>
              <span class="value">{{ user.bio }}</span>
            </div>
          </div>
          <form v-else class="user-edit-form" @submit.prevent="saveUser(user.id)">
            <div class="detail-row">
              <label class="label" :for="`email-${user.id}`">Email:</label>
              <input
                class="value-input"
                :id="`email-${user.id}`"
                v-model="editForm.email"
                type="email"
                required
              />
            </div>
            <div class="detail-row">
              <label class="label" :for="`first-${user.id}`">First Name:</label>
              <input
                class="value-input"
                :id="`first-${user.id}`"
                v-model="editForm.firstName"
                type="text"
              />
            </div>
            <div class="detail-row">
              <label class="label" :for="`last-${user.id}`">Last Name:</label>
              <input
                class="value-input"
                :id="`last-${user.id}`"
                v-model="editForm.lastName"
                type="text"
              />
            </div>
            <div class="detail-row">
              <label class="label" :for="`phone-${user.id}`">Phone:</label>
              <input
                class="value-input"
                :id="`phone-${user.id}`"
                v-model="editForm.phoneNumber"
                type="text"
              />
            </div>
            <div class="detail-row">
              <label class="label" :for="`bio-${user.id}`">Bio:</label>
              <textarea
                class="value-input"
                :id="`bio-${user.id}`"
                v-model="editForm.bio"
                rows="3"
              ></textarea>
            </div>
            <div class="action-row">
              <button type="submit" class="action-btn save">Save</button>
              <button type="button" class="action-btn cancel" @click="cancelEdit">Cancel</button>
            </div>
          </form>
          <div class="action-row" v-if="isAdmin && editingUserId !== user.id">
            <button class="action-btn edit" @click="startEdit(user)">Edit</button>
            <button class="action-btn delete" @click="deleteUser(user)">Delete</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { authService } from '../services/authService'

export default {
  setup() {
    const users = ref([])
    const loading = ref(true)
    const errorMessage = ref('')
    const editingUserId = ref(null)
    const editForm = ref({
      email: '',
      firstName: '',
      lastName: '',
      phoneNumber: '',
      bio: ''
    })

    const isAdmin = computed(() => authService.isAdmin())

    const fetchUsers = async () => {
      loading.value = true
      errorMessage.value = ''
      try {
        // Extra guard in the UI even if the backend already checks.
        if (!authService.isAdmin()) {
          errorMessage.value = 'Only admins can view this page.'
          users.value = []
          return
        }
        const data = await authService.getAllUsers()
        users.value = data
      } catch (error) {
        errorMessage.value = 'Failed to load users'
        console.error('Error fetching users:', error)
      } finally {
        loading.value = false
      }
    }

    const startEdit = (user) => {
      editingUserId.value = user.id
      editForm.value = {
        email: user.email || '',
        firstName: user.firstName || '',
        lastName: user.lastName || '',
        phoneNumber: user.phoneNumber || '',
        bio: user.bio || ''
      }
    }

    const cancelEdit = () => {
      editingUserId.value = null
      editForm.value = {
        email: '',
        firstName: '',
        lastName: '',
        phoneNumber: '',
        bio: ''
      }
    }

    const saveUser = async (userId) => {
      errorMessage.value = ''
      try {
        const updated = await authService.updateUserByAdmin(userId, editForm.value)
        users.value = users.value.map(user => (user.id === userId ? updated : user))
        cancelEdit()
      } catch (error) {
        errorMessage.value = error.message || 'Failed to update user'
      }
    }

    const deleteUser = async (user) => {
      errorMessage.value = ''
      const confirmed = window.confirm(`Delete ${user.username}? This cannot be undone.`)
      if (!confirmed) {
        return
      }
      try {
        await authService.deleteUserByAdmin(user.id)
        users.value = users.value.filter(existing => existing.id !== user.id)
      } catch (error) {
        errorMessage.value = error.message || 'Failed to delete user'
      }
    }

    const getInitials = (user) => {
      const first = user.firstName ? user.firstName.charAt(0).toUpperCase() : ''
      const last = user.lastName ? user.lastName.charAt(0).toUpperCase() : ''
      return first + last || user.username.charAt(0).toUpperCase()
    }

    onMounted(() => {
      fetchUsers()
    })

    return {
      users,
      loading,
      errorMessage,
      editingUserId,
      editForm,
      isAdmin,
      getInitials,
      startEdit,
      cancelEdit,
      saveUser,
      deleteUser
    }
  }
}
</script>

<style scoped>
.users-container {
  width: 100%;
  max-width: 1000px;
  padding: 20px;
}

.users-card {
  background: white;
  padding: 40px;
  border-radius: 10px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
}

h1 {
  color: #333;
  margin-bottom: 30px;
  text-align: center;
  font-size: 28px;
}

.loading {
  text-align: center;
  color: #666;
  padding: 40px;
  font-size: 16px;
}

.error {
  background: #fee;
  color: #c33;
  padding: 12px;
  border-radius: 5px;
  margin-bottom: 20px;
  border: 1px solid #fcc;
}

.users-list {
  display: grid;
  gap: 20px;
}

.no-users {
  text-align: center;
  color: #999;
  padding: 40px;
  font-size: 16px;
}

.user-card {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
  transition: all 0.3s ease;
  background: #fafafa;
}

.user-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.user-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e0e0e0;
}

.user-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
  flex-shrink: 0;
}

.user-main-info h3 {
  margin: 0 0 5px 0;
  color: #333;
  font-size: 20px;
}

.username {
  margin: 0;
  color: #667eea;
  font-weight: 500;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.user-edit-form {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.detail-row {
  display: flex;
  gap: 10px;
}

.detail-row .label {
  font-weight: 600;
  color: #555;
  min-width: 80px;
}

.detail-row .value {
  color: #333;
  flex: 1;
  word-break: break-word;
}

.value-input {
  flex: 1;
  border: 1px solid #ddd;
  border-radius: 6px;
  padding: 8px 10px;
  font-size: 14px;
}

.action-row {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

.action-btn {
  border: none;
  padding: 8px 14px;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
}

.action-btn.edit {
  background: #667eea;
  color: white;
}

.action-btn.save {
  background: #2d9c5a;
  color: white;
}

.action-btn.cancel {
  background: #e0e0e0;
  color: #333;
}

.action-btn.delete {
  background: #e55353;
  color: white;
}

@media (max-width: 768px) {
  .users-card {
    padding: 20px;
  }

  .user-avatar {
    width: 50px;
    height: 50px;
    font-size: 20px;
  }

  .user-main-info h3 {
    font-size: 18px;
  }
}
</style>
