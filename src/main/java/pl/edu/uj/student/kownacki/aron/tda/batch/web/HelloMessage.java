package pl.edu.uj.student.kownacki.aron.tda.batch.web;

/**
 * Created by Aron Kownacki on 10.07.2017.
 */
public class HelloMessage {
    private String name;

    public HelloMessage() {
    }

    public HelloMessage(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
