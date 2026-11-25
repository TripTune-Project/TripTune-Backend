import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = __ENV.BASE_URL;

export const options = {
    scenarios: {
        warmup: {
            executor: 'constant-arrival-rate',
            rate: 14,
            timeUnit: '1s',
            duration: '30s',
            preAllocatedVUs: 14,
            maxVUs: 28,
            startTime: '0s',
            tags: { phase: 'warmup' },
        },
        main: {
            executor: 'constant-arrival-rate',
            rate: 28,
            timeUnit: '1s',
            duration: '2m',
            preAllocatedVUs: 28,
            maxVUs: 56,
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
    const res = http.get(
        `${BASE_URL}/api/travels/recommend?theme=culture`
    );

    check(res, {
        '테마별 추천 여행지 조회 성공': (r) => r.status === 200,
    });

    if (res.status !== 200) {
        console.log(`status: ${res.status}, body: ${res.body}`);
    }

}