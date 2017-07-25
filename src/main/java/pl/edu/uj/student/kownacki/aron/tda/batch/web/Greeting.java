package pl.edu.uj.student.kownacki.aron.tda.batch.web;

import java.io.Serializable;

/**
 * Created by Aron Kownacki on 10.07.2017.
 */
public class Greeting implements Serializable{
    private String content;

    public Greeting() {
    }

    public Greeting(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
