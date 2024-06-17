# BENCHMARK

- Query
```shell
ghz --insecure --proto ./bank-libs/bank-cluster-proto/src/main/proto/balance.proto \
--call gc.garcol.bank.proto.BalanceCommandService/sendCommand \
-d '{
    "balanceFilterQuery": {
        "id": 1,
        "correlationId": "random-uuid"
    }
}' \
-c 200 -n 100000 \
127.0.0.1:9500
```

- Deposit
```shell
ghz --insecure --proto ./bank-libs/bank-cluster-proto/src/main/proto/balance.proto \
--call gc.garcol.bank.proto.BalanceCommandService/sendCommand \
-d '{
    "depositCommand": {
        "id": 1,
        "amount": 1,
        "correlationId": "random-uuid"
    }
}' \
-c 200 -n 100000 \
127.0.0.1:9500
```
