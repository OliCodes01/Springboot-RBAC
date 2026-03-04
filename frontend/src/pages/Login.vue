<template>
  <div class="login-container">
    <div class="login-card">
      <h1>Login</h1>
      <div v-if="errorMessage" class="error">{{ errorMessage }}</div>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="username">Username</label>
          <input
            id="username"
            v-model="form.username"
            type="text"
            placeholder="Enter your username"
            required
          />
        </div>
        <div class="form-group">
          <label for="password">Password</label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            placeholder="Enter your password"
            required
          />
        </div>
        <button type="submit" :disabled="loading">{{ loading ? 'Logging in...' : 'Login' }}</button>
      </form>
      <p class="signup-link">
        Don't have an account?
        <button type="button" @click="goToRegister" class="link-button">Sign up</button>
      </p>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { authService } from '../services/authService'

export default {
  emits: ['navigate'],
  setup(props, { emit }) {
    const form = ref({
      username: '',
      password: ''
    })
    const loading = ref(false)
    const errorMessage = ref('')

    const handleLogin = async () => {
      loading.value = true
      errorMessage.value = ''
      try {
        await authService.login(form.value.username, form.value.password)
        emit('navigate', 'home')
      } catch (error) {
        console.error('Login error:', error)
        if (error.response?.data?.message) {
          errorMessage.value = error.response.data.message
        } else if (error.response?.status === 401) {
          errorMessage.value = 'Invalid username or password'
        } else if (error.code === 'ERR_NETWORK') {
          errorMessage.value = 'Cannot connect to server. Make sure backend is running on port 8080.'
        } else if (error.message === 'Network Error') {
          errorMessage.value = 'Network error - check if a browser extension is blocking requests (ad blocker, etc.)'
        } else {
          errorMessage.value = error.message || 'Login failed'
        }
      } finally {
        loading.value = false
      }
    }

    const goToRegister = () => {
      emit('navigate', 'register')
    }

    return {
      form,
      loading,
      errorMessage,
      handleLogin,
      goToRegister
    }
  }
}
</script>

<style scoped>
.login-container {
  width: 100%;
  max-width: 400px;
}

.login-card {
  background: white;
  padding: 40px;
  border-radius: 10px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
}

h1 {
  color: #333;
  margin-bottom: 30px;
  text-align: center;
}

.form-group {
  margin-bottom: 20px;
}

label {
  display: block;
  margin-bottom: 8px;
  color: #555;
  font-weight: 500;
}

input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 14px;
  transition: border-color 0.3s;
}

input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 5px rgba(102, 126, 234, 0.3);
}

button[type="submit"] {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 5px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s;
}

button[type="submit"]:hover:not(:disabled) {
  transform: translateY(-2px);
}

button[type="submit"]:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.error {
  background-color: #fee;
  color: #c33;
  padding: 12px;
  border-radius: 5px;
  margin-bottom: 20px;
  border-left: 4px solid #c33;
}

.signup-link {
  text-align: center;
  margin-top: 20px;
  color: #666;
}

.link-button {
  background: none;
  border: none;
  color: #667eea;
  cursor: pointer;
  font-weight: 600;
  text-decoration: underline;
  padding: 0;
}

.link-button:hover {
  color: #764ba2;
}
</style>
