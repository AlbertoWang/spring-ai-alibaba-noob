package alberto.ai.agent_demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"alberto.ai"})
public class AgentDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgentDemoApplication.class, args);
	}
}
