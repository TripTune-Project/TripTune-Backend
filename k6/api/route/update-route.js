import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Counter } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL;
const EMAIL = __ENV.EMAIL;
const PASSWORD = __ENV.PASSWORD;
const SCHEDULE_COUNT = 30;      // VU 숫자
const ROUTE_COUNT = 3;
const PLACE_IDS = [1, 2, 3];

http.setResponseCallback(http.expectedStatuses(200, 201, 409));

const updateSuccess = new Rate('route_update_success');
const conflictCount = new Counter('route_update_conflicts');


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
        route_update_success: ['rate>0.99'],
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
    // ====== 로그인 ======
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

    // ====== 일정 생성 ======
    const schedules = [];

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

        if (scheduleRes.status !== 200 && scheduleRes.status !== 201){
            throw new Error(`일정 생성 실패: ${scheduleRes.status} ${scheduleRes.body}`)
        }

        const scheduleId = scheduleRes.json('data.scheduleId');

        // ====== 여행 루트 생성 ======
        let routeVersion = 0;
        const routeIds = [];

        for(let j=0; j<ROUTE_COUNT; j++){
            const routeRes = http.post(
                `${BASE_URL}/api/schedules/${scheduleId}/routes`,
                JSON.stringify({
                    placeId: PLACE_IDS[j % PLACE_IDS.length],
                    routeVersion: routeVersion
                }),
                authHeaders(accessToken)
            );
            if (routeRes.status !== 200 && routeRes.status !== 201){
                throw new Error(`루트 생성 실패: ${routeRes.status} ${routeRes.body}`)
            }

            routeIds.push(routeRes.json('data.routeId'));
            routeVersion = routeRes.json('data.routeVersion');
        }

        schedules.push({ scheduleId, routeIds, routeVersion });
    }


    check(null, {
        [`일정 ${schedules.length}개 / 루트 ${schedules.length * ROUTE_COUNT}개 생성 성공`]: () => true,
    });

    return { accessToken, schedules };
}

let routeVersion = null;
let reversed = false;

export default function(data) {
    const s = data.schedules[(__VU - 1) % data.schedules.length];

    if (routeVersion === null){
        routeVersion = s.routeVersion;
    }

    const ordered = reversed ? [...s.routeIds].reverse() : s.routeIds;

    const res = http.patch(
        `${BASE_URL}/api/schedules/${s.scheduleId}/routes`,
        JSON.stringify({
            routeVersion: routeVersion,
            orderedRouteIds: ordered
        }),
        authHeaders(data.accessToken)
    );

    const ok = res.status === 200;
    updateSuccess.add(ok);

    if (ok){
        routeVersion = res.json('data.routeVersion');
        reversed = !reversed;
    } else if (res.status === 409) {
        conflictCount.add(1);
        console.log(`409 발생 schedule=${s.scheduleId} 요청버전=${routeVersion}`);
        routeVersion = fetchRouteVersion(s.scheduleId, data.accessToken);
    } else {
        console.log(`여행 루트 수정 실패: status: ${res.status}, body: ${res.body}`);
    }

    sleep(1);
}

export function teardown(data){
    for (const s of data.schedules){
        const res = http.del(
            `${BASE_URL}/api/schedules/${s.scheduleId}`,
            null,
            authHeaders(data.accessToken)
        );

        if (res.status !== 200 && res.status !== 204){
            console.log(`일정 ${s.scheduleId} 삭제 실패: ${res.status}`);
        }
    }

    check(null, {  "테스트 데이터 정리 완료": () => true });
}