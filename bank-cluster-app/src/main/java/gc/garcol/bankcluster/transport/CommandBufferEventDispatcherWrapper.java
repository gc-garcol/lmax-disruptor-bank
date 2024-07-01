package gc.garcol.bankcluster.transport;

import gc.garcol.bank.proto.BalanceProto;
import gc.garcol.bankclustercore.BaseCommand;
import gc.garcol.bankclustercore.CommandBufferEvent;
import gc.garcol.bankclustercore.CommandBufferEventDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * @author thaivc
 * @since 2024
 */
@Profile("leader")
@Component
@RequiredArgsConstructor
public class CommandBufferEventDispatcherWrapper {

    private final CommandBufferEventDispatcher commandBufferEventDispatcher;

    public void create(String replyChannel, BalanceProto.BalanceCommand balanceCommand) {
        commandBufferEventDispatcher.dispatch(
            new CommandBufferEvent(
                replyChannel,
                balanceCommand.getCreateBalanceCommand().getCorrelationId(),
                new BaseCommand(BalanceProto.CommandLog.newBuilder()
                    .setCreateBalanceCommand(balanceCommand.getCreateBalanceCommand())
                    .build()
                )
            )
        );
    }

    public void deposit(String replyChannel, BalanceProto.BalanceCommand balanceCommand) {
        commandBufferEventDispatcher.dispatch(
            new CommandBufferEvent(
                replyChannel,
                balanceCommand.getDepositCommand().getCorrelationId(),
                new BaseCommand(BalanceProto.CommandLog.newBuilder()
                    .setDepositCommand(balanceCommand.getDepositCommand())
                    .build()
                )
            )
        );
    }

    public void withdraw(String replyChannel, BalanceProto.BalanceCommand balanceCommand) {
        commandBufferEventDispatcher.dispatch(
            new CommandBufferEvent(
                replyChannel,
                balanceCommand.getWithdrawCommand().getCorrelationId(),
                new BaseCommand(BalanceProto.CommandLog.newBuilder()
                    .setWithdrawCommand(balanceCommand.getWithdrawCommand())
                    .build()
                )
            )
        );
    }

    public void transfer(String replyChannel, BalanceProto.BalanceCommand balanceCommand) {
        commandBufferEventDispatcher.dispatch(
            new CommandBufferEvent(
                replyChannel,
                balanceCommand.getTransferCommand().getCorrelationId(),
                new BaseCommand(BalanceProto.CommandLog.newBuilder()
                    .setTransferCommand(balanceCommand.getTransferCommand())
                    .build()
                )
            )
        );
    }
}
