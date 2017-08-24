package pl.edu.uj.student.kownacki.aron.tda.batch.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by Aron Kownacki on 14.08.2017.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Tweet {

    private Long receivedAt;

    private Integer favoriteCount;

    private Integer id;

    private String user;
}
