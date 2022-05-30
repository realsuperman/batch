package com.example.demo.job;

import com.example.demo.domain.Teacher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class ProcessorNullJobConfiguration {

    //public static final String JOB_NAME = "processorNullBatch";
    //public static final String BEAN_PREFIX = JOB_NAME + "_";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory emf;

    @Value("${chunkSize:1000}")
    private int chunkSize;

    //@Bean(JOB_NAME)
    @Bean
    public Job job() {
        return jobBuilderFactory.get("processorNullBatch")
                .preventRestart() // job name과 job parameters를 이용해 job 인스턴스를 식별하는데, job이 실패했으면 동일한 식별정보로 job 실행요청이 들어오면 실패한 step부터 job을 실행하게되는데 이게 있으면 다시 실행되는걸 막음
                .start(cshStep())
                .build();
    }

    //@Bean(BEAN_PREFIX + "step")
    @Bean
    @JobScope
    public Step cshStep() {
        return stepBuilderFactory.get("processorNullBatch_Step")
                .<Teacher, Teacher>chunk(chunkSize)
                .reader(cshReader())
                .processor(cshProcessor())
                .writer(cshWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Teacher> cshReader() {
        return new JpaPagingItemReaderBuilder<Teacher>()
                .name("processorNullBatch"+"reader")
                .entityManagerFactory(emf)
                .pageSize(chunkSize)
                .queryString("SELECT t FROM Teacher t")
                .build();
    }

    @Bean
    public ItemProcessor<Teacher, Teacher> cshProcessor() {
        return teacher -> {

            boolean isIgnoreTarget = teacher.getId() % 2 == 0;
            if(isIgnoreTarget){
                log.info(">>>>>>>>> Teacher name={}, isIgnoreTarget={}", teacher.getName(), isIgnoreTarget);
                return null; // 만약에 데이터가 null이면 ItemWrtier에 전달되지 않는다
            }

            return teacher;
        };
    }

    private ItemWriter<Teacher> cshWriter() {
        return items -> {
            for (Teacher item : items) {
                log.info("Teacher Name={}", item.getName());
            }
        };
    }
}