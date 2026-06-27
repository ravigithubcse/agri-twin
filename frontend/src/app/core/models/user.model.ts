export type UserRole = 'FARMER' | 'COOPERATIVE_ADMIN' | 'ENTERPRISE_BUYER' | 'PLATFORM_ADMIN';

export type SubscriptionTier = 'FREE' | 'KISAN_PRO' | 'KISAN_EXPERT' | 'COOPERATIVE' | 'ENTERPRISE';

export interface UserResponse {
  id: string;
  phone: string;
  fullName: string;
  role: UserRole;
  tier: SubscriptionTier;
  stateCode: string | null;
  districtCode: string | null;
  languagePreference: string;
  createdAt: string;
}

export interface RegisterRequest {
  phone: string;
  password: string;
  fullName: string;
  stateCode?: string;
  districtCode?: string;
  languagePreference?: string;
}

export interface LoginRequest {
  phone: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  accessTokenExpiresInSeconds: number;
  user: UserResponse;
}

export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  details: string[];
}
