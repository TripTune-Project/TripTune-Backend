import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL;

export const options = {
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

    sleep(1);
}