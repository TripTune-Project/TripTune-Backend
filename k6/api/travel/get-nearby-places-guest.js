import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = __ENV.BASE_URL;

export const options = {
    scenarios: {
        warmup: {
            executor: 'constant-arrival-rate',
            rate: 50,
            timeUnit: '1s',
            duration: '30s',
            preAllocatedVUs: 50,
            maxVUs: 100,
            startTime: '0s',
            tags: { phase: 'warmup' },
        },
        main: {
            executor: 'constant-arrival-rate',
            rate: 100,
            timeUnit: '1s',
            duration: '2m',
            preAllocatedVUs: 100,
            maxVUs: 200,
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
    const res = http.post(
        `${BASE_URL}/api/travels?page=1`,
        JSON.stringify({
            latitude: 37.5642135,
            longitude: 127.0016985
        }),
        {
            headers: {
                "Content-Type": "application/json"
            }
        }
    );

    check(res, {
        '비회원 - 현재 위치와 가까운 여행지 목록 조회 성공': (r) => r.status === 200,
    });

    if (res.status !== 200) {
        console.log(`status: ${res.status}, body: ${res.body}`);
    }

}