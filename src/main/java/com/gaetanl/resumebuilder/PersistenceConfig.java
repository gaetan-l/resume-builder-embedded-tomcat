package com.gaetanl.resumebuilder;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * {@literal @}EnableTransactionManagement  not needed, by default, methods
 * inherited from CrudRepository inherit the transactional configuration from
 * SimpleJpaRepository.
 */
@Configuration
public class PersistenceConfig {
    public static volatile EntityManagerFactory emf = null;

    /**
     * This function returns a TransactionManager bean, it is mandatory and
     * must be named "transactionManager", or be defined in the
     * {@literal @}EnableJpaRepositories annotation,
     * e.g. {@literal @}EnableJpaRepositories(transactionManagerRef="myCustomNameTxManager")
     * Cf. EnableJpaRepositories.transactionManagerRef()
     */
    @Bean
    public PlatformTransactionManager transactionManager() {
        return new JpaTransactionManager(entityManagerFactory());
    }

    /**
     * This function returns an EntityManagerFactory bean, it is mandatory and
     * must be named "entityManagerFactory", or be defined in the
     * {@literal @}EnableJpaRepositories annotation,
     * i.e. @EnableJpaRepositories(entityManagerFactory="myCustomNameEmf")
     * Cf. EnableJpaRepositories.entityManagerFactoryRef()
     */
    @Bean
    public EntityManagerFactory entityManagerFactory() {
        if (emf == null) {
            synchronized (EntityManagerFactory.class) {
                if (emf == null) {
                    final StandardServiceRegistry sr = new StandardServiceRegistryBuilder()
                            .configure() // Configures setting from hibernate.cfg.xml
                            .build();
                    try {
                        emf = new MetadataSources(sr).buildMetadata().buildSessionFactory();
                    }
                    catch (Exception e) {
                        StandardServiceRegistryBuilder.destroy(sr);
                        throw e;
                    }
                }
            }
        }
        return emf;
    }
}