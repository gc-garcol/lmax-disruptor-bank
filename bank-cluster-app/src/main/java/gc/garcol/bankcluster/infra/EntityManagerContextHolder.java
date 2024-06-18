package gc.garcol.bankcluster.infra;

import jakarta.persistence.EntityManager;

public class EntityManagerContextHolder {
    public static ThreadLocal<EntityManager> CONTEXT = new ThreadLocal<>();
}
