package com.example.demo.job;

import com.example.demo.domain.Pay;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import javax.sql.DataSource;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class JdbcCursorItemReaderJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource; // DataSource DI

    private static final int chunkSize = 10;

    @Bean
    public Job jdbcCursorItemReaderJob() {
        return jobBuilderFactory.get("jdbcCursorItemReaderJob")
                .start(jdbcCursorItemReaderStep())
                .build();
    }

    @Bean
    public Step jdbcCursorItemReaderStep() {
        return stepBuilderFactory.get("jdbcCursorItemReaderStep")
                .<Pay, Pay>chunk(chunkSize) // 첫번째 Pay는 Reader에서 반환할 타입이며, 두번째 Pay는 Writer에 파라미터로 넘어올 타입임
                .reader(jdbcCursorItemReader()) // reader는 tasklet이 아니기 때문에 reader만으로는 수행될 수 없고 Writer가 필요함
                .writer(jdbcCursorItemWriter())
                .build();
        //참고로 processor는 필수가 아니다 (reader만 하면 된다면 processor는 생략해도 됨)
    }

    @Bean
    public JdbcCursorItemReader<Pay> jdbcCursorItemReader() {
        return new JdbcCursorItemReaderBuilder<Pay>()
                .fetchSize(chunkSize) // Database에서 한번에 가져올 데이터 양을 나타낸다
                .dataSource(dataSource) // Database에 접근하기 위해 사용할 Datasource 객체를 할당
                .rowMapper(new BeanPropertyRowMapper<>(Pay.class)) // 쿼리 결과를 Java 인스턴스로 매핑하기 위한 Mapper(일반적으로 BeanPropertyRowMapper를 씀)
                .sql("SELECT id, amount, tx_name, tx_date_time FROM pay") // 실제 쿼리문
                .name("jdbcCursorItemReader") // reader의 이름을 지정
                .build();
    }

    private ItemWriter<Pay> jdbcCursorItemWriter() {
        return list -> {
            for (Pay pay: list) {
                log.info("Current Pay={}", pay);
            }
        };
    }
}

/*
ItemReader의 가장 큰 장점은 데이터를 Streaming 할 수 있다는 것입니다.
read() 메소드는 데이터를 하나씩 가져와 ItemWriter로 데이터를 전달하고, 다음 데이터를 다시 가져 옵니다.
이를 통해 reader & processor & writer가 Chunk 단위로 수행되고 주기적으로 Commit 됩니다.
이는 고성능의 배치 처리에서는 핵심입니다.
 */
