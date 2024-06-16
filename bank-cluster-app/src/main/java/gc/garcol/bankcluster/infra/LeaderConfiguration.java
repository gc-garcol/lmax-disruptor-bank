package gc.garcol.bankcluster.infra;

import gc.garcol.bankclustercore.account.AccountRepository;
import gc.garcol.bankclustercore.account.Balances;
import gc.garcol.bankclustercore.offset.SnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * @author thaivc
 * @since 2024
 */
@Profile("leader")
@Configuration
@RequiredArgsConstructor
public class LeaderConfiguration {

    private final SnapshotRepository snapshotRepository;
    private final AccountRepository accountRepository;

    @Bean
    public Balances balances() {
        var balances = new Balances();
        balances.setLastedId(accountRepository.lastedId());
        return balances;
    }

}
