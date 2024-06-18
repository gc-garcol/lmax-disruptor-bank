# BANK-APP
A simple high performance bank application

## Technologies
- `Java 21`
- `Lmax Disruptor`
- `Kafka`
- `Protobuf` serde
- `Grpc`

## Features

### Cluster core features
- Journaling command logs.
- Replaying command logs.
- Snapshotting state machine.
- Providing domain logic.

### Business features
- `Admin`:
  - [ ] Create balance.
  - [ ] List all balances.
  - [ ] Get balance by id.
  - [ ] Update balance.
- `User`:
  - [ ] Get current balance.
  - [ ] Deposit money.
  - [ ] Withdraw money.
  - [ ] Transfer money.

## Architecture
### High-level design
![high level design](./docs/bank-app-v1.0.0.png)

### Cluster structure
![cluster-ddd.png](./docs/cluster-ddd.png)

- [X] cluster-core: domain logic.
- [X] cluster-app: framework layer.

## Project structure
- `cluster-core`: Domain logic.
- `cluster-app`: Implements `cluster-core` and provides transport-layer (ex: grpc), framework-layer.
- `client`: Interacts with `cluster-app`, providers `api-resource`.

## Show helps
```shell
make help
```

## FAQ
### How to test grpc endpoint?
In order to test grpc server, you can use portman to send message like this

![grpc-query.png](docs/examples/grpc-query.png)

### How to benchmark grpc server?
We use `ghz`([link](https://github.com/bojand/ghz)) as a benchmarking and load testing tool.

```shell
ghz --insecure --proto ./bank-libs/bank-cluster-proto/src/main/proto/balance.proto \
--call gc.garcol.bank.proto.BalanceQueryService/sendQuery \
-d '{"balanceFilterQuery": {"id": 1,"correlationId": "random-uuid"}}' \
-c 200 -n 100000 \
127.0.0.1:9500
```

See [BENCHMARK](./README_benchmark.md) for more details.
