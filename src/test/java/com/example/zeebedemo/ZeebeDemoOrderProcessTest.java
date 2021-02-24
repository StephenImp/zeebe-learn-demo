package com.example.zeebedemo;

import com.google.common.collect.Maps;
import io.zeebe.client.api.response.DeploymentEvent;
import io.zeebe.client.api.response.WorkflowInstanceEvent;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class ZeebeDemoOrderProcessTest extends ZeebeDemoBaseTests{

	/**
	 * 部署bpmn模型到borker
	 */
	@Test
	public void deployOrderProcessTest() {
		DeploymentEvent deployment = client.newDeployCommand()
				.addResourceFromClasspath("order-process.bpmn").send().join();
		String bpmnProcessId = deployment.getWorkflows().get(0).getBpmnProcessId();
		System.out.println(bpmnProcessId);

		final int version = deployment.getWorkflows().get(0).getVersion();
		System.out.println("Workflow deployed. Version: " + version);

		assert bpmnProcessId.equals("order-process");
	}



	@Test
	public void createInstance1Test() {
		Map<String, Object> params = Maps.newHashMap();
		params.put("orderId", "123456");
		//创建一个工作流实例
		WorkflowInstanceEvent workflowInstance = client.newCreateInstanceCommand().bpmnProcessId("order-process").latestVersion().variables(params)
				.send().join();
		System.out.println("workflowInstanceKey: " + workflowInstance.getWorkflowInstanceKey());
	}
}
