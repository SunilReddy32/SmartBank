import { createContext, useContext, useState, useEffect } from 'react'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    // Restore session from localStorage on page refresh
    const token  = localStorage.getItem('token')
    const userId = localStorage.getItem('userId')
    const email  = localStorage.getItem('email')
    const role   = localStorage.getItem('role')
    const name   = localStorage.getItem('name')

    if (token && userId) {
      setUser({ token, userId: Number(userId), email, role, name })
    }
    setLoading(false)
  }, [])

  const loginUser = (data) => {
    // data = { token, email, role, userId, name }
    localStorage.setItem('token',  data.token)
    localStorage.setItem('userId', data.userId)
    localStorage.setItem('email',  data.email)
    localStorage.setItem('role',   data.role)
    localStorage.setItem('name',   data.name)
    setUser(data)
  }

  const logout = () => {
    localStorage.clear()
    setUser(null)
  }

  const isAdmin = user?.role === 'ROLE_ADMIN'

  return (
    <AuthContext.Provider value={{ user, loginUser, logout, isAdmin, loading }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)