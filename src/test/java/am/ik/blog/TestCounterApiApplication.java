package am.ik.blog;

import org.springframework.boot.SpringApplication;

public class TestCounterApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(CounterApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
