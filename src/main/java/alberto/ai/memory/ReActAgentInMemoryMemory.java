package alberto.ai.memory;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import org.springframework.ai.chat.model.ChatModel;

/**
 * ReAct Agent 的 Memory，底层其实走的是内存 Memory，并且是一个默认配置
 * @see com.alibaba.cloud.ai.graph.CompileConfig#saverConfig
 *
 * @author albertowang@foxmail.com
 * @date 2026/2/18 19:52
 **/

public class ReActAgentInMemoryMemory {
    public static void main(String[] args) throws GraphRunnerException {
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(System.getenv("DASH_SCOPE_API_KEY"))
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

        // 配置 checkpointer
        ReactAgent agent = ReactAgent.builder()
                .name("my_agent")
                .model(chatModel)
                .saver(new MemorySaver())
                .build();

        // 使用 thread_id 维护对话上下文
        RunnableConfig config = RunnableConfig.builder()
                .threadId("1") // threadId 指定会话 ID
                .build();

        System.out.println(agent.call("你好！我叫 Bob。", config).getText());
        System.out.println("======");
        System.out.println(agent.call("我叫什么名字？", config).getText());
    }
}
