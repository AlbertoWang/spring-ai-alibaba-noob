package alberto.ai.graph;

import com.alibaba.cloud.ai.graph.*;
import com.alibaba.cloud.ai.graph.action.AsyncEdgeAction;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;

import java.util.HashMap;
import java.util.Map;


/**
 * @author albertowang@foxmail.com
 * @date 2026/2/20 17:14
 **/

public class MyWorkflowGraph {
    public static void main(String[] args) throws GraphStateException {
        // 配置 workflow
        StateGraph workflow = new StateGraph("Calculator Workflow",
                () -> {
                    // 配置 Graph 中可以存在的中间变量（即上下文，存储在 state.data 这个 Map 中）
                    HashMap<String, KeyStrategy> strategies = new HashMap<>();
                    strategies.put(Var.INPUT, new ReplaceStrategy());
                    strategies.put(Var.CLASSIFIER_OUTPUT, new ReplaceStrategy());
                    strategies.put(Var.SOLUTION, new ReplaceStrategy());
                    return strategies;
                })
                // 第一个节点：接口实现的方式
                .addNode(Node.INTENT, new IntentClassifierNode())
                // 第二个节点：匿名类实现的方式
                .addNode(Node.ADD, AsyncNodeAction.node_async(state -> {
                    Long result = state.value(Var.INPUT).filter(String.class::isInstance).map(String.class::cast)
                            .map(str -> str.split("\\+"))
                            .map(arr -> Long.parseLong(arr[0]) + Long.parseLong(arr[1]))
                            .orElseThrow(() -> new IllegalArgumentException("Invalid input"));
                    HashMap<String, Object> hashMap = new HashMap<>(state.data());
                    hashMap.put(Var.SOLUTION, result);
                    return hashMap;
                }))
                // 第三个节点
                .addNode(Node.SUB, AsyncNodeAction.node_async(state -> {
                    Long result = state.value(Var.INPUT).filter(String.class::isInstance).map(String.class::cast)
                            .map(str -> str.split("-"))
                            .map(arr -> Long.parseLong(arr[0]) - Long.parseLong(arr[1]))
                            .orElseThrow(() -> new IllegalArgumentException("Invalid input"));
                    HashMap<String, Object> hashMap = new HashMap<>(state.data());
                    hashMap.put(Var.SOLUTION, result);
                    return hashMap;
                }))
                // 第四个节点
                .addNode(Node.PRINT_OUTPUT, AsyncNodeAction.node_async(state -> {
                    HashMap<String, Object> hashMap = new HashMap<>(state.data());
                    state.value(Var.SOLUTION).ifPresent(solution -> {
                        System.out.println("结果：" + solution);
                        hashMap.put(Var.SOLUTION, solution);
                    });
                    return hashMap;
                }))
                // 第一个边
                .addEdge(Node.START, Node.INTENT)
                // 第二个条件边
                .addConditionalEdges(
                        // from
                        Node.INTENT,
                        // to
                        AsyncEdgeAction.edge_async(state -> {
                            if (state.value(Var.CLASSIFIER_OUTPUT).filter("加法请求"::equals).isPresent()) {
                                return Node.ADD;
                            }
                            if (state.value(Var.CLASSIFIER_OUTPUT).filter("减法请求"::equals).isPresent()) {
                                return Node.SUB;
                            }
                            return Node.END;
                        }),
                        // to 里其实可以返回其他的内容，只要通过下面这个 map 可以映射到对应的 node name 就行。简单的实现就是 to 直接返回 node name
                        Map.of(
                                Node.ADD, Node.ADD,
                                Node.SUB, Node.SUB,
                                Node.END, Node.END
                        ))
                // 第三个边
                .addEdge(Node.ADD, Node.PRINT_OUTPUT)
                // 第四个边
                .addEdge(Node.SUB, Node.PRINT_OUTPUT)
                // 第五个边
                .addEdge(Node.PRINT_OUTPUT, Node.END);

        CompileConfig compileConfig = CompileConfig.builder().build();
        CompiledGraph compiledWorkflow = workflow.compile(compileConfig);

        // 生成 PlantUML 表示
        GraphRepresentation representation = compiledWorkflow.getGraph(
                GraphRepresentation.Type.PLANTUML,
                "My Workflow"
        );

        // 显示 PlantUML 代码
        System.out.println("PlantUML representation:");
        System.out.println(representation.content());

        Map<String, Object> input = new HashMap<>();
        RunnableConfig runnableConfig = RunnableConfig.builder()
                .threadId("test")
                .build();

        input.put(Var.INPUT, "1+2");
        compiledWorkflow.invoke(input, runnableConfig);

        input.put(Var.INPUT, "3-1");
        compiledWorkflow.invoke(input, runnableConfig);

        RunnableConfig runnableConfig2 = RunnableConfig.builder()
                .threadId("test_2")
                .build();
        input.put(Var.INPUT, "3*1");
        compiledWorkflow.invoke(input, runnableConfig)
                .map(OverAllState::data)
                .ifPresent(System.out::println);
        compiledWorkflow.invoke(input, runnableConfig2)
                .map(OverAllState::data)
                .ifPresent(System.out::println);
    }
}
