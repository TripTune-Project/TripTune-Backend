import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL;
const EMAIL = __ENV.EMAIL;
const PASSWORD = __ENV.PASSWORD;

export const options = {
    vus : 100,
    duration: '1m',
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
        `${BASE_URL}/api/schedules?page=1`,
        {
            headers: {
                Authorization: `Bearer ${data.accessToken}`,
            },
        }
    );

    check(res, {
        '전체 일정 목록 조회 성공': (r) => r.status === 200,
    });

    sleep(1);
}