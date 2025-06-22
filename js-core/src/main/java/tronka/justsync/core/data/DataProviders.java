package tronka.justsync.core.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
public class DataProviders {
    private static final List<DataSource<?, ?>> SOURCES = new ArrayList<>();

    public static final DataSource<UUID, String> USERNAME = registerProvider();

    private static <T, R> DataSource<T, R> registerProvider() {
        DataSource<T, R> source = new DataSource<>();
        SOURCES.add(source);
        return source;
    }
}
