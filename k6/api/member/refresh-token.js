import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL;

const members = open('../../data/test_member_list.csv')
    .trim()
    .split('\n')
    .slice(1)
    .map((line) => {
        const [member_id, email, password] = line.trim().split(',');
        return { email, password, nickname: email.split('@')[0], };
    })

export const options = {
    setupTimeout: '120s',
    stages: [
        { duration: '30s', target: 10 },
        { duration: '30s', target: 100 },
        { duration: '3m', target: 100 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<500'],
    },
};

export function setup(){
    return members.map((acc) => {
        const loginRes = http.post(
            `${BASE_URL}/api/members/login`,
            JSON.stringify({
                email: acc.email,
                password: acc.password,
            }),
            {
                headers: { "Content-Type": "application/json" }
            }
        );

        check(loginRes, {
            "로그인 성공": (r) => r.status === 200,
        });

        return {
            nickname: acc.nickname,
            accessToken: loginRes.json('data.accessToken'),
            refreshToken: loginRes.cookies.refreshToken
                ? loginRes.cookies.refreshToken[0].value
                : null,
        };
    });

}

export default function(data) {
    // VU 1~100 -> 계정 0~99 하나씩 고정
    const s = data[(__VU - 1) % data.length];

    const res = http.post(
        `${BASE_URL}/api/members/refresh`,
        null,
        {
            headers: {
                Cookie: `accessToken=${s.accessToken}; refreshToken=${s.refreshToken}; nickname=${s.nickname}`,
            }
        }
    );

    check(res, {
        '토큰 갱신 성공': (r) => r.status === 200,
    });

    if (res.status !== 200) {
        console.log(`status: ${res.status}, body: ${res.body}`);
    }

    sleep(1);
}