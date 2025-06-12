package tronka.test.linking;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface LinkData {

    Optional<Integer> getInteger(UUID playerId);

    Optional<Integer> getInteger(long discordId);

    void addInteger(Integer playerLink);

    void removeInteger(Integer playerLink);

    void updateInteger(Integer playerLink);

    Stream<Integer> getIntegers();
}
