package com.mcit.admissionsystem.entities;

import java.io.Serializable;

public class CSKey implements Serializable {

    private Course course;

    private Student student;

    public CSKey() {

    }

    public CSKey(Course course, Student student) {
        this.course = course;
        this.student = student;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }
}
