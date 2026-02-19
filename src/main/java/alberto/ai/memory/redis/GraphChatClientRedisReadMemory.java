package alberto.ai.memory.redis;

import alberto.ai.memory.GraphChatClient;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.StateSnapshot;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author albertowang@foxmail.com
 * @date 2026/2/19 19:17
 **/

public class GraphChatClientRedisReadMemory {
    public static void main(String[] args) throws GraphStateException {
        Config redisConfig = new Config();
        redisConfig.useSingleServer().setAddress("redis://localhost:6379");
        RedissonClient redisson = Redisson.create(redisConfig);

        CompiledGraph graph = GraphChatClient.getRedisGraph(redisson);

        RunnableConfig config = RunnableConfig.builder()
                .threadId("1")
                .build();

        Collection<StateSnapshot> stateHistory = graph.getStateHistory(config);
        System.out.println(stateHistory);
        System.out.println("===========");

        StateSnapshot state = graph.getState(config);
        System.out.println(state);

        graph.invoke(Map.of("messages", List.of("我的名字是什么？")), config)
                .map(OverAllState::data)
                .map(data -> data.get("messages"))
                .ifPresent(l -> {
                    List<String> list = (List<String>) l;
                    list.forEach(System.out::println);
                });
    }
}
