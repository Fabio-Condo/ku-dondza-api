package com.fabiocondo.domain;

import com.fabiocondo.enumeration.ExamType;
import com.fabiocondo.enumeration.Institution;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "exame")
public class Exam implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable=false)
    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ExamType examType;

    @Enumerated(EnumType.STRING)
    private Institution institution;

    private boolean premium = false; // Controle dos exames para users pagos e nao pagos

    private String fileName;

    private String urlFile;

    private Date date;

    private Long totalDownloadNumber;

    private String number; // Se for UEM

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;

    public Exam() {
    }

    public Exam(Long id, Subject subject, ExamType examType, boolean premium, String fileName, String urlFile, Date date, Long totalDownloadNumber) {
        this.id = id;
        this.subject = subject;
        this.examType = examType;
        this.premium = premium;
        this.fileName = fileName;
        this.urlFile = urlFile;
        this.date = date;
        this.totalDownloadNumber = totalDownloadNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public ExamType getExamType() {
        return examType;
    }

    public void setExamType(ExamType examType) {
        this.examType = examType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public boolean isPremium() {
        return premium;
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
    }

    public String getUrlFile() {
        return urlFile;
    }

    public void setUrlFile(String urlFile) {
        this.urlFile = urlFile;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Long getTotalDownloadNumber() {
        return totalDownloadNumber;
    }

    public void setTotalDownloadNumber(Long totalDownloadNumber) {
        this.totalDownloadNumber = totalDownloadNumber;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}