package com.example.springbootdemo.common;


import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.TaskListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ApplyListener implements TaskListener, ExecutionListener {
    private static final long serialVersionUID = -6000293034409953144L;

    @Override
    public void notify(DelegateTask delegateTask) {
        delegateTask.setAssignee("zhangsan");
    }

    @Override
    public void notify(DelegateExecution delegateExecution) {

    }

}