package alberto.ai.tools;

import com.alibaba.fastjson.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.definition.ToolDefinition;

/**
 * @author albertowang@foxmail.com
 * @date 2026/2/7 15:36
 **/
public class WeatherTool implements ToolCallback {
    @Tool(description = "Get the current weather in a city")
    String getCurrentCityWeather(@ToolParam(description = "The city to get the weather for") String city) {
        if ("杭州".equals(city)) {
            return "天气晴";
        }
        return "下雪了";
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return ToolDefinition.builder()
                .name("getCurrentCityWeather")
                .description("Get the current weather in a city")
                .inputSchema("""
                        {
                            "city": "string",
                            "description": "The city to get the weather for",
                            "required": true
                        }
                        """)
                .build();
    }

    @Override
    public String call(@NotNull String toolInput) {
        JSONObject jsonObject = JSONObject.parseObject(toolInput);
        System.out.println("Tool called with input: " + toolInput);
        String result = getCurrentCityWeather(jsonObject.getString("city"));
        System.out.println("Tool result: " + result);
        return result;
    }
}
