package cz.bedla.h2;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResourceLoader;

import java.sql.*;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

class MainAppTest {
    private JdbcDataSource dataSource;

    @Test
    void concurrency() throws InterruptedException, LiquibaseException {
        createDataSource();
        initDatabaseSchema();

        final long userId = createUser("user2");

        final ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newWorkStealingPool());
        final List<ListenableFuture<Long>> work = IntStream.range(0, 1000)
                .mapToObj(i -> (Callable<Long>) () -> create(i, userId))
                .map(executorService::submit)
                .collect(toList());
        final List<Object> result = Futures.getUnchecked(
                Futures.whenAllComplete(work)
                        .call(() -> work.stream().map(future -> {
                            try {
                                return Futures.getUnchecked(future);
                            } catch (Exception e) {
                                return e;
                            }
                        }).collect(toList()), executorService));
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        final List<Long> ids = result.stream()
                .filter(Long.class::isInstance)
                .map(Long.class::cast)
                .collect(toList());
        final List<Exception> exceptions = result.stream()
                .filter(Exception.class::isInstance)
                .map(Exception.class::cast)
                .collect(toList());
        assertThat(exceptions)
                .hasSize(0);
        assertThat(ids)
                .hasSize(1000);
    }

    private void initDatabaseSchema() throws LiquibaseException {
        final SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setChangeLog("classpath:/db/changelog/db.changelog-master.xml");
        liquibase.setDataSource(dataSource);
        liquibase.setResourceLoader(new FileSystemResourceLoader());
        liquibase.afterPropertiesSet();
    }

    private void createDataSource() {
        final String dbName = UUID.randomUUID().toString();
        dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        dataSource.setUser("sa");
        dataSource.setPassword("");
    }

    private Long create(int idx, long userId) {
        try {
            try (Connection connection = dataSource.getConnection()) {
                final long leadId = createLead();
                createActivityUser(leadId, userId, connection);
                createActivityA(leadId, connection);
                createActivityB(leadId, connection);
                createActivityC(leadId, connection);
                connection.commit();
                System.out.println("create " + idx);
                return leadId;
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        } catch (Exception e) {
            System.out.println("create " + idx + "\t" + e.getMessage());
            throw e;
        }
    }

    private static long nextSequenceValue(Connection connection, String sequenceName) {
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(String.format("SELECT %s.NEXTVAL FROM DUAL", sequenceName))) {
                if (resultSet.next()) {
                    return resultSet.getLong(1);
                } else {
                    throw new IllegalStateException("no value from seq");
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private long createLead() {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO lead (version) VALUES(1)", Statement.RETURN_GENERATED_KEYS)) {
                statement.executeUpdate();
                connection.commit();
                return generatedKey(statement);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private long createUser(String name) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO app_user (login) VALUES(?)", Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, name);
                statement.executeUpdate();
                connection.commit();
                return generatedKey(statement);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private static long createActivityUser(long leadId, long userId, Connection connection) {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO lead_activity " +
                "(lead_id, discriminator, timeline, assign_rule_user_id) " +
                "VALUES(?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, leadId);
            statement.setString(2, "ASSIGN_RULE_USER");
            statement.setLong(3, nextSequenceValue(connection, "seq_timeline"));
            statement.setLong(4, userId);
            statement.executeUpdate();
            return generatedKey(statement);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private static long createActivityA(long leadId, Connection connection) {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO lead_activity " +
                "(lead_id, discriminator, timeline, a_value) " +
                "VALUES(?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, leadId);
            statement.setString(2, "A");
            statement.setLong(3, nextSequenceValue(connection, "seq_timeline"));
            statement.setString(4, UUID.randomUUID().toString());
            statement.executeUpdate();
            return generatedKey(statement);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private static long createActivityB(long leadId, Connection connection) {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO lead_activity " +
                "(lead_id, discriminator, timeline, b_value) " +
                "VALUES(?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, leadId);
            statement.setString(2, "B");
            statement.setLong(3, nextSequenceValue(connection, "seq_timeline"));
            statement.setString(4, UUID.randomUUID().toString());
            statement.executeUpdate();
            return generatedKey(statement);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private static long createActivityC(long leadId, Connection connection) {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO lead_activity " +
                "(lead_id, discriminator, timeline, c_value) " +
                "VALUES(?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, leadId);
            statement.setString(2, "C");
            statement.setLong(3, nextSequenceValue(connection, "seq_timeline"));
            statement.setString(4, UUID.randomUUID().toString());
            statement.executeUpdate();
            return generatedKey(statement);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private static long generatedKey(Statement statement) throws SQLException {
        try (final ResultSet generatedKeys = statement.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getLong(1);
            } else {
                throw new IllegalStateException("No generated keys");
            }
        }
    }
}
