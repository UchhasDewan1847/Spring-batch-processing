package com.agent47.springbatch.config;

import com.agent47.springbatch.entity.Student;
import org.springframework.batch.item.ItemProcessor;

public class StudentProcessor implements ItemProcessor<Student,Student> {
    @Override
    public Student process(Student student) throws Exception {
        // all the business logic codes belong here, and for the sake of simplicity we are keeping the data as it is
        student.setId(null);
        return student;
    }
}
