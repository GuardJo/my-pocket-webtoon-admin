import {BaseResponse, toBaseResponse} from "@/lib/models";

const API_BASE_URL = process.env.BACKEND_API_URL;

export const authService = {
    login: async (username: string, password: string): Promise<BaseResponse<string>> => {
        const loginRequest: LoginRequest = {
            id: username,
            password: password
        };

        const response = await fetch(`${API_BASE_URL}/api/v1/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(loginRequest),
        });

        return toBaseResponse<string>(response);
    }
}

// 로그인 요청 포맷
export interface LoginRequest {
    id: string;
    password: string;
}
