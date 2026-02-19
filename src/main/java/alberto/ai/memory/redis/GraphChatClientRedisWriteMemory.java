package alberto.ai.memory.redis;

import alberto.ai.memory.GraphChatClient;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.List;
import java.util.Map;

/**
 * 1. 新建 docker 镜像：docker run -d --name my-redis -p 6379:6379 redis:latest
 * 2. 进入 docker 容器：docker exec -it my-redis redis-cli
 * 3. 关闭 docker 容器：docker stop my-redis
 * 4. 启动 docker 容器：docker start my-redis
 *
 * @author albertowang@foxmail.com
 * @date 2026/2/19 18:28
 **/

public class GraphChatClientRedisWriteMemory {
    public static void main(String[] args) throws GraphStateException {
        Config redisConfig = new Config();
        redisConfig.useSingleServer().setAddress("redis://localhost:6379");
        RedissonClient redisson = Redisson.create(redisConfig);

        CompiledGraph graph = GraphChatClient.getRedisGraph(redisson);

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
    }
}
