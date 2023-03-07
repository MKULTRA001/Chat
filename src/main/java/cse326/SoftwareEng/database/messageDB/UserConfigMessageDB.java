/*Configuration file for the user_message database*/

package cse326.SoftwareEng.database.messageDB;
import cse326.SoftwareEng.database.messageDB.UserMessageDB;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(basePackages = "cse326.SoftwareEng.database.messageDB",
        entityManagerFactoryRef = "userEntityManagerFactoryMessageDB",
        transactionManagerRef= "userTransactionManagerMessageDB")
public class UserConfigMessageDB {


    @Bean(name = "userDataSourceMessageDB")
    @ConfigurationProperties(prefix="spring.message.datasource")
    public DataSource userDataSourceMessageDB() {
        return DataSourceBuilder.create().build();
    }

    @Bean("userEntityManagerFactoryMessageDB")
    public LocalContainerEntityManagerFactoryBean userEntityManagerFactoryMessageDB(
            EntityManagerFactoryBuilder builder,
            @Qualifier("userDataSourceMessageDB") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages(UserMessageDB.class)
                .persistenceUnit("userDBPU")
                .build();
    }

    @Bean(name = "userTransactionManagerMessageDB")
    public PlatformTransactionManager barTransactionManager(
            @Qualifier("userEntityManagerFactoryMessageDB") EntityManagerFactory userEntityManagerFactoryMessageDB) {
        return new JpaTransactionManager(userEntityManagerFactoryMessageDB);
    }
}
