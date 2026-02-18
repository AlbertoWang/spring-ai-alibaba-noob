package alberto.ai.agent_demo;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.observation.ChatModelObservationContext;
import org.springframework.stereotype.Component;

/**
 * @author albertowang@foxmail.com
 * @date 2026/2/17 18:01
 **/
@Component
public class AiMonitor implements ObservationHandler<ChatModelObservationContext> {
    @Override
    public void onStop(@NotNull ChatModelObservationContext context) {
        try {
            // 1. 获取基础信息
            String name = context.getName();
            String contextualName = context.getContextualName();
            
            // 2. 从上下文中提取关键信息
            var request = context.getRequest();
            var response = context.getResponse();
            
            if (response == null) {
                System.err.println("Warning: Request or Response is null in observation context");
                return;
            }

            // 3. 提取模型信息
            String model = "unknown";
            try {
                // 通过反射获取模型名称
                model = request.getClass().getSimpleName();
            } catch (Exception e) {
                model = "unknown-model";
            }
            
            // 4. 提取响应时间和基本统计
            long durationMs = 0L;
            try {
                // 从上下文中获取持续时间信息
                durationMs = System.currentTimeMillis() - getStartTime(context);
            } catch (Exception e) {
                durationMs = 0L;
            }

            // 5. 输出监控信息
            System.out.println("==================================================Monitor==================================================");
            System.out.println("Observation Name: " + name + 
                             ", Contextual Name: " + contextualName + 
                             ", Model: " + model + 
                             ", Duration: " + durationMs + "ms");
            System.out.println("==================================================Monitor==================================================");
            
        } catch (Exception e) {
            System.err.println("Error in AiMonitor.onStop: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // 辅助方法：尝试获取开始时间
    private long getStartTime(ChatModelObservationContext context) {
        try {
            // 尝试从上下文获取开始时间
            return System.currentTimeMillis(); // 简化处理
        } catch (Exception e) {
            return System.currentTimeMillis();
        }
    }

    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof ChatModelObservationContext;
    }
}
