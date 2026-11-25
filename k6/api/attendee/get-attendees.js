import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = __ENV.BASE_URL;
const EMAIL = __ENV.EMAIL;
const PASSWORD = __ENV.PASSWORD;

export const options = {
    scenarios: {
        warmup: {
            executor: 'constant-arrival-rate',
            rate: 56,
            timeUnit: '1s',
            duration: '30s',
            preAllocatedVUs: 56,
            maxVUs: 112,
            startTime: '0s',
            tags: { phase: 'warmup' },
        },
        main: {
            executor: 'constant-arrival-rate',
            rate: 112,
            timeUnit: '1s',
            duration: '2m',
            preAllocatedVUs: 112,
            maxVUs: 224,
            startTime: '35s',
            tags: { phase: 'main' },
        },
    },
    thresholds: {
        'http_req_failed{phase:main}': ['rate<0.01'],
        'http_req_duration{phase:main}': ['p(95)<500'],
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
            headers: { "Content-Type": "application/json" }
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
        `${BASE_URL}/api/schedules/7/attendees`,
        {
            headers: { Authorization: `Bearer ${data.accessToken}` }
        }
    );

    check(res, {
        '일정 참석자 조회 성공': (r) => r.status === 200,
    });

    if (res.status !== 200) {
        console.log(`status: ${res.status}, body: ${res.body}`);
    }
}