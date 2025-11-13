import {defineStore} from 'pinia'
import {ref} from 'vue'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const currentUser = ref(null)

  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  const setCurrentUser = (user) => {
    currentUser.value = user
  }

  return {
    sidebarCollapsed,
    currentUser,
    toggleSidebar,
    setCurrentUser
  }
})

