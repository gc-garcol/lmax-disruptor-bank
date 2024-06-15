package gc.garcol.bankclustercore;

/**
 * @author thaivc
 * @since 2024
 */
public interface CommandHandler {

    BaseResult onCommand(BaseCommand command);

}
