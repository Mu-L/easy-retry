package com.aizuda.easy.retry.server.web.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.aizuda.easy.retry.common.core.util.JsonUtil;
import com.aizuda.easy.retry.server.common.exception.EasyRetryServerException;
import com.aizuda.easy.retry.server.web.model.request.WorkflowRequestVO;
import com.aizuda.easy.retry.server.web.model.request.WorkflowRequestVO.NodeConfig;
import com.aizuda.easy.retry.server.web.model.request.WorkflowRequestVO.NodeInfo;
import com.aizuda.easy.retry.server.web.service.WorkflowService;
import com.aizuda.easy.retry.server.web.service.convert.WorkflowConverter;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.WorkflowMapper;
import com.aizuda.easy.retry.template.datasource.persistence.mapper.WorkflowNodeMapper;
import com.aizuda.easy.retry.template.datasource.persistence.po.Workflow;
import com.aizuda.easy.retry.template.datasource.persistence.po.WorkflowNode;
import com.google.common.collect.Lists;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author xiaowoniu
 * @date 2023-12-12 21:54:05
 * @since 2.6.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WorkflowServiceImpl implements WorkflowService {
    private final WorkflowMapper workflowMapper;
    private final WorkflowNodeMapper workflowNodeMapper;
    @Override
    @Transactional
    public boolean saveWorkflow(WorkflowRequestVO workflowRequestVO) {

        Long root = -1L;
        MutableGraph<Long> graph = GraphBuilder.directed().allowsSelfLoops(false).build();
        // 添加虚拟头节点
        graph.addNode(root);

        // 组装工作流信息
        Workflow workflow = WorkflowConverter.INSTANCE.toWorkflow(workflowRequestVO);
        workflow.setFlowInfo(StrUtil.EMPTY);
        Assert.isTrue(1 ==  workflowMapper.insert(workflow), () -> new EasyRetryServerException("新增工作流失败"));

        // 获取DAG节点配置
        NodeConfig nodeConfig = workflowRequestVO.getNodeConfig();

        // 递归构建图
        buildGraph(Lists.newArrayList(root), workflowRequestVO.getGroupName(), workflow.getId(), nodeConfig, graph);

        log.info("图构建完成. graph:[{}]", graph);
        // 保存图信息
        workflow.setFlowInfo(JsonUtil.toJsonString(convertGraphToAdjacencyList(graph)));
        workflowMapper.updateById(workflow);

        return true;
    }

    private Map<Long, Iterable<Long>> convertGraphToAdjacencyList(MutableGraph<Long> graph) {
        Map<Long, Iterable<Long>> adjacencyList = new HashMap<>();

        for (Long node : graph.nodes()) {
            adjacencyList.put(node, graph.successors(node));
        }

        return adjacencyList;
    }

    public void buildGraph(List<Long> parentIds, String groupName, Long workflowId, NodeConfig nodeConfig, MutableGraph<Long> graph) {

        if (Objects.isNull(nodeConfig)) {
            return;
        }

        // 获取节点信息
        List<NodeInfo> conditionNodes = nodeConfig.getConditionNodes();
        List<Long> parentIds1 = Lists.newArrayList();
        if (!CollectionUtils.isEmpty(conditionNodes)) {
            for (final NodeInfo nodeInfo : conditionNodes) {
                WorkflowNode workflowNode = WorkflowConverter.INSTANCE.toWorkflowNode(nodeInfo);
                workflowNode.setWorkflowId(workflowId);
                workflowNode.setGroupName(groupName);
                Assert.isTrue(1 ==  workflowNodeMapper.insert(workflowNode), () -> new EasyRetryServerException("新增工作流节点失败"));
                // 添加节点
                graph.addNode(workflowNode.getId());
                for (final Long parentId : parentIds) {
                    // 添加边
                    graph.putEdge(parentId, workflowNode.getId());
                }
                parentIds1.add(workflowNode.getId());
                buildGraph(Lists.newArrayList(workflowNode.getId()), groupName, workflowId, nodeInfo.getChildNode(), graph);
            }
        }

        buildGraph(parentIds1, groupName, workflowId, nodeConfig.getChildNode(), graph);
    }


}
