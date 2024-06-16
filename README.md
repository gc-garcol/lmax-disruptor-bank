# BANK-APP
A simple high performance bank application

## Technologies
- `Java 21`
- `Lmax Disruptor`
- `Kafka`
- `Protobuf`

## Architecture
### High-level design
![high level design](./docs/bank-app-v1.0.0.png)

### Cluster structure
![cluster-ddd.png](./docs/cluster-ddd.png)

- [X] cluster-core: domain logic.
- [X] cluster-app: framework layer.

## Project structure
- `cluster-core`: Domain logic.
- `cluster`: Implements `cluster-core` and provides transport-layer (ex: grpc), framework-layer.
- `client-core`: Provides interfaces for communicating with `clusters`, provides transport-layer (ex: grpc),
- `client`: Implements `client-core` and provides framework-layer.
## Show helps
```shell
make help
```
