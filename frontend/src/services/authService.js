import api from '../api'

// In-memory state - secure, cannot be manipulated by user
let authState = {
  isAuthenticated: false,
  userRole: null,
  userProfile: null
}

const notifyAuthChanged = () => {
  window.dispatchEvent(new Event('auth-changed'))
}

const setAuthState = (profile) => {
  authState.isAuthenticated = true
  authState.userRole = profile.role
  authState.userProfile = profile
  notifyAuthChanged()
}

const clearAuthState = () => {
  authState.isAuthenticated = false
  authState.userRole = null
  authState.userProfile = null
  notifyAuthChanged()
}

// Listen for unauthorized events from API interceptor
if (typeof window !== 'undefined') {
  window.addEventListener('unauthorized', () => {
    clearAuthState()
  })
}

export const authService = {
  // Initialize authentication state by fetching profile
  // Call this on app startup to check if user is logged in
  initialize: async () => {
    try {
      const profile = await api.get('/user/profile')
      setAuthState(profile.data)
      return true
    } catch (error) {
      clearAuthState()
      return false
    }
  },

  register: async (username, email, password, firstName, lastName) => {
    const response = await api.post('/auth/register', {
      username,
      email,
      password,
      firstName,
      lastName
    })
    // After successful registration, fetch profile to get role
    try {
      const profile = await api.get('/user/profile')
      setAuthState(profile.data)
    } catch (error) {
      console.error('Failed to fetch profile after registration:', error)
    }
    return response.data
  },

  login: async (username, password) => {
    const response = await api.post('/auth/login', {
      username,
      password
    })
    // After successful login, fetch profile to get role
    try {
      const profile = await api.get('/user/profile')
      setAuthState(profile.data)
    } catch (error) {
      console.error('Failed to fetch profile after login:', error)
    }
    return response.data
  },

  logout: async () => {
    try {
      await api.post('/auth/logout')
    } finally {
      clearAuthState()
    }
  },

  getProfile: async () => {
    const response = await api.get('/user/profile')
    setAuthState(response.data)
    return response.data
  },

  updateProfile: async (userData) => {
    const response = await api.put('/user/profile', userData)
    setAuthState(response.data)
    return response.data
  },

  getAllUsers: async () => {
    const response = await api.get('/user/all')
    return response.data
  },

  updateUserByAdmin: async (userId, userData) => {
    const response = await api.put(`/user/${userId}`, userData)
    return response.data
  },

  deleteUserByAdmin: async (userId) => {
    const response = await api.delete(`/user/${userId}`)
    return response.data
  },

  changePassword: async (currentPassword, newPassword) => {
    const response = await api.post('/user/change-password', {
      currentPassword,
      newPassword
    })
    return response.data
  },

  // Check authentication status (from in-memory state)
  isAuthenticated: () => {
    return authState.isAuthenticated
  },

  // Get user role (from in-memory state, fetched from backend)
  getUserRole: () => {
    return authState.userRole
  },

  // Check if user is admin (from in-memory state)
  isAdmin: () => {
    return authState.userRole === 'ADMIN'
  },

  // Get cached user profile
  getCachedProfile: () => {
    return authState.userProfile
  }
}
