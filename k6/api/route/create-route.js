import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Counter } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL;
const EMAIL = __ENV.EMAIL;
const PASSWORD = __ENV.PASSWORD;
const SCHEDULE_COUNT = Number(__ENV.SCHEDULE_COUNT || 30); // VU 숫자

http.setResponseCallback(http.expectedStatuses(200, 201, 409));

const createSuccess = new Rate('route_create_success');
const conflictCount = new Counter('route_create_conflicts');


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
        route_create_success: ['rate>0.99'],
    },
};

function authHeaders(token){
    return {
        headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${token}`,
        },
    };
}

function fetchRouteVersion(scheduleId, token){
    const res = http.get(
        `${BASE_URL}/api/schedules/${scheduleId}/routes?page=1`,
        authHeaders(token)
    );
    return res.json('data.routeVersion');
}

export function setup(){
    // 로그인
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

    const accessToken = loginRes.json('data.accessToken');

    // 일정 생성
    const scheduleIds = [];
    for(let i=0; i<SCHEDULE_COUNT; i++){
        const scheduleRes = http.post(
            `${BASE_URL}/api/schedules`,
            JSON.stringify({
                scheduleName: `k6-load-test-${i}`,
                startDate: '2026-12-01',
                endDate: '2026-12-03',
            }),
            authHeaders(accessToken)
        );

        if (scheduleRes.status === 200 || scheduleRes.status === 201){
            scheduleIds.push(scheduleRes.json('data.scheduleId'));
        } else{
            console.log(`일정 생성 실패: ${scheduleRes.status} ${scheduleRes.body}`);
        }
    }

    if(scheduleIds.length < SCHEDULE_COUNT){
        throw new Error(`일정 ${scheduleIds.length}/${SCHEDULE_COUNT}개만 생성됨`);
    }


    check(null, {
        [`일정 ${scheduleIds.length}개 생성 성공`]: () => true,
    });

    return { accessToken, scheduleIds };
}

let routeVersion = 0;

export default function(data) {
    const scheduleId = data.scheduleIds[(__VU - 1) % data.scheduleIds.length];

    const res = http.post(
        `${BASE_URL}/api/schedules/${scheduleId}/routes`,
        JSON.stringify({
            placeId: 10,
            routeVersion: routeVersion
        }),
        authHeaders(data.accessToken)
    );

    const ok = res.status === 200;
    createSuccess.add(ok);

    if (ok){
        routeVersion = res.json('data.routeVersion');
    } else if (res.status === 409) {
        conflictCount.add(1);
        console.log(`409 발생 schedule=${scheduleId} 요청버전=${routeVersion}`);
        routeVersion = fetchRouteVersion(scheduleId, data.accessToken);
    } else {
        console.log(`여행 루트 추가 실패: status: ${res.status}, body: ${res.body}`);
    }

    sleep(1);
}

export function teardown(data){
    for (const scheduleId of data.scheduleIds){
        const res = http.del(
            `${BASE_URL}/api/schedules/${scheduleId}`,
            null,
            authHeaders(data.accessToken)
        );

        if (res.status !== 200 && res.status !== 204){
            console.log(`일정 ${scheduleId} 삭제 실패: ${res.status}`);
        }
    }

    check(null, {
        "테스트 데이터 정리 완료": () => true,
    });
}