package gc.garcol.bankclustercore;

import gc.garcol.bank.proto.BalanceProto;
import gc.garcol.bankclustercore.account.BalanceResult;
import gc.garcol.bankclustercore.account.Balances;
import gc.garcol.common.exception.Bank4xxException;
import gc.garcol.common.exception.Bank5xxException;
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
        try {
            return switch (command.getCommandLog().getTypeCase()) {
                case CREATEBALANCECOMMAND -> handle(command.getCommandLog().getCreateBalanceCommand());
                case DEPOSITCOMMAND -> handle(command.getCommandLog().getDepositCommand());
                case WITHDRAWCOMMAND -> handle(command.getCommandLog().getWithdrawCommand());
                case TRANSFERCOMMAND -> handle(command.getCommandLog().getTransferCommand());
                case TYPE_NOT_SET -> BaseResultError.COMMAND_NOT_FOUND;
            };
        } catch (Bank4xxException e) {
            log.error("4xx Exception", e);
            return BalanceResult.of(e.getMessage(), 400);
        } catch (Bank5xxException e) {
            log.error("5xx Exception", e);
            return BalanceResult.of(e.getMessage(), 500);
        } catch (Exception e) {
            log.error("Unknown exception", e);
            return BalanceResult.of("Unknown exception", 500);
        }
    }

    private BalanceResult handle(BalanceProto.CreateBalanceCommand command) {
        long id = balances.newBalance();
        return BalanceResult.of("Create balance success::" + id, 0);
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
