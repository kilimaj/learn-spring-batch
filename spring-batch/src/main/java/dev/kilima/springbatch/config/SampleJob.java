package dev.kilima.springbatch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import dev.kilima.springbatch.listener.FirstJobListener;
import dev.kilima.springbatch.listener.FirstStepListener;
import dev.kilima.springbatch.service.SecondTasklet;

@Configuration
public class SampleJob {
	
	@Autowired
	private JobRepository jobRepository;
	
	@Autowired
	private PlatformTransactionManager transactionManager;
	
	@Autowired
	private SecondTasklet secondTask;

	@Autowired
	private FirstJobListener firstJobListener;

	@Autowired
	private FirstStepListener firstStepListener;

	@Bean
	public Job firstJob() {
		return new JobBuilder("FirstJob", jobRepository)
				.incrementer(new RunIdIncrementer())
				.start(firstStep())
				.next(secondStep())
				.listener(firstJobListener)
				.build();
	}
	
	@Bean
	public Step firstStep() {
		return new StepBuilder("FirstStep", jobRepository)
				.tasklet(firstTask(), transactionManager)
				.listener(firstStepListener)
				.build();
	}
	
	@Bean
	public Tasklet firstTask() {
		return new Tasklet() {
			
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println("This is the First tasklet step");
				System.out.println("SEC = " + chunkContext.getStepContext().getStepExecutionContext());
				return RepeatStatus.FINISHED;
			}
		};
	}

	@Bean
	public Step secondStep() {
		return new StepBuilder("SecondStep", jobRepository).tasklet(secondTask, transactionManager).build();
	}

//	@Bean
//	public Tasklet secondTask() {
//		return new Tasklet() {
//
//			@Override
//			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
//				System.out.println("This is the Second Tasklet step");
//				return RepeatStatus.FINISHED;
//			}
//		};
//	}

	@Bean
	public Job secondJob() {
		return new JobBuilder("Second Job", jobRepository).incrementer(new RunIdIncrementer()).build();
	}
}
