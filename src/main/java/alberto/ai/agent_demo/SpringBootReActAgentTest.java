package alberto.ai.agent_demo;

import alberto.ai.tools.WeatherTool;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.content.Content;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import reactor.core.publisher.Flux;

import java.util.concurrent.CountDownLatch;

/**
 * @author albertowang@foxmail.com
 * @date 2026/2/17 18:30
 * 
 * 基于 Spring Boot 的 ReAct Agent 测试类
 * 这个版本会正确触发 AiMonitor 的观察处理器
 **/
@SpringBootApplication
@ComponentScan(basePackages = {"alberto.ai"})
public class SpringBootReActAgentTest implements CommandLineRunner {

    @Autowired
    private ChatModel chatModel;

    public static void main(String[] args) {
        SpringApplication.run(SpringBootReActAgentTest.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // 直接使用 Spring 注入的 ChatModel

        // 创建 Agent
        ReactAgent agent = ReactAgent.builder()
                .name("weather_agent_spring")
                .model(chatModel)
                .instruction("你是一个测试通用agent")
                .tools(new WeatherTool())
                .returnReasoningContents(true)
                .build();

        System.out.println("===================同步调用===================");

        AssistantMessage assistantMessage = agent.call("上海天气怎么样？杭州天气怎么样？");
        System.out.println(assistantMessage.getText());
        System.out.println("\n同步调用完成");

        System.out.println("===================异步流式调用===================");

        // 使用 CountDownLatch 等待流式调用完成
        CountDownLatch latch = new CountDownLatch(1);

        // 运行 Agent - 必须订阅 Flux 才能触发执行
        // 实时打印每个字符
        Flux<String> stream = agent.streamMessages("上海天气怎么样？杭州天气怎么样？")
                .map(Content::getText)
                .doOnNext(System.out::print)
                .doOnComplete(() -> {
                    System.out.println("\n流式调用完成");
                    latch.countDown();
                })
                .doOnError(error -> {
                    System.err.println("流式调用出错: " + error.getMessage());
                    latch.countDown();
                });

        // 订阅流并触发执行
        stream.subscribe();

        // 等待流式调用完成
        latch.await();
        
        // 优雅关闭
        System.exit(0);
    }
}