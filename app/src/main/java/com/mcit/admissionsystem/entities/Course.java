package com.mcit.admissionsystem.entities;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Course implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private Professor professor;

    private List<CS> courseStudents;

    private Date startDate;

    private Date endDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Professor getProfessor() {
        return professor;
    }

    public void setProfessor(Professor professor) {
        this.professor = professor;
    }

    public List<CS> getCourseStudents() {
        return courseStudents;
    }

    public void setCourseStudents(List<CS> courseStudents) {
        this.courseStudents = courseStudents;
    }

}
