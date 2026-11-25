import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = __ENV.BASE_URL;

export const options = {
    scenarios: {
        warmup: {
            executor: 'constant-arrival-rate',
            rate: 3,
            timeUnit: '1s',
            duration: '30s',
            preAllocatedVUs: 3,
            maxVUs: 6,
            startTime: '0s',
            tags: { phase: 'warmup'},
        },
        main: {
            executor: 'constant-arrival-rate',
            rate: 5,
            timeUnit: '1s',
            duration: '2m',
            preAllocatedVUs: 5,
            maxVUs: 10,
            startTime: '35s',
            tags: { phase: 'main'},
        },
    },
    thresholds: {
        'http_req_failed{phase:main}': ['rate<0.01'],
        'http_req_duration{phase:main}': ['p(95)<500'],
    },
};


export default function() {
    const res = http.post(
        `${BASE_URL}/api/travels/search?page=1`,
        JSON.stringify({
            latitude: 37.5642135,
            longitude: 127.0016985,
            keyword: '서울'
        }),
        {
            headers: {
                "Content-Type": "application/json"
            }
        }
    );

    check(res, {
        '비회원 - 현재 위치를 포함한 검색 성공': (r) => r.status === 200,
    });

    if (res.status !== 200) {
        console.log(`status: ${res.status}, body: ${res.body}`);
    }

}