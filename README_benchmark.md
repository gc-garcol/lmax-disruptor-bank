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
ghz \
--insecure \
--skipFirst 100000 \
--proto ./bank-libs/bank-cluster-proto/src/main/proto/balance.proto \
--call gc.garcol.bank.proto.BalanceCommandService/sendCommand \
-d '{
    "depositCommand": {
        "id": 1,
        "amount": 1,
        "correlationId": "random-uuid"
    }
}' \
-c 200 -n 600000 \
127.0.0.1:9500
```

Results:
- Latency: p50 = 5.65 ms, p90 = 10.00 ms, p99 = 17.39 ms
- Throughput: 21854.27 RPS

```shell
Summary:
  Count:        500000
  Total:        22.88 s
  Slowest:      58.71 ms
  Fastest:      0.13 ms
  Average:      6.21 ms
  Requests/sec: 21854.27

Response time histogram:
  0.128  [1]      |
  5.986  [272885] |∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎
  11.845 [203033] |∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎
  17.704 [19343]  |∎∎∎
  23.562 [2365]   |
  29.421 [1462]   |
  35.279 [797]    |
  41.138 [96]     |
  46.997 [13]     |
  52.855 [4]      |
  58.714 [1]      |

Latency distribution:
  10 % in 2.77 ms 
  25 % in 4.05 ms 
  50 % in 5.65 ms 
  75 % in 7.72 ms 
  90 % in 10.00 ms 
  95 % in 11.75 ms 
  99 % in 17.39 ms 

Status code distribution:
  [OK]   500000 responses  
```

## Benchmark client-nodes using `autocannon`
```shell
autocannon -c 16 -d 20 -m POST -H 'Content-Type: application/json' \
-b '{"id":"1","amount":"1"}' \
http://localhost:8900/api/v1/balance/command/deposit
```
