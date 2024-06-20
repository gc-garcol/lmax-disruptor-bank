# BENCHMARK

## Using simple code for benchmarking the `leader` node
We open only one grpc connection from the benchmark tool to the `leader` node.
(In real-world, one client-app must open only one connection to the `leader` node.)

Start leader
```shell
make run-leader
```

Start benchmark tool
```shell
make run-benchmark-tool
```

Warming up
```shell
curl --location --request POST 'http://localhost:8900/api/balance-benchmark/warmup/1000'
```

Benchmark deposit
```shell
curl --location --request POST 'http://localhost:8900/api/balance-benchmark/benchmark/100000'
```

## Using Ghz
We open many grpc connections to the `leader` node. 

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
