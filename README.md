# BANK-APP
A simple high performance bank application

## Technologies
- `Java 21`
- `Lmax Disruptor`
- `Kafka`
- `Protobuf`

## Architecture
![high level design](./docs/bank-app-v1.0.0.png)

## Project structure
- `cluster-core`: Domain logic.
- `cluster`: Implements `cluster-core` and provides transport-layer (ex: grpc), framework-layer.
- `client-core`: Provides interfaces for communicating with `clusters`, provides transport-layer (ex: grpc),
- `client`: Implements `client-core` and provides framework-layer.
## Show helps
```shell
make help
```
