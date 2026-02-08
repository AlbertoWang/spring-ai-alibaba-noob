package alberto.ai.agent_demo;

import alberto.ai.tools.WeatherTool;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author albertowang@foxmail.com
 * @date 2026/2/7 15:31
 **/

public class ReActAgentTest {
    @Value("${spring.ai.dashscope.api-key}")
    private static String apiKey;

    public static void main(String[] args) throws Exception {
        // 创建模型实例
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey("sk-0b2ae77b54df46139fc9d4a5701163c1")
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

        // 运行 Agent
        AssistantMessage call = agent.call("上海天气怎么样");
        System.out.println(call.getText());
    }
}
