import axios from 'axios'

// In Docker, nginx proxies /auth and /user to backend, so base URL is '' (same origin).
// For local development, point directly to the backend at port 8080.
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  },
  withCredentials: true,
  timeout: 10000
})

api.interceptors.response.use(
  response => response,
  error => {
    if (error.response) {
      if (error.response.status === 401) {
        window.dispatchEvent(new Event('unauthorized'))
      }
      return Promise.reject(error)
    } else if (error.request) {
      error.message = 'Server is not responding. Please try again later.'
      return Promise.reject(error)
    } else {
      return Promise.reject(error)
    }
  }
)

export default api
