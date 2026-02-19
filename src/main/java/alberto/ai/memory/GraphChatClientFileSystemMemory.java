package alberto.ai.memory;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;

import java.util.List;
import java.util.Map;

/**
 * 使用文件系统持久化的 Memory
 *
 * @author albertowang@foxmail.com
 * @date 2026/2/18 18:44
 **/

public class GraphChatClientFileSystemMemory {
    public static void main(String[] args) {
        CompiledGraph graph = GraphChatClient.getFileSystemGraph("/Users/alberto/CodingSpace/IDEA-workspace/agent-memory");

        RunnableConfig config = RunnableConfig.builder()
                .threadId("1")
                .build();

        graph.invoke(Map.of("messages", List.of("我是 Bob")), config)
                .map(OverAllState::data)
                .map(data -> data.get("messages"))
                .ifPresent(l -> {
                    List<String> list = (List<String>) l;
                    list.forEach(System.out::println);
                });

        System.out.println("=======");

        graph.invoke(Map.of("messages", List.of("我的名字是什么？")), config)
                .map(OverAllState::data)
                .map(data -> data.get("messages"))
                .ifPresent(l -> {
                    List<String> list = (List<String>) l;
                    list.forEach(System.out::println);
                });
    }
}
