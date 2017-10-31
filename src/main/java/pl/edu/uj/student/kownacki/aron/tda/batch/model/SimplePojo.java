package pl.edu.uj.student.kownacki.aron.tda.batch.model;

import java.io.Serializable;

/**
 * Created by Aron Kownacki on 26.08.2017.
 */
public class SimplePojo implements Serializable{
    private Integer id;
    private String user;
    private Integer count;

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
