<template>
  <div class="profile-container">
    <div class="profile-card">
      <div class="header">
        <h1>My Profile</h1>
        <button type="button" @click="handleLogout" class="logout-btn">Logout</button>
      </div>

      <div v-if="errorMessage" class="error">{{ errorMessage }}</div>
      <div v-if="successMessage" class="success">{{ successMessage }}</div>

      <div v-if="loading" class="loading">Loading profile...</div>

      <div v-else class="profile-content">
        <div v-if="!isEditing" class="view-mode">
          <div class="profile-field">
            <span class="label">Username:</span>
            <span class="value">{{ profile.username }}</span>
          </div>
          <div class="profile-field">
            <span class="label">Email:</span>
            <span class="value">{{ profile.email }}</span>
          </div>
          <div class="profile-field">
            <span class="label">Role:</span>
            <span class="value role-badge" :class="profile.role">{{ profile.role }}</span>
          </div>
          <div class="profile-field">
            <span class="label">First Name:</span>
            <span class="value">{{ profile.firstName || '-' }}</span>
          </div>
          <div class="profile-field">
            <span class="label">Last Name:</span>
            <span class="value">{{ profile.lastName || '-' }}</span>
          </div>
          <div class="profile-field">
            <span class="label">Phone Number:</span>
            <span class="value">{{ profile.phoneNumber || '-' }}</span>
          </div>
          <div class="profile-field bio-field">
            <span class="label">Bio:</span>
            <span class="value">{{ profile.bio || '-' }}</span>
          </div>
          <button @click="startEditing" class="edit-btn">Edit Profile</button>
          
          <div class="password-section">
            <h2>Change Password</h2>
            <button v-if="!isChangingPassword" @click="startChangingPassword" class="change-password-btn">
              Change Password
            </button>
            <form v-else @submit.prevent="handlePasswordChange" class="password-form">
              <div class="form-group">
                <label for="currentPassword">Current Password</label>
                <input
                  id="currentPassword"
                  v-model="passwordForm.currentPassword"
                  type="password"
                  required
                />
              </div>
              <div class="form-group">
                <label for="newPassword">New Password</label>
                <input
                  id="newPassword"
                  v-model="passwordForm.newPassword"
                  type="password"
                  required
                  minlength="6"
                />
              </div>
              <div class="form-group">
                <label for="confirmPassword">Confirm New Password</label>
                <input
                  id="confirmPassword"
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  required
                  minlength="6"
                />
              </div>
              <div class="button-group">
                <button type="submit" :disabled="passwordLoading" class="save-btn">
                  {{ passwordLoading ? 'Changing...' : 'Change Password' }}
                </button>
                <button type="button" @click="cancelPasswordChange" class="cancel-btn">Cancel</button>
              </div>
            </form>
          </div>
        </div>

        <div v-else class="edit-mode">
          <form @submit.prevent="handleUpdate">
            <div class="form-group">
              <label for="email">Email</label>
              <input
                id="email"
                v-model="editForm.email"
                type="email"
                required
              />
            </div>
            <div class="form-group">
              <label for="firstName">First Name</label>
              <input
                id="firstName"
                v-model="editForm.firstName"
                type="text"
              />
            </div>
            <div class="form-group">
              <label for="lastName">Last Name</label>
              <input
                id="lastName"
                v-model="editForm.lastName"
                type="text"
              />
            </div>
            <div class="form-group">
              <label for="phoneNumber">Phone Number</label>
              <input
                id="phoneNumber"
                v-model="editForm.phoneNumber"
                type="tel"
              />
            </div>
            <div class="form-group">
              <label for="bio">Bio</label>
              <textarea
                id="bio"
                v-model="editForm.bio"
                placeholder="Tell us about yourself"
                rows="4"
              ></textarea>
            </div>
            <div class="button-group">
              <button type="submit" :disabled="updateLoading" class="save-btn">
                {{ updateLoading ? 'Saving...' : 'Save Changes' }}
              </button>
              <button type="button" @click="cancelEditing" class="cancel-btn">Cancel</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { authService } from '../services/authService'

