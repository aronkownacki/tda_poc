package pl.edu.uj.student.kownacki.aron.tda.batch.config;

import java.io.IOException;

import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongoCmdOptionsBuilder;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.process.config.IRuntimeConfig;

/**
 * Created by Aron Kownacki on 24.08.2017.
 */
@Configuration
public class PersistentEmbeddedMongoConfiguartion extends EmbeddedMongoAutoConfiguration {

    public PersistentEmbeddedMongoConfiguartion(MongoProperties properties, EmbeddedMongoProperties embeddedProperties,
                                                ApplicationContext context, IRuntimeConfig runtimeConfig) {
        super(properties, embeddedProperties, context, runtimeConfig);
    }

    @Bean
    @Override
    public IMongodConfig embeddedMongoConfiguration() throws IOException {
        IMongodConfig iMongodConfig = super.embeddedMongoConfiguration();
        return new MongodConfigBuilder()
            .cmdOptions(new MongoCmdOptionsBuilder()
                .useNoJournal(false)
                .useStorageEngine("wiredTiger")
                .defaultSyncDelay()
                .build())
            .net(iMongodConfig.net())
            .replication(iMongodConfig.replication())
            .version(iMongodConfig.version())
            .build();
    }
}
