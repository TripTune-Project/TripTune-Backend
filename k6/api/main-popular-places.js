import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL;

export const options = {
    vus : 100,
    duration: '1m',
};


export default function() {
    const res = http.get(`${BASE_URL}/api/travels/popular?city=seoul`);

    check(res, {
        '응답 코드가 200인가?': (r) => r.status === 200,
    });

    sleep(1);
}