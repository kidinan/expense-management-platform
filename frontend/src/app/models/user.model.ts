export interface User {
  id: number;
  username: string;
  email: string;
  fullName: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  id: number;
  username: string;
  email: string;
  fullName: string;
}
