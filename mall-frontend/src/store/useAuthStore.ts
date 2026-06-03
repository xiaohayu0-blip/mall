import { create } from 'zustand';

interface AuthState {
  token: string;
  userName: string;
  role: string;
  userId: number;
  isLoggedIn: boolean;
  setAuth: (token: string, userName: string, role: string, userId: number) => void;
  clearAuth: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  token: localStorage.getItem('mall_token') || '',
  userName: localStorage.getItem('mall_user') || '',
  role: localStorage.getItem('mall_role') || '',
  userId: Number(localStorage.getItem('mall_userId') || 0),
  isLoggedIn: !!localStorage.getItem('mall_token'),
  setAuth: (token, userName, role, userId) => {
    localStorage.setItem('mall_token', token);
    localStorage.setItem('mall_user', userName);
    localStorage.setItem('mall_role', role);
    localStorage.setItem('mall_userId', String(userId));
    set({ token, userName, role, userId, isLoggedIn: true });
  },
  clearAuth: () => {
    localStorage.removeItem('mall_token');
    localStorage.removeItem('mall_user');
    localStorage.removeItem('mall_role');
    localStorage.removeItem('mall_userId');
    set({ token: '', userName: '', role: '', userId: 0, isLoggedIn: false });
  },
}));
