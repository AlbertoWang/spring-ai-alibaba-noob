package alberto.ai.graph;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 接口实现的方式实现一个 node
 *
 * @author albertowang@foxmail.com
 * @date 2026/2/20 18:35
 **/

public class IntentClassifierNode implements AsyncNodeAction {
    @Override
    public CompletableFuture<Map<String, Object>> apply(OverAllState state) {
        Object inputValue = state.value(Var.INPUT).orElse(null);
        Map<String, Object> result = new HashMap<>();

        if (inputValue instanceof String str) {
            if (str.contains("+")) {
                result.put(Var.CLASSIFIER_OUTPUT, "加法请求");
            } else if (str.contains("-")) {
                result.put(Var.CLASSIFIER_OUTPUT, "减法请求");
            } else {
                result.put(Var.CLASSIFIER_OUTPUT, "unknown");
            }
        } else {
            result.put(Var.CLASSIFIER_OUTPUT, "unknown");
        }

        return CompletableFuture.completedFuture(result);
    }
}
