package lk.ijse.dep10.app.model;

import java.io.Serializable;
import java.sql.Blob;

public class Student implements Serializable {
    private String id;
    private String name;
    private Blob picture;

    public Student() {
    }

    public Student(String id, String name, Blob picture) {
        this.id = id;
        this.name = name;
        this.picture = picture;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Blob getPicture() {
        return picture;
    }

    public void setPicture(Blob picture) {
        this.picture = picture;
    }
}
