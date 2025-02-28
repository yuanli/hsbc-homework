import http from 'k6/http';

import {check, sleep} from 'k6';

export let options = {
    vus: 100, // 100 个虚拟用户
    iterations: 10000, // 总共 1000 次迭代 (100 用户 * 10 次)
};

function createRecord(idx) {

    const vuId = __VU;
    const payload = JSON.stringify({
        clientTransactionId: `clientTransactionId${idx}-${vuId}-${Math.random()}`,
        fromAccountId: `fromAccountId${idx}-${vuId}`,
        toAccountId: `toAccountId${idx}-${vuId}`,
        amount: 100,
        currency: 'USD',
        remark: `remark${idx}-${vuId}`,
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const res = http.post('http://localhost:8000/api/transaction/create', payload, params);
    const body = JSON.parse(res.body);
    check(res, {
        'create: status 200': (r) => r.status === 200,
    });
    check(body, {
        'create: code 0': (r) => r.code === 0,
    })
    return body.data.id
}


function deleteRecord(recordId) {
    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };
    const res = http.del(`http://localhost:8000/api/transaction/delete?id=${recordId}`, params);
    const body = JSON.parse(res.body);

    check(res, {
        'delete: status 200': (r) => r.status === 200,
    });
    check(body, {
        'delete: code 0': (r) => r.code === 0,
    })
}

function getTransaction(id) {
    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };
    const res = http.get(`http://localhost:8000/api/transaction/getById?id=${id}`, params);
    const body = JSON.parse(res.body);

    check(res, {
        'get: status 200': (r) => r.status === 200,
    });
    check(body, {
        'get: code 0': (r) => r.code === 0,
    })
}

function modifyTransaction(id, versionId) {
    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };
    const res = http.put(`http://localhost:8000/api/transaction/modify`, JSON.stringify({
        transactionId: id,
        version: versionId,
        remark: "modified"
    }), params);
    const body = JSON.parse(res.body);
    check(res, {
        'modify: status 200': (r) => r.status === 200,
    });
    check(body, {
        'modify: code is 0': (r) => r.code === 0,
    })
}

/**
 * 模拟单个用户的行为
 */
export default function () {

    let ids = []
    /**
     * 创建10个transaction
     */
    for (let i = 0; i < 10; i++) {
        ids.push(createRecord(__ITER))
    }


    /**
     * 获取transaction
     */
    for (let i = 0; i < 10; i++) {
        getTransaction(ids[i])
    }

    for (let i = 0; i < 10; i++) {
        modifyTransaction(ids[i], 1)
    }

    /**
     * 删除一个transaction
     */
    deleteRecord(ids[0])
}