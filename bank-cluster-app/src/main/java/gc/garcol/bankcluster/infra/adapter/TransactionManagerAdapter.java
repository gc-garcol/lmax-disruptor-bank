package gc.garcol.bankcluster.infra.adapter;

import gc.garcol.bankclustercore.TransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author thaivc
 * @since 2024
 */
@Component
public class TransactionManagerAdapter implements TransactionManager {

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void doInNewTransaction(Runnable runnable) {
        runnable.run();
    }
}
