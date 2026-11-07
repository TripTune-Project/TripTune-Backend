import http from 'k6/http';
import exec from 'k6/execution';
import { check } from 'k6';

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
    scenarios: {
        warmup: {
            executor: 'constant-arrival-rate',
            rate: 10,
            timeUnit: '1s',
            duration: '30s',
            preAllocatedVUs: 10,
            maxVUs: 20,
            startTime: '0s',
            tags: { phase: 'warmup' },
        },
        main: {
            executor: 'constant-arrival-rate',
            rate: 20,
            timeUnit: '1s',
            duration: '2m',
            preAllocatedVUs: 20,
            maxVUs: 40,
            startTime: '35s',
            tags: { phase: 'main' },
        },
    },
    thresholds: {
        'http_req_failed{phase:main}': ['rate<0.01'],
        'http_req_duration{phase:main}': ['p(95)<500'],
    },
};


export default function() {
    const acc = members[exec.scenario.iterationInTest % members.length];

    // 로그인
    const loginRes = http.post(
        `${BASE_URL}/api/members/login`,
        JSON.stringify({
            email: acc.email,
            password: acc.password,
        }),
        {
            headers: { "Content-Type": "application/json" },
            tags: { name: 'login' }
        }
    );

    check(loginRes, {
        '로그인 성공': (r) => r.status === 200,
    });

    if (loginRes.status !== 200) {
        console.log(`status: ${loginRes.status}, body: ${loginRes.body}`);
        return;
    }

    // 로그아웃
    const logoutRes = http.patch(
        `${BASE_URL}/api/members/logout`,
        JSON.stringify({
            nickname: acc.nickname
        }),
        {
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${loginRes.json('data.accessToken')}`
            },
            tags: { name: 'logout' }
        }
    );


    check(logoutRes, {
        '로그아웃 성공': (r) => r.status === 200,
    });

    if (logoutRes.status !== 200) {
        console.log(`status: ${logoutRes.status}, body: ${logoutRes.body}`);
    }

}