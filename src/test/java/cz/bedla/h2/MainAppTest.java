package cz.bedla.h2;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import cz.bedla.h2.entity.*;
import cz.bedla.h2.repository.ActivityRepository;
import cz.bedla.h2.repository.LeadRepository;
import cz.bedla.h2.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.datasource.embedded.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.sql.Driver;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigurationPackage
@DataJpaTest
public class MainAppTest {
    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        transactionTemplate = newTransactionTemplateWithPropagationRequiresNew(platformTransactionManager);
    }

    @Test
    public void name() {
        final Long userId = transactionTemplate.execute(status -> userRepository.save(new User("user")).getId());

        final Long leadId = createLead(userId);
    }

    @Test
    void concurrency() throws InterruptedException {
        final Long userId = transactionTemplate.execute(status -> userRepository.save(new User("user2")).getId());

        final ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newWorkStealingPool());
        final List<ListenableFuture<Long>> work = IntStream.range(0, 1000)
                .mapToObj(i -> (Callable<Long>) () -> createLead(userId))
                .map(executorService::submit)
                .collect(toList());
        final List<Long> result = Futures.getUnchecked(
                Futures.whenAllSucceed(work)
                        .call(() -> work.stream().map(Futures::getUnchecked).collect(toList()), executorService));
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
        assertThat(newHashSet(result))
                .hasSize(result.size())
                .containsAll(result);
    }

    public Long createLead(Long userId) {
        return transactionTemplate.execute(status -> {
            final Long leadId = transactionTemplate.execute(status2 -> {
                final Lead entity = new Lead();
                return leadRepository.saveAndFlush(entity).getId();
            });

            final User user = userRepository.findById(userId).orElseThrow(IllegalStateException::new);
            final Lead entity = leadRepository.findById(leadId).orElseThrow(IllegalStateException::new);
            entity.getActivities().add(activityRepository.saveAndFlush(new ActivityUser(user, nextSequenceValue("seq_timeline"))));
            entity.getActivities().add(activityRepository.saveAndFlush(new ActivityA("A" + UUID.randomUUID().toString(), nextSequenceValue("seq_timeline"))));
            entity.getActivities().add(activityRepository.saveAndFlush(new ActivityB("B" + UUID.randomUUID().toString(), nextSequenceValue("seq_timeline"))));
            entity.getActivities().add(activityRepository.saveAndFlush(new ActivityC("C" + UUID.randomUUID().toString(), nextSequenceValue("seq_timeline"))));
            return leadRepository.saveAndFlush(entity).getId();
        });
    }

    public static TransactionTemplate newTransactionTemplateWithPropagationRequiresNew(PlatformTransactionManager platformTransactionManager) {
        final TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transactionTemplate.afterPropertiesSet();
        return transactionTemplate;
    }

    private long nextSequenceValue(String sequenceName) {
        final String sql = String.format("SELECT %s.NEXTVAL FROM DUAL", sequenceName);
        final Object resultObj = entityManager.createNativeQuery(sql).getSingleResult();
        if (resultObj instanceof Number) {
            return ((Number) resultObj).longValue();
        } else {
            throw new IllegalStateException(String.format("Invalid result from %s sequence: %s", sequenceName, resultObj));
        }
    }

    @Configuration
    public static class Config {
        @Bean(destroyMethod = "shutdown")
        public EmbeddedDatabase dataSource() {
            final SimpleDriverDataSource dataSource = new SimpleDriverDataSource();

            return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.H2)
                    .setDataSourceFactory(new DataSourceFactory() {
                        @Override
                        public ConnectionProperties getConnectionProperties() {
                            return new ConnectionProperties() {
                                @Override
                                public void setDriverClass(Class<? extends Driver> driverClass) {
                                    dataSource.setDriverClass(driverClass);
                                }

                                @Override
                                public void setUrl(String url) {
                                    dataSource.setUrl(url);
                                }

                                @Override
                                public void setUsername(String username) {
                                    dataSource.setUsername(username);
                                }

                                @Override
                                public void setPassword(String password) {
                                    dataSource.setPassword(password);
                                }
                            };
                        }

                        ;

                        @Override
                        public DataSource getDataSource() {
                            return dataSource;
                        }
                    })
                    .build();
        }
    }
}
