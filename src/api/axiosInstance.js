import axios from "axios";

const api = axios.create({
    baseURL: "https://emojournal.djloghub.com",
    withCredentials: true
});

// requst 를 보내기 전에 헤더에 토큰을 집어넣음
api.interceptors.request.use((config) =>  {
    // 요청 보내기전에 토큰 헤더에 적어놓기
    let accessToken = localStorage.getItem("accessToken");
    if(accessToken) {
        console.log("accessToken : " + accessToken)
        config.headers["Authorization"] = `Bearer ${accessToken}`
    }
    return config;
})

// 응답 전 새 access 토큰 받으면 갈아 끼기
api.interceptors.response.use(
    (response) => response,
    async (error) => {
        
        let accessToken = localStorage.getItem("accessToken");

        const originalRequest = error.config;

        // 응답이 401 이거나 이미 재시도 한 요청이 아니면 
        if(error.response?.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true;

            try {
                // access token 재발급 요청
                const res = await api.post("/api/auth/reissue");
                
                // 서버에서 보내준 토큰 가져오기
                const newToken = res.data.accessToken;
                
                // 만료된 토큰 지우기
                localStorage.removeItem("accessToken");
                // 새 발급받은 토큰 집어넣기
                localStorage.setItem("accessToken",newToken);

                originalRequest.headers["Authorization"] = `Bearer ${newToken}`;
                // 실패했던 요청 다시 보내기
                return api(originalRequest)
            } catch(reissueError) {
                console.error("재발급 실패", reissueError);
                return Promise.reject(reissueError);
            }
        }

        return Promise.reject(error);
    }
)

export default api