package alberto.ai.spring;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author albertowang@foxmail.com
 * @date 2026/2/20 16:35
 **/
@Configuration
public class ChatModelConfig {
    @Bean
    public ChatModel myChatModel() {
        return DashScopeChatModel.builder()
                .dashScopeApi(DashScopeApi.builder()
                        .apiKey(System.getenv("DASH_SCOPE_API_KEY"))
                        .build()).build();
    }
}