export default {
  emits: ['navigate'],
  setup(props, { emit }) {
    const profile = ref({})
    const editForm = ref({})
    const passwordForm = ref({
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
    const loading = ref(true)
    const updateLoading = ref(false)
    const passwordLoading = ref(false)
    const isEditing = ref(false)
    const isChangingPassword = ref(false)
    const errorMessage = ref('')
    const successMessage = ref('')

    const fetchProfile = async () => {
      loading.value = true
      errorMessage.value = ''
      try {
        const data = await authService.getProfile()
        profile.value = data
        initializeEditForm()
      } catch (error) {
        if (error.response?.status === 401) {
          emit('navigate', 'login')
        } else {
          errorMessage.value = 'Failed to load profile'
        }
      } finally {
        loading.value = false
      }
    }

    const initializeEditForm = () => {
      editForm.value = {
        email: profile.value.email || '',
        firstName: profile.value.firstName || '',
        lastName: profile.value.lastName || '',
        phoneNumber: profile.value.phoneNumber || '',
        bio: profile.value.bio || ''
      }
    }

    const startEditing = () => {
      isEditing.value = true
      successMessage.value = ''
    }

    const cancelEditing = () => {
      isEditing.value = false
      initializeEditForm()
      errorMessage.value = ''
    }

    const handleUpdate = async () => {
      updateLoading.value = true
      errorMessage.value = ''
      try {
        const updatedProfile = await authService.updateProfile(editForm.value)
        profile.value = updatedProfile
        isEditing.value = false
        successMessage.value = 'Profile updated successfully!'
        setTimeout(() => {
          successMessage.value = ''
        }, 3000)
      } catch (error) {
        errorMessage.value = error.response?.data?.message || 'Failed to update profile'
      } finally {
        updateLoading.value = false
      }
    }

    const startChangingPassword = () => {
      isChangingPassword.value = true
      successMessage.value = ''
      errorMessage.value = ''
    }

    const cancelPasswordChange = () => {
      isChangingPassword.value = false
      passwordForm.value = {
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
      errorMessage.value = ''
    }

    const handlePasswordChange = async () => {
      errorMessage.value = ''
      
      if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
        errorMessage.value = 'New passwords do not match'
        return
      }

      if (passwordForm.value.newPassword.length < 6) {
        errorMessage.value = 'New password must be at least 6 characters long'
        return
      }

      passwordLoading.value = true
      try {
        await authService.changePassword(
          passwordForm.value.currentPassword,
          passwordForm.value.newPassword
        )
        isChangingPassword.value = false
        passwordForm.value = {
          currentPassword: '',
          newPassword: '',
          confirmPassword: ''
        }
        successMessage.value = 'Password changed successfully!'
        setTimeout(() => {
          successMessage.value = ''
        }, 3000)
      } catch (error) {
        errorMessage.value = error.response?.data || 'Failed to change password'
      } finally {
        passwordLoading.value = false
      }
    }

    const handleLogout = async () => {
      await authService.logout()
      emit('navigate', 'login')
    }

    onMounted(() => {
      fetchProfile()
    })

    return {
      profile,
      editForm,
      passwordForm,
      loading,
      updateLoading,
      passwordLoading,
      isEditing,
      isChangingPassword,
      errorMessage,
      successMessage,
      startEditing,
      cancelEditing,
      handleUpdate,
      startChangingPassword,
      cancelPasswordChange,
      handlePasswordChange,
      handleLogout
    }
  }
}
</script>

<style scoped>
.profile-container {
  width: 100%;
  max-width: 600px;
}

.profile-card {
  background: white;
  padding: 40px;
  border-radius: 10px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 20px;
}

h1 {
  color: #333;
  margin: 0;
}

.logout-btn {
  padding: 8px 16px;
  background-color: #ff6b6b;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-weight: 600;
  transition: background-color 0.3s;
}

.logout-btn:hover {
  background-color: #ff5252;
}

.error {
  background-color: #fee;
  color: #c33;
  padding: 12px;
  border-radius: 5px;
  margin-bottom: 20px;
  border-left: 4px solid #c33;
}

.success {
  background-color: #efe;
  color: #3c3;
  padding: 12px;
  border-radius: 5px;
  margin-bottom: 20px;
  border-left: 4px solid #3c3;
}

.loading {
  text-align: center;
  color: #667eea;
  font-size: 16px;
  padding: 40px 0;
}

.profile-content {
  margin-top: 20px;
}

.view-mode {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-field {
  display: flex;
  justify-content: space-between;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 5px;
  border-left: 4px solid #667eea;
}

.profile-field.bio-field {
  flex-direction: column;
  gap: 8px;
}

.label {
  font-weight: 600;
  color: #555;
}

.value {
  color: #333;
  word-break: break-word;
}

.role-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  text-transform: uppercase;
}

.role-badge.ADMIN {
  background-color: #ff6b6b;
  color: white;
}

.role-badge.USER {
  background-color: #4ecdc4;
  color: white;
}

.bio-field .value {
  white-space: pre-wrap;
  max-height: 100px;
  overflow-y: auto;
  background-color: white;
  padding: 10px;
  border-radius: 3px;
}

.edit-btn {
  padding: 12px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 5px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s;
  align-self: flex-start;
}

.edit-btn:hover {
  transform: translateY(-2px);
}

.edit-mode {
  margin-top: 20px;
}

.form-group {
  margin-bottom: 20px;
}

label {
  display: block;
  margin-bottom: 8px;
  color: #555;
  font-weight: 600;
}

input,
textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 14px;
  font-family: inherit;
  transition: border-color 0.3s;
}

input:focus,
textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 5px rgba(102, 126, 234, 0.3);
}

textarea {
  resize: vertical;
}

.button-group {
  display: flex;
  gap: 10px;
  margin-top: 30px;
}

.save-btn,
.cancel-btn {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 5px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s;
}

.save-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.save-btn:hover:not(:disabled) {
  transform: translateY(-2px);
}

.save-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.cancel-btn {
  background-color: #f0f0f0;
  color: #333;
  border: 1px solid #ddd;
}

.cancel-btn:hover {
  background-color: #e0e0e0;
}

.password-section {
  margin-top: 40px;
  padding-top: 30px;
  border-top: 2px solid #f0f0f0;
}

.password-section h2 {
  color: #333;
  font-size: 20px;
  margin-bottom: 20px;
}

.change-password-btn {
  padding: 12px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 5px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s;
}

.change-password-btn:hover {
  transform: translateY(-2px);
}

.password-form {
  margin-top: 20px;
}
</style>
