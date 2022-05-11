package com.example.demo.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CustomerExitStatusTestClass {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job cusomterJob() {
        return jobBuilderFactory.get("CustomerTestJob")
                .start(csh1())
                    .on("FAILED")
                    .end()
                .from(csh1())
                    .on("COMPLETED WITH SKIPS")
                    .to(csh4())
                    .end()
                .build();
    }

    @Bean
    public Step csh1() {
        return stepBuilderFactory.get("test1")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>> This is stepNextConditionalJob Step1");

                    /**
                     ExitStatus를 FAILED로 지정한다.
                     해당 status를 보고 flow가 진행된다.
                     **/
                    contribution.setExitStatus(new ExitStatus("COMPLETED WITH SKIPS")); // 사용자 정의 ExitStatus 생성

                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step csh2() {
        return stepBuilderFactory.get("test2")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>> This is stepNextConditionalJob Step2");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step csh3() {
        return stepBuilderFactory.get("test3")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>> This is stepNextConditionalJob Step3");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step csh4() {
        return stepBuilderFactory.get("test4")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>> This is stepNextConditionalJob Step4");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }
}