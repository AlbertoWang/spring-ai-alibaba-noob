package alberto.ai.agent_demo;

import alberto.ai.tools.WeatherTool;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.content.Content;
import reactor.core.publisher.Flux;

import java.util.concurrent.CountDownLatch;

/**
 * @author albertowang@foxmail.com
 * @date 2026/2/7 15:31
 **/

public class ReActAgentTest {
    public static void main(String[] args) throws Exception {
        String apiKey = System.getenv("DASH_SCOPE_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.err.println("错误：无法获取DASH_SCOPE_API_KEY环境变量！");
            return;
        }

        // 创建模型实例
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(apiKey)
                .build();
        ChatModel chatModel = DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .defaultOptions(DashScopeChatOptions.builder()
                        .model("qwen-plus")
                        .temperature(0D)
                        .maxToken(1024)
                        .stream(true)
                        .build())
                .build();

        // 创建 Agent
        ReactAgent agent = ReactAgent.builder()
                .name("weather_agent")
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
    }
}
