<template>
  <div class="register-container">
    <div class="register-card">
      <h1>Create Account</h1>
      <div v-if="errorMessage" class="error">{{ errorMessage }}</div>
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label for="username">Username</label>
          <input
            id="username"
            v-model="form.username"
            type="text"
            placeholder="Choose a username"
            required
          />
        </div>
        <div class="form-group">
          <label for="email">Email</label>
          <input
            id="email"
            v-model="form.email"
            type="email"
            placeholder="Enter your email"
            required
          />
        </div>
        <div class="form-group">
          <label for="firstName">First Name</label>
          <input
            id="firstName"
            v-model="form.firstName"
            type="text"
            placeholder="Your first name"
          />
        </div>
        <div class="form-group">
          <label for="lastName">Last Name</label>
          <input
            id="lastName"
            v-model="form.lastName"
            type="text"
            placeholder="Your last name"
          />
        </div>
        <div class="form-group">
          <label for="password">Password</label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            placeholder="Create a password"
            required
          />
        </div>
        <button type="submit" :disabled="loading">{{ loading ? 'Creating account...' : 'Sign Up' }}</button>
      </form>
      <p class="login-link">
        Already have an account?
        <button type="button" @click="goToLogin" class="link-button">Login</button>
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
      email: '',
      firstName: '',
      lastName: '',
      password: ''
    })
    const loading = ref(false)
    const errorMessage = ref('')

    const handleRegister = async () => {
      loading.value = true
      errorMessage.value = ''
      try {
        await authService.register(
          form.value.username,
          form.value.email,
          form.value.password,
          form.value.firstName,
          form.value.lastName
        )
        emit('navigate', 'home')
      } catch (error) {
        errorMessage.value = error.response?.data?.message || 'Registration failed'
      } finally {
        loading.value = false
      }
    }

    const goToLogin = () => {
      emit('navigate', 'login')
    }

    return {
      form,
      loading,
      errorMessage,
      handleRegister,
      goToLogin
    }
  }
}
</script>

<style scoped>
.register-container {
  width: 100%;
  max-width: 450px;
}

.register-card {
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

.login-link {
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
