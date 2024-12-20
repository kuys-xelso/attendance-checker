package com.example.ccisattendancechecker;

public class StudentNameClass {

    String studentName;
    String status;

    public StudentNameClass() {}

    public StudentNameClass(String studentName, String status) {
        this.studentName = studentName;
        this.status = status;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
