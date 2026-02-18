package alberto.ai.controller;

import alberto.ai.tools.WeatherTool;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.content.Content;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * ReAct Agent 流式控制器
 * 支持工具调用的流式输出
 */
@RestController
@RequestMapping("/api/ai")
public class ReactAgentStreamController {

    private static final Logger logger = LoggerFactory.getLogger(ReactAgentStreamController.class);

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    private ReactAgent createReactAgent() {
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

        // 创建 ReAct Agent
        return ReactAgent.builder()
                .name("react_weather_agent")
                .model(chatModel)
                .instruction("你是一个智能助手，可以查询天气信息。当用户询问天气时，请使用天气工具获取准确信息。")
                .tools(new WeatherTool())
                .returnReasoningContents(true)
                .build();
    }

    /**
     * ReAct Agent 流式调用接口
     * 支持工具调用的实时流式输出
     *
     * @param message 用户输入消息
     * @return SSE流式响应
     */
    @GetMapping(value = "/react/agent/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamReactAgent(@RequestParam(value = "message", defaultValue = "北京天气怎么样？") String message) {

        logger.info("收到ReAct Agent流式请求: {}", message);

        try {
            ReactAgent agent = createReactAgent();

            return agent.streamMessages(message)
                    .map(Content::getText)
                    .map(content -> {
                        logger.info("流式输出内容: {}", content);
                        // 返回SSE格式数据
                        return content.replace("\n", "\\n");
                    })
                    .doOnComplete(() -> logger.info("ReAct Agent流式调用完成"))
                    .doOnError(error -> logger.error("ReAct Agent流式调用出错", error));
        } catch (Exception e) {
            logger.error("创建ReAct Agent失败", e);
            return Flux.just("data: 系统错误: " + e.getMessage() + "\n\n");
        }
    }

    /**
     * 带思考过程的流式输出
     * 展示Agent的推理过程
     *
     * @param message 用户输入消息
     * @return 包含思考过程的SSE流式响应
     */
    @GetMapping(value = "/react/agent/stream-with-reasoning", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamReactAgentWithReasoning(@RequestParam(value = "message", defaultValue = "查询杭州和上海的天气") String message) {

        logger.info("收到带思考过程的流式请求: {}", message);

        try {
            // 创建支持思考过程的Agent
            DashScopeApi dashScopeApi = DashScopeApi.builder()
                    .apiKey(apiKey)
                    .build();

            ChatModel chatModel = DashScopeChatModel.builder()
                    .dashScopeApi(dashScopeApi)
                    .defaultOptions(DashScopeChatOptions.builder()
                            .model("qwen-plus")
                            .temperature(0D)
                            .maxToken(2048)  // 增加token限制以容纳思考过程
                            .stream(true)
                            .build())
                    .build();

            ReactAgent agent = ReactAgent.builder()
                    .name("thinking_agent")
                    .model(chatModel)
                    .instruction("你是一个会思考的智能助手。在回答问题前，请先展示你的思考过程，然后给出最终答案。")
                    .tools(new WeatherTool())
                    .returnReasoningContents(true)
                    .build();

            return agent.streamMessages(message)
                    .map(Content::getText)
                    .map(content -> {
                        logger.info("思考过程输出: {}", content);
                        // 对思考过程进行特殊标记
                        return content;
                    })
                    .doOnComplete(() -> logger.info("带思考过程的流式调用完成"))
                    .doOnError(error -> logger.error("带思考过程的流式调用出错", error));
        } catch (Exception e) {
            logger.error("创建带思考过程的Agent失败", e);
            return Flux.just("data: 系统错误: " + e.getMessage() + "\n\n");
        }
    }
}