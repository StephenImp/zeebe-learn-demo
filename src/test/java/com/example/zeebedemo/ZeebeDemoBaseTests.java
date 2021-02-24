package com.example.zeebedemo;

import io.zeebe.client.ZeebeClient;
import io.zeebe.client.api.response.DeploymentEvent;
import io.zeebe.client.api.response.PartitionInfo;
import io.zeebe.client.api.response.Topology;
import io.zeebe.client.api.response.WorkflowInstanceEvent;
import io.zeebe.client.api.worker.JobWorker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

class ZeebeDemoBaseTests {
	ZeebeClient client;

	@BeforeEach
	@Test
	public void initZeebeClient(){
		client = ZeebeClient.newClientBuilder()
				.brokerContactPoint("192.168.18.18:26500")
				.usePlaintext()
				.build();
		System.out.println("connected...");
	}

	@AfterEach
	public void closeZeebeClient() {
		client.close();
		System.out.println("closed...");
	}


	/**
	 * Deploy a workflow
	 * 要将建模的工作流部署到broker。broker将工作流存储在其bpmn流程ID下，并分配一个版本（即修订版）。
	 */
	@Test
	public void testZeebeApi1(){

		DeploymentEvent deployment = client.newDeployCommand()
				.addResourceFromClasspath("order-process3.bpmn")
				.send()
				.join();

		final int version = deployment.getWorkflows().get(0).getVersion();
		System.out.println("Workflow deployed. Version: " + version);
	}

	/**
	 * Create a workflow instance
	 *
	 * 准备创建已部署工作流的第一个实例。
	 * 工作流实例是由特定版本的工作流创建的，可以在创建时进行设置。
	 */
	@Test
	public void testZeebeApi2(){
		// after the workflow is deployed

		final WorkflowInstanceEvent wfInstance = client.newCreateInstanceCommand()
				.bpmnProcessId("order-process")
				.latestVersion()
				.send()
				.join();

		final long workflowInstanceKey = wfInstance.getWorkflowInstanceKey();
		System.out.println("Workflow instance created. Key: " + workflowInstanceKey);

		// ...
	}


	@Test
	public void testZeebeApi3() throws InterruptedException {

		// after the workflow instance is created

		final JobWorker jobWorker = client.newWorker()
				.jobType("payment-service")
				.handler((jobClient, job) ->
				{
					System.out.println("Collect money");

					// ...

					jobClient.newCompleteCommand(job.getKey())
							.send()
							.join();
				})
				.open();

		// waiting for the jobs
		System.out.println();

		// Don't close, we need to keep polling to get work
		// jobWorker.close();

		// ...

	}
}
