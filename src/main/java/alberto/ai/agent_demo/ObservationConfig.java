package alberto.ai.agent_demo;

import io.micrometer.observation.ObservationRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * 观察处理器配置类
 * 将自定义的观察处理器注册到 ObservationRegistry 中
 *
 * @author albertowang@foxmail.com
 * @date 2026/2/18 10:30
 */
@Configuration
public class ObservationConfig {

    @Autowired
    private ObservationRegistry observationRegistry;

    @Autowired
    private AiMonitor aiMonitor;

    @PostConstruct
    public void registerHandlers() {
        // 将自定义的观察处理器注册到 registry 中
        observationRegistry.observationConfig()
                .observationHandler(aiMonitor);
        
        System.out.println("✅ AiMonitor 已成功注册到 ObservationRegistry");
    }
}