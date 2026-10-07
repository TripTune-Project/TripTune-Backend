import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL;
const EMAIL = __ENV.EMAIL;
const PASSWORD = __ENV.PASSWORD;

export const options = {
    stages: [
      { duration: '30s', target: 3 },
      { duration: '30s', target: 30 },
      { duration: '3m', target: 30 },
      { duration: '30s', target: 0 },
    ],
    thresholds: {
      http_req_failed: ['rate<0.01'],
      http_req_duration: ['p(95)<500'],
    },
};

export function setup(){
    const loginRes = http.post(
        `${BASE_URL}/api/members/login`,
        JSON.stringify({
            email: EMAIL,
            password: PASSWORD,
        }),
        {
            headers: {
                "Content-Type": "application/json",
            },
        }
    );

    check(loginRes, {
        "로그인 성공": (r) => r.status === 200,
    });

    return {
        accessToken:loginRes.json("data.accessToken"),
    };
}

export default function(data) {
    const res = http.get(
        `${BASE_URL}/api/schedules/42?page=1`,
        {
            headers: {
                Authorization: `Bearer ${data.accessToken}`,
            },
        }
    );

    check(res, {
        '일정 상세 조회 성공': (r) => r.status === 200,
    });

    sleep(1);
}