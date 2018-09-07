package pl.edu.uj.student.kownacki.aron.tda.batch.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties("twitter.oauth")
@Validated
public class TwitterAccountConfigProperties {
    @NotNull
    private String consumerKey;
    @NotNull
    private String consumerSecret;
    @NotNull
    private String accessToken;
    @NotNull
    private String accessTokenSecret;
}
