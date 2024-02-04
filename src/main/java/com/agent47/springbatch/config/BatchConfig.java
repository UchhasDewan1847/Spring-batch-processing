package com.agent47.springbatch.config;

import com.agent47.springbatch.entity.Student;
import com.agent47.springbatch.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {
    private final StudentRepository studentRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final JobRepository jobRepository;
    @Bean
    public FlatFileItemReader<Student> itemReader(){
        FlatFileItemReader<Student> flatFileItemReader = new FlatFileItemReader<>();
        flatFileItemReader.setResource(new FileSystemResource("src/main/resources/students.csv"));
        flatFileItemReader.setName("CsvReader");
        flatFileItemReader.setLinesToSkip(1);
        flatFileItemReader.setLineMapper(lineMapper());
        return flatFileItemReader;

    }
    @Bean
    public StudentProcessor studentProcessor(){
        return new StudentProcessor();
    }
    @Bean
    public RepositoryItemWriter<Student> write(){
        RepositoryItemWriter<Student> writer= new RepositoryItemWriter<>();
        writer.setRepository(studentRepository);
        writer.setMethodName("save");
        return writer;
    }
    @Bean
    public Step stepImporter(){
      return new StepBuilder("csvImport",jobRepository)
              .<Student,Student>chunk(1000, platformTransactionManager)
              .reader(itemReader())
              .processor(studentProcessor())
              .writer(write())
              .taskExecutor(taskExecutor())
              .build();
    }
    @Bean
    public Job jobRun(){
        return new JobBuilder("studentImporter",jobRepository)
                .start(stepImporter())
                .build();
    }
    @Bean
    public TaskExecutor taskExecutor(){
        SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
        asyncTaskExecutor.setConcurrencyLimit(10);
        return asyncTaskExecutor;
    }
    private LineMapper<Student> lineMapper() {
        DefaultLineMapper<Student> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("id","firstName","lastName","age");

        BeanWrapperFieldSetMapper<Student> fieldSetMapper= new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Student.class);

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;

    }
}
