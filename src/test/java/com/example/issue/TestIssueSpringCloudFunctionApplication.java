package com.example.issue;

import org.springframework.boot.SpringApplication;

public class TestIssueSpringCloudFunctionApplication {

  public static void main(String[] args) {
    SpringApplication.from(IssueSpringCloudFunctionApplication::main).with(TestcontainersConfiguration.class).run(args);
  }

}
