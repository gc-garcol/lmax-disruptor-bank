package gc.garcol.bankclustercore;

import gc.garcol.bank.proto.BalanceProto;
import gc.garcol.bankclustercore.account.BalanceResult;
import gc.garcol.bankclustercore.account.Balances;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author thaivc
 * @since 2024
 */
@Slf4j
@RequiredArgsConstructor
public class CommandHandlerImpl implements CommandHandler {

    private final Balances balances;

    @Override
    public BaseResult onCommand(BaseCommand command) {
        return switch (command.getCommandLog().getTypeCase()) {
            case CREATEBALANCECOMMAND -> handle(command.getCommandLog().getCreateBalanceCommand());
            case DEPOSITCOMMAND -> handle(command.getCommandLog().getDepositCommand());
            case WITHDRAWCOMMAND -> handle(command.getCommandLog().getWithdrawCommand());
            case TRANSFERCOMMAND -> handle(command.getCommandLog().getTransferCommand());
            case TYPE_NOT_SET -> BaseResultError.COMMAND_NOT_FOUND;
        };
    }

    private BalanceResult handle(BalanceProto.CreateBalanceCommand command) {
        balances.newBalance();
        return BalanceResult.of("Create balance success", 0);
    }

    private BalanceResult handle(BalanceProto.DepositCommand command) {
        balances.deposit(command.getId(), command.getAmount());
        return BalanceResult.of("Deposit success", 0);
    }

    private BalanceResult handle(BalanceProto.WithdrawCommand command) {
        balances.withdraw(command.getId(), command.getAmount());
        return BalanceResult.of("Withdraw success", 0);
    }

    private BalanceResult handle(BalanceProto.TransferCommand command) {
        balances.transfer(command.getFromId(), command.getToId(), command.getAmount());
        return BalanceResult.of("Transfer success", 0);
    }
}
