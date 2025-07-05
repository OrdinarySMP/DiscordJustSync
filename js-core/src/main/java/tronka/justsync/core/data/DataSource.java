package tronka.justsync.core.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class DataSource<T, R> {
    private List<Function<T, Optional<R>>> providers = new ArrayList<>();

    public void addProvider(Function<T, Optional<R>> provider) {
        this.providers.add(provider);
    }

    public void clear() {
        this.providers.clear();
    }

    public Optional<R> getValue(T param) {
        for (Function<T, Optional<R>> provider : this.providers) {
            Optional<R> value = provider.apply(param);
            if (value.isPresent()) {
                return value;
            }
        }
        return Optional.empty();
    }
}
