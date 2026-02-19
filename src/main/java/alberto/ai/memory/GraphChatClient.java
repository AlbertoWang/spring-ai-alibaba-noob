package alberto.ai.memory;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.*;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.file.FileSystemSaver;
import com.alibaba.cloud.ai.graph.checkpoint.savers.redis.RedisSaver;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;
import org.redisson.api.RedissonClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.StateGraph.START;

/**
 * @author albertowang@foxmail.com
 * @date 2026/2/19 19:17
 **/

public class GraphChatClient {
    private static AsyncNodeAction chatNode = AsyncNodeAction.node_async(state -> {
        // 这个 messages 才是重点，无论使用什么 Memory 持久化方式，都需要将持久化的 Memory 拿到并且拼到下一次的 request
        List<Message> messages = new ArrayList<>();
        state.value("messages")
                .filter(List.class::isInstance)
                .map(List.class::cast)
                .orElseGet(List::of)
                .forEach(message -> messages.add(new UserMessage(message.toString())));

        // 调用 AI
        // 创建模型实例
        ChatClient chatClient = ChatClient.builder(DashScopeChatModel.builder()
                        .dashScopeApi(DashScopeApi.builder()
                                .apiKey(System.getenv("DASH_SCOPE_API_KEY"))
                                .build())
                        .defaultOptions(DashScopeChatOptions.builder()
                                .model("qwen-turbo")
                                .build())
                        .build())
                .build();
        String response = chatClient.prompt("返回的文本长度控制在5个字以内")
                .messages(messages)
                .call()
                .chatResponse()
                .getResult()
                .getOutput()
                .getText();

        return Map.of("messages", List.of(response));
    });

    private static KeyStrategyFactory keyStrategyFactory = () -> {
        Map<String, KeyStrategy> keyStrategyMap = new HashMap<>();
        keyStrategyMap.put("messages", new AppendStrategy());
        return keyStrategyMap;
    };

    public static CompiledGraph getRedisGraph(RedissonClient redisson) {
        try {
            return new StateGraph(keyStrategyFactory)
                    .addNode("chat", chatNode)
                    .addEdge(START, "chat")
                    .addEdge("chat", END)
                    .compile(CompileConfig.builder()
                            .saverConfig(SaverConfig.builder()
                                    .register(RedisSaver.builder().redisson(redisson).build())
                                    .build())
                            .build()
                    );
        } catch (GraphStateException e) {
            throw new RuntimeException(e);
        }
    }

    public static CompiledGraph getFileSystemGraph(String path) {
        try {
            return new StateGraph(keyStrategyFactory)
                    .addNode("chat", chatNode)
                    .addEdge(START, "chat")
                    .addEdge("chat", END)
                    .compile(CompileConfig.builder()
                            .saverConfig(SaverConfig.builder()
                                    .register(FileSystemSaver.builder().targetFolder(Path.of(path)).build())
                                    .build())
                            .build()
                    );
        } catch (GraphStateException e) {
            throw new RuntimeException(e);
        }
    }
}
