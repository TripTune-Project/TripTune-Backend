import http from 'k6/http';
import { check } from 'k6';
import { Rate, Counter } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL;
const EMAIL = __ENV.EMAIL;
const PASSWORD = __ENV.PASSWORD;
const SCHEDULE_COUNT = 150      // warmup maxVUs + main maxVUs

http.setResponseCallback(http.expectedStatuses(200, 201, 409));

const createSuccess = new Rate('route_create_success');
const conflictCount = new Counter('route_create_conflicts');


export const options = {
    setupTimeout: '900s',
    scenarios: {
        warmup: {
            executor: 'constant-arrival-rate',
            rate: 25,
            timeUnit: '1s',
            duration: '30s',
            preAllocatedVUs: 25,
            maxVUs: 50,
            startTime: '0s',
            tags: { phase: 'warmup' },
        },
        main: {
            executor: 'constant-arrival-rate',
            rate: 50,
            timeUnit: '1s',
            duration: '2m',
            preAllocatedVUs: 50,
            maxVUs: 100,
            startTime: '35s',
            tags: { phase: 'main' },
        },
    },
    thresholds: {
        'http_req_failed{phase:main}': ['rate<0.01'],
        'http_req_duration{phase:main}': ['p(95)<500'],
        'route_create_success{phase:main}': ['rate>0.99'],
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

}

export function teardown(data) {
    let successCount = 0;

    for (const scheduleId of data.scheduleIds) {
        const res = http.del(
            `${BASE_URL}/api/schedules/${scheduleId}`,
            null,
            authHeaders(data.accessToken)
        );

        if (res.status === 200 || res.status === 204) {
            successCount++;
        } else {
            console.log(`일정 ${scheduleId} 삭제 실패: ${res.status}`);
        }
    }

    check(successCount, {
        [`테스트 데이터 정리 성공 (${data.scheduleIds.length}개)`]:
            (count) => count === data.scheduleIds.length,
    });
}