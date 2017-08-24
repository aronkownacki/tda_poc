package pl.edu.uj.student.kownacki.aron.tda.batch.model;

import java.io.Serializable;

public class StreamOutput implements Serializable{
    private String content;

    public StreamOutput() {
    }

    public StreamOutput(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
