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

Benchmark deposit
```shell
curl --location --request POST 'http://localhost:8900/api/balance-benchmark/benchmark/500000/64'
```

Result of sending 500,000 write requests with 64 grpc connections to cluster:

<img style="width: 400px; max-width: 100vw; border: 2px solid grey;" src="./docs/benchmark/simple-benchmark.png" alt="simple benchmark">

## Using Ghz for benchmarking the `cluster` nodes (`leader`, `follower`)
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
autocannon \
--warmup [ -c 32 -d 20 ] \
-c 32 -d 30 \
-m POST \
-H 'Content-Type: application/json' \
-b '{"id":"1","amount":"1"}' \
http://localhost:8900/api/v1/balance/command/deposit
```

```shell
┌─────────┬──────┬────────┬────────┬────────┬───────────┬───────┬─────────┐
│ Stat    │ 2.5% │ 50%    │ 97.5%  │ 99%    │ Avg       │ Stdev │ Max     │
├─────────┼──────┼────────┼────────┼────────┼───────────┼───────┼─────────┤
│ Latency │ 8 ms │ 140 ms │ 326 ms │ 359 ms │ 147.04 ms │ 99 ms │ 1298 ms │
└─────────┴──────┴────────┴────────┴────────┴───────────┴───────┴─────────┘
┌───────────┬─────────┬─────────┬────────┬─────────┬───────────┬────────┬─────────┐
│ Stat      │ 1%      │ 2.5%    │ 50%    │ 97.5%   │ Avg       │ Stdev  │ Min     │
├───────────┼─────────┼─────────┼────────┼─────────┼───────────┼────────┼─────────┤
│ Req/Sec   │ 9,079   │ 9,079   │ 10,287 │ 12,375  │ 10,478.94 │ 848.02 │ 9,072   │
├───────────┼─────────┼─────────┼────────┼─────────┼───────────┼────────┼─────────┤
│ Bytes/Sec │ 1.94 MB │ 1.94 MB │ 2.2 MB │ 2.65 MB │ 2.24 MB   │ 181 kB │ 1.94 MB │
└───────────┴─────────┴─────────┴────────┴─────────┴───────────┴────────┴─────────┘

Req/Bytes counts sampled once per second.
# of samples: 30

317k requests in 30.03s, 67.2 MB read
```

The results of the test indicate that the `client-app` is capable of handling 10,000 write requests per second.
Given the cluster's maximum capacity of 60,000 write requests per second,
we can achieve this throughput by scaling the `client-app` to 6 instances.
This will ensure that we fully utilize the cluster's capacity.
