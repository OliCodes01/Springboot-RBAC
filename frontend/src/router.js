class SimpleRouter {
  constructor() {
    this.currentRoute = 'login'
    this.listeners = []
  }

  navigate(routeName) {
    this.currentRoute = routeName
    this.notify()
  }

  subscribe(listener) {
    this.listeners.push(listener)
  }

  notify() {
    this.listeners.forEach(listener => listener())
  }

  getCurrentRoute() {
    return this.currentRoute
  }
}

export const router = new SimpleRouter()

export default router
