package pl.edu.uj.student.kownacki.aron.tda.batch.mongo;

/**
 * Created by bartoszjedrzejewski on 31/10/2016.
 */
public class Recognition {

    public String quote;

    public Recognition(String quote) {
        this.quote = quote;
    }

    @Override
    public String toString() {
        return "Recognition{" +
            "quote='" + quote + '\'' +
            '}';
    }
}
