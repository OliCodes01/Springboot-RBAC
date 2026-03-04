<template>
  <div class="not-found-container">
    <div class="not-found-card">
      <h1>404</h1>
      <p>Page not found</p>
      <button @click="goHome" class="home-btn">Go Home</button>
    </div>
  </div>
</template>

<script>
export default {
  emits: ['navigate'],
  setup(props, { emit }) {
    const goHome = () => {
      emit('navigate', 'login')
    }

    return {
      goHome
    }
  }
}
</script>

<style scoped>
.not-found-container {
  width: 100%;
  max-width: 400px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.not-found-card {
  background: white;
  padding: 60px 40px;
  border-radius: 10px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
  text-align: center;
}

h1 {
  font-size: 72px;
  color: #667eea;
  margin: 0 0 20px 0;
}

p {
  color: #666;
  font-size: 18px;
  margin-bottom: 30px;
}

.home-btn {
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

.home-btn:hover {
  transform: translateY(-2px);
}
</style>
